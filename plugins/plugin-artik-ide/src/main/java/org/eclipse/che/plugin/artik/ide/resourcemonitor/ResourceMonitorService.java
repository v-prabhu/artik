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
package org.eclipse.che.plugin.artik.ide.resourcemonitor;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper;
import org.eclipse.che.ide.api.machine.MachineServiceClient;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.util.UUID;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.MessageBusProvider;
import org.eclipse.che.ide.websocket.WebSocketException;
import org.eclipse.che.ide.websocket.rest.SubscriptionHandler;
import org.eclipse.che.plugin.artik.ide.ArtikResources;
import org.eclipse.che.plugin.artik.ide.updatesdk.OutputMessageUnmarshaller;

import static org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper.createFromAsyncRequest;

/**
 * Provides system resources usage information.
 *
 * @author Artem Zatsarynnyi
 */
public class ResourceMonitorService {

    private final MachineServiceClient  machineServiceClient;
    private final MessageBus            messageBus;
    private final DtoFactory            dtoFactory;
    private final ArtikResources        resources;
    private       AsyncCallback<String> commandCallback;

    @Inject
    public ResourceMonitorService(MachineServiceClient machineServiceClient,
                                  MessageBusProvider messageBusProvider,
                                  DtoFactory dtoFactory,
                                  ArtikResources resources) {
        this.machineServiceClient = machineServiceClient;
        this.messageBus = messageBusProvider.getMessageBus();
        this.dtoFactory = dtoFactory;
        this.resources = resources;
    }

    private static boolean isErrorMessage(String message) {
        return message.startsWith("[STDERR]") || message.startsWith("\"[STDERR]");
    }

    public Promise<String> getTotalMemory(String machineId) {
        final String cmd = resources.getTotalMemoryCommand().getText();
        return executeCommand(cmd, machineId);
    }

    public Promise<String> getUsedMemory(String machineId) {
        final String cmd = resources.getUsedMemoryCommand().getText();
        return executeCommand(cmd, machineId);
    }

    public Promise<String> getCpuUtilization(String machineId) {
        final String cmd = resources.getCpuCommand().getText();
        return executeCommand(cmd, machineId);
    }

    public Promise<String> getTotalStorageSpace(String machineId) {
        final String cmd = resources.getTotalStorageSpaceCommand().getText();
        return executeCommand(cmd, machineId);
    }

    public Promise<String> getUsedStorageSpace(String machineId) {
        final String cmd = resources.getUsedStorageSpaceCommand().getText();
        return executeCommand(cmd, machineId);
    }

    /** Executes a command and returns first message from output as a result. */
    private Promise<String> executeCommand(String cmd, String machineId) {
        final String chanel = "process:output:" + UUID.uuid();

        try {
            messageBus.subscribe(chanel, new SubscriptionHandler<String>(new OutputMessageUnmarshaller()) {
                @Override
                protected void onMessageReceived(String message) {
                    unsubscribe(chanel, this);
                    if (isErrorMessage(message)) {
                        commandCallback.onFailure(new Exception(message));
                    } else {
                        commandCallback.onSuccess(message);
                    }
                }

                @Override
                protected void onErrorReceived(Throwable throwable) {
                    unsubscribe(chanel, this);
                    commandCallback.onFailure(throwable);
                }
            });
        } catch (WebSocketException e) {
            commandCallback.onFailure(new Exception(e));
        }

        final Promise<String> promise = createFromAsyncRequest(new AsyncPromiseHelper.RequestCall<String>() {
            @Override
            public void makeCall(AsyncCallback<String> callback) {
                commandCallback = callback;
            }
        });

        final Command command = dtoFactory.createDto(CommandDto.class)
                                          .withName("name")
                                          .withType("custom")
                                          .withCommandLine(cmd);

        machineServiceClient.executeCommand(machineId, command, chanel);

        return promise;
    }

    private void unsubscribe(String channelId, SubscriptionHandler<String> handler) {
        try {
            messageBus.unsubscribe(channelId, handler);
        } catch (WebSocketException e) {
            Log.error(ResourceMonitorService.class, e);
        }
    }
}
