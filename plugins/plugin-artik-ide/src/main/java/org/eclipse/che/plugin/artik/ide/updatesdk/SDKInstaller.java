/*******************************************************************************
 * Copyright (c) 2016-2017 Samsung Electronics Co., Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - Initial implementation
 *   Samsung Electronics Co., Ltd. - Initial implementation
 *******************************************************************************/
package org.eclipse.che.plugin.artik.ide.updatesdk;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessDiedEventDto;
import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessStdErrEventDto;
import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessStdOutEventDto;
import org.eclipse.che.api.machine.shared.dto.recipe.NewRecipe;
import org.eclipse.che.api.machine.shared.dto.recipe.RecipeDescriptor;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper.RequestCall;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentDto;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentRecipeDto;
import org.eclipse.che.api.workspace.shared.dto.ExtendedMachineDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.ExecAgentCommandManager;
import org.eclipse.che.ide.api.machine.RecipeServiceClient;
import org.eclipse.che.ide.api.workspace.WorkspaceServiceClient;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.util.UUID;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.MessageBusProvider;
import org.eclipse.che.ide.websocket.WebSocketException;
import org.eclipse.che.ide.websocket.rest.StringUnmarshallerWS;
import org.eclipse.che.ide.websocket.rest.SubscriptionHandler;
import org.eclipse.che.plugin.artik.ide.ArtikResources;
import org.eclipse.che.plugin.artik.ide.machine.DeviceServiceClient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.machine.shared.Constants.LINK_REL_GET_RECIPE_SCRIPT;
import static org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper.createFromAsyncRequest;

/**
 * Responsible for installing Artik SDK on the specific target.
 *
 * @author Artem Zatsarynnyi
 */
public class SDKInstaller {

    private final AppContext              appContext;
    private final DeviceServiceClient     deviceServiceClient;
    private final DtoFactory              dtoFactory;
    private final MessageBusProvider      messageBusProvider;
    private final ArtikResources          resources;
    private final RecipeServiceClient     recipeServiceClient;
    private final WorkspaceServiceClient  workspaceServiceClient;
    private final ExecAgentCommandManager execAgentCommandManager;

    private AsyncCallback<List<String>> checkVersionsCallback;
    private AsyncCallback<String>       checkVersionCallback;
    private AsyncCallback<String>       updateCallback;

    @Inject
    public SDKInstaller(AppContext appContext,
                        DeviceServiceClient deviceServiceClient,
                        DtoFactory dtoFactory,
                        MessageBusProvider messageBusProvider,
                        ArtikResources resources,
                        RecipeServiceClient recipeServiceClient,
                        WorkspaceServiceClient workspaceServiceClient,
                        ExecAgentCommandManager execAgentCommandManager) {
        this.appContext = appContext;
        this.deviceServiceClient = deviceServiceClient;
        this.dtoFactory = dtoFactory;
        this.messageBusProvider = messageBusProvider;
        this.resources = resources;
        this.recipeServiceClient = recipeServiceClient;
        this.workspaceServiceClient = workspaceServiceClient;
        this.execAgentCommandManager = execAgentCommandManager;
    }

    private static boolean isErrorMessage(String message) {
        return message.startsWith("[STDERR]") || message.startsWith("\"[STDERR]");
    }

    /** Returns list of Artik SDK versions available to install. */
    public Promise<List<String>> getAvailableSDKVersions() {
        // use set in order to avoid duplicated versions
        final Set<String> versions = new HashSet<>();

        final Promise<List<String>> promise = createFromAsyncRequest(new RequestCall<List<String>>() {
            @Override
            public void makeCall(AsyncCallback<List<String>> callback) {
                checkVersionsCallback = callback;
            }
        });

        final String cmd = resources.getCheckAvailableSDKVersionsCommand().getText();

        final Command command = dtoFactory.createDto(CommandDto.class)
                                          .withName("get_available_SDK_versions")
                                          .withType("custom")
                                          .withCommandLine(cmd);

        execAgentCommandManager.startProcess(appContext.getDevMachine().getId(), command)
                               .thenIfProcessStdOutEvent(new Operation<ProcessStdOutEventDto>() {
                                   @Override
                                   public void apply(ProcessStdOutEventDto arg) throws OperationException {
                                       String message = arg.getText();
                                       if (!">>> end <<<".equals(message)) {
                                           versions.add(message);
                                       }
                                   }
                               })
                               .thenIfProcessStdErrEvent(new Operation<ProcessStdErrEventDto>() {
                                   @Override
                                   public void apply(ProcessStdErrEventDto arg) throws OperationException {
                                       checkVersionsCallback.onFailure(new Exception(arg.getText()));
                                   }
                               })
                               .thenIfProcessDiedEvent(new Operation<ProcessDiedEventDto>() {
                                   @Override
                                   public void apply(ProcessDiedEventDto arg) throws OperationException {
                                       checkVersionsCallback.onSuccess(new ArrayList<>(versions));
                                   }
                               });

        return promise;
    }

