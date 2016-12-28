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
package org.eclipse.che.plugin.artik.ide.installpkg;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.plugin.artik.ide.ArtikLocalizationConstant;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link PackageInstallerAction}.
 *
 * @author Lijuan Xue
 */
@RunWith(MockitoJUnitRunner.class)
public class PackageInstallerActionTest {

    @Mock
    private ArtikLocalizationConstant local;

    @Mock
    private Machine                   machine;

    @Mock
    private PackageInstallerPresenter presenter;

    @InjectMocks
    private PackageInstallerAction action;

    @Test
    public void shouldSetTitle() throws Exception {
        verify(local).installPackageActionTitle();
        verify(local).installPackageActionActionDescription();

        verify(presenter, never()).showDialog(machine);
    }

    @Test
    public void shouldShowDialog() throws Exception {
        action.actionPerformed(mock(ActionEvent.class));

        verify(presenter).showDialog(machine);
    }
}
