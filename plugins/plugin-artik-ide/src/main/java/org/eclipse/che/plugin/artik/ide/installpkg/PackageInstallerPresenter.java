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
package org.eclipse.che.plugin.artik.ide.installpkg;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.ide.api.machine.ExecAgentCommandManager;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.workspace.event.MachineStatusChangedEvent;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.extension.machine.client.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.plugin.artik.ide.ArtikLocalizationConstant;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;

/**
 * The purpose of this class is install package to the target machine
 *
 * @author Lijuan Xue
 */
@Singleton
public class PackageInstallerPresenter implements PackageInstallerView.ActionDelegate, MachineStatusChangedEvent.Handler {
    private final PackageInstallerView      view;
    private final ExecAgentCommandManager   commandManager;
    private final NotificationManager       notificationManager;
    private final ProcessesPanelPresenter   processesPanelPresenter;
    private final ArtikLocalizationConstant locale;
    private final DtoFactory                dtoFactory;

    private Machine            machine;
    private StatusNotification progressNotification;

    @Inject
    public PackageInstallerPresenter(PackageInstallerView view,
                                     ExecAgentCommandManager commandManager,
                                     NotificationManager notificationManager,
                                     DtoFactory dtoFactory,
                                     ArtikLocalizationConstant locale,
                                     EventBus eventBus,
                                     ProcessesPanelPresenter processesPanelPresenter) {
        this.view = view;
        this.commandManager = commandManager;
        this.notificationManager = notificationManager;
        this.dtoFactory = dtoFactory;
        this.locale = locale;
        this.view.setDelegate(this);
        this.processesPanelPresenter = processesPanelPresenter;

        eventBus.addHandler(MachineStatusChangedEvent.TYPE, this);
    }

    /**
     * Show dialog.
     */
    public void showDialog(Machine machine) {
        this.machine = machine;
        view.showDialog();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCancelClicked() {
        view.closeDialog();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onInstallButtonClicked() {
        final String packageName = view.getPackageName();
        final String deviceName = machine.getConfig().getName();

        if (!isNullOrEmpty(packageName)) {
            progressNotification = notificationManager.notify("Installing package: " +
                                                              packageName +
                                                              " on the target machine: " +
                                                              machine.getConfig().getName() +
                                                              ".", StatusNotification.Status.PROGRESS, FLOAT_MODE);

            String commandLine = "dnf install " + packageName + " -y";

            final Command command = dtoFactory.createDto(CommandDto.class)
                                              .withName("name")
                                              .withType("custom")
                                              .withCommandLine(commandLine);

            commandManager.startProcess(machine.getId(), command)
                          .thenIfProcessStdOutEvent(
                                  processStdOut -> processesPanelPresenter.printMachineOutput(deviceName, processStdOut.getText()))
                          .thenIfProcessStdErrEvent(
                                  processSdtErr -> processesPanelPresenter.printMachineOutput(deviceName, processSdtErr.getText()))
                          .thenIfProcessDiedEvent(processDied -> {
                              String message = "Installing process completed.";
                              progressNotification.setTitle(message);
                              progressNotification.setStatus(SUCCESS);
                          });
            view.closeDialog();
        }
    }

    @Override
    public void onMachineStatusChanged(MachineStatusChangedEvent machineStatusChangedEvent) {
        switch (machineStatusChangedEvent.getEventType()) {
            case DESTROYED:
                if (PROGRESS.equals(progressNotification.getStatus())) {
                    progressNotification.setStatus(FAIL);
                    progressNotification.setContent(locale.operationAborted(machineStatusChangedEvent.getMachineName()));
                }
                break;
            case RUNNING:
                break;
            case ERROR:
                break;
        }
    }
}
