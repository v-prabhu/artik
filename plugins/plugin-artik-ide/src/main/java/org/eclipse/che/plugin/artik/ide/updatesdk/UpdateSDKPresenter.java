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
package org.eclipse.che.plugin.artik.ide.updatesdk;

import com.google.gwt.core.client.JsArrayMixed;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.dialogs.ConfirmCallback;
import org.eclipse.che.ide.api.dialogs.ConfirmDialog;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.machine.MachineServiceClient;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.workspace.WorkspaceServiceClient;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;
import org.eclipse.che.ide.ui.loaders.request.MessageLoader;
import org.eclipse.che.plugin.artik.ide.ArtikLocalizationConstant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;

/**
 * Presenter for updating Artik SDK in workspace or in Artik devices.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class UpdateSDKPresenter implements UpdateSDKView.ActionDelegate {

    private final UpdateSDKView              view;
    private final AppContext                 appContext;
    private final NotificationManager        notificationManager;
    private final MachineServiceClient       machineServiceClient;
    private final Provider<SDKInstaller>     sdkUpdaterProvider;
    private final ArtikLocalizationConstant  localizationConstants;
    private final DialogFactory              dialogFactory;
    private final WorkspaceServiceClient     workspaceServiceClient;
    private final MessageLoader              loader;
    private final ArrayList<TargetForUpdate> targetsList;

    @Inject
    public UpdateSDKPresenter(UpdateSDKView view,
                              AppContext appContext,
                              NotificationManager notificationManager,
                              MachineServiceClient machineServiceClient,
                              Provider<SDKInstaller> sdkUpdaterProvider,
                              ArtikLocalizationConstant localizationConstants,
                              DialogFactory dialogFactory,
                              WorkspaceServiceClient workspaceServiceClient,
                              LoaderFactory loaderFactory) {
        this.view = view;
        this.appContext = appContext;
        this.notificationManager = notificationManager;
        this.machineServiceClient = machineServiceClient;
        this.sdkUpdaterProvider = sdkUpdaterProvider;
        this.localizationConstants = localizationConstants;
        this.dialogFactory = dialogFactory;
        this.workspaceServiceClient = workspaceServiceClient;

        view.setDelegate(this);
        loader = loaderFactory.newLoader(localizationConstants.updateSDKViewLoaderMessage());

        targetsList = new ArrayList<>();
    }

    /** Show the dialog. */
    public void show() {
        targetsList.clear();

        view.show();
        view.setTargets(targetsList);
        view.setAvailableVersions(Collections.<String>emptyList());
        view.setEnabledInstallButton(false);

        loader.show();

        sdkUpdaterProvider.get().getAvailableSDKVersions().then(new Operation<List<String>>() {
            @Override
            public void apply(List<String> versions) throws OperationException {
                view.setAvailableVersions(versions);
                fillTargetsForUpdate();
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                loader.hide();

                notificationManager.notify(localizationConstants.updateSDKNotificationGetVersionsTitle(),
                                           localizationConstants.updateSDKNotificationGetVersionsFailMessage() + arg.getMessage(),
                                           FAIL,
                                           FLOAT_MODE);
            }
        });
    }

    private void fillTargetsForUpdate() {
        loader.show();
        machineServiceClient.getMachines(appContext.getWorkspaceId()).then(new Function<List<MachineDto>, List<TargetForUpdate>>() {
            @Override
            public List<TargetForUpdate> apply(List<MachineDto> machines) throws FunctionException {
                List<TargetForUpdate> list = new ArrayList<>();
                for (MachineDto machineDto : machines) {
                    final MachineConfigDto machineConfig = machineDto.getConfig();
                    if ("artik".equals(machineConfig.getType()) || machineConfig.isDev()) {
                        final String name = machineConfig.isDev() ? "Workspace" : machineConfig.getName();
                        list.add(new TargetForUpdate(machineDto.getId(), name));
                    }
                }
                return list;
            }
        }).then(new Operation<List<TargetForUpdate>>() {
            @Override
            public void apply(List<TargetForUpdate> targets) throws OperationException {
                final Promise<String>[] promises = (Promise<String>[])new Promise[targets.size()];

                for (int i = 0; i < targets.size(); i++) {
                    final TargetForUpdate target = targets.get(i);
                    promises[i] = sdkUpdaterProvider.get()
                                                    .getInstalledSDKVersion(target.getId())
                                                    .then(new Operation<String>() {
                                                        @Override
                                                        public void apply(String arg) throws OperationException {
                                                            target.setCurrentVersion(arg);
                                                            targetsList.add(target);

                                                            view.setTargets(targetsList);
                                                            view.setEnabledInstallButton(!targetsList.isEmpty());
                                                        }
                                                    }).catchError(new Operation<PromiseError>() {
                                @Override
                                public void apply(PromiseError arg) throws OperationException {
                                    target.setCurrentVersion("n/a");
                                    targetsList.add(target);

                                    view.setTargets(targetsList);

                                    notificationManager.notify(target.getName(),
                                                               localizationConstants.updateSDKNotificationGetInstalledVersionFailMessage() +
                                                               arg.getMessage(),
                                                               FAIL,
                                                               FLOAT_MODE);
                                }
                            });
                }

                Promises.all(promises).then(new Operation<JsArrayMixed>() {
                    @Override
                    public void apply(JsArrayMixed arg) throws OperationException {
                        loader.hide();
                    }
                }).catchError(new Operation<PromiseError>() {
                    @Override
                    public void apply(PromiseError arg) throws OperationException {
                        loader.hide();
                    }
                });
            }
        });
    }

    @Override
    public void onInstallClicked() {
        Promise<String>[] promises = (Promise<String>[])new Promise[targetsList.size()];
        for (TargetForUpdate target : targetsList) {
            promises[targetsList.indexOf(target)] = update(target);
        }

        view.close();

        Promises.all(promises).then(new Operation<JsArrayMixed>() {
            @Override
            public void apply(JsArrayMixed jsArrayMixed) throws OperationException {
                ConfirmCallback confirmCallback = new ConfirmCallback() {
                    @Override
                    public void accepted() {
                        workspaceServiceClient.stop(appContext.getWorkspaceId()).then(new Operation<Void>() {
                            @Override
                            public void apply(Void aVoid) throws OperationException {
                                Window.Location.reload();
                            }
                        });
                    }
                };

                ConfirmDialog dialog = dialogFactory.createConfirmDialog(localizationConstants.updateSDKViewTitle(),
                                                                         localizationConstants.restartViewText(),
                                                                         localizationConstants.restartViewButtonRestartNow(),
                                                                         localizationConstants.restartViewButtonRestartLater(),
                                                                         confirmCallback,
                                                                         null);
                dialog.show();
            }
        });
    }

    private Promise<String> update(TargetForUpdate target) {
        final StatusNotification notification = notificationManager.notify(target.getName(),
                                                                           localizationConstants.updateSDKNotificationUpdatingMessage(),
                                                                           PROGRESS,
                                                                           FLOAT_MODE);

        return sdkUpdaterProvider.get().installSDK(target.getId(), view.getSelectedVersion()).then(new Operation<String>() {
            @Override
            public void apply(String message) throws OperationException {
                notification.setStatus(StatusNotification.Status.SUCCESS);
                notification.setContent(message);
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                notification.setStatus(FAIL);
                notification.setContent(localizationConstants.updateSDKNotificationUpdateFailMessage() + arg.getMessage());
            }
        });
    }

    @Override
    public void onCancelClicked() {
        view.close();
    }
}
