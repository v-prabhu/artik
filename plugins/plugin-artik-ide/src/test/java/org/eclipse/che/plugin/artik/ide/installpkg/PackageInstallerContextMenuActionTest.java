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

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.extension.machine.client.processes.ProcessTreeNode;
import org.eclipse.che.ide.extension.machine.client.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.plugin.artik.ide.ArtikLocalizationConstant;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link PackageInstallerContextMenuAction}.
 *
 * @author Lijuan Xue
 */
@RunWith(MockitoJUnitRunner.class)
public class PackageInstallerContextMenuActionTest {

    @Mock
    private ArtikLocalizationConstant local;

    @Mock
    private Machine                   machine;

    @Mock
    private PackageInstallerPresenter presenter;

    @Mock
    private ProcessesPanelPresenter panelPresenter;

    @InjectMocks
    private PackageInstallerContextMenuAction contextMenuAction;


    @Test
    public void shouldSetTitle() throws Exception {
        verify(local).installPackageActionTitle();
        verify(local).installPackageActionActionDescription();
    }

    @Test
    public void shouldShowDialog() throws Exception {
        ProcessTreeNode processTreeNode = mock(ProcessTreeNode.class);
        when(panelPresenter.getContextTreeNode()).thenReturn(processTreeNode);
        Machine machine = mock(Machine.class);
        when(processTreeNode.getData()).thenReturn(machine);

        contextMenuAction.actionPerformed(mock(ActionEvent.class));

        verify(panelPresenter).getContextTreeNode();
        verify(presenter).showDialog(machine);
    }
}