    /** Get version of Artik SDK which is installed on the device with the specified ID. */
    public Promise<String> getInstalledSDKVersion(TargetForUpdate target) {
        final String chanel = "process:output:" + UUID.uuid();
        final String deviceId = target.getId();
        final MessageBus messageBus = isWsAgent(target) ?
                                      messageBusProvider.getMessageBus() :
                                      messageBusProvider.getMachineMessageBus();

        try {
            messageBus.subscribe(chanel, new SubscriptionHandler<String>(new OutputMessageUnmarshaller()) {
                @Override
                protected void onMessageReceived(String message) {
                    if (isErrorMessage(message)) {
                        checkVersionCallback.onFailure(new Exception(message));
                    } else {
                        checkVersionCallback.onSuccess(message);
                    }
                }

                @Override
                protected void onErrorReceived(Throwable throwable) {
                    checkVersionCallback.onFailure(throwable);
                }
            });
        } catch (WebSocketException e) {
            checkVersionCallback.onFailure(new Exception(e));
        }

        final Promise<String> promise = createFromAsyncRequest(new RequestCall<String>() {
            @Override
            public void makeCall(AsyncCallback<String> callback) {
                checkVersionCallback = callback;
            }
        });

        final String cmd = deviceId.equals(appContext.getDevMachine().getId()) ? resources.getCheckSDKVersionOnAgentCommand().getText()
                                                                               : resources.getCheckSDKVersionOnDeviceCommand().getText();

        final Command command = dtoFactory.createDto(CommandDto.class)
                                          .withName("get_installed_SDK_version")
                                          .withType("custom")
                                          .withCommandLine(cmd);
        if (isWsAgent(target)) {
            execAgentCommandManager.startProcess(appContext.getDevMachine().getId(), command)
                                   .thenIfProcessStdOutEvent(new Operation<ProcessStdOutEventDto>() {
                                       @Override
                                       public void apply(ProcessStdOutEventDto arg) throws OperationException {
                                           checkVersionCallback.onSuccess(arg.getText());
                                       }
                                   })
                                   .thenIfProcessStdErrEvent(new Operation<ProcessStdErrEventDto>() {
                                       @Override
                                       public void apply(ProcessStdErrEventDto arg) throws OperationException {
                                           checkVersionsCallback.onFailure(new Exception(arg.getText()));
                                       }
                                   });
        } else {
            deviceServiceClient.executeCommand(deviceId, command, chanel);
        }

        return promise;
    }

