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
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.component.Component;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.machine.events.MachineStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateHandler;
import org.eclipse.che.plugin.artik.ide.ArtikLocalizationConstant;
import org.eclipse.che.plugin.artik.ide.ArtikResources;
import org.eclipse.che.plugin.artik.ide.machine.DeviceServiceClient;
import org.eclipse.che.plugin.artik.ide.run.RunAction;
import org.eclipse.che.plugin.artik.ide.run.RunActionFactory;

import java.util.List;

import static org.eclipse.che.api.core.model.machine.MachineStatus.RUNNING;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_MAIN_TOOLBAR;

/**
 * Listens for the events of creating/destroying machines and crates/removes
 * an appropriate actions for connecting to the debugger on the related machine and run binary.
 *
 * @author Artem Zatsarynnyi
 * @author Valeriy Svydenko
 */
@Singleton
public class DebugAndRunActionsManager implements MachineStateEvent.Handler, WsAgentStateHandler, Component {

    private final ArtikLocalizationConstant locale;
    private final ActionManager             actionManager;
    private final DebugActionFactory        debugActionFactory;
    private final RunActionFactory          runActionFactory;
    private final DeviceServiceClient       deviceServiceClient;
    private final ArtikResources            resources;

    private DefaultActionGroup debugActionsPopUpGroup;
    private DefaultActionGroup runActionsPopUpGroup;

    @Inject
    public DebugAndRunActionsManager(EventBus eventBus,
                                     ArtikLocalizationConstant locale,
                                     ActionManager actionManager,
                                     DebugActionFactory debugActionFactory,
                                     RunActionFactory runActionFactory,
                                     DeviceServiceClient deviceServiceClient,
                                     ArtikResources resources) {
        this.locale = locale;
        this.actionManager = actionManager;
        this.debugActionFactory = debugActionFactory;
        this.runActionFactory = runActionFactory;
        this.deviceServiceClient = deviceServiceClient;
        this.resources = resources;

        runActionsPopUpGroup = new DefaultActionGroup(locale.runActionDescription(), true, actionManager);
        actionManager.registerAction("runActionsPopUpGroup", runActionsPopUpGroup);

        debugActionsPopUpGroup = new DefaultActionGroup(locale.debugActionDescription(), true, actionManager);
        actionManager.registerAction("debugActionsPopUpGroup", debugActionsPopUpGroup);

        eventBus.addHandler(MachineStateEvent.TYPE, this);
    }

    @Override
    public void start(Callback<Component, Exception> callback) {
        callback.onSuccess(DebugAndRunActionsManager.this);

        debugActionsPopUpGroup.getTemplatePresentation().setDescription(locale.debugActionDescription());
        debugActionsPopUpGroup.getTemplatePresentation().setSVGResource(resources.debug());

        runActionsPopUpGroup.getTemplatePresentation().setDescription(locale.runActionDescription());
        runActionsPopUpGroup.getTemplatePresentation().setSVGResource(resources.run());

        // add debug group to the context menu
        DefaultActionGroup resourceOperationGroup = (DefaultActionGroup)actionManager.getAction("resourceOperation");
        resourceOperationGroup.addSeparator();
        resourceOperationGroup.add(runActionsPopUpGroup);
        resourceOperationGroup.addSeparator();
        resourceOperationGroup.add(debugActionsPopUpGroup);

        // add debug pop-up group to the main toolbar
        DefaultActionGroup debugActionsToolbarGroup = new DebugAndRunActionsToolbarGroup(actionManager);
        debugActionsToolbarGroup.add(runActionsPopUpGroup);
        debugActionsToolbarGroup.addSeparator();
        debugActionsToolbarGroup.add(debugActionsPopUpGroup);
        DefaultActionGroup mainToolbarGroup = (DefaultActionGroup)actionManager.getAction(GROUP_MAIN_TOOLBAR);
        mainToolbarGroup.add(debugActionsToolbarGroup);
    }

    @Override
    public void onMachineCreating(MachineStateEvent event) {
    }

    @Override
    public void onMachineRunning(MachineStateEvent event) {
        addAction(event.getMachine());
    }

    private void addAction(Machine machine) {
        if (!isArtik(machine)) {
            return;
        }

        DebugAction debugAction = debugActionFactory.create(machine);
        actionManager.registerAction("debug" + machine.getId(), debugAction);

        debugActionsPopUpGroup.add(debugAction, Constraints.FIRST);

        RunAction runAction = runActionFactory.create(machine);
        actionManager.registerAction("run" + machine.getId(), runAction);

        runActionsPopUpGroup.add(runAction, Constraints.FIRST);
    }

    @Override
    public void onMachineDestroyed(MachineStateEvent event) {
        removeDebugAction(event.getMachine());
    }

    private void removeDebugAction(Machine machine) {
        if (!isArtik(machine)) {
            return;
        }

        Action action = actionManager.getAction("debug" + machine.getId());
        actionManager.unregisterAction("debug" + machine.getId());

        debugActionsPopUpGroup.remove(action);

        Action runAction = actionManager.getAction("run" + machine.getId());
        actionManager.unregisterAction("run" + machine.getId());

        runActionsPopUpGroup.remove(runAction);
    }

    private boolean isArtik(Machine machine) {
        String type = machine.getConfig().getType();
        return "artik".equals(type);
    }

    @Override
    public void onWsAgentStarted(WsAgentStateEvent event) {
        deviceServiceClient.getDevices().then(new Operation<List<MachineDto>>() {
            @Override
            public void apply(List<MachineDto> arg) throws OperationException {
                for (MachineDto machineDto : arg) {
                    if (isArtik(machineDto) && RUNNING.equals(machineDto.getStatus())) {
                        addAction(machineDto);
                    }
                }
            }
        });
    }

    @Override
    public void onWsAgentStopped(WsAgentStateEvent event) {
    }

    /**
     * Action group for placing {@link DebugAction} and {@link RunAction} on the toolbar.
     * It's visible when at least one {@link DebugAction} or {@link RunAction} exists.
     */
    private class DebugAndRunActionsToolbarGroup extends DefaultActionGroup {

        DebugAndRunActionsToolbarGroup(ActionManager actionManager) {
            super(actionManager);
        }

        @Override
        public void update(ActionEvent e) {
            e.getPresentation().setEnabledAndVisible(debugActionsPopUpGroup.getChildrenCount() != 0 ||
                                                     runActionsPopUpGroup.getChildrenCount() != 0);
        }
    }
}
