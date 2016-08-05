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
package org.eclipse.che.plugin.artik.ide.profile;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import static java.util.Collections.singletonList;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;
import org.eclipse.che.plugin.artik.ide.ArtikLocalizationConstant;

import javax.validation.constraints.NotNull;

/**
 * Action to turn on development mode at Artik device.
 *
 * @author Vitaliy Guliy
 */
public class TurnDevelopmentModeAction extends AbstractPerspectiveAction {

    private final DevelopmentModeManager    developmentModeManager;
    private final String                    machineName;

    @Inject
    public TurnDevelopmentModeAction(ArtikLocalizationConstant locale,
                                    DevelopmentModeManager developmentModeManager,
                                    @Assisted String machineName) {
        super(singletonList(PROJECT_PERSPECTIVE_ID),
                locale.turnDevelopmentModeActionTitle(),
                locale.turnDevelopmentModeActionDescription(),
                null,
                null);

        this.developmentModeManager = developmentModeManager;
        this.machineName = machineName;
    }

    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        developmentModeManager.turnOnDevelopmentMode(machineName);
    }

}
