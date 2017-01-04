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

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.plugin.artik.ide.ArtikLocalizationConstant;
import org.eclipse.che.plugin.cpp.shared.Constants;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * Action for connecting to the debugger for debugging project's binary file.
 *
 * @author Artem Zatsarynnyi
 */
public class DebugAction extends AbstractPerspectiveAction {

    private final AppContext        appContext;
    private final Machine           machine;
    private final DebuggerConnector debuggerConnector;

    @Inject
    public DebugAction(ArtikLocalizationConstant locale,
                       AppContext appContext,
                       @Assisted Machine machine,
                       DebuggerConnector debuggerConnector) {
        super(singletonList(PROJECT_PERSPECTIVE_ID), machine.getConfig().getName(), locale.debugActionDescription(), null, null);

        this.appContext = appContext;
        this.machine = machine;
        this.debuggerConnector = debuggerConnector;
    }

    @Override
    public void updateInPerspective(ActionEvent event) {
        final Optional<Project> currentProject = getCurrentProject();
        event.getPresentation().setEnabled(currentProject.isPresent() && currentProject.get().isTypeOf(Constants.C_PROJECT_TYPE_ID));
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        debuggerConnector.debug(machine);
    }

    private Optional<Project> getCurrentProject() {
        final Resource[] resources = appContext.getResources();
        if (resources == null || resources.length != 1) {
            return Optional.absent();
        }

        Resource resource = appContext.getResource();
        return resource.getRelatedProject();
    }
}
