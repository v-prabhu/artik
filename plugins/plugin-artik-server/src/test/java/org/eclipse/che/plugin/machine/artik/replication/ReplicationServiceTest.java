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
package org.eclipse.che.plugin.machine.artik.replication;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.commons.json.JsonParseException;
import org.eclipse.che.plugin.artik.shared.dto.ArtikVfsModificationEventDto;
import org.eclipse.che.plugin.machine.artik.replication.event.ArtikVfsModificationEventQueueHolder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Random;

import static java.io.File.separator;
import static java.lang.Integer.MAX_VALUE;
import static java.lang.System.currentTimeMillis;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.eclipse.che.api.core.model.machine.MachineStatus.CREATING;
import static org.eclipse.che.api.core.model.machine.MachineStatus.RUNNING;
import static org.eclipse.che.plugin.machine.artik.replication.ReplicationManager.USE_REPLICATION_ROOT;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ReplicationService}
 *
 * @author Dmitry Kuleshov
 *
 * @since 4.5
 */
@Listeners(value = {MockitoTestNGListener.class})
public class ReplicationServiceTest {
    private static final String MACHINE_ID        = Integer.toString(new Random(currentTimeMillis()).nextInt(MAX_VALUE));
    private static final String ARTIK_TYPE        = "artik";
    private static final String PATH_TO_DIRECTORY = separator + "path" + separator + "to" + separator + "directory";


    @Spy
    private ArtikVfsModificationEventQueueHolder queueHolder;
    @Mock
    private ReplicationManager                   replicationManager;
    @Mock
    private ApiRequestHelper                     apiRequestHelper;
    @InjectMocks
    private ReplicationService                   replicationService;

    @Mock
    private ArtikVfsModificationEventDto event;
    @Mock
    private MachineConfigDto             machineConfig;
    @Mock
    private MachineDto                   machine;

    @BeforeMethod
    public void beforeMethod() throws ApiException, IOException {
        when(machineConfig.getType()).thenReturn(ARTIK_TYPE);

        when(machine.getStatus()).thenReturn(RUNNING);
        when(machine.getConfig()).thenReturn(machineConfig);
        when(machine.getId()).thenReturn(MACHINE_ID);

        when(apiRequestHelper.getMachines()).thenReturn(singletonList(machine));

        when(event.getCreatedDirectories()).thenReturn(emptyList());
        when(event.getModifiedDirectories()).thenReturn(emptyList());
        when(event.getRemovedDirectories()).thenReturn(emptyList());

        when(event.getCreatedFiles()).thenReturn(emptyList());
        when(event.getModifiedFiles()).thenReturn(emptyList());
        when(event.getRemovedFiles()).thenReturn(emptyList());
    }

    @Test
    public void shouldCallReplicationManagerToMkdirAfterDirectoryIsCreated() throws ServerException, IOException {
        when(event.getCreatedDirectories()).thenReturn(singletonList(PATH_TO_DIRECTORY));

        queueHolder.put(event);
        replicationService.run();

        verify(replicationManager, times(1)).makeDirectory(MACHINE_ID, PATH_TO_DIRECTORY);
    }

    @Test
    public void shouldCallTwiceReplicationManagerToMkdirAfterDirectoryIsCreatedTwice()
            throws ServerException, IOException, JsonParseException {
        when(event.getCreatedDirectories()).thenReturn(singletonList(PATH_TO_DIRECTORY));

        queueHolder.put(event);
        replicationService.run();
        queueHolder.put(event);
        replicationService.run();

        verify(replicationManager, times(2)).makeDirectory(MACHINE_ID, PATH_TO_DIRECTORY);
    }

    @Test
    public void shouldCallReplicationManagerToMkdirAfterDirectoryIsModified() throws ServerException, IOException {
        when(event.getModifiedDirectories()).thenReturn(singletonList(PATH_TO_DIRECTORY));

        queueHolder.put(event);
        replicationService.run();

        verify(replicationManager, times(1)).makeDirectory(MACHINE_ID, PATH_TO_DIRECTORY);
    }

    @Test
    public void shouldCallTwiceReplicationManagerToMkdirAfterDirectoryIsModifiedTwice() throws ServerException, IOException {
        when(event.getModifiedDirectories()).thenReturn(singletonList(PATH_TO_DIRECTORY));

        queueHolder.put(event);
        replicationService.run();
        queueHolder.put(event);
        replicationService.run();

        verify(replicationManager, times(2)).makeDirectory(MACHINE_ID, PATH_TO_DIRECTORY);
    }

    @Test
    public void shouldCallReplicationManagerToCopyAfterFileIsCreated() throws ServerException, IOException, JsonParseException {
        when(event.getCreatedFiles()).thenReturn(singletonList(PATH_TO_DIRECTORY));

        queueHolder.put(event);
        replicationService.run();

        verify(replicationManager, times(1)).copy(MACHINE_ID, PATH_TO_DIRECTORY, PATH_TO_DIRECTORY, USE_REPLICATION_ROOT);
    }

    @Test
    public void shouldCallTwiceReplicationManagerToCopyAfterFileIsCreatedTwice() throws ServerException, IOException, JsonParseException {
        when(event.getCreatedFiles()).thenReturn(singletonList(PATH_TO_DIRECTORY));

        queueHolder.put(event);
        replicationService.run();
        queueHolder.put(event);
        replicationService.run();

        verify(replicationManager, times(2)).copy(MACHINE_ID, PATH_TO_DIRECTORY, PATH_TO_DIRECTORY, USE_REPLICATION_ROOT);

    }

