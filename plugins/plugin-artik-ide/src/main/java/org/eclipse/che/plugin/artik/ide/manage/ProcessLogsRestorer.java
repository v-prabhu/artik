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
package org.eclipse.che.plugin.artik.ide.manage;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.machine.shared.dto.execagent.GetProcessesResponseDto;
import org.eclipse.che.ide.api.machine.ExecAgentCommandManager;
import org.eclipse.che.ide.extension.machine.client.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.ide.util.loging.Log;

import static java.util.Arrays.asList;

/**
 * Mechanism for restoring processes' logs.
 */
@Singleton
public class ProcessLogsRestorer {
    private final ExecAgentCommandManager execAgentCommandManager;
    private final ProcessesPanelPresenter processesPanelPresenter;

    @Inject
    public ProcessLogsRestorer(ExecAgentCommandManager execAgentCommandManager,
                               ProcessesPanelPresenter processesPanelPresenter) {
        this.execAgentCommandManager = execAgentCommandManager;
        this.processesPanelPresenter = processesPanelPresenter;
    }

    /**
     * Restores logs from all alive processes
     *
     * @param device
     *         device where processes are running
     */
    public void restoreLogs(MachineDto device) {
        final String deviceId = device.getId();
        execAgentCommandManager.getProcesses(deviceId, false).then(getProcessesResponseDtos -> {
            for (GetProcessesResponseDto machineProcess : getProcessesResponseDtos) {
                if (!machineProcess.getName().endsWith("_installation")) {
                    break;
                }
                final int processPid = machineProcess.getPid();
                final String stderr = "stderr";
                final String stdout = "stdout";
                final String processStatus = "processStatus";
                final String deviceName = device.getConfig().getName();
                execAgentCommandManager.subscribe(deviceId, processPid, asList(stderr, stdout, processStatus), null)
                                       .then(processSubscribeResponseDto -> Log.info(getClass(), processSubscribeResponseDto.getText()))
                                       .thenIfProcessStdOutEvent(output -> printInfoMessage(deviceName, output.getText()))
                                       .thenIfProcessStdErrEvent(output -> printErrorMessage(deviceName, output.getText()));
            }
        }).catchError(promiseError -> {
            Log.error(getClass(), promiseError.getMessage());
        });
    }

    private void printInfoMessage(String deviceName, String text) {
        processesPanelPresenter.printMachineOutput(deviceName, text);
    }

    private void printErrorMessage(String deviceName, String text) {
        processesPanelPresenter.printMachineOutput(deviceName, text, "red");
    }
}
