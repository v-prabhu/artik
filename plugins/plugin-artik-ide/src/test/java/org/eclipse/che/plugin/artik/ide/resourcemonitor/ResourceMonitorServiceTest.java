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
package org.eclipse.che.plugin.artik.ide.resourcemonitor;

import com.google.gwt.resources.client.TextResource;
import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.ide.api.machine.MachineServiceClient;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.MessageBusProvider;
import org.eclipse.che.ide.websocket.events.MessageHandler;
import org.eclipse.che.plugin.artik.ide.ArtikResources;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** @author Artem Zatsarynnyi */
@RunWith(GwtMockitoTestRunner.class)
public class ResourceMonitorServiceTest {

    private static final String SOME_TEXT  = "some_text";
    private static final String MACHINE_ID = "device_id";

    @Mock
    private MachineServiceClient machineServiceClient;
    @Mock
    private MessageBusProvider   messageBusProvider;
    @Mock
    private DtoFactory           dtoFactory;
    @Mock
    private ArtikResources       artikResources;

    @Mock
    private MessageBus   messageBus;
    @Mock
    private CommandDto   commandDto;
    @Mock
    private TextResource textResource;

    private ResourceMonitorService resourceMonitorService;

    @Before
    public void setUp() {
        when(textResource.getText()).thenReturn(SOME_TEXT);

        when(messageBusProvider.getMessageBus()).thenReturn(messageBus);

        when(commandDto.withName(anyString())).thenReturn(commandDto);
        when(commandDto.withType(anyString())).thenReturn(commandDto);
        when(commandDto.withCommandLine(anyString())).thenReturn(commandDto);
        when(dtoFactory.createDto(CommandDto.class)).thenReturn(commandDto);

        resourceMonitorService = new ResourceMonitorService(machineServiceClient, messageBusProvider, dtoFactory, artikResources);
    }

    @Test
    public void testGetTotalMemory() throws Exception {
        when(artikResources.getTotalMemoryCommand()).thenReturn(textResource);

        resourceMonitorService.getTotalMemory(MACHINE_ID);

        verify(artikResources).getTotalMemoryCommand();
        verify(textResource).getText();
        verifyCommandExecution();
    }

    @Test
    public void testGetUsedMemory() throws Exception {
        when(artikResources.getUsedMemoryCommand()).thenReturn(textResource);

        resourceMonitorService.getUsedMemory(MACHINE_ID);

        verify(artikResources).getUsedMemoryCommand();
        verify(textResource).getText();
        verifyCommandExecution();
    }

    @Test
    public void testGetCpuUtilization() throws Exception {
        when(artikResources.getCpuCommand()).thenReturn(textResource);

        resourceMonitorService.getCpuUtilization(MACHINE_ID);

        verify(artikResources).getCpuCommand();
        verify(textResource).getText();
        verifyCommandExecution();
    }

    @Test
    public void testTotalStorageSpace() throws Exception {
        when(artikResources.getTotalStorageSpaceCommand()).thenReturn(textResource);

        resourceMonitorService.getTotalStorageSpace(MACHINE_ID);

        verify(artikResources).getTotalStorageSpaceCommand();
        verify(textResource).getText();
        verifyCommandExecution();
    }

    @Test
    public void testUsedStorageSpace() throws Exception {
        when(artikResources.getUsedStorageSpaceCommand()).thenReturn(textResource);

        resourceMonitorService.getUsedStorageSpace(MACHINE_ID);

        verify(artikResources).getUsedStorageSpaceCommand();
        verify(textResource).getText();
        verifyCommandExecution();
    }

    private void verifyCommandExecution() throws Exception {
        verify(messageBus).subscribe(anyString(), any(MessageHandler.class));
        verify(machineServiceClient).executeCommand(anyString(), eq(commandDto), anyString());
    }
}
