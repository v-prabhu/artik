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
package org.eclipse.che.plugin.artik.ide.command.macro;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.ide.api.machine.events.MachineStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateHandler;
import org.eclipse.che.ide.api.macro.Macro;
import org.eclipse.che.ide.api.macro.MacroRegistry;
import org.eclipse.che.plugin.artik.ide.machine.DeviceServiceClient;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Registers/unregisters {@link ReplicationFolderMacro}s on connecting/disconnecting Artik devices.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class ReplicationFolderMacroRegistrar implements WsAgentStateHandler, MachineStateEvent.Handler {

    private final ReplicationFolderMacroFactory replicationFolderMacroFactory;
    private final MacroRegistry                 commandPropertyValueProviderRegistry;
    private final DeviceServiceClient           deviceServiceClient;

    private final Map<Machine, Macro> macrosByMachines;

    @Inject
    public ReplicationFolderMacroRegistrar(EventBus eventBus,
                                           ReplicationFolderMacroFactory replicationFolderMacroFactory,
                                           MacroRegistry commandPropertyValueProviderRegistry,
                                           DeviceServiceClient deviceServiceClient) {
        this.replicationFolderMacroFactory = replicationFolderMacroFactory;
        this.commandPropertyValueProviderRegistry = commandPropertyValueProviderRegistry;
        this.deviceServiceClient = deviceServiceClient;

        macrosByMachines = new HashMap<>();

        eventBus.addHandler(WsAgentStateEvent.TYPE, this);
        eventBus.addHandler(MachineStateEvent.TYPE, this);
    }

    @Override
    public void onWsAgentStarted(WsAgentStateEvent event) {
        deviceServiceClient.getDevices().then(new Operation<List<MachineDto>>() {
            @Override
            public void apply(List<MachineDto> arg) throws OperationException {
                for (MachineDto machine : arg) {
                    if (!isAlreadyRegistered(machine)) {
                        registerMacroForMachine(machine);
                    }
                }
            }
        });
    }

    private boolean isAlreadyRegistered(Machine machineDto) {
        for (Machine machine : macrosByMachines.keySet()) {
            if (machine.getId().equals(machineDto.getId())) {
                return true;
            }
        }

        return false;
    }

    private void registerMacroForMachine(Machine machine) {
        Set<Macro> valueProviders = new HashSet<>();

        Macro macro = replicationFolderMacroFactory.create(machine);
        valueProviders.add(macro);

        commandPropertyValueProviderRegistry.register(valueProviders);

        macrosByMachines.put(machine, macro);
    }

    @Override
    public void onWsAgentStopped(WsAgentStateEvent event) {
    }

    @Override
    public void onMachineCreating(MachineStateEvent event) {
    }

    @Override
    public void onMachineRunning(MachineStateEvent event) {
        Machine machine = event.getMachine();
        if (!isAlreadyRegistered(machine)) {
            registerMacroForMachine(machine);
        }
    }

    @Override
    public void onMachineDestroyed(MachineStateEvent event) {
        Macro macro = macrosByMachines.remove(event.getMachine());
        if (macro != null) {
            commandPropertyValueProviderRegistry.unregister(macro);
        }
    }
}
