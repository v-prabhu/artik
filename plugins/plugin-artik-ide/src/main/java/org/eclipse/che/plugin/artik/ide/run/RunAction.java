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
package org.eclipse.che.plugin.artik.ide.run;

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

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;
import static org.eclipse.che.plugin.cpp.shared.Constants.CPP_PROJECT_TYPE_ID;
import static org.eclipse.che.plugin.cpp.shared.Constants.C_PROJECT_TYPE_ID;
import static org.eclipse.che.plugin.nodejs.shared.Constants.NODE_JS_PROJECT_TYPE_ID;

/**
 * Action for running binary file.
 */
public class RunAction extends AbstractPerspectiveAction {

    private final AppContext   appContext;
    private final Machine      device;
    private final NodeJsRunner nodeJsRunner;
    private final BinaryRunner binaryFileRunner;

    @Inject
    public RunAction(ArtikLocalizationConstant locale,
                     AppContext appContext,
                     @Assisted Machine machine,
                     NodeJsRunner nodeJsRunner,
                     BinaryRunner binaryFileRunner) {
        super(singletonList(PROJECT_PERSPECTIVE_ID), machine.getConfig().getName(), locale.runActionDescription(), null, null);

        this.appContext = appContext;
        this.device = machine;
        this.nodeJsRunner = nodeJsRunner;
        this.binaryFileRunner = binaryFileRunner;
    }

    @Override
    public void updateInPerspective(ActionEvent event) {
        final Optional<Project> currentProject = getCurrentProject();
        event.getPresentation().setEnabled(currentProject.isPresent() && (isEnableForC(currentProject.get()) ||
                                                                          isEnableForCpp(currentProject.get()) ||
                                                                          isEnableForNode(currentProject.get())));
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        final Optional<Project> currentProject = getCurrentProject();
        if (!currentProject.isPresent()) {
            return;
        }

        final Project project = currentProject.get();

        if (project.isTypeOf(NODE_JS_PROJECT_TYPE_ID)) {
            nodeJsRunner.run(device);
        } else if (project.isTypeOf(C_PROJECT_TYPE_ID) || project.isTypeOf(CPP_PROJECT_TYPE_ID)) {
            binaryFileRunner.run(device);
        }
    }

    private boolean isEnableForC(Project project) {
        return project.isTypeOf(C_PROJECT_TYPE_ID);
    }

    private boolean isEnableForCpp(Project project) {
        return project.isTypeOf(CPP_PROJECT_TYPE_ID);
    }

    private boolean isEnableForNode(Project project) {
        if (!project.isTypeOf(NODE_JS_PROJECT_TYPE_ID)) {
            return false;
        }
        final Resource resource = appContext.getResource();
        return !(resource == null || !resource.getName().endsWith(".js"));
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
