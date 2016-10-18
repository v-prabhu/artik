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
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.component.Component;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.machine.MachineServiceClient;
import org.eclipse.che.ide.extension.machine.client.machine.MachineStateEvent;
import org.eclipse.che.plugin.artik.ide.ArtikResources;

import java.util.List;

import static org.eclipse.che.ide.api.action.IdeActions.GROUP_MAIN_TOOLBAR;

/**
 * Listens for the events of creating/destroying machines and crates/removes
 * an appropriate actions for connecting to the debugger on the related machine.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class DebugBinaryActionsManager implements MachineStateEvent.Handler, Component {

    private final ActionManager            actionManager;
    private final DebugBinaryActionFactory debugBinaryActionFactory;
    private final MachineServiceClient     machineServiceClient;
    private final AppContext               appContext;
    private final ArtikResources           resources;

    private DefaultActionGroup debugActionsPopUpGroup;

    @Inject
    public DebugBinaryActionsManager(EventBus eventBus,
                                     ActionManager actionManager,
                                     DebugBinaryActionFactory debugBinaryActionFactory,
                                     MachineServiceClient machineServiceClient,
                                     AppContext appContext,
                                     ArtikResources resources) {
        this.actionManager = actionManager;
        this.debugBinaryActionFactory = debugBinaryActionFactory;
        this.machineServiceClient = machineServiceClient;
        this.appContext = appContext;
        this.resources = resources;

        eventBus.addHandler(MachineStateEvent.TYPE, this);
    }

    @Override
    public void start(Callback<Component, Exception> callback) {
        callback.onSuccess(DebugBinaryActionsManager.this);

        debugActionsPopUpGroup = new DefaultActionGroup("Debug", true, actionManager);
        actionManager.registerAction("debugActionsPopUpGroup", debugActionsPopUpGroup);
        debugActionsPopUpGroup.getTemplatePresentation().setDescription("Debug Binary");
        debugActionsPopUpGroup.getTemplatePresentation().setSVGResource(resources.debug());

        // add debug group to the context menu
        DefaultActionGroup resourceOperationGroup = (DefaultActionGroup)actionManager.getAction("resourceOperation");
        resourceOperationGroup.addSeparator();
        resourceOperationGroup.add(debugActionsPopUpGroup);

        // add debug pop-up group to the main toolbar
        DefaultActionGroup debugActionsToolbarGroup = new DebugActionsToolbarGroup(actionManager);
        debugActionsToolbarGroup.add(debugActionsPopUpGroup);
        DefaultActionGroup mainToolbarGroup = (DefaultActionGroup)actionManager.getAction(GROUP_MAIN_TOOLBAR);
        mainToolbarGroup.add(debugActionsToolbarGroup);

        machineServiceClient.getMachines(appContext.getWorkspaceId()).then(new Operation<List<MachineDto>>() {
            @Override
            public void apply(List<MachineDto> arg) throws OperationException {
                for (MachineDto machineDto : arg) {
                    if (isArtikOrSsh(machineDto)) {
                        addDebugAction(machineDto);
                    }
                }
            }
        });
    }

    @Override
    public void onMachineCreating(MachineStateEvent event) {
    }

    @Override
    public void onMachineRunning(MachineStateEvent event) {
        addDebugAction(event.getMachine());
    }

    private void addDebugAction(Machine machine) {
        if (!isArtikOrSsh(machine)) {
            return;
        }

        DebugBinaryAction debugBinaryAction = debugBinaryActionFactory.create(machine);
        actionManager.registerAction("debug" + machine.getId(), debugBinaryAction);

        debugActionsPopUpGroup.add(debugBinaryAction, Constraints.FIRST);
    }

    @Override
    public void onMachineDestroyed(MachineStateEvent event) {
        removeDebugAction(event.getMachine());
    }

    private void removeDebugAction(Machine machine) {
        if (!isArtikOrSsh(machine)) {
            return;
        }

        Action action = actionManager.getAction("debug" + machine.getId());
        actionManager.unregisterAction("debug" + machine.getId());

        debugActionsPopUpGroup.remove(action);
    }

    private boolean isArtikOrSsh(Machine machine) {
        String type = machine.getConfig().getType();
        return "ssh".equals(type) || "artik".equals(type);
    }

    /**
     * Action group for placing {@link DebugBinaryAction}s on the toolbar.
     * It's visible when at least one {@link DebugBinaryAction} exists.
     */
    private class DebugActionsToolbarGroup extends DefaultActionGroup {

        DebugActionsToolbarGroup(ActionManager actionManager) {
            super(actionManager);
        }

        @Override
        public void update(ActionEvent e) {
            e.getPresentation().setEnabledAndVisible(debugActionsPopUpGroup.getChildrenCount() != 0);
        }
    }
}
