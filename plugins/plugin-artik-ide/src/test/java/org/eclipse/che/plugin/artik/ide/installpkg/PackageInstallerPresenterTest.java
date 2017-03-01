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

import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.api.machine.shared.dto.execagent.ProcessStartResponseDto;
import org.eclipse.che.ide.api.machine.ExecAgentCommandManager;
import org.eclipse.che.ide.api.machine.execagent.ExecAgentPromise;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.extension.machine.client.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.plugin.artik.ide.ArtikLocalizationConstant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
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
    private ExecAgentCommandManager   execAgentCommandManager;
    @Mock
    private NotificationManager       notificationManager;
    @Mock
    private DtoFactory                dtoFactory;
    @Mock
    private ArtikLocalizationConstant locale;
    @Mock
    private EventBus                  eventBus;
    @Mock
    private ProcessesPanelPresenter   processesPanelPresenter;
    @Mock
    private PackageInstallerView      view;

    @Mock
    private Machine                                   machine;
    @Mock
    private MachineConfig                             machineConfig;
    @Mock
    private ExecAgentPromise<ProcessStartResponseDto> promise;

    @InjectMocks
    private PackageInstallerPresenter presenter;

    @Before
    public void setUp() {
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
        when(machine.getConfig()).thenReturn(machineConfig);
        when(machineConfig.getName()).thenReturn("deviceName");

        presenter.onInstallButtonClicked();

        verify(view, never()).closeDialog();
    }

    @Test
    public void testInstallButtonClicked() throws Exception {
        final String notificationMessage = "Installing package: packageName on the target machine: deviceName.";

        CommandDto commandDto = Mockito.mock(CommandDto.class);
        StatusNotification statusNotification = Mockito.mock(StatusNotification.class);
        when(dtoFactory.createDto(CommandDto.class)).thenReturn(commandDto);
        when(commandDto.withType(anyString())).thenReturn(commandDto);
        when(commandDto.withCommandLine(anyString())).thenReturn(commandDto);
        when(commandDto.withName(anyString())).thenReturn(commandDto);
        when(notificationManager.notify(notificationMessage, PROGRESS, FLOAT_MODE)).thenReturn(statusNotification);

        when(execAgentCommandManager.startProcess(anyString(), anyObject())).thenReturn(promise);
        when(promise.thenIfProcessStdOutEvent(Matchers.any())).thenReturn(promise);
        when(promise.thenIfProcessDiedEvent(Matchers.any())).thenReturn(promise);
        when(promise.thenIfProcessStdErrEvent(Matchers.any())).thenReturn(promise);

        when(view.getPackageName()).thenReturn("packageName");
        when(machine.getConfig()).thenReturn(machineConfig);
        when(machine.getId()).thenReturn("deviceId");
        when(machineConfig.getName()).thenReturn("deviceName");


        presenter.onInstallButtonClicked();

        verify(notificationManager).notify(notificationMessage, PROGRESS, FLOAT_MODE);

        verify(dtoFactory).createDto(CommandDto.class);
        verify(commandDto).withName("name");
        verify(commandDto).withType("custom");
        verify(commandDto).withCommandLine("dnf install packageName -y");

        verify(execAgentCommandManager).startProcess("deviceId", commandDto);
        verify(view).closeDialog();
    }
}
