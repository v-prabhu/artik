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
package org.eclipse.che.plugin.artik.ide.run.params;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.plugin.artik.ide.ArtikLocalizationConstant;
import org.eclipse.che.plugin.nodejs.shared.Constants;

import javax.validation.constraints.NotNull;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * Action which opens the presenter for editing run options.
 */
@Singleton
public class EditRunParamsAction extends AbstractPerspectiveAction {

    private final EdiRunParametersPresenter presenter;
    private final AppContext                appContext;

    @Inject
    public EditRunParamsAction(ArtikLocalizationConstant locale,
                               EdiRunParametersPresenter presenter,
                               AppContext appContext) {
        super(singletonList(PROJECT_PERSPECTIVE_ID),
              locale.editRunParamsActionTitle(),
              locale.editRunParamsActionDescription(),
              null, null);

        this.presenter = presenter;
        this.appContext = appContext;
    }

    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
        final Project rootProject = appContext.getRootProject();
        event.getPresentation().setEnabled(rootProject != null && rootProject.isTypeOf(Constants.NODE_JS_PROJECT_TYPE_ID));
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        presenter.show();
    }
}
