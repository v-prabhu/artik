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

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.dialogs.CancelCallback;
import org.eclipse.che.ide.api.dialogs.ConfirmCallback;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.machine.events.MachineStateEvent;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.workspace.event.MachineStatusChangedEvent;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.plugin.artik.ide.ArtikLocalizationConstant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;

/**
 * @author Dmitry Kuleshov
 */
@Singleton
public class SoftwareManager implements MachineStateEvent.Handler {

    private final SoftwareAnalyzer          softwareAnalyzer;
    private final SoftwareInstaller         softwareInstaller;
    private final ArtikLocalizationConstant artikLocalizationConstant;
    private final NotificationManager       notificationManager;
    private final EventBus                  eventBus;
    private final DialogFactory             dialogFactory;

    private final Map<String, Machine> sshMachines = new HashMap<>();

    @Inject
    public SoftwareManager(SoftwareAnalyzer softwareAnalyzer,
                           SoftwareInstaller softwareInstaller,
                           ArtikLocalizationConstant artikLocalizationConstant,
                           NotificationManager notificationManager,
                           EventBus eventBus,
                           DialogFactory dialogFactory) {

        this.softwareAnalyzer = softwareAnalyzer;
        this.softwareInstaller = softwareInstaller;
        this.artikLocalizationConstant = artikLocalizationConstant;
        this.notificationManager = notificationManager;
        this.eventBus = eventBus;
        this.dialogFactory = dialogFactory;

        eventBus.addHandler(MachineStateEvent.TYPE, this);
    }

    public void checkAndInstall(final String machineName) {
        final Machine machine = sshMachines.get(machineName);
        final String machineId = machine.getId();

        softwareAnalyzer.getMissingSoft(machineId)
                        .then(new InstallationDialogue(machine, eventBus, artikLocalizationConstant))
                        .catchError(new ErrorHandling());
    }

    @Override
    public void onMachineCreating(MachineStateEvent machineStateEvent) {

    }

    @Override
    public void onMachineRunning(MachineStateEvent event) {
        final Machine machine = event.getMachine();
        final MachineConfig config = machine.getConfig();
        final String name = config.getName();
        sshMachines.put(name, machine);
    }

    @Override
    public void onMachineDestroyed(MachineStateEvent event) {
        final Machine machine = event.getMachine();
        final MachineConfig config = machine.getConfig();
        final String name = config.getName();
        sshMachines.remove(name);
    }

    private static class ErrorHandling implements Operation<PromiseError> {
        @Override
        public void apply(PromiseError arg) throws OperationException {
            Log.error(getClass(), arg.getMessage());
        }
    }

    private class InstallationDialogue implements Operation<Set<Software>>, MachineStatusChangedEvent.Handler {
        private final ArtikLocalizationConstant artikLocalizationConstant;
        private final String                    machineName;
        private final String                    title;
        private final String                    content;
        private final String                    ok;
        private final String                    cancel;
        private final CancelCallback            cancelCallback;
        private final Machine                   machine;

        private List<StatusNotification> notifications;

        private InstallationDialogue(Machine machine, EventBus eventBus, ArtikLocalizationConstant artikLocalizationConstant) {
            this.machine = machine;
            this.machineName = machine.getConfig().getName();
            this.artikLocalizationConstant = artikLocalizationConstant;

            this.title = "Development Mode";
            this.content = "Rsync and/or gdbserver not found in %PATH on <b>" + machineName + "</b>. This will <br>" +
                           "result in project replication and debugging failures. Do you want to install <br>" +
                           " this software by turning on development profile?";

            this.ok = "Ok";
            this.cancel = "Cancel";
            this.cancelCallback = null;

            notifications = new ArrayList<>();

            eventBus.addHandler(MachineStatusChangedEvent.TYPE, this);
        }

        @Override
        public void apply(Set<Software> arg) throws OperationException {
            if (arg.isEmpty()) {
                return;
            }

            final ConfirmCallback confirmCallback = new Installation(arg);

            dialogFactory.createConfirmDialog(title, content, ok, cancel, confirmCallback, cancelCallback).show();
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

        private class Installation implements ConfirmCallback {
            private final Set<Software> arg;

            private Installation(Set<Software> arg) {
                this.arg = arg;
            }

            @Override
            public void accepted() {
                for (Software software : arg) {
                    final String message = "Installing " + software.name + " for development mode to " + machineName;
                    final StatusNotification notification = notificationManager.notify(message, PROGRESS, FLOAT_MODE);
                    notifications.add(notification);

                    softwareInstaller.install(software, machine).then(arg -> {
                        final String message1 = "Software installed";
                        notification.setTitle(message1);
                        notification.setStatus(SUCCESS);
                    }).catchError(new Operation<PromiseError>() {
                        @Override
                        public void apply(PromiseError arg) throws OperationException {
                            Log.error(getClass(), arg.getMessage());
                        }
                    });
                }
            }
        }
    }
}
