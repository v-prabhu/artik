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

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.plugin.artik.shared.dto.ArtikVfsModificationEventDto;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


/**
 * Tests for {@link ArtikVfsModificationEventListener}
 *
 * @author Dmitry Kuleshov
 *
 * @since 4.5
 */
@Listeners(value = {MockitoTestNGListener.class})
public class ArtikVfsModificationEventListenerTest {

    private EventService                         eventService;
    @Mock
    private ArtikVfsModificationEventQueueHolder queueHolder;
    @InjectMocks
    private ArtikVfsModificationEventListener    listener;

    @BeforeMethod
    public void beforeMethod() {
        eventService = new EventService();
        listener = new ArtikVfsModificationEventListener(eventService, queueHolder);

        listener.postConstruct();
    }

    @AfterMethod
    public void afterMethod() {
        listener.preDestroy();
    }

    @Test
    public void shouldRunOnEventAndAddEventToQueueHolder() {
        final ArtikVfsModificationEventDto eventDto = mock(ArtikVfsModificationEventDto.class);

        eventService.publish(eventDto);

        verify(queueHolder, times(1)).put(eq(eventDto));
    }
}
