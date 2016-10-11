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
package org.eclipse.che.plugin.artik.ide.debug;

import com.google.gwt.core.client.Callback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.component.Component;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.machine.MachineServiceClient;
import org.eclipse.che.ide.extension.machine.client.machine.MachineStateEvent;

import java.util.List;

/**
 * Listens for the events of creating/destroying machines and crates/removes
 * an appropriate actions for connecting to the debugger on the related machine.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class DebugBinaryActionsManager implements MachineStateEvent.Handler, Component {

    private final ActionManager            actionManager;
    private final DebugBinaryActionFactory debugBinaryActionFactory;
    private final MachineServiceClient     machineServiceClient;
    private final AppContext               appContext;

    private DefaultActionGroup debugGroup;

    @Inject
    public DebugBinaryActionsManager(EventBus eventBus,
                                     ActionManager actionManager,
                                     DebugBinaryActionFactory debugBinaryActionFactory,
                                     MachineServiceClient machineServiceClient,
                                     AppContext appContext) {
        this.actionManager = actionManager;
        this.debugBinaryActionFactory = debugBinaryActionFactory;
        this.machineServiceClient = machineServiceClient;
        this.appContext = appContext;

        eventBus.addHandler(MachineStateEvent.TYPE, this);
    }

    @Override
    public void start(Callback<Component, Exception> callback) {
        callback.onSuccess(DebugBinaryActionsManager.this);

        debugGroup = new DefaultActionGroup("Debug", true, actionManager);
        actionManager.registerAction("debugBinaryGroup", debugGroup);

        DefaultActionGroup resourceOperationGroup = (DefaultActionGroup)actionManager.getAction("resourceOperation");
        resourceOperationGroup.addSeparator();
        resourceOperationGroup.add(debugGroup);

        machineServiceClient.getMachines(appContext.getWorkspaceId()).then(new Operation<List<MachineDto>>() {
            @Override
            public void apply(List<MachineDto> arg) throws OperationException {
                for (MachineDto machineDto : arg) {
                    if (isArtikOrSsh(machineDto)) {
                        addDebugAction(machineDto);
                    }
                }
            }
        });
    }

    @Override
    public void onMachineCreating(MachineStateEvent event) {
    }

    @Override
    public void onMachineRunning(MachineStateEvent event) {
        addDebugAction(event.getMachine());
    }

    private void addDebugAction(Machine machine) {
        if (!isArtikOrSsh(machine)) {
            return;
        }

        DebugBinaryAction pushToDeviceAction = debugBinaryActionFactory.create(machine);
        actionManager.registerAction(machine.getId(), pushToDeviceAction);

        debugGroup.add(pushToDeviceAction, Constraints.FIRST);
    }

    @Override
    public void onMachineDestroyed(MachineStateEvent event) {
        removeDebugAction(event.getMachine());
    }

    private void removeDebugAction(Machine machine) {
        if (!isArtikOrSsh(machine)) {
            return;
        }

        Action action = actionManager.getAction(machine.getId());
        actionManager.unregisterAction(machine.getId());

        debugGroup.remove(action);
    }

    private boolean isArtikOrSsh(Machine machine) {
        String type = machine.getConfig().getType();
        return "ssh".equals(type) || "artik".equals(type);
    }
}
