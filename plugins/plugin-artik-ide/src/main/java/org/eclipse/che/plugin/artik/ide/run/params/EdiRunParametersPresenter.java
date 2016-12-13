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
package org.eclipse.che.plugin.artik.ide.run.params;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.project.MutableProjectConfig;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;

import java.util.Collections;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.plugin.artik.ide.command.macro.NodeJsRunParametersMacro.DEFAULT_RUN_PARAMETERS;
import static org.eclipse.che.plugin.nodejs.shared.Constants.RUN_PARAMETERS_ATTRIBUTE;

/**
 * Presenter which allows to edit Artik run parameters.
 */
@Singleton
public class EdiRunParametersPresenter implements EditRunParametersView.ActionDelegate {

    private final AppContext            appContext;
    private final EditRunParametersView view;

    @Inject
    public EdiRunParametersPresenter(AppContext appContext, EditRunParametersView view) {
        this.appContext = appContext;
        this.view = view;

        view.setDelegate(this);
    }

    /** Show the presenter's view. */
    public void show() {
        view.show();

        Optional<Project> projectOptional = getCurrentProject();
        if (!projectOptional.isPresent()) {
            return;
        }

        Project project = projectOptional.get();

        String runParameters = project.getAttribute(RUN_PARAMETERS_ATTRIBUTE);
        view.setRunParameters(!isNullOrEmpty(runParameters) ? runParameters : DEFAULT_RUN_PARAMETERS);
    }

    @Override
    public void onClose() {
        view.close();
    }

    @Override
    public void onSave() {
        saveParameters().then(new Operation<Project>() {
            @Override
            public void apply(Project project) throws OperationException {
                view.close();
            }
        });
    }

    private Promise<Project> saveParameters() {
        Optional<Project> projectOptional = getCurrentProject();
        if (!projectOptional.isPresent()) {
            return null;
        }

        final Project project = projectOptional.get();
        final MutableProjectConfig updateConfig = new MutableProjectConfig(project);

        updateConfig.getAttributes().put(RUN_PARAMETERS_ATTRIBUTE, Collections.singletonList(view.getRunParameters()));

        return project.update().withBody(updateConfig).send();
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
