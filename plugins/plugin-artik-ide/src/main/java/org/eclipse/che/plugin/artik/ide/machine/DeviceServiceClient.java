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
package org.eclipse.che.plugin.artik.ide.machine;

import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.machine.shared.dto.MachineProcessDto;
import org.eclipse.che.api.promises.client.Promise;

import java.util.List;

/**
 * Client for Artik device API
 *
 * @author Valeriy Svydenko
 */
public interface DeviceServiceClient {

    /**
     * Connect to device.
     *
     * @param config
     *         the new device configuration
     * @return a promise that resolves to device id, or rejects with an error
     */
    Promise<MachineDto> connect(MachineConfigDto config);

    /**
     * Connect to existing device.
     *
     * @param deviceId
     *         device's id
     * @return a promise that resolves to device id, or rejects with an error
     */
    Promise<MachineDto> connectById(String deviceId);

    /**
     * Returns device with the specified ID.
     *
     * @param deviceId
     *         ID of device
     * @return a promise that will resolve device by ID, or rejects with an error
     */
    Promise<MachineDto> getDevice(String deviceId);

    /**
     * Returns list of devices which are connected.
     *
     * @return a promise that will provide a list of {@link MachineDto}s, or rejects with an error
     */
    Promise<List<MachineDto>> getDevices();

    /**
     * Disconnect device with the specified ID.
     *
     * @param deviceId
     *         ID of device that should be destroyed
     * @param remove
     *         is true if device should be removed from the storage
     * @return a promise that will resolve when the device has been disconnected, or rejects with an error
     */
    Promise<MachineDto> disconnect(String deviceId, boolean remove);

    /**
     * Get processes from the specified device.
     *
     * @param deviceId
     *         ID of device to get processes information from
     * @return a promise that will provide a list of {@link MachineProcessDto}s for the given device ID
     */
    Promise<List<MachineProcessDto>> getProcesses(String deviceId);

    /**
     * Execute a command in device.
     *
     * @param deviceId
     *         ID of the device where command should be executed
     * @param command
     *         the command that should be executed in the device
     * @param outputChannel
     *         websocket chanel for execution logs
     * @return a promise that resolves to the {@link MachineProcessDto}, or rejects with an error
     */
    Promise<MachineProcessDto> executeCommand(String deviceId, Command command, String outputChannel);

    /**
     * Stop process in device.
     *
     * @param deviceId
     *         ID of the device where process should be stopped
     * @param processId
     *         ID of the process to stop
     * @return a promise that will resolve when the process has been stopped, or rejects with an error
     */
    Promise<Void> stopProcess(String deviceId, int processId);
}
