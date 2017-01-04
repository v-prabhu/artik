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
package org.eclipse.che.plugin.artik.shared.dto;

import org.eclipse.che.api.core.notification.EventOrigin;
import org.eclipse.che.dto.shared.DTO;

/**
 * Describes event about status of device
 *
 * @author Valeriy Svydenko
 */
@EventOrigin("artik")
@DTO
public interface ArtikDeviceStatusEventDto {
    enum EventType {
        CONNECTED,
        DISCONNECTED,
        ERROR
    }

    EventType getEventType();

    void setEventType(EventType eventType);

    ArtikDeviceStatusEventDto withEventType(EventType eventType);

    String getDeviceId();

    void setDeviceId(String deviceId);

    ArtikDeviceStatusEventDto withDeviceId(String deviceId);

    String getError();

    void setError(String error);

    ArtikDeviceStatusEventDto withError(String error);

    String getDeviceName();

    ArtikDeviceStatusEventDto withDeviceName(String deviceName);
}
