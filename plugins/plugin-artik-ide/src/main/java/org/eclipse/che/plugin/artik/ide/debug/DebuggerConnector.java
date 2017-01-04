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
package org.eclipse.che.plugin.artik.ide.debug;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineSource;
import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.api.machine.shared.dto.MachineProcessDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper;
import org.eclipse.che.api.promises.client.js.JsPromiseError;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.debug.DebugConfiguration;
import org.eclipse.che.ide.api.debug.DebugConfigurationType;
import org.eclipse.che.ide.api.debug.DebugConfigurationsManager;
import org.eclipse.che.ide.api.macro.MacroProcessor;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.CommandConsoleFactory;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.DefaultOutputConsole;
import org.eclipse.che.ide.extension.machine.client.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.ide.util.UUID;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.MessageBusProvider;
import org.eclipse.che.ide.websocket.WebSocketException;
import org.eclipse.che.ide.websocket.events.MessageHandler;
import org.eclipse.che.ide.websocket.rest.SubscriptionHandler;
import org.eclipse.che.plugin.artik.ide.command.macro.ReplicationFolderMacro;
import org.eclipse.che.plugin.artik.ide.machine.DeviceServiceClient;
import org.eclipse.che.plugin.artik.ide.updatesdk.OutputMessageUnmarshaller;
import org.eclipse.che.plugin.debugger.ide.configuration.DebugConfigurationTypeRegistry;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.gwt.json.client.JSONParser.parseLenient;
import static org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper.createFromAsyncRequest;
import static org.eclipse.che.plugin.artik.ide.command.macro.BinaryNameMacro.DEFAULT_BINARY_NAME;
import static org.eclipse.che.plugin.cpp.shared.Constants.BINARY_NAME_ATTRIBUTE;

