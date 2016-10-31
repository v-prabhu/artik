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
package org.eclipse.che.plugin.artik.ide.run;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.plugin.artik.ide.ArtikLocalizationConstant;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * Action for running binary file.
 *
 * @author Valeriy Svydenko
 */
public class RunBinaryAction extends AbstractPerspectiveAction {

    private final AppContext       appContext;
    private final Machine          machine;
    private final BinaryFileRunner binaryFileRunner;

    @Inject
    public RunBinaryAction(ArtikLocalizationConstant locale,
                           AppContext appContext,
                           @Assisted Machine machine,
                           BinaryFileRunner binaryFileRunner) {
        super(singletonList(PROJECT_PERSPECTIVE_ID), machine.getConfig().getName(), locale.runActionDescription(), null, null);

        this.appContext = appContext;
        this.machine = machine;
        this.binaryFileRunner = binaryFileRunner;
    }

    @Override
    public void updateInPerspective(ActionEvent event) {
        event.getPresentation().setEnabled(appContext.getRootProject() != null);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        binaryFileRunner.run(machine);
    }
}
