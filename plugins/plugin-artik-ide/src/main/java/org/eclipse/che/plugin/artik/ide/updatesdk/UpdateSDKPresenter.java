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
package org.eclipse.che.plugin.artik.ide.updatesdk;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.dialogs.ConfirmCallback;
import org.eclipse.che.ide.api.dialogs.ConfirmDialog;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.workspace.WorkspaceServiceClient;
import org.eclipse.che.ide.api.workspace.event.MachineStatusChangedEvent;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;
import org.eclipse.che.ide.ui.loaders.request.MessageLoader;
import org.eclipse.che.plugin.artik.ide.ArtikLocalizationConstant;
import org.eclipse.che.plugin.artik.ide.machine.DeviceServiceClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.eclipse.che.api.core.model.machine.MachineStatus.RUNNING;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;

/**
 * Presenter for updating Artik SDK in workspace or in Artik devices.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class UpdateSDKPresenter implements UpdateSDKView.ActionDelegate, MachineStatusChangedEvent.Handler {

    private final UpdateSDKView              view;
    private final AppContext                 appContext;
    private final DeviceServiceClient        deviceServiceClient;
    private final NotificationManager        notificationManager;
    private final Provider<SDKInstaller>     sdkUpdaterProvider;
    private final ArtikLocalizationConstant  localizationConstants;
    private final DialogFactory              dialogFactory;
    private final WorkspaceServiceClient     workspaceServiceClient;
    private final MessageLoader              loader;
    private final ArrayList<TargetForUpdate> targetsList;

    private List<StatusNotification> notifications;

    @Inject
    public UpdateSDKPresenter(UpdateSDKView view,
                              AppContext appContext,
                              EventBus eventBus,
                              DeviceServiceClient deviceServiceClient,
                              NotificationManager notificationManager,
                              Provider<SDKInstaller> sdkUpdaterProvider,
                              ArtikLocalizationConstant localizationConstants,
                              DialogFactory dialogFactory,
                              WorkspaceServiceClient workspaceServiceClient,
                              LoaderFactory loaderFactory) {
        this.view = view;
        this.appContext = appContext;
        this.deviceServiceClient = deviceServiceClient;
        this.notificationManager = notificationManager;
        this.sdkUpdaterProvider = sdkUpdaterProvider;
        this.localizationConstants = localizationConstants;
        this.dialogFactory = dialogFactory;
        this.workspaceServiceClient = workspaceServiceClient;

        view.setDelegate(this);
        loader = loaderFactory.newLoader(localizationConstants.updateSDKViewLoaderMessage());

        targetsList = new ArrayList<>();
        notifications = new ArrayList<>();

        eventBus.addHandler(MachineStatusChangedEvent.TYPE, this);
    }

    /** Show the dialog. */
    public void show() {
        targetsList.clear();

        view.show();
        view.setTargets(targetsList);
        view.setAvailableVersions(Collections.emptyList());
        view.setEnabledInstallButton(false);

        loader.show();

        sdkUpdaterProvider.get().getAvailableSDKVersions().then(versions -> {
            view.setAvailableVersions(versions);
            fillTargetsForUpdate();
        }).catchError(arg -> {
            loader.hide();

            notificationManager.notify(localizationConstants.updateSDKNotificationGetVersionsTitle(),
                                       localizationConstants.updateSDKNotificationGetVersionsFailMessage() + arg.getMessage(),
                                       FAIL,
                                       FLOAT_MODE);
        });
    }

    private void fillTargetsForUpdate() {
        loader.show();
        deviceServiceClient.getDevices().then((Function<List<MachineDto>, List<TargetForUpdate>>)machines -> {
            List<TargetForUpdate> list = new ArrayList<>();
            for (Machine machine : machines) {
                if (RUNNING.equals(machine.getStatus())) {
                    list.add(new TargetForUpdate(machine.getId(), machine.getConfig().getName()));
                }
            }
            list.add(new TargetForUpdate(appContext.getDevMachine().getId(), "Workspace"));
            return list;
        }).then(targets -> {
            final Promise<String>[] promises = (Promise<String>[])new Promise[targets.size()];

            for (int i = 0; i < targets.size(); i++) {
                final TargetForUpdate target = targets.get(i);
                promises[i] = sdkUpdaterProvider.get()
                                                .getInstalledSDKVersion(target)
                                                .then(arg -> {
                                                    target.setCurrentVersion(arg);
                                                    targetsList.add(target);
                                                    view.setTargets(targetsList);
                                                    view.setEnabledInstallButton(!targetsList.isEmpty());
                                                }).catchError(arg -> {
                            target.setCurrentVersion("n/a");
                            targetsList.add(target);

                            view.setTargets(targetsList);

                            notificationManager.notify(target.getName(),
                                                       localizationConstants.updateSDKNotificationGetInstalledVersionFailMessage() +
                                                       arg.getMessage(),
                                                       FAIL,
                                                       FLOAT_MODE);
                        });
            }

            Promises.all(promises).then(arg -> {
                loader.hide();
            }).catchError(arg -> {
                loader.hide();
            });
        });
    }

    @Override
    public void onInstallClicked() {
        Promise<String>[] promises = (Promise<String>[])new Promise[targetsList.size()];
        for (TargetForUpdate target : targetsList) {
            StatusNotification notification = notificationManager.notify(target.getName(),
                                                                         localizationConstants.updateSDKNotificationUpdatingMessage(),
                                                                         PROGRESS,
                                                                         FLOAT_MODE);
            notifications.add(notification);
            promises[targetsList.indexOf(target)] = update(target, notification);
        }

        view.close();

        Promises.all(promises).then(jsArrayMixed -> {
            ConfirmCallback confirmCallback = () -> {
                final String workspaceId = appContext.getWorkspaceId();
                workspaceServiceClient.stop(workspaceId).then(aVoid -> {
                    checkWsStatus(workspaceId);
                });
            };

            ConfirmDialog dialog = dialogFactory.createConfirmDialog(localizationConstants.updateSDKViewTitle(),
                                                                     localizationConstants.restartViewText(),
                                                                     localizationConstants.restartViewButtonRestartNow(),
                                                                     localizationConstants.restartViewButtonRestartLater(),
                                                                     confirmCallback,
                                                                     null);
            dialog.show();
        });
    }

    private void checkWsStatus(final String workspaceId) {
        workspaceServiceClient.getWorkspace(workspaceId).then(workspaceDto -> {
            if (WorkspaceStatus.STOPPED.equals(workspaceDto.getStatus())) {
                new Timer() {
                    @Override
                    public void run() {
                        Window.Location.reload();
                    }
                }.schedule(1000);
            } else {
                new Timer() {
                    @Override
                    public void run() {
                        checkWsStatus(workspaceId);
                    }
                }.schedule(1000);
            }
        });
    }

    private Promise<String> update(TargetForUpdate target, StatusNotification notification) {
        return sdkUpdaterProvider.get().installSDK(target, view.getSelectedVersion()).then(message -> {
            notification.setStatus(StatusNotification.Status.SUCCESS);
            notification.setContent(message);
        }).catchError(arg -> {
            notification.setStatus(FAIL);
            notification.setContent(localizationConstants.updateSDKNotificationUpdateFailMessage() + arg.getMessage());
        });
    }

    @Override
    public void onCancelClicked() {
        view.close();
    }

    @Override
    public void onMachineStatusChanged(MachineStatusChangedEvent machineStatusChangedEvent) {
        switch (machineStatusChangedEvent.getEventType()) {
            case DESTROYED:
                for (StatusNotification notification : notifications) {
                    if (PROGRESS.equals(notification.getStatus())) {
                        notification.setStatus(FAIL);
                        notification.setContent(localizationConstants.updateSDKNotificationUpdateFailMessage() + " device " +
                                                machineStatusChangedEvent.getMachineName() + " disconnected");
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