    /** Update Artik SDK on the device with the specified ID. */
    public Promise<String> installSDK(TargetForUpdate target, String sdkVersion) {
        final String targetId = target.getId();
        if (targetId.equals(appContext.getDevMachine().getId())) {
            return installSdkOnWsAgent(targetId, sdkVersion);
        }

        final String chanel = "process:output:" + UUID.uuid();
        final MessageBus messageBus = isWsAgent(target) ?
                                      messageBusProvider.getMessageBus() :
                                      messageBusProvider.getMachineMessageBus();

        try {
            messageBus.subscribe(chanel, new SubscriptionHandler<String>(new StringUnmarshallerWS()) {
                @Override
                protected void onMessageReceived(String message) {
                    if (message.contains("The latest Artik SDK installed")) {
                        updateCallback.onSuccess(message);
                    } else if (isErrorMessage(message)) {
                        updateCallback.onFailure(new Exception(message));
                    }
                }

                @Override
                protected void onErrorReceived(Throwable throwable) {
                    updateCallback.onFailure(throwable);
                }
            });
        } catch (WebSocketException e) {
            updateCallback.onFailure(new Exception(e));
        }

        final Promise<String> promise = createFromAsyncRequest(new RequestCall<String>() {
            @Override
            public void makeCall(AsyncCallback<String> callback) {
                updateCallback = callback;
            }
        });

        final String cmd = resources.getInstallSDKOnDeviceCommand().getText();

        final Command command = dtoFactory.createDto(CommandDto.class)
                                          .withName("install_SDK")
                                          .withType("custom")
                                          .withCommandLine(cmd.replace("${sdk.version}", sdkVersion));
        if (isWsAgent(target)) {
            execAgentCommandManager.startProcess(appContext.getDevMachine().getId(), command)
                                   .thenIfProcessStdOutEvent(new Operation<ProcessStdOutEventDto>() {
                                       @Override
                                       public void apply(ProcessStdOutEventDto arg) throws OperationException {
                                           String message = arg.getText();
                                           if (message.contains("The latest Artik SDK installed")) {
                                               updateCallback.onSuccess(message);
                                           }
                                       }
                                   })
                                   .thenIfProcessStdErrEvent(new Operation<ProcessStdErrEventDto>() {
                                       @Override
                                       public void apply(ProcessStdErrEventDto arg) throws OperationException {
                                           checkVersionsCallback.onFailure(new Exception(arg.getText()));
                                       }
                                   });
        } else {
            deviceServiceClient.executeCommand(targetId, command, chanel);
        }

        return promise;
    }

    private Promise<String> installSdkOnWsAgent(final String targetId, String sdkVersion) {
        final NewRecipe newRecipe = dtoFactory.createDto(NewRecipe.class)
                                              .withType("docker")
                                              .withName(targetId)
                                              .withScript(resources.recipe().getText().replace("${sdk.version}", sdkVersion));

        return recipeServiceClient.createRecipe(newRecipe).then(new Function<RecipeDescriptor, String>() {
            @Override
            public String apply(RecipeDescriptor recipe) throws FunctionException {
                return recipe.getLink(LINK_REL_GET_RECIPE_SCRIPT).getHref();
            }
        }).then(new Function<String, EnvironmentDto>() {
            @Override
            public EnvironmentDto apply(String recipeLink) throws FunctionException {
                return dtoFactory.createDto(EnvironmentDto.class)
                                 .withMachines(singletonMap("dev-machine",
                                                            dtoFactory.createDto(ExtendedMachineDto.class)
                                                                      .withAgents(asList("org.eclipse.che.terminal",
                                                                                         "org.eclipse.che.ws-agent"))))
                                 .withRecipe(dtoFactory.createDto(EnvironmentRecipeDto.class)
                                                       .withType("dockerfile")
                                                       .withContentType("text/x-dockerfile")
                                                       .withLocation(recipeLink));
            }
        }).thenPromise(new Function<EnvironmentDto, Promise<WorkspaceDto>>() {
            @Override
            public Promise<WorkspaceDto> apply(EnvironmentDto environment) throws FunctionException {
                return workspaceServiceClient.addEnvironment(appContext.getWorkspaceId(),
                                                             targetId,
                                                             environment).thenPromise(
                        new Function<WorkspaceDto, Promise<WorkspaceDto>>() {
                            @Override
                            public Promise<WorkspaceDto> apply(WorkspaceDto arg) throws FunctionException {
                                arg.withConfig(arg.getConfig().withDefaultEnv(targetId));
                                return workspaceServiceClient.update(appContext.getWorkspaceId(), arg);
                            }
                        });
            }
        }).then(new Function<WorkspaceDto, String>() {
            @Override
            public String apply(WorkspaceDto arg) throws FunctionException {
                return "Workspace has been successfully updated";
            }
        });
    }

    private boolean isWsAgent(TargetForUpdate target) {
        return "Workspace".equals(target.getName());
    }
}
