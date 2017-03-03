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
package org.eclipse.che.plugin.artik.ide.run;

import com.google.common.base.Optional;
import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.api.machine.shared.dto.execagent.ProcessStartResponseDto;
import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.ExecAgentCommandManager;
import org.eclipse.che.ide.api.machine.execagent.ExecAgentPromise;
import org.eclipse.che.ide.api.macro.MacroProcessor;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.project.ProjectServiceClient;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.CommandConsoleFactory;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.CommandOutputConsole;
import org.eclipse.che.ide.extension.machine.client.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.ide.part.explorer.project.macro.ExplorerCurrentFileNameMacro;
import org.eclipse.che.ide.part.explorer.project.macro.ExplorerCurrentFileParentPathMacro;
import org.eclipse.che.ide.resource.Path;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(GwtMockitoTestRunner.class)
public class BinaryRunnerTest {
    private static final String DEVICE_ID        = "artik1";
    private static final String COMMAND_TEMPLATE = "cd ${artik.replication.folder.artik1}/${current.project.path} && ./a.out";
    private static final String COMMAND_LINE     = "cd /projects/replication-folder/app && ./a.out";

    @Mock
    private DtoFactory                         dtoFactory;
    @Mock
    private AppContext                         appContext;
    @Mock
    private NotificationManager                notificationManager;
    @Mock
    private ProjectServiceClient               projectServiceClient;
    @Mock
    private MacroProcessor                     macroProcessor;
    @Mock
    private CommandConsoleFactory              consoleFactory;
    @Mock
    private ExplorerCurrentFileParentPathMacro currentFileParentPathMacro;
    @Mock
    private ExplorerCurrentFileNameMacro       currentFileNameMacro;
    @Mock
    private ProcessesPanelPresenter            processesPanelPresenter;
    @Mock
    private ExecAgentCommandManager            execAgentCommandManager;

    private BinaryRunner binaryRunner;

    @Mock
    private Machine                                   device;
    @Mock
    private Resource                                  resource;
    @Mock
    private Project                                   project;
    @Mock
    private ItemReference                             itemReference;
    @Mock
    private CommandDto                                commandDto;
    @Mock
    private CommandOutputConsole                      commandOutputConsole;
    @Mock
    private Promise<String>                           macroProcessorPromise;
    @Mock
    private ExecAgentPromise<ProcessStartResponseDto> processPromise;
    @Mock
    private Promise<ItemReference>                    itemReferencePromise;
    @Captor
    private ArgumentCaptor<Operation<ItemReference>>  itemReferenceCapture;
    @Captor
    private ArgumentCaptor<Operation<PromiseError>>   itemReferenceErrorCapture;
    @Captor
    private ArgumentCaptor<Operation<String>>         macroArgumentCapture;

    @Before
    public void setUp() throws Exception {
        when(device.getId()).thenReturn(DEVICE_ID);
        when(currentFileParentPathMacro.getName()).thenReturn(ExplorerCurrentFileParentPathMacro.KEY);
        when(currentFileNameMacro.getName()).thenReturn(ExplorerCurrentFileNameMacro.KEY);
        when(macroProcessor.expandMacros(anyString())).thenReturn(macroProcessorPromise);
        when(macroProcessorPromise.then(Matchers.<Operation<String>>any())).thenReturn(macroProcessorPromise);

        when(project.getAttribute(anyString())).thenReturn("a.out");
        when(project.getLocation()).thenReturn(new Path("/project/path"));

        when(projectServiceClient.getItem(anyObject())).thenReturn(itemReferencePromise);
        when(itemReferencePromise.then(Matchers.<Operation<ItemReference>>any())).thenReturn(itemReferencePromise);
        when(itemReferencePromise.catchError(Matchers.<Operation<PromiseError>>any())).thenReturn(itemReferencePromise);

        when(dtoFactory.createDto(CommandDto.class)).thenReturn(commandDto);
        when(commandDto.withName(anyString())).thenReturn(commandDto);
        when(commandDto.withCommandLine(anyString())).thenReturn(commandDto);
        when(commandDto.withType(anyString())).thenReturn(commandDto);

        when(execAgentCommandManager.startProcess(anyString(), anyObject())).thenReturn(processPromise);
        when(processPromise.thenIfProcessStartedEvent(Matchers.any())).thenReturn(processPromise);
        when(processPromise.thenIfProcessDiedEvent(Matchers.any())).thenReturn(processPromise);
        when(processPromise.thenIfProcessStdErrEvent(Matchers.any())).thenReturn(processPromise);
        when(processPromise.thenIfProcessStdOutEvent(Matchers.any())).thenReturn(processPromise);

        when(consoleFactory.create(anyObject(), anyObject())).thenReturn(commandOutputConsole);

        binaryRunner = new BinaryRunner(dtoFactory,
                                        appContext,
                                        execAgentCommandManager,
                                        notificationManager,
                                        projectServiceClient,
                                        consoleFactory,
                                        macroProcessor,
                                        processesPanelPresenter);
    }

    @Test
    public void shouldBeRanBinaryFileWithDefaultName() throws Exception {
        Resource[] resources = {resource};
        when(appContext.getResources()).thenReturn(resources);
        when(appContext.getResource()).thenReturn(resource);
        when(resource.getRelatedProject()).thenReturn(Optional.of(project));

        binaryRunner.run(device);

        verify(projectServiceClient).getItem(anyObject());
        verify(itemReferencePromise).then(itemReferenceCapture.capture());
        itemReferenceCapture.getValue().apply(itemReference);

        verify(macroProcessor).expandMacros(COMMAND_TEMPLATE);
        verify(macroProcessorPromise).then(macroArgumentCapture.capture());
        macroArgumentCapture.getValue().apply(COMMAND_LINE);

        verify(dtoFactory).createDto(CommandDto.class);
        verify(commandDto).withCommandLine(COMMAND_LINE);
        verify(commandDto).withName("run");
        verify(commandDto).withType("custom");

        verify(consoleFactory).create(anyObject(), anyObject());
        verify(processesPanelPresenter).addCommandOutput(DEVICE_ID, commandOutputConsole);

        verify(execAgentCommandManager).startProcess(DEVICE_ID, commandDto);
        verify(commandOutputConsole).getProcessStartedOperation();
        verify(commandOutputConsole).getProcessDiedOperation();
        verify(commandOutputConsole).getStdOutOperation();
        verify(commandOutputConsole).getStdErrOperation();
    }

    @Test
    public void shouldNotRunIfNoBinaryFile() throws Exception {
        Resource[] resources = {resource};
        when(appContext.getResources()).thenReturn(resources);
        when(appContext.getResource()).thenReturn(resource);
        when(resource.getRelatedProject()).thenReturn(Optional.of(project));

        binaryRunner.run(device);

        verify(projectServiceClient).getItem(anyObject());
        verify(itemReferencePromise).catchError(itemReferenceErrorCapture.capture());
        itemReferenceErrorCapture.getValue().apply(Mockito.mock(PromiseError.class));

        verify(notificationManager).notify("",
                                           "No binary file found. Compile your app and re-run.",
                                           StatusNotification.Status.FAIL,
                                           StatusNotification.DisplayMode.EMERGE_MODE);

    }
}
