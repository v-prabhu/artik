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
package org.eclipse.che.plugin.machine.artik;

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.plugin.artik.shared.dto.ArtikDeviceStatusEventDto;
import org.everrest.websockets.WSConnectionContext;
import org.everrest.websockets.message.ChannelBroadcastMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

import static org.eclipse.che.plugin.artik.shared.Constants.ARTIK_DEVICE_STATUS_CHANNEL;

/**
 * Send artik device status events using websocket channel to the clients
 *
 * @author Valeriy Svydenko
 */
@Singleton // should be eager
public class ArtikDeviceStateMessenger implements EventSubscriber<ArtikDeviceStatusEventDto> {
    private static final Logger LOG = LoggerFactory.getLogger(ArtikDeviceStateMessenger.class);

    private final EventService eventService;

    @Inject
    public ArtikDeviceStateMessenger(EventService eventService) {
        this.eventService = eventService;
    }

    @Override
    public void onEvent(ArtikDeviceStatusEventDto event) {
        try {
            final ChannelBroadcastMessage bm = new ChannelBroadcastMessage();
            bm.setChannel(ARTIK_DEVICE_STATUS_CHANNEL);
            bm.setBody(DtoFactory.getInstance().toJson(event));
            WSConnectionContext.sendMessage(bm);
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    @PostConstruct
    private void subscribe() {
        eventService.subscribe(this);
    }

    @PreDestroy
    private void unsubscribe() {
        eventService.unsubscribe(this);
    }
}
