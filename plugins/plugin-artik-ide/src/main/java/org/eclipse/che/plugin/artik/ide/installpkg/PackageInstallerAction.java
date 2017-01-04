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
package org.eclipse.che.plugin.artik.ide.installpkg;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.plugin.artik.ide.ArtikLocalizationConstant;

import javax.validation.constraints.NotNull;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * Action to install online package on Artik device.
 *
 * @author Bin Lv
 */
public class PackageInstallerAction extends AbstractPerspectiveAction {
    private final Machine machine;
    private final PackageInstallerPresenter packageInstallerPresenter;

    @Inject
    public PackageInstallerAction(ArtikLocalizationConstant locale,
                                  PackageInstallerPresenter packageInstallerPresenter,
                                  @Assisted Machine machine) {
        super(singletonList(PROJECT_PERSPECTIVE_ID),
                locale.installPackageActionTitle(),
                locale.installPackageActionActionDescription(),
                null,
                null);

        this.packageInstallerPresenter = packageInstallerPresenter;
        this.machine = machine;
    }

    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        packageInstallerPresenter.showDialog(machine);
    }
}
