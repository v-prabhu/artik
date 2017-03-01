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
package org.eclipse.che.plugin.artik.ide.profile;

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
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.plugin.artik.ide.ArtikLocalizationConstant;
import org.eclipse.che.plugin.artik.ide.ArtikResources;

import java.util.ArrayList;
import java.util.List;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;
import static org.eclipse.che.plugin.artik.ide.profile.Software.GDB_SERVER;
import static org.eclipse.che.plugin.artik.ide.profile.Software.RSYNC;

/**
 * @author Dmitry Kuleshov
 */
@Singleton
public class SoftwareInstaller implements MachineStatusChangedEvent.Handler {
    private final DtoFactory                dtoFactory;
    private final NotificationManager       notificationManager;
    private final ArtikLocalizationConstant artikLocalizationConstant;
    private final ProcessesPanelPresenter   processesPanelPresenter;
    private final ExecAgentCommandManager   execAgentCommandManager;

    private List<StatusNotification> notifications;

    @Inject
    public SoftwareInstaller(DtoFactory dtoFactory,
                             NotificationManager notificationManager,
                             ArtikLocalizationConstant artikLocalizationConstant,
                             EventBus eventBus,
                             ProcessesPanelPresenter processesPanelPresenter,
                             ArtikResources artikResources,
                             ExecAgentCommandManager execAgentCommandManager) {
        this.dtoFactory = dtoFactory;
        this.notificationManager = notificationManager;
        this.artikLocalizationConstant = artikLocalizationConstant;
        this.processesPanelPresenter = processesPanelPresenter;
        this.execAgentCommandManager = execAgentCommandManager;


        GDB_SERVER.setInstallationCommand(artikResources.gdbServerInstallationCommand().getText());
        RSYNC.setInstallationCommand(artikResources.rsyncInstallationCommand().getText());

        notifications = new ArrayList<>();

        eventBus.addHandler(MachineStatusChangedEvent.TYPE, this);
    }

    public void install(final Software software, final Machine device) {
        Log.debug(getClass(), "Installing missing software: " + software);

        final String message = "Installing " + software.name + " for development mode to " + device.getConfig().getName();
        final StatusNotification notification = notificationManager.notify(message, PROGRESS, FLOAT_MODE);
        notifications.add(notification);

        final String commandLine = software.getInstallationCommand();
        final String commandName = software.name() + "_installation";
        final String commandType = "custom";

        final Command command = dtoFactory.createDto(CommandDto.class)
                                          .withName(commandName)
                                          .withType(commandType)
                                          .withCommandLine(commandLine);

        Log.debug(getClass(), "Installation command: " + command);

        execAgentCommandManager.startProcess(device.getId(), command).thenIfProcessStartedEvent(processStartedEventDto -> {

        }).thenIfProcessDiedEvent(processDiedEventDto -> {
            final String softwareInstalledMessage = "Software installed";
            notification.setTitle(softwareInstalledMessage);
            notification.setStatus(SUCCESS);

            processesPanelPresenter.printMachineOutput(device.getConfig().getName(), "\n");

            Log.debug(getClass(), message);
        }).thenIfProcessStdOutEvent(processStdOutEventDto -> {
            processesPanelPresenter.printMachineOutput(device.getConfig().getName(), processStdOutEventDto.getText());

            Log.debug(getClass(), message);
        }).thenIfProcessStdErrEvent(processStdErrEventDto -> {
            processesPanelPresenter.printMachineOutput(device.getConfig().getName(), processStdErrEventDto.getText(), "red");

            Log.debug(getClass(), message);
        });

    }

    @Override
    public void onMachineStatusChanged(MachineStatusChangedEvent machineStatusChangedEvent) {
        switch (machineStatusChangedEvent.getEventType()) {
            case DESTROYED:
                for (StatusNotification notification : notifications) {
                    if (PROGRESS.equals(notification.getStatus())) {
                        notification.setStatus(FAIL);
                        notification.setContent(artikLocalizationConstant.operationAborted(machineStatusChangedEvent.getMachineName()));
                    }
                }
                break;
            case RUNNING:
                break;
            case ERROR:
                break;
        }
    }
}
