/*******************************************************************************
 * Copyright (c) 2016 Samsung Electronics Co., Ltd.
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

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;

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
import org.eclipse.che.ide.api.debug.DebugConfiguration;
import org.eclipse.che.ide.api.debug.DebugConfigurationType;
import org.eclipse.che.ide.api.debug.DebugConfigurationsManager;
import org.eclipse.che.ide.api.machine.MachineServiceClient;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.extension.machine.client.command.CommandManager;
import org.eclipse.che.ide.extension.machine.client.command.custom.CustomCommandConfiguration;
import org.eclipse.che.ide.extension.machine.client.command.custom.CustomCommandType;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.CommandConsoleFactory;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.CommandOutputConsole;
import org.eclipse.che.ide.extension.machine.client.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.ide.util.UUID;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.MessageBusProvider;
import org.eclipse.che.ide.websocket.WebSocketException;
import org.eclipse.che.ide.websocket.rest.SubscriptionHandler;
import org.eclipse.che.plugin.artik.ide.command.macro.ReplicationFolderMacro;
import org.eclipse.che.plugin.artik.ide.updatesdk.OutputMessageUnmarshaller;
import org.eclipse.che.plugin.debugger.ide.configuration.DebugConfigurationTypeRegistry;

import java.util.HashMap;
import java.util.Map;

import static org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper.createFromAsyncRequest;

/**
 * Connects to the debugger for debugging project's binary file.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class DebuggerConnector {

    private final DtoFactory                     dtoFactory;
    private final CustomCommandType              customCommandType;
    private final CommandManager                 commandManager;
    private final DebugConfigurationTypeRegistry debugConfigurationTypeRegistry;
    private final DebugConfigurationsManager     debugConfigurationsManager;
    private final NotificationManager            notificationManager;
    private final MachineServiceClient           machineServiceClient;

    private final MessageBus              messageBus;
    private final ProcessesPanelPresenter processesPanelPresenter;
    private final CommandConsoleFactory   commandConsoleFactory;

    private AsyncCallback<Integer> runCallback;

    @Inject
    public DebuggerConnector(DtoFactory dtoFactory,
                             CustomCommandType customCommandType,
                             CommandManager commandManager,
                             DebugConfigurationTypeRegistry debugConfigurationTypeRegistry,
                             DebugConfigurationsManager debugConfigurationsManager,
                             NotificationManager notificationManager,
                             MachineServiceClient machineServiceClient,
                             MessageBusProvider messageBusProvider,
                             ProcessesPanelPresenter processesPanelPresenter,
                             CommandConsoleFactory commandConsoleFactory) {
        this.dtoFactory = dtoFactory;
        this.customCommandType = customCommandType;
        this.commandManager = commandManager;
        this.debugConfigurationTypeRegistry = debugConfigurationTypeRegistry;
        this.debugConfigurationsManager = debugConfigurationsManager;
        this.notificationManager = notificationManager;
        this.machineServiceClient = machineServiceClient;
        this.messageBus = messageBusProvider.getMessageBus();
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
                connect(machine, port);
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


        final String launchGdbServerCommand = "cd " + ReplicationFolderMacro.KEY.replace("%machineId%", machine.getId()) +
                                              "${explorer.current.file.parent.path} && gdbserver :" + debugPort + " ${binary.name}";

        final CommandDto commandDto = dtoFactory.createDto(CommandDto.class)
                                                .withName("run gdbserver")
                                                .withType("custom")
                                                .withCommandLine(launchGdbServerCommand);
        final CustomCommandConfiguration commandConfiguration = customCommandType.getConfigurationFactory().createFromDto(commandDto);

        final CommandOutputConsole console = commandConsoleFactory.create(commandConfiguration, machine);
        console.listenToOutput(chanel);
        processesPanelPresenter.addCommandOutput(machine.getId(), console);

        commandManager.substituteProperties(launchGdbServerCommand).then(new Operation<String>() {
            @Override
            public void apply(String arg) throws OperationException {
                final CommandDto command = dtoFactory.createDto(CommandDto.class)
                                                     .withName("run gdbserver")
                                                     .withCommandLine(arg)
                                                     .withType("custom");

                final Promise<MachineProcessDto> processPromise =
                        machineServiceClient.executeCommand(machine.getWorkspaceId(),
                                                            machine.getId(),
                                                            command,
                                                            chanel);
                processPromise.then(new Operation<MachineProcessDto>() {
                    @Override
                    public void apply(MachineProcessDto process) throws OperationException {
                        console.attachToProcess(process);
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

        commandManager.substituteProperties("${explorer.current.file.parent.path}/${binary.name}").then(new Operation<String>() {
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

        final String location = source.getLocation();

        final RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, location);

        return AsyncPromiseHelper.createFromAsyncRequest(new AsyncPromiseHelper.RequestCall<String>() {
            @Override
            public void makeCall(final AsyncCallback<String> callback) {
                requestBuilder.setCallback(new RequestCallback() {
                    @Override
                    public void onResponseReceived(Request request, Response response) {
                        try {
                            JSONValue jsonValue = JSONParser.parseStrict(response.getText());
                            JSONValue host = jsonValue.isObject().get("host");

                            callback.onSuccess(host.isString().stringValue());
                        } catch (Exception e) {
                            callback.onFailure(e);
                        }
                    }

                    @Override
                    public void onError(Request request, Throwable exception) {
                        callback.onFailure(exception);
                    }
                });

                try {
                    requestBuilder.send();
                } catch (RequestException e) {
                    callback.onFailure(e);
                }
            }
        });
    }
}
