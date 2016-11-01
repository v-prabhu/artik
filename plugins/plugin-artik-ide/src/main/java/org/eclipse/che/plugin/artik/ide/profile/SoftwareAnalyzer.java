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
package org.eclipse.che.plugin.artik.ide.profile;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.util.UUID;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.MessageBusProvider;
import org.eclipse.che.ide.websocket.WebSocketException;
import org.eclipse.che.ide.websocket.rest.StringUnmarshallerWS;
import org.eclipse.che.ide.websocket.rest.SubscriptionHandler;
import org.eclipse.che.plugin.artik.ide.ArtikResources;
import org.eclipse.che.plugin.artik.ide.machine.DeviceServiceClient;

import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper.createFromAsyncRequest;
import static org.eclipse.che.plugin.artik.ide.profile.Software.GDB_SERVER;
import static org.eclipse.che.plugin.artik.ide.profile.Software.RSYNC;

/**
 * @author Dmitry Kuleshov
 */
@Singleton
public class SoftwareAnalyzer {
    private static Software[] REQUIRED_SOFTWARE = Software.values();

    private final MessageBusProvider  messageBusProvider;
    private final DtoFactory          dtoFactory;
    private final DeviceServiceClient deviceServiceClient;

    @Inject
    public SoftwareAnalyzer(DeviceServiceClient deviceServiceClient,
                            MessageBusProvider messageBusProvider,
                            DtoFactory dtoFactory,
                            ArtikResources artikResources) {
        this.deviceServiceClient = deviceServiceClient;
        this.messageBusProvider = messageBusProvider;
        this.dtoFactory = dtoFactory;


        GDB_SERVER.setVerificationCommand(artikResources.gdbServerVerificationCommand().getText());
        RSYNC.setVerificationCommand(artikResources.rsyncVerificationCommand().getText());
    }

    public Promise<Set<Software>> getMissingSoft(final String machineId) {
        Log.debug(getClass(), "Verifying software for machine: " + machineId);

        final String chanel = "process:output:" + UUID.uuid();
        final Set<Software> missingSoftware = new HashSet<>(asList(REQUIRED_SOFTWARE));


        final Promise<Set<Software>> promise = createFromAsyncRequest(new AsyncPromiseHelper.RequestCall<Set<Software>>() {
            @Override
            public void makeCall(AsyncCallback<Set<Software>> callback) {
                readChannel(machineId, chanel, missingSoftware, callback);
            }
        });

        final StringBuilder commandLineBuilder = new StringBuilder();
        for (Software softwareType : REQUIRED_SOFTWARE) {
            final String checkCommand = softwareType.getVerificationCommand();
            commandLineBuilder.append(checkCommand);
            commandLineBuilder.append("\n");
        }
        commandLineBuilder.append("echo \">>> end <<<\"");
        final String commandLine = commandLineBuilder.toString();
        final String commandName = "get-missing-software";
        final String commandType = "custom";

        final Command command = dtoFactory.createDto(CommandDto.class)
                                          .withName(commandName)
                                          .withType(commandType)
                                          .withCommandLine(commandLine);

        Log.debug(getClass(), "Verification command: " + command);

        deviceServiceClient.executeCommand(machineId, command, chanel);

        return promise;
    }

    private void readChannel(final String machineId,
                             final String chanel,
                             final Set<Software> requiredSoftware,
                             final AsyncCallback<Set<Software>> commandCallback) {
        final MessageBus messageBus = messageBusProvider.getMachineMessageBus();
        try {
            messageBus.subscribe(chanel, new SubscriptionHandler<String>(new StringUnmarshallerWS()) {
                @Override
                protected void onMessageReceived(String message) {
                    if ("[STDOUT] >>> end <<<".equals(message)) {
                        messageBus.unsubscribeSilently(chanel, this);
                        commandCallback.onSuccess(requiredSoftware);

                        Log.debug(getClass(), "Found missing software: " + requiredSoftware);
                    } else {
                        if (message.startsWith("[STDOUT] ") && message.contains(GDB_SERVER.name)) {
                            requiredSoftware.remove(GDB_SERVER);

                            Log.debug(getClass(), "Debug: " + machineId + ", " + message);
                        } else if (message.startsWith("[STDOUT] ") && message.contains(RSYNC.name)) {
                            requiredSoftware.remove(RSYNC);

                            Log.debug(getClass(), "Debug: " + machineId + ", " + message);
                        } else if (message.startsWith("[STDERR] ")) {
                            Log.error(getClass(), "Error: " + machineId + ", " + message.substring(9));
                        } else {
                            Log.debug(getClass(), "Debug: " + machineId + ", " + message);
                        }
                    }
                }

                @Override
                protected void onErrorReceived(Throwable throwable) {
                    messageBus.unsubscribeSilently(chanel, this);
                }
            });
        } catch (WebSocketException e) {
            commandCallback.onFailure(new Exception(e));
        }
    }
}
