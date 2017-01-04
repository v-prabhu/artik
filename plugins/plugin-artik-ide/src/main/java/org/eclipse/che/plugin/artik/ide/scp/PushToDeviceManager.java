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
package org.eclipse.che.plugin.artik.ide.scp;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.machine.events.MachineStateEvent;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.plugin.artik.ide.ArtikLocalizationConstant;
import org.eclipse.che.plugin.artik.ide.machine.DeviceServiceClient;
import org.eclipse.che.plugin.artik.ide.scp.action.ChooseTargetAction;
import org.eclipse.che.plugin.artik.ide.scp.action.PushToDeviceAction;
import org.eclipse.che.plugin.artik.ide.scp.action.PushToDeviceActionFactory;
import org.eclipse.che.plugin.artik.ide.scp.service.PushToDeviceServiceClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.eclipse.che.api.core.model.machine.MachineStatus.RUNNING;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;

/**
 * The class which contains business logic to work with secure copy. The business logic reacts on connecting and disconnecting to
 * Artik device and adds or removes actions in 'Push To Device' action group. Also manager stores all connected devices.
 *
 * @author Dmitry Shnurenko
 */
@Singleton
public class PushToDeviceManager implements MachineStateEvent.Handler {
    private final Map<String, String>          devices;
    private final PushToDeviceServiceClient    scpService;
    private final NotificationManager          notificationManager;
    private final ArtikLocalizationConstant    locale;
    private final ActionManager                actionManager;
    private final PushToDeviceActionFactory    pushToDeviceActionFactory;
    private final DefaultActionGroup           pushToDeviceGroup;
    private final Provider<ChooseTargetAction> chooseTargetActionProvider;
    private final DeviceServiceClient          deviceServiceClient;

    @Inject
    public PushToDeviceManager(PushToDeviceServiceClient scpService,
                               DeviceServiceClient deviceServiceClient,
                               NotificationManager notificationManager,
                               ArtikLocalizationConstant locale,
                               EventBus eventBus,
                               ActionManager actionManager,
                               PushToDeviceActionFactory pushToDeviceActionFactory,
                               Provider<ChooseTargetAction> chooseTargetActionProvider) {
        this.deviceServiceClient = deviceServiceClient;
        this.devices = new HashMap<>();
        this.scpService = scpService;
        this.notificationManager = notificationManager;
        this.locale = locale;
        this.actionManager = actionManager;
        this.pushToDeviceActionFactory = pushToDeviceActionFactory;
        this.chooseTargetActionProvider = chooseTargetActionProvider;
        this.pushToDeviceGroup = new DefaultActionGroup("Push To Device", true, actionManager);

        eventBus.addHandler(MachineStateEvent.TYPE, this);
    }

    /** The method fetches all devices and adds to push to device action group. */
    public void fetchSshMachines() {
        DefaultActionGroup resourceOperationGroup = (DefaultActionGroup)actionManager.getAction("resourceOperation");
        resourceOperationGroup.add(pushToDeviceGroup);
        deviceServiceClient.getDevices().then(new Operation<List<MachineDto>>() {
            @Override
            public void apply(List<MachineDto> devices) throws OperationException {
                for (Machine device : devices) {
                    if (RUNNING.equals(device.getStatus())) {
                        MachineConfig config = device.getConfig();
                        String deviceId = device.getId();
                        String deviceName = config.getName();

                        PushToDeviceManager.this.devices.put(deviceName, deviceId);

                        addPushToDeviceAction(deviceName, deviceId);
                    }
                }
                pushToDeviceGroup.addSeparator();
                pushToDeviceGroup.add(chooseTargetActionProvider.get(), Constraints.LAST);
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError error) throws OperationException {
                Log.error(getClass(), error.getMessage());
            }
        });
    }

    /**
     * The method send request to service to do secure copy file or folder to device.
     *
     * @param deviceName
     *         name of device to which file will be copied
     * @param sourcePath
     *         path to file or folder which will be copied
     * @param targetPath
     *         destination path where file or folder will be copied
     */
    public void pushToDevice(String deviceName, String sourcePath, final String targetPath) {
        String deviceId = devices.get(deviceName);

        final String fileName = getFileName(sourcePath);

        Promise<Void> promise = scpService.pushToDevice(deviceId, sourcePath, targetPath);
        promise.then(new Operation<Void>() {
            @Override
            public void apply(Void arg) throws OperationException {
                notificationManager.notify(locale.pushToDeviceSuccess(fileName, targetPath),
                                           SUCCESS,
                                           StatusNotification.DisplayMode.FLOAT_MODE);
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError error) throws OperationException {
                notificationManager.notify(locale.pushToDeviceFail(fileName, targetPath), FAIL, StatusNotification.DisplayMode.FLOAT_MODE);
            }
        });
    }

    /** Returns <code>true</code> if device exists, <code>false</code> otherwise. */
    public boolean isSshDeviceExist() {
        return !devices.isEmpty();
    }

    /** Returns set of devices' names. */
    public Set<String> getDeviceNames() {
        return devices.keySet();
    }

    private String getFileName(String sourcePath) {
        if (Strings.isNullOrEmpty(sourcePath)) {
            throw new IllegalArgumentException("Source path can not be null");
        }

        return sourcePath.substring(sourcePath.lastIndexOf("/") + 1);
    }

    @Override
    public void onMachineCreating(MachineStateEvent event) {
    }

    @Override
    public void onMachineRunning(MachineStateEvent event) {
        Machine device = event.getMachine();
        if (!isArtik(device)) {
            return;
        }

        String deviceName = device.getConfig().getName();
        String deviceId = device.getId();
        devices.put(deviceName, deviceId);

        addPushToDeviceAction(deviceName, deviceId);
    }

    private boolean isArtik(Machine device) {
        String type = device.getConfig().getType();
        return "artik".equals(type);
    }

    private void addPushToDeviceAction(String deviceName, String deviceId) {
        if (actionManager.getAction(deviceId) != null) {
            return;
        }
        PushToDeviceAction pushToDeviceAction = pushToDeviceActionFactory.create(deviceName);
        actionManager.registerAction(deviceId, pushToDeviceAction);

        pushToDeviceGroup.add(pushToDeviceAction, Constraints.FIRST);
    }

    @Override
    public void onMachineDestroyed(MachineStateEvent event) {
        Machine device = event.getMachine();
        if (!isArtik(device)) {
            return;
        }

        devices.remove(device.getConfig().getName());

        Action action = actionManager.getAction(device.getId());
        pushToDeviceGroup.remove(action);
    }
}
