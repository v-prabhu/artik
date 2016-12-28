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

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.extension.machine.client.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.MessageBusProvider;
import org.eclipse.che.plugin.artik.ide.machine.DeviceServiceClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link PackageInstallerPresenter}.
 *
 * @author Lijuan Xue
 */
@RunWith(MockitoJUnitRunner.class)
public class PackageInstallerPresenterTest {

    @Mock
    private PackageInstallerView view;

    @Mock
    private NotificationManager notificationManager;

    @Mock
    private Machine machine;

    @Mock
    private DtoFactory dtoFactory;

    @Mock
    private DeviceServiceClient deviceServiceClient;

    @Mock
    private MessageBusProvider messageBusProvider;

    @Mock
    private MessageBus messageBus;

    @Mock
    private ProcessesPanelPresenter processesPanelPresenter;

    @Mock
    private AsyncCallback<String> commandCallback;

    @InjectMocks
    private PackageInstallerPresenter presenter;

    @Before
    public void setUp() {
        when(messageBusProvider.getMachineMessageBus()).thenReturn(messageBus);

        presenter.showDialog(machine);

        verify(view).showDialog();
    }

    @Test
    public void shouldCloseDialogue() throws Exception {
        presenter.onCancelClicked();
        verify(view).closeDialog();
    }

    @Test
    public void testInstallButtonClickedWithoutPkgName() throws Exception {
        when(view.getPackageName()).thenReturn("");

        presenter.onInstallButtonClicked();

        verify(view, never()).closeDialog();
    }
}