    @Test
    public void shouldCallReplicationManagerToCopyAfterFileIsModified() throws ServerException, IOException, JsonParseException {
        when(event.getModifiedFiles()).thenReturn(singletonList(PATH_TO_DIRECTORY));

        queueHolder.put(event);
        replicationService.run();

        verify(replicationManager, times(1)).copy(MACHINE_ID, PATH_TO_DIRECTORY, PATH_TO_DIRECTORY, USE_REPLICATION_ROOT);
    }

    @Test
    public void shouldCallTwiceReplicationManagerToCopyAfterFileIsModifiedTwice() throws ServerException, IOException, JsonParseException {
        when(event.getModifiedFiles()).thenReturn(singletonList(PATH_TO_DIRECTORY));

        queueHolder.put(event);
        replicationService.run();
        queueHolder.put(event);
        replicationService.run();

        verify(replicationManager, times(2)).copy(MACHINE_ID, PATH_TO_DIRECTORY, PATH_TO_DIRECTORY, USE_REPLICATION_ROOT);
    }

    @Test
    public void shouldNotCallReplicationManagerBecauseNoArticMachineAvailable() throws ServerException, IOException, JsonParseException {
        when(machineConfig.getType()).thenReturn("");

        when(event.getCreatedDirectories()).thenReturn(singletonList(PATH_TO_DIRECTORY));
        when(event.getCreatedFiles()).thenReturn(singletonList(PATH_TO_DIRECTORY));


        queueHolder.put(event);
        replicationService.run();

        verify(replicationManager, never()).makeDirectory(MACHINE_ID, PATH_TO_DIRECTORY);
        verify(replicationManager, never()).copy(MACHINE_ID, PATH_TO_DIRECTORY, PATH_TO_DIRECTORY, USE_REPLICATION_ROOT);
    }

    @Test
    public void shouldNotCallReplicationManagerBecauseArticMachineIsNotRunning() throws ServerException, IOException, JsonParseException {
        when(machine.getStatus()).thenReturn(CREATING);

        when(event.getCreatedDirectories()).thenReturn(singletonList(PATH_TO_DIRECTORY));
        when(event.getCreatedFiles()).thenReturn(singletonList(PATH_TO_DIRECTORY));

        queueHolder.put(event);
        replicationService.run();

        verify(replicationManager, never()).makeDirectory(MACHINE_ID, PATH_TO_DIRECTORY);
        verify(replicationManager, never()).copy(MACHINE_ID, PATH_TO_DIRECTORY, PATH_TO_DIRECTORY, USE_REPLICATION_ROOT);
    }

    @Test
    public void shouldCallReplicationManagerCopyAndMkdirBecauseFileAndDirectoryAreCreated()
            throws ServerException, IOException, JsonParseException {
        when(event.getCreatedDirectories()).thenReturn(singletonList(PATH_TO_DIRECTORY));
        when(event.getCreatedFiles()).thenReturn(singletonList(PATH_TO_DIRECTORY));

        queueHolder.put(event);
        replicationService.run();

        verify(replicationManager, times(1)).makeDirectory(MACHINE_ID, PATH_TO_DIRECTORY);
        verify(replicationManager, times(1)).copy(MACHINE_ID, PATH_TO_DIRECTORY, PATH_TO_DIRECTORY, USE_REPLICATION_ROOT);
    }

    @Test
    public void shouldCallReplicationManagerCopyAndMkdirBecauseFileAndDirectoryAreModified()
            throws ServerException, IOException, JsonParseException {
        when(event.getModifiedDirectories()).thenReturn(singletonList(PATH_TO_DIRECTORY));
        when(event.getModifiedFiles()).thenReturn(singletonList(PATH_TO_DIRECTORY));

        queueHolder.put(event);
        replicationService.run();

        verify(replicationManager, times(1)).makeDirectory(MACHINE_ID, PATH_TO_DIRECTORY);
        verify(replicationManager, times(1)).copy(MACHINE_ID, PATH_TO_DIRECTORY, PATH_TO_DIRECTORY, USE_REPLICATION_ROOT);
    }

    @Test
    public void shouldCallReplicationManagerCopyAndMkdirTwiceBecauseFileAndDirectoryAreModifiedAndCreated()
            throws ServerException, IOException, JsonParseException {
        when(event.getCreatedDirectories()).thenReturn(singletonList(PATH_TO_DIRECTORY));
        when(event.getCreatedFiles()).thenReturn(singletonList(PATH_TO_DIRECTORY));
        when(event.getModifiedDirectories()).thenReturn(singletonList(PATH_TO_DIRECTORY));
        when(event.getModifiedFiles()).thenReturn(singletonList(PATH_TO_DIRECTORY));

        queueHolder.put(event);
        replicationService.run();

        verify(replicationManager, times(2)).makeDirectory(MACHINE_ID, PATH_TO_DIRECTORY);
        verify(replicationManager, times(2)).copy(MACHINE_ID, PATH_TO_DIRECTORY, PATH_TO_DIRECTORY, USE_REPLICATION_ROOT);
    }
}
