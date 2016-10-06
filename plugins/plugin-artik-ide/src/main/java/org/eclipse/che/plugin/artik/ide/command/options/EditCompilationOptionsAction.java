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
package org.eclipse.che.plugin.artik.ide.command.options;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.plugin.artik.ide.ArtikLocalizationConstant;

import javax.validation.constraints.NotNull;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * Action which opens the presenter for editing compilation options.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class EditCompilationOptionsAction extends AbstractPerspectiveAction {

    private final EditCompilationOptionsPresenter presenter;
    private final AppContext                      appContext;

    @Inject
    public EditCompilationOptionsAction(ArtikLocalizationConstant locale,
                                        EditCompilationOptionsPresenter presenter,
                                        AppContext appContext) {
        super(singletonList(PROJECT_PERSPECTIVE_ID),
              locale.editCompilationOptionsActionTitle(),
              locale.editCompilationOptionsActionDescription(),
              null, null);

        this.presenter = presenter;
        this.appContext = appContext;
    }

    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
        event.getPresentation().setEnabled(appContext.getRootProject() != null);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        presenter.show();
    }
}
