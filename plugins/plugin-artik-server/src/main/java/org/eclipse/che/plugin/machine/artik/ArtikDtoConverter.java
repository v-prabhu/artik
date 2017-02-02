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

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.machine.MachineProcess;
import org.eclipse.che.api.core.model.machine.MachineRuntimeInfo;
import org.eclipse.che.api.core.model.machine.Server;
import org.eclipse.che.api.machine.server.DtoConverter;
import org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.machine.shared.dto.MachineProcessDto;
import org.eclipse.che.api.machine.shared.dto.MachineRuntimeInfoDto;
import org.eclipse.che.api.machine.shared.dto.ServerDto;
import org.eclipse.che.plugin.machine.ssh.SshMachineInstance;
import org.eclipse.che.plugin.machine.ssh.SshMachineProcess;

import java.util.Map;

import static java.util.stream.Collectors.toMap;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * Helps to convert to/from DTOs related to machine.
 *
 * @author Valeriy Svydenko
 */
public final class ArtikDtoConverter {
    /**
     * Converts {@link Machine} to {@link MachineDto}.
     */
    public static MachineDto asDto(SshMachineInstance machine) {
        final MachineDto machineDto = newDto(MachineDto.class).withConfig(asDto(machine.getMachineConfig()))
                                                              .withId(machine.getId())
                                                              .withStatus(machine.getStatus())
                                                              .withOwner(machine.getOwner())
                                                              .withEnvName(machine.getEnvName())
                                                              .withWorkspaceId(machine.getWorkspaceId());
        if (machine.getRuntime() != null) {
            machineDto.withRuntime(asDto(machine.getRuntime()));
        }
        return machineDto;
    }

    /**
     * Converts {@link MachineConfig} to {@link MachineConfigDto}.
     */
    public static MachineConfigDto asDto(MachineConfig config) {
        return DtoConverter.asDto(config);
    }

    /**
     * Converts {@link MachineRuntimeInfo} to {@link MachineRuntimeInfoDto}.
     */
    private static MachineRuntimeInfoDto asDto(MachineRuntimeInfo runtime) {
        final Map<String, ServerDto> servers = runtime.getServers()
                                                      .entrySet()
                                                      .stream()
                                                      .collect(toMap(Map.Entry::getKey, entry -> asDto(entry.getValue())));

        return newDto(MachineRuntimeInfoDto.class).withEnvVariables(runtime.getEnvVariables())
                                                  .withProperties(runtime.getProperties())
                                                  .withServers(servers);
    }

    /**
     * Converts {@link Server} to {@link ServerDto}.
     */
    public static ServerDto asDto(Server server) {
        return newDto(ServerDto.class).withAddress(server.getAddress())
                                      .withRef(server.getRef())
                                      .withProtocol(server.getProtocol())
                                      .withUrl(server.getUrl())
                                      .withProperties(server.getProperties() != null ? DtoConverter.asDto(server.getProperties()) : null);
    }


    /**
     * Converts {@link MachineProcess} to {@link MachineProcessDto}.
     */
    public static MachineProcessDto asDto(SshMachineProcess machineProcess) {
        return newDto(MachineProcessDto.class).withPid(machineProcess.getPid())
                                              .withCommandLine(machineProcess.getCommandLine())
                                              .withAlive(machineProcess.isAlive())
                                              .withName(machineProcess.getName())
                                              .withAttributes(machineProcess.getAttributes())
                                              .withType(machineProcess.getType())
                                              .withOutputChannel(machineProcess.getOutputChannel())
                                              .withLinks(null);
    }
}
