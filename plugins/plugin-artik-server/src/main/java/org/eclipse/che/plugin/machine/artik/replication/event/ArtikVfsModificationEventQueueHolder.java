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

import org.eclipse.che.plugin.artik.shared.dto.ArtikVfsModificationEventDto;
import org.eclipse.che.plugin.machine.artik.replication.ReplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static java.util.Optional.empty;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * A queue holder. It is used to benefit of Guice DI mechanism in sharing access to {@link BlockingQueue}
 * instance for both {@link ArtikVfsModificationEventListener} and {@link ReplicationService}.
 *
 * @author Dmitry Kuleshov
 *
 * @since 4.5
 */
@Beta
@Singleton
public class ArtikVfsModificationEventQueueHolder {

    private static final Logger LOG = getLogger(ArtikVfsModificationEventQueueHolder.class);

    private final BlockingQueue<ArtikVfsModificationEventDto> eventQueue;

    public ArtikVfsModificationEventQueueHolder() {
        this.eventQueue = new LinkedBlockingQueue<>();
    }

    public void put(ArtikVfsModificationEventDto eventDto) {
        try {
            eventQueue.put(eventDto);
        } catch (InterruptedException e) {
            LOG.error("Error trying to put an event to an event queue: {}", eventDto, e);
        }
    }

    public Optional<ArtikVfsModificationEventDto> take() {
        try {
            return Optional.of(eventQueue.take());
        } catch (InterruptedException e) {
            LOG.error("Error trying to poll an event out of an event queue", e);
            return empty();
        }
    }
}
