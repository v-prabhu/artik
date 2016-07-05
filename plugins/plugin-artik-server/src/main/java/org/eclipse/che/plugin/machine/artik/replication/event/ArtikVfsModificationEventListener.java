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
package org.eclipse.che.plugin.machine.artik.replication.event;

import com.google.common.annotations.Beta;

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.plugin.artik.shared.dto.ArtikVfsModificationEventDto;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Dmitry Kuleshov
 *
 * @since 4.5
 */
@Beta
@Singleton
public class ArtikVfsModificationEventListener implements EventSubscriber<ArtikVfsModificationEventDto> {
    private final EventService                         eventService;
    private final ArtikVfsModificationEventQueueHolder queueHolder;

    @Inject
    public ArtikVfsModificationEventListener(EventService eventService, ArtikVfsModificationEventQueueHolder queueHolder) {
        this.eventService = eventService;
        this.queueHolder = queueHolder;
    }

    @PostConstruct
    public void postConstruct() {
        eventService.subscribe(this);
    }

    @PreDestroy
    public void preDestroy() {
        eventService.unsubscribe(this);
    }

    @Override
    public void onEvent(ArtikVfsModificationEventDto event) {
        queueHolder.put(event);
    }
}