/**
 * Connects to the debugger for debugging project's binary file.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class DebuggerConnector {

    private final DtoFactory                     dtoFactory;
    private final ProcessListener                processListener;
    private final AppContext                     appContext;
    private final DeviceServiceClient            deviceServiceClient;
    private final MacroProcessor                 macroProcessor;
    private final DebugConfigurationTypeRegistry debugConfigurationTypeRegistry;
    private final DebugConfigurationsManager     debugConfigurationsManager;
    private final NotificationManager            notificationManager;

    private final MessageBus              messageBus;
    private final ProcessesPanelPresenter processesPanelPresenter;
    private final CommandConsoleFactory   commandConsoleFactory;

    private AsyncCallback<Integer> runCallback;

    @Inject
    public DebuggerConnector(DtoFactory dtoFactory,
                             ProcessListener processListener,
                             AppContext appContext,
                             MacroProcessor macroProcessor,
                             DebugConfigurationTypeRegistry debugConfigurationTypeRegistry,
                             DebugConfigurationsManager debugConfigurationsManager,
                             NotificationManager notificationManager,
                             DeviceServiceClient deviceServiceClient,
                             MessageBusProvider messageBusProvider,
                             ProcessesPanelPresenter processesPanelPresenter,
                             CommandConsoleFactory commandConsoleFactory) {
        this.dtoFactory = dtoFactory;
        this.processListener = processListener;
        this.appContext = appContext;
        this.deviceServiceClient = deviceServiceClient;
        this.macroProcessor = macroProcessor;
        this.debugConfigurationTypeRegistry = debugConfigurationTypeRegistry;
        this.debugConfigurationsManager = debugConfigurationsManager;
        this.notificationManager = notificationManager;
        this.messageBus = messageBusProvider.getMachineMessageBus();
        this.processesPanelPresenter = processesPanelPresenter;
        this.commandConsoleFactory = commandConsoleFactory;
    }

    private static boolean isSuccessMessage(String message) {
        return message.contains("Listening on port");
    }

    /**
     * Run GDB in the specified {@code machine} and connects
     * to the debugger on the specified {@code machine}
     * for debugging project's binary file.
     */
    public void debug(final Machine machine) {
        runGdbServer(machine).then(new Operation<Integer>() {
            @Override
            public void apply(Integer port) throws OperationException {
                if (port != null) {
                    connect(machine, port);
                }
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError promiseError) throws OperationException {
                notificationManager.notify("", promiseError.getMessage());
            }
        });
    }

    /** Runs GDB server and returns the listened port. */
    private Promise<Integer> runGdbServer(final Machine machine) {
        Optional<Project> projectOptional = getCurrentProject();
        if (!projectOptional.isPresent()) {
            return Promises.resolve(null);
        }

        Project project = projectOptional.get();
        final String chanel = "process:output:" + UUID.uuid();
        final int debugPort = 1234;

        try {
            messageBus.subscribe(chanel, new SubscriptionHandler<String>(new OutputMessageUnmarshaller()) {
                @Override
                protected void onMessageReceived(String message) {
                    if (isSuccessMessage(message)) {
                        runCallback.onSuccess(debugPort);

                        try {
                            messageBus.unsubscribe(chanel, this);
                        } catch (WebSocketException e) {
                            Log.error(getClass(), e);
                        }
                    }
                }

                @Override
                protected void onErrorReceived(Throwable throwable) {
                    runCallback.onFailure(throwable);
                }
            });
        } catch (WebSocketException e) {
            runCallback.onFailure(new Exception(e));
        }

        final Promise<Integer> promise = createFromAsyncRequest(new AsyncPromiseHelper.RequestCall<Integer>() {
            @Override
            public void makeCall(AsyncCallback<Integer> callback) {
                runCallback = callback;
            }
        });

        final Command command = buildCommand(project, machine, debugPort);
        final DefaultOutputConsole console = (DefaultOutputConsole)commandConsoleFactory.create(command.getName());
        final MessageHandler handler = new MessageHandler() {
            @Override
            public void onMessage(String message) {
                console.printText(message);
            }
        };
        try {
            messageBus.subscribe(chanel, handler);
        } catch (WebSocketException e) {
            //do nothing
        }

        macroProcessor.expandMacros(command.getCommandLine()).then(new Operation<String>() {
            @Override
            public void apply(String arg) throws OperationException {
                final CommandDto commandDto = dtoFactory.createDto(CommandDto.class)
                                                        .withName(command.getName())
                                                        .withCommandLine(arg)
                                                        .withType(command.getType());

                final Promise<MachineProcessDto> processPromise = deviceServiceClient.executeCommand(machine.getId(), commandDto, chanel);
                processPromise.then(new Operation<MachineProcessDto>() {
                    @Override
                    public void apply(MachineProcessDto process) throws OperationException {
                        processesPanelPresenter.addCommandOutput(machine.getId(), console);
                        processListener.attachToProcess(process, machine, chanel, handler);
                    }
                });
            }
        });

        return promise;
    }

    /** Connect the debugger to the specified device. */
    private void connect(final Machine machine, final int debugPort) {
        final DebugConfigurationType gdbType = debugConfigurationTypeRegistry.getConfigurationTypeById("gdb");
        if (gdbType == null) {
            return;
        }

        final Map<String, String> connectionProperties = new HashMap<>();

        macroProcessor.expandMacros("${current.project.path}/${binary.name}").then(new Operation<String>() {
            @Override
            public void apply(String cmd) throws OperationException {
                connectionProperties.put("BINARY", cmd);

                getDeviceIP(machine).then(new Operation<String>() {
                    @Override
                    public void apply(String ip) throws OperationException {
                        DebugConfiguration debugConfiguration = new DebugConfiguration(gdbType,
                                                                                       "debug",
                                                                                       ip,
                                                                                       debugPort,
                                                                                       connectionProperties);
                        debugConfigurationsManager.apply(debugConfiguration);
                    }
                }).catchError(new Operation<PromiseError>() {
                    @Override
                    public void apply(PromiseError arg) throws OperationException {
                        notificationManager.notify("", arg.getMessage());
                    }
                });
            }
        });
    }

    /** Read the specified devices's IP from it's config. */
    private Promise<String> getDeviceIP(Machine machine) {
        final MachineSource source = machine.getConfig().getSource();
        if (!"ssh-config".equals(source.getType())) {
            return Promises.reject(JsPromiseError.create(new Exception("Can't get machine's address. Machine " +
                                                                       machine.getConfig().getName() +
                                                                       " isn't Artik device.")));
        }

        final String content = machine.getConfig().getSource().getContent();
        if (Strings.isNullOrEmpty(content)) {
            return Promises.resolve("");
        }
        final JSONObject jsonObject = parseLenient(content).isObject();
        final String host = jsonObject.get("host").isString().stringValue();
        return Promises.resolve(host);
    }

    private Command buildCommand(Project project, Machine machine, int debugPort) {
        final String commandName = "run gdbserver";
        final String commandType = "custom";

        StringBuilder commandLine = new StringBuilder("cd ").append(ReplicationFolderMacro.KEY.replace("%machineId%", machine.getId()))
                                                            .append("/${current.project.path} && gdbserver :")
                                                            .append(debugPort);

        String binaryName = project.getAttribute(BINARY_NAME_ATTRIBUTE);
        commandLine.append(' ').append(!isNullOrEmpty(binaryName) ? binaryName : DEFAULT_BINARY_NAME);

        return new CommandImpl(commandName, commandLine.toString(), commandType);
    }

    private Optional<Project> getCurrentProject() {
        final Resource[] resources = appContext.getResources();
        if (resources == null || resources.length != 1) {
            return Optional.absent();
        }

        Resource resource = appContext.getResource();
        return resource.getRelatedProject();
    }
}
