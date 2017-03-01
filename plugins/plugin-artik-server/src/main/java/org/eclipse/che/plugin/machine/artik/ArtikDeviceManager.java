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

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.WorkspaceIdProvider;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.machine.MachineLogMessage;
import org.eclipse.che.api.core.model.machine.MachineStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.util.AbstractLineConsumer;
import org.eclipse.che.api.core.util.AbstractMessageConsumer;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.core.util.ListLineConsumer;
import org.eclipse.che.api.core.util.MessageConsumer;
import org.eclipse.che.api.core.util.ProcessUtil;
import org.eclipse.che.api.core.util.WebsocketMessageConsumer;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.model.impl.MachineImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineLogMessageImpl;
import org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.commons.schedule.executor.ThreadPullLauncher;
import org.eclipse.che.plugin.artik.shared.dto.ArtikDeviceStatusEventDto;
import org.eclipse.che.plugin.machine.ssh.SshMachineInstance;
import org.eclipse.che.plugin.machine.ssh.SshMachineProcess;
import org.eclipse.che.plugin.machine.ssh.SshMachineRecipe;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.eclipse.che.api.core.model.machine.MachineStatus.RUNNING;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.eclipse.che.plugin.artik.shared.Constants.ARTIK_DEVICE_STATUS_CHANNEL;
import static org.eclipse.che.plugin.machine.artik.ArtikDevice.Status.CONNECTED;
import static org.eclipse.che.plugin.machine.artik.ArtikDevice.Status.ERROR;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Facade for Artik device operations
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class ArtikDeviceManager {
    private static final Logger LOG  = getLogger(ArtikDeviceManager.class);
    private static final Gson   GSON = new Gson();

    private final EventService                eventService;
    private final ThreadPullLauncher          launcher;
    private final ArtikTerminalLauncher       artikTerminalLauncher;
    private final ArtikDeviceInstanceProvider machineInstanceProvider;

    private Map<String, ArtikDevice>         instances;
    private Map<String, DeviceHealthChecker> checkers;

    @Inject
    public ArtikDeviceManager(EventService eventService,
                              ThreadPullLauncher launcher,
                              ArtikTerminalLauncher artikTerminalLauncher,
                              ArtikDeviceInstanceProvider machineInstanceProvider) {
        this.eventService = eventService;
        this.launcher = launcher;
        this.artikTerminalLauncher = artikTerminalLauncher;
        this.machineInstanceProvider = machineInstanceProvider;

        instances = new ConcurrentHashMap<>();
        checkers = new ConcurrentHashMap<>();
    }

    /**
     * Retrieves device instance that allows to execute commands in a device.
     *
     * @param deviceId
     *         ID of requested device
     * @return instance of requested device
     * @throws NotFoundException
     *         if workspace is not running
     */
    public MachineDto getDeviceById(String deviceId) throws NotFoundException {
        final SshMachineInstance machine = instances.get(deviceId).getInstance();
        if (machine == null) {
            throw new NotFoundException(format("Machine with ID '%s' is not found", deviceId));
        }
        return ArtikDtoConverter.asDto(machine);
    }

    /**
     * Get all created devices. Each device has status {@link MachineStatus#RUNNING} or {@link MachineStatus#DESTROYING}
     *
     * @return list of alive devices
     */
    public List<MachineDto> getDevices() {
        return instances.values()
                        .stream()
                        .map(artikDevice -> ArtikDtoConverter.asDto(artikDevice.getInstance()))
                        .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Disconnect a device.
     *
     * @param deviceId
     *         ID of device that should be disconnected
     * @param remove
     *         is {@code true} when device should be removed from the storage
     * @return disconnected device
     * @throws NotFoundException
     *         if device is not found
     * @throws MachineException
     *         if other error occurs
     */
    MachineDto disconnect(String deviceId, boolean remove) throws MachineException, NotFoundException {
        final ArtikDevice device = instances.get(deviceId);
        final SshMachineInstance instance = device.getInstance();

        final DeviceHealthChecker deviceHealthChecker = checkers.get(deviceId);
        if (deviceHealthChecker != null) {
            deviceHealthChecker.stop();
        }

        if (remove) {
            for (SshMachineProcess process : instance.getProcesses()) {
                process.kill();
            }
            instances.remove(deviceId);
            checkers.remove(deviceId);
        } else {
            device.disconnect();
            if (ArtikDevice.Status.CONNECTED.equals(device.getStatus())) {
                device.setStatus(ArtikDevice.Status.DISCONNECTED);
            }
        }

        return ArtikDtoConverter.asDto(instance);
    }

    /**
     * Creates new device.
     *
     * @param deviceId
     *         id of the device
     * @return configuration of the connected device
     * @throws ServerException
     *         if some exception occurs during starting
     * @throws NotFoundException
     *         if device is not found in running workspace
     */
    MachineDto connectById(String deviceId) throws ServerException, NotFoundException {
        final ArtikDevice device = instances.get(deviceId);
        if (device == null) {
            throw new NotFoundException(format("Device with ID '%s' is not found", deviceId));
        }
        device.connect();
        SshMachineInstance instance = device.getInstance();
        instance = createNewInstance(deviceId, instance);

        artikTerminalLauncher.launch(instance);

        DeviceHealthChecker deviceHealthChecker = checkers.get(deviceId);
        if (deviceHealthChecker != null) {
            deviceHealthChecker.start();
        } else {
            deviceHealthChecker = new DeviceHealthChecker(device);
            checkers.put(deviceId, deviceHealthChecker);
            launcher.scheduleWithFixedDelay(deviceHealthChecker, 2L, 2L, SECONDS);
        }

        return ArtikDtoConverter.asDto(instance);
    }

    private SshMachineInstance createNewInstance(String deviceId, SshMachineInstance instance) throws NotFoundException, ServerException {
        MachineImpl machine = MachineImpl.builder()
                                         .setConfig(instance.getMachineConfig())
                                         .setWorkspaceId(WorkspaceIdProvider.getWorkspaceId())
                                         .setStatus(MachineStatus.CREATING)
                                         .setOwner(instance.getOwner())
                                         .setId(deviceId)
                                         .build();

        final LineConsumer machineLogger = getDeviceLogger(getMessageConsumer(), instance.getMachineConfig().getName());
        final SshMachineInstance newInstance = machineInstanceProvider.createInstance(machine, machineLogger);

        artikTerminalLauncher.launch(newInstance);
        machine.setStatus(RUNNING);

        final ArtikDevice artikDevice = new ArtikDevice(newInstance, CONNECTED);
        instances.put(deviceId, artikDevice);
        final DeviceHealthChecker deviceHealthChecker = checkers.get(deviceId);
        if (deviceHealthChecker != null) {
            deviceHealthChecker.setDevice(artikDevice);
        }

        return newInstance;
    }

    /**
     * Creates new device.
     *
     * @param deviceConfig
     *         configuration of device to connect
     * @return connected device
     * @throws ServerException
     *         if some exception occurs during connecting
     */
    MachineDto connect(MachineConfigDto deviceConfig) throws ServerException {
        final String creator = EnvironmentContext.getCurrent().getSubject().getUserId();
        final String deviceId = generateDeviceId();

        MachineImpl machine = MachineImpl.builder()
                                         .setConfig(deviceConfig)
                                         .setWorkspaceId(WorkspaceIdProvider.getWorkspaceId())
                                         .setStatus(MachineStatus.CREATING)
                                         .setOwner(creator)
                                         .setId(deviceId)
                                         .build();
        try {
            final LineConsumer machineLogger = getDeviceLogger(getMessageConsumer(), deviceConfig.getName());
            final SshMachineInstance instance = machineInstanceProvider.createInstance(machine, machineLogger);

            artikTerminalLauncher.launch(instance);

            machine.setStatus(RUNNING);
            final ArtikDevice artikDevice = new ArtikDevice(instance, CONNECTED);

            instances.put(deviceId, artikDevice);

            eventService.publish(newDto(ArtikDeviceStatusEventDto.class)
                                         .withEventType(ArtikDeviceStatusEventDto.EventType.CONNECTED)
                                         .withDeviceName(deviceConfig.getName())
                                         .withDeviceId(deviceId));

            final DeviceHealthChecker deviceHealthChecker = new DeviceHealthChecker(artikDevice);
            checkers.put(deviceId, deviceHealthChecker);
            launcher.scheduleWithFixedDelay(deviceHealthChecker, 1L, 2L, SECONDS);

            return ArtikDtoConverter.asDto(instance);
        } catch (ApiException e) {
            eventService.publish(newDto(ArtikDeviceStatusEventDto.class)
                                         .withEventType(ArtikDeviceStatusEventDto.EventType.ERROR)
                                         .withDeviceName(deviceConfig.getName())
                                         .withDeviceId(deviceId));

            throw new ServerException(e);
        }
    }

    /**
     * Restores created devices.
     *
     * @param devicesConfigs
     *         list of configurations
     * @return list of restored devices
     * @throws ServerException
     *         if some exception occurs during creating
     */
    List<MachineDto> restoreDevices(List<MachineConfigDto> devicesConfigs) throws ServerException {
        List<MachineDto> devices = new LinkedList<>();
        for (MachineConfigDto deviceConfig : devicesConfigs) {
            final String creator = EnvironmentContext.getCurrent().getSubject().getUserId();
            String deviceId = generateDeviceId();

            MachineImpl machine = MachineImpl.builder()
                                             .setConfig(deviceConfig)
                                             .setWorkspaceId(WorkspaceIdProvider.getWorkspaceId())
                                             .setStatus(MachineStatus.CREATING)
                                             .setOwner(creator)
                                             .setId(deviceId)
                                             .build();
            try {
                final LineConsumer machineLogger = getDeviceLogger(getMessageConsumer(), deviceConfig.getName());
                final SshMachineInstance instance = machineInstanceProvider.createInstance(machine, machineLogger);

                artikTerminalLauncher.launch(instance);

                final ArtikDevice artikDevice = new ArtikDevice(instance, ArtikDevice.Status.DISCONNECTED);
                artikDevice.disconnect();
                instances.put(deviceId, artikDevice);

                devices.add(ArtikDtoConverter.asDto(instance));
            } catch (ApiException e) {
                throw new ServerException(e);
            }
        }

        return devices;
    }

    private String generateDeviceId() {
        return NameGenerator.generate("artik-device", 16);
    }

    private LineConsumer getDeviceLogger(MessageConsumer<MachineLogMessage> deviceStatusLogger,
                                         String machineName) throws ServerException {
        return new ArtikDeviceConsumer(deviceStatusLogger, machineName);
    }

    private MessageConsumer<MachineLogMessage> getMessageConsumer() throws ServerException {
        WebsocketMessageConsumer<MachineLogMessage> deviceMessageConsumer = new WebsocketMessageConsumer<>(ARTIK_DEVICE_STATUS_CHANNEL);
        return new ArtikMessageConsumer(deviceMessageConsumer);
    }

    private static class ArtikMessageConsumer extends AbstractMessageConsumer<MachineLogMessage> {
        private final WebsocketMessageConsumer<MachineLogMessage> deviceMessageConsumer;

        ArtikMessageConsumer(WebsocketMessageConsumer<MachineLogMessage> deviceMessageConsumer) {
            this.deviceMessageConsumer = deviceMessageConsumer;
        }

        @Override
        public void consume(MachineLogMessage message) throws IOException {
            deviceMessageConsumer.consume(message);
        }
    }

    private static class ArtikDeviceConsumer extends AbstractLineConsumer {
        private final MessageConsumer<MachineLogMessage> deviceStatusLogger;
        private final String                             machineName;

        ArtikDeviceConsumer(MessageConsumer<MachineLogMessage> deviceStatusLogger, String machineName) {
            this.deviceStatusLogger = deviceStatusLogger;
            this.machineName = machineName;
        }

        @Override
        public void writeLine(String line) throws IOException {
            deviceStatusLogger.consume(new MachineLogMessageImpl(machineName, line));
        }
    }

    /**
     * Mechanism for verifying state of the connection to the device.
     */
    private class DeviceHealthChecker implements Runnable {
        private boolean     stop;
        private ArtikDevice device;
        private String      host;
        private Integer     port;

        public DeviceHealthChecker(ArtikDevice device) {
            this.device = device;
            this.stop = false;

            final String content = device.getInstance().getMachineConfig().getSource().getContent();
            final SshMachineRecipe deviceRecipe = GSON.fromJson(content, SshMachineRecipe.class);
            this.host = deviceRecipe.getHost();
            this.port = deviceRecipe.getPort();
        }

        @Override
        public void run() {
            if (!stop) {
                checkConnection(device);
            }
        }

        /**
         * Starts checking of the connection.
         */
        public void start() {
            this.stop = false;
        }

        /**
         * Stops checking of the connection
         */
        public void stop() {
            this.stop = true;
        }

        public void setDevice(ArtikDevice device) {
            this.device = device;

            final String content = device.getInstance().getMachineConfig().getSource().getContent();
            final SshMachineRecipe deviceRecipe = GSON.fromJson(content, SshMachineRecipe.class);
            this.host = deviceRecipe.getHost();
            this.port = deviceRecipe.getPort();
        }

        private void checkConnection(ArtikDevice device) {
            final ListLineConsumer listLineConsumer = new ListLineConsumer();
            final SshMachineInstance instance = device.getInstance();
            final ProcessBuilder processBuilder = new ProcessBuilder("nc", "-vi", "0.01", host, port.toString());

            try {
                ProcessUtil.execute(processBuilder, listLineConsumer);

                sleep(100);

                final String outputText = listLineConsumer.getText();
                if (outputText.contains("No route to host") ||
                    outputText.contains("Connection refused") ||
                    outputText.contains("Connection timed out")) {
                    device.setStatus(ERROR);
                    eventService.publish(newDto(ArtikDeviceStatusEventDto.class)
                                                 .withEventType(ArtikDeviceStatusEventDto.EventType.DISCONNECTED)
                                                 .withDeviceName(instance.getMachineConfig().getName())
                                                 .withDeviceId(instance.getId()));
                }
            } catch (IOException | InterruptedException e) {
                LOG.error(e.getMessage());
            }
        }
    }
}
