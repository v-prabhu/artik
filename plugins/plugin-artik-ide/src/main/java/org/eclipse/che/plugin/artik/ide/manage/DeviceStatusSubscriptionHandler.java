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
package org.eclipse.che.plugin.artik.ide.manage;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.workspace.event.MachineStatusChangedEvent;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.websocket.rest.SubscriptionHandler;
import org.eclipse.che.ide.workspace.WorkspaceEventsHandler;
import org.eclipse.che.plugin.artik.shared.dto.ArtikDeviceStatusEventDto;

/**
 * Handler to receive messages by subscription of Artik device.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class DeviceStatusSubscriptionHandler extends SubscriptionHandler<ArtikDeviceStatusEventDto>{
    private final EventBus eventBus;
    private final AppContext appContext;

    @Inject
    DeviceStatusSubscriptionHandler(final EventBus eventBus,
                                    final DtoUnmarshallerFactory dtoUnmarshallerFactory,
                                    final AppContext appContext) {
        super(dtoUnmarshallerFactory.newWSUnmarshaller(ArtikDeviceStatusEventDto.class));

        this.eventBus = eventBus;
        this.appContext = appContext;
    }

    @Override
    protected void onMessageReceived(ArtikDeviceStatusEventDto result) {
        MachineStatusEvent.EventType machineStatusType;
        switch (result.getEventType()) {
            case CONNECTED:
                machineStatusType = MachineStatusEvent.EventType.RUNNING;
                break;
            case DISCONNECTED:
                machineStatusType = MachineStatusEvent.EventType.DESTROYED;
                break;
            default:
                machineStatusType = MachineStatusEvent.EventType.ERROR;
        }

        MachineStatusChangedEvent machineStatusChangedEvent = new MachineStatusChangedEvent(appContext.getWorkspaceId(),
                                                                                            result.getDeviceId(),
                                                                                            result.getDeviceName(),
                                                                                            false,
                                                                                            machineStatusType,
                                                                                            result.getError());

        eventBus.fireEvent(machineStatusChangedEvent);
    }

    @Override
    protected void onErrorReceived(Throwable exception) {
        Log.error(WorkspaceEventsHandler.class, exception);
    }
}
