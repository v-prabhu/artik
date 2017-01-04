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
package org.eclipse.che.plugin.artik.ide.profile;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.extension.machine.client.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.ide.util.UUID;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.MessageBusProvider;
import org.eclipse.che.ide.websocket.WebSocketException;
import org.eclipse.che.ide.websocket.rest.StringUnmarshallerWS;
import org.eclipse.che.ide.websocket.rest.SubscriptionHandler;
import org.eclipse.che.plugin.artik.ide.ArtikResources;
import org.eclipse.che.plugin.artik.ide.machine.DeviceServiceClient;

import static org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper.createFromAsyncRequest;
import static org.eclipse.che.plugin.artik.ide.profile.Software.GDB_SERVER;
import static org.eclipse.che.plugin.artik.ide.profile.Software.RSYNC;

/**
 * @author Dmitry Kuleshov
 */
@Singleton
public class SoftwareInstaller {
    private final MessageBusProvider      messageBusProvider;
    private final DtoFactory              dtoFactory;
    private final ProcessesPanelPresenter processesPanelPresenter;
    private final DeviceServiceClient     deviceServiceClient;

    @Inject
    public SoftwareInstaller(DeviceServiceClient deviceServiceClient,
                             MessageBusProvider messageBusProvider,
                             DtoFactory dtoFactory,
                             ProcessesPanelPresenter processesPanelPresenter,
                             ArtikResources artikResources) {
        this.deviceServiceClient = deviceServiceClient;
        this.messageBusProvider = messageBusProvider;
        this.dtoFactory = dtoFactory;
        this.processesPanelPresenter = processesPanelPresenter;


        GDB_SERVER.setInstallationCommand(artikResources.gdbServerInstallationCommand().getText());
        RSYNC.setInstallationCommand(artikResources.rsyncInstallationCommand().getText());
    }

    public Promise<Void> install(final Software softwareType, final Machine device) {
        Log.debug(getClass(), "Installing missing software: " + softwareType);

        final String chanel = "process:output:" + UUID.uuid();

        final Promise<Void> promise = createFromAsyncRequest(new AsyncPromiseHelper.RequestCall<Void>() {
            @Override
            public void makeCall(AsyncCallback<Void> callback) {
                readChannel(device.getConfig().getName(), chanel, callback);
            }
        });


        final String commandLine = softwareType.getInstallationCommand() + "echo \">>> end <<<\"";
        final String commandName = softwareType.name() + "_installation";
        final String commandType = "custom";

        final Command command = dtoFactory.createDto(CommandDto.class)
                                          .withName(commandName)
                                          .withType(commandType)
                                          .withCommandLine(commandLine);

        Log.debug(getClass(), "Installation command: " + command);

        deviceServiceClient.executeCommand(device.getId(), command, chanel);

        return promise;
    }

    private void readChannel(final String deviceName, final String chanel, final AsyncCallback<Void> commandCallback) {
        final MessageBus messageBus = messageBusProvider.getMachineMessageBus();
        try {
            messageBus.subscribe(chanel, new SubscriptionHandler<String>(new StringUnmarshallerWS()) {
                @Override
                protected void onMessageReceived(String message) {
                    if ("[STDOUT] >>> end <<<".equals(message)) {
                        messageBus.unsubscribeSilently(chanel, this);
                        processesPanelPresenter.printMachineOutput(deviceName, "\n");
                        commandCallback.onSuccess(null);

                        Log.debug(getClass(), message);
                    } else {
                        if (message.startsWith("[STDOUT] ")) {
                            processesPanelPresenter.printMachineOutput(deviceName, message.substring(9));

                            Log.debug(getClass(), message);
                        } else if (message.startsWith("[STDERR] ")) {
                            processesPanelPresenter.printMachineOutput(deviceName, message.substring(9), "red");

                            Log.error(getClass(), message);
                        } else {
                            processesPanelPresenter.printMachineOutput(deviceName, message);

                            Log.debug(getClass(), message);
                        }
                    }
                }

                @Override
                protected void onErrorReceived(Throwable throwable) {
                    messageBus.unsubscribeSilently(chanel, this);

                    Log.error(getClass(), throwable);
                }
            });
        } catch (WebSocketException e) {
            commandCallback.onFailure(new Exception(e));
        }
    }
}
