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

import com.google.inject.Inject;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.machine.shared.Constants;
import org.eclipse.che.api.machine.shared.dto.MachineProcessDto;
import org.eclipse.che.api.machine.shared.dto.event.MachineProcessEvent;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.StringUnmarshaller;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.MessageBusProvider;
import org.eclipse.che.ide.websocket.WebSocketException;
import org.eclipse.che.ide.websocket.events.MessageHandler;
import org.eclipse.che.ide.websocket.rest.SubscriptionHandler;
import org.eclipse.che.ide.websocket.rest.Unmarshallable;

/**
 * Listening process status.
 *
 * @author Valeriy Svydenko
 */
public class ProcessListener {
    private final AsyncRequestFactory    asyncRequestFactory;
    private final DtoUnmarshallerFactory dtoUnmarshallerFactory;
    private final MessageBus             messageBus;

    private int     pid;
    private Machine machine;

    @Inject
    public ProcessListener(AsyncRequestFactory asyncRequestFactory,
                           DtoUnmarshallerFactory dtoUnmarshallerFactory,
                           MessageBusProvider messageBusProvider) {
        this.asyncRequestFactory = asyncRequestFactory;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.messageBus = messageBusProvider.getMachineMessageBus();
    }

    /**
     * Handles process events.
     *
     * @param process
     *         running process
     * @param machine
     *         current machine
     * @param outputChannel
     *         ws channel
     * @param outputMessageHandler
     *         messages handler
     */
    public void attachToProcess(final MachineProcessDto process,
                                final Machine machine,
                                final String outputChannel,
                                final MessageHandler outputMessageHandler) {
        this.pid = process.getPid();
        this.machine = machine;
        final Link link = process.getLink(Constants.LINK_REL_GET_PROCESS_LOGS);
        if (link != null) {
            asyncRequestFactory.createGetRequest(link.getHref()).send(new StringUnmarshaller()).then(
                    new Operation<String>() {
                        @Override
                        public void apply(String arg) throws OperationException {
                            handelProcessEvents(outputChannel, outputMessageHandler); //start handel  incoming events
                        }
                    }).catchError(new Operation<PromiseError>() {
                @Override
                public void apply(PromiseError arg) throws OperationException {
                    //if logs not found will handel incoming events any way
                    handelProcessEvents(outputChannel, outputMessageHandler);
                }
            });
        } else {
            handelProcessEvents(outputChannel, outputMessageHandler);
        }
    }

    private void handelProcessEvents(final String outputChannel, final MessageHandler outputMessageHandler) {
        final Unmarshallable<MachineProcessEvent> unmarshaller = dtoUnmarshallerFactory.newWSUnmarshaller(MachineProcessEvent.class);
        final String processStateChannel = "machine:process:" + machine.getId();
        final MessageHandler handler = new SubscriptionHandler<MachineProcessEvent>(unmarshaller) {
            @Override
            protected void onMessageReceived(MachineProcessEvent result) {
                final int processId = result.getProcessId();
                if (pid != processId) {
                    return;
                }
                switch (result.getEventType()) {
                    case STOPPED:
                    case ERROR:
                        wsUnsubscribe(processStateChannel, this);
                        wsUnsubscribe(outputChannel, outputMessageHandler);
                        break;
                }
            }

            @Override
            protected void onErrorReceived(Throwable exception) {
                wsUnsubscribe(processStateChannel, this);
                wsUnsubscribe(outputChannel, outputMessageHandler);
            }
        };
        wsSubscribe(processStateChannel, handler);
    }

    private void wsSubscribe(String wsChannel, MessageHandler handler) {
        try {
            messageBus.subscribe(wsChannel, handler);
        } catch (WebSocketException e) {
            Log.error(getClass(), e);
        }
    }

    private void wsUnsubscribe(String wsChannel, MessageHandler handler) {
        try {
            messageBus.unsubscribe(wsChannel, handler);
        } catch (WebSocketException e) {
            Log.error(getClass(), e);
        }
    }

}
