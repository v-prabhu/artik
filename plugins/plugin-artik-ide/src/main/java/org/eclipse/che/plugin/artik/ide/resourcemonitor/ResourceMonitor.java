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
package org.eclipse.che.plugin.artik.ide.resourcemonitor;

import com.google.gwt.core.client.JsArrayMixed;
import com.google.gwt.user.client.Timer;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.MachineServiceClient;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStartedEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.extension.machine.client.machine.MachineStateEvent;
import org.eclipse.che.ide.extension.machine.client.processes.monitoring.MachineMonitors;

import java.util.ArrayList;
import java.util.List;

/**
 * Resources monitor asks Artik machines for CPU, memory and disk usages
 *  and displays them in the consoles tree.
 *
 * @author Vitaliy Guliy
 */
@Singleton
public class ResourceMonitor implements MachineStateEvent.Handler,
        WorkspaceStartedEvent.Handler, WorkspaceStoppedEvent.Handler {

    private final MachineServiceClient              machineServiceClient;
    private final AppContext                        appContext;
    private final MachineMonitors                   machineMonitors;
    private final Provider<ResourceMonitorService>  resourceMonitorProvider;

    private final List<Machine>                     artikMachines;

    @Inject
    public ResourceMonitor(MachineServiceClient machineServiceClient,
                           EventBus eventBus,
                           AppContext appContext,
                           MachineMonitors machineMonitors,
                           Provider<ResourceMonitorService> resourceMonitorProvider) {
        this.machineServiceClient = machineServiceClient;
        this.appContext = appContext;
        this.machineMonitors = machineMonitors;
        this.resourceMonitorProvider = resourceMonitorProvider;

        artikMachines = new ArrayList<>();

        eventBus.addHandler(MachineStateEvent.TYPE, this);
        eventBus.addHandler(WorkspaceStartedEvent.TYPE, this);
        eventBus.addHandler(WorkspaceStoppedEvent.TYPE, this);
    }

    /**
     * Loads the machine list.
     */
    private void loadMachines() {
        machineServiceClient.getMachines(appContext.getWorkspaceId()).then(new Operation<List<MachineDto>>() {
            @Override
            public void apply(List<MachineDto> machines) throws OperationException {
                for (MachineDto machine : machines) {
                    if ("artik".equals(machine.getConfig().getType())) {
                        artikMachines.add(machine);
                    }
                }

                UPDATE_TIMER.scheduleRepeating(3000);
            }
        });
    }

    /**
     * Timer to periodical ask the artik machines for cpu, memory and disk usages
     *  and to update the corresponding widget in the consoles tree.
     */
    private Timer UPDATE_TIMER = new Timer() {
        @Override
        public void run() {
            for (final Machine machine : artikMachines) {
                updateCPU(machine.getId())
                        .then(updateMemory(machine.getId()))
                        .then(updateStorage(machine.getId()));
            }
        }
    };

    /**
     * Fetches CPU utilization for machine with a given ID and updates the corresponding widget.
     *
     * @param machineID
     *          machine ID
     */
    protected Promise updateCPU(final String machineID) {
        return resourceMonitorProvider.get().getCpuUtilization(machineID).then(new Operation<String>() {
            @Override
            public void apply(String value) {
                int cpu = (int) Math.rint(Double.valueOf(value) * 100);

                machineMonitors.setCpuUsage(machineID, cpu);
            }
        });
    }

    /**
     * Fetches memory usage for machine with a given ID and updates the corresponding widget.
     *
     * @param machineID
     *          machine ID
     */
    private Promise updateMemory(final String machineID) {
        return Promises.all(new Promise[]{
                resourceMonitorProvider.get().getTotalMemory(machineID),
                resourceMonitorProvider.get().getUsedMemory(machineID)})
                .then(new Operation<JsArrayMixed>() {
                    @Override
                    public void apply(JsArrayMixed jsArrayMixed) {
                        final String totalMemory = jsArrayMixed.getString(0);
                        final String usedMemory = jsArrayMixed.getString(1);

                        machineMonitors.setMemoryUsage(machineID, Integer.parseInt(usedMemory), Integer.parseInt(totalMemory));
                    }
                });
    }

    /**
     * Fetches disk usage for machine with a given ID and updates the corresponding widget.
     *
     * @param machineID
     *          machine ID
     */
    protected Promise updateStorage(final String machineID) {
        return Promises.all(new Promise[]{resourceMonitorProvider.get().getTotalStorageSpace(machineID),
                resourceMonitorProvider.get().getUsedStorageSpace(machineID)})
                .then(new Operation<JsArrayMixed>() {
                    @Override
                    public void apply(JsArrayMixed jsArrayMixed) {
                        final String totalSpace = jsArrayMixed.getString(0);
                        final String usedSpace = jsArrayMixed.getString(1);

                        machineMonitors.setDiskUsage(machineID, Integer.parseInt(usedSpace), Integer.parseInt(totalSpace));
                    }
                });
    }

    @Override
    public void onMachineCreating(MachineStateEvent event) {
    }

    @Override
    public void onMachineRunning(MachineStateEvent event) {
        if ("artik".equals(event.getMachine().getConfig().getType())) {
            artikMachines.add(event.getMachine());
        }
    }

    @Override
    public void onMachineDestroyed(MachineStateEvent event) {
        for (Machine machine : artikMachines) {
            if (machine.getId().equals(event.getMachineId())) {
                artikMachines.remove(machine);
                return;
            }
        }

    }

    @Override
    public void onWorkspaceStarted(WorkspaceStartedEvent event) {
        loadMachines();
    }

    @Override
    public void onWorkspaceStopped(WorkspaceStoppedEvent event) {
        UPDATE_TIMER.cancel();
        artikMachines.clear();
    }

}
