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
package org.eclipse.che.plugin.artik.ide.resourcemonitor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.machine.shared.Constants;
import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.machine.shared.dto.execagent.GetProcessesResponseDto;
import org.eclipse.che.ide.api.machine.ExecAgentCommandManager;
import org.eclipse.che.ide.api.machine.MachineEntity;
import org.eclipse.che.ide.api.machine.events.MachineStateEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStartedEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.extension.machine.client.processes.monitoring.MachineMonitors;
import org.eclipse.che.plugin.artik.ide.ArtikResources;
import org.eclipse.che.plugin.artik.ide.machine.DeviceServiceClient;

import java.util.HashMap;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * Resources monitor asks Artik machines for CPU, memory and disk usages
 * and displays them in the consoles tree.
 *
 * @author Vitaliy Guliy
 */
@Singleton
public class ResourceMonitor implements MachineStateEvent.Handler,
                                        WorkspaceStartedEvent.Handler,
                                        WorkspaceStoppedEvent.Handler {

    private final DeviceServiceClient     deviceServiceClient;
    private final ExecAgentCommandManager execAgentCommandManager;
    private final MachineMonitors         machineMonitors;
    private final ArtikResources          resources;
    private final DtoFactory              dtoFactory;

    private final HashMap<String, MonitorAgent> monitorAgents;

    @Inject
    public ResourceMonitor(DeviceServiceClient deviceServiceClient,
                           EventBus eventBus,
                           ExecAgentCommandManager execAgentCommandManager,
                           MachineMonitors machineMonitors,
                           ArtikResources resources,
                           DtoFactory dtoFactory) {
        this.deviceServiceClient = deviceServiceClient;
        this.execAgentCommandManager = execAgentCommandManager;
        this.machineMonitors = machineMonitors;
        this.resources = resources;
        this.dtoFactory = dtoFactory;

        monitorAgents = new HashMap<>();

        eventBus.addHandler(MachineStateEvent.TYPE, this);
        eventBus.addHandler(WorkspaceStartedEvent.TYPE, this);
        eventBus.addHandler(WorkspaceStoppedEvent.TYPE, this);
    }

    @Override
    public void onMachineCreating(MachineStateEvent event) {
    }

    @Override
    public void onMachineRunning(MachineStateEvent event) {
        final MachineEntity machine = event.getMachine();
        if ("artik".equals(machine.getConfig().getType())) {
            monitorAgents.put(machine.getId(), new MonitorAgent(machine));
        }
    }

    @Override
    public void onMachineDestroyed(MachineStateEvent event) {
        MonitorAgent monitorAgent = monitorAgents.remove(event.getMachineId());
        if (monitorAgent != null) {
            monitorAgent.stop();
        }
    }

    @Override
    public void onWorkspaceStarted(WorkspaceStartedEvent event) {
        deviceServiceClient.getDevices().then(devices -> {
            for (MachineDto device : devices) {
                monitorAgents.put(device.getId(), new MonitorAgent(device));
            }
        });
    }

    @Override
    public void onWorkspaceStopped(WorkspaceStoppedEvent event) {
        for (MonitorAgent monitorAgent : monitorAgents.values()) {
            monitorAgent.stop();
        }

        monitorAgents.clear();
    }

    public void addMonitor(Machine device) {
        monitorAgents.put(device.getId(), new MonitorAgent(device));
    }

    /**
     * Monitor agent running a special command to get the statistic,
     * listening the command output and updating the monitor widgets.
     */
    private class MonitorAgent {
        private final Machine device;

        private int processPid;

        public MonitorAgent(Machine machine) {
            this.device = machine;

            checkMonitorProcess();
        }

        private void checkMonitorProcess() {
            execAgentCommandManager.getProcesses(device.getId(), false).then(processes -> {
                for (GetProcessesResponseDto process : processes) {
                    if (process.getCommandLine() != null && !process.getCommandLine().isEmpty()
                        && process.getCommandLine().startsWith("#hidden monitor process")) {
                        connectToProcess(process.getPid());
                        return;
                    }
                }
                runMonitorProcess();
            }).catchError(getProcessError -> {
                runMonitorProcess();
            });
        }

        private void runMonitorProcess() {
            Command command = dtoFactory.createDto(CommandDto.class)
                                        .withName("name")
                                        .withType("custom")
                                        .withCommandLine(resources.getMonitorAllCommand().getText());

            execAgentCommandManager.startProcess(device.getId(), command)
                                   .thenIfProcessStartedEvent(arg -> processPid = arg.getPid())
                                   .thenIfProcessStdOutEvent(arg -> updateMonitorData(arg.getText()));


        }

        private void connectToProcess(int pid) {
            processPid = pid;

            String stderr = "stderr";
            String stdout = "stdout";
            String processStatus = "process_status";

            execAgentCommandManager.subscribe(device.getId(), pid, asList(stderr, stdout, processStatus), null)
                                   .thenIfProcessStdOutEvent(arg -> updateMonitorData(arg.getText()));

        }

        private void updateMonitorData(String message) {
            // CPU_USED MEM_USED MEM_TOTAL DISK_USED DISK_TOTAL
            String[] parts = message.split(" ");

            // CPU usage
            int cpu = (int)Math.rint(Double.valueOf(parts[0]) * 100);
            machineMonitors.setCpuUsage(device.getId(), cpu);

            // Memory usage
            machineMonitors
                    .setMemoryUsage(device.getId(), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));

            // Disk usage
            machineMonitors.setDiskUsage(device.getId(), Integer.parseInt(parts[3]), Integer.parseInt(parts[4]));
        }

        public void stop() {
            if (processPid > -1) {
                execAgentCommandManager.killProcess(device.getId(), processPid);
            }
        }
    }

    private String getExecAgentUrl(List<Link> machineLinks) {
        for (Link link : machineLinks) {
            if (Constants.EXEC_AGENT_REFERENCE.equals(link.getRel())) {
                return link.getHref();
            }
        }
        //should not be
        return "";
    }

}
