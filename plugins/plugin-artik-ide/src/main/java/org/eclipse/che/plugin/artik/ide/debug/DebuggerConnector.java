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
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.extension.machine.client.command.CommandManager;
import org.eclipse.che.ide.extension.machine.client.command.custom.CustomCommandConfiguration;
import org.eclipse.che.ide.extension.machine.client.command.custom.CustomCommandType;
import org.eclipse.che.plugin.artik.ide.command.macro.ReplicationFolderMacro;
import org.eclipse.che.plugin.debugger.ide.configuration.DebugConfigurationTypeRegistry;

import java.util.HashMap;
import java.util.Map;

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

    @Inject
    public DebuggerConnector(DtoFactory dtoFactory,
                             CustomCommandType customCommandType,
                             CommandManager commandManager,
                             DebugConfigurationTypeRegistry debugConfigurationTypeRegistry,
                             DebugConfigurationsManager debugConfigurationsManager,
                             NotificationManager notificationManager) {
        this.dtoFactory = dtoFactory;
        this.customCommandType = customCommandType;
        this.commandManager = commandManager;
        this.debugConfigurationTypeRegistry = debugConfigurationTypeRegistry;
        this.debugConfigurationsManager = debugConfigurationsManager;
        this.notificationManager = notificationManager;
    }

    /** Connects to the debugger on the specified {@code machine} for debugging project's binary file. */
    public void connect(final Machine machine) {
        final int debugPort = 1234;
        final String launchGdbServerCommand = "cd " + ReplicationFolderMacro.KEY.replace("%machineId%", machine.getId()) +
                                              "${explorer.current.file.parent.path} && gdbserver :" + debugPort + " ${binary.name}";

        final CommandDto commandDto = dtoFactory.createDto(CommandDto.class)
                                                .withName("run gdbserver")
                                                .withType("custom")
                                                .withCommandLine(launchGdbServerCommand);
        final CustomCommandConfiguration commandConfiguration = customCommandType.getConfigurationFactory().createFromDto(commandDto);
        commandManager.executeCommand(commandConfiguration, machine);

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
                        notificationManager.notify(arg.getCause().getMessage());
                    }
                });
            }
        });
    }

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
