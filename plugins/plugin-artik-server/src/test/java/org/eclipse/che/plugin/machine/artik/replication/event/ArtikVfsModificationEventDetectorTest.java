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
package org.eclipse.che.plugin.machine.artik.replication.event;

import org.eclipse.che.api.vfs.impl.file.event.EventTreeNode;
import org.eclipse.che.api.vfs.impl.file.event.HiEvent;
import org.eclipse.che.api.vfs.impl.file.event.HiEventBroadcaster;
import org.eclipse.che.api.vfs.impl.file.event.LoEvent;
import org.eclipse.che.plugin.artik.shared.dto.ArtikVfsModificationEventDto;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType.CREATED;
import static org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType.DELETED;
import static org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType.MODIFIED;
import static org.eclipse.che.api.vfs.impl.file.event.EventTreeHelper.addEventAndCreatePrecedingNodes;
import static org.eclipse.che.api.vfs.impl.file.event.EventTreeNode.newRootInstance;
import static org.eclipse.che.api.vfs.impl.file.event.LoEvent.ItemType.DIR;
import static org.eclipse.che.api.vfs.impl.file.event.LoEvent.ItemType.FILE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;


/**
 * Tests for {@link ArtikVfsModificationEventDetector}
 *
 * @author Dmitry Kuleshov
 *
 * @since 4.5
 */
@Listeners(value = {MockitoTestNGListener.class})
public class ArtikVfsModificationEventDetectorTest {
    private static final String CREATED_DIR_NAME   = "createdDir";
    private static final String CREATED_DIR_PATH   = "/createdDir";
    private static final String MODIFIED_DIR_NAME  = "modifiedDir";
    private static final String MODIFIED_DIR_PATH  = "/modifiedDir";
    private static final String REMOVED_DIR_NAME   = "removedDir";
    private static final String REMOVED_DIR_PATH   = "/removedDir";
    private static final String CREATED_FILE_NAME  = "createdFile";
    private static final String CREATED_FILE_PATH  = "/createdFile";
    private static final String MODIFIED_FILE_NAME = "modifiedFile";
    private static final String MODIFIED_FILE_PATH = "/modifiedFile";
    private static final String REMOVED_FILE_NAME  = "removedFile";
    private static final String REMOVED_FILE_PATH  = "/removedFile";

    @Mock
    private HiEventBroadcaster                broadcaster;
    @InjectMocks
    private ArtikVfsModificationEventDetector detector;

    private EventTreeNode root;

    @BeforeMethod
    public void beforeMethod() {
        root = newRootInstance();
    }


    @Test
    public void shouldDetectCreatedDirEvent() {
        addEventAndCreatePrecedingNodes(root, LoEvent.newInstance()
                                                     .withName(CREATED_DIR_NAME)
                                                     .withItemType(DIR)
                                                     .withPath(CREATED_DIR_PATH)
                                                     .withEventType(CREATED)
                                                     .withTime(System.currentTimeMillis()));


        final Optional<HiEvent<ArtikVfsModificationEventDto>> eventOptional = detector.detect(root);
        assertTrue(eventOptional.isPresent());
        final ArtikVfsModificationEventDto eventDto = eventOptional.get().getDto();

        assertEquals(eventDto.getCreatedDirectories().size(), 1);
        assertTrue(eventDto.getModifiedDirectories().isEmpty());
        assertTrue(eventDto.getRemovedDirectories().isEmpty());
        assertTrue(eventDto.getCreatedFiles().isEmpty());
        assertTrue(eventDto.getModifiedFiles().isEmpty());
        assertTrue(eventDto.getRemovedFiles().isEmpty());

        assertEquals(eventDto.getCreatedDirectories().get(0), CREATED_DIR_PATH);
    }

    @Test
    public void shouldDetectModifiedDirEvent() {
        addEventAndCreatePrecedingNodes(root, LoEvent.newInstance()
                                                     .withName(MODIFIED_DIR_NAME)
                                                     .withItemType(DIR)
                                                     .withPath(MODIFIED_DIR_PATH)
                                                     .withEventType(MODIFIED)
                                                     .withTime(System.currentTimeMillis()));

        final Optional<HiEvent<ArtikVfsModificationEventDto>> eventOptional = detector.detect(root);
        assertTrue(eventOptional.isPresent());
        final ArtikVfsModificationEventDto eventDto = eventOptional.get().getDto();

        assertEquals(eventDto.getModifiedDirectories().size(), 1);
        assertTrue(eventDto.getCreatedDirectories().isEmpty());
        assertTrue(eventDto.getRemovedDirectories().isEmpty());
        assertTrue(eventDto.getCreatedFiles().isEmpty());
        assertTrue(eventDto.getModifiedFiles().isEmpty());
        assertTrue(eventDto.getRemovedFiles().isEmpty());

        assertEquals(eventDto.getModifiedDirectories().get(0), MODIFIED_DIR_PATH);
    }

    @Test
    public void shouldDetectRemovedDirEvent() {
        addEventAndCreatePrecedingNodes(root, LoEvent.newInstance()
                                                     .withName(REMOVED_DIR_NAME)
                                                     .withItemType(DIR)
                                                     .withPath(REMOVED_DIR_PATH)
                                                     .withEventType(DELETED)
                                                     .withTime(System.currentTimeMillis()));

        final Optional<HiEvent<ArtikVfsModificationEventDto>> eventOptional = detector.detect(root);
        assertTrue(eventOptional.isPresent());
        final ArtikVfsModificationEventDto eventDto = eventOptional.get().getDto();

        assertEquals(eventDto.getRemovedDirectories().size(), 1);
        assertTrue(eventDto.getCreatedDirectories().isEmpty());
        assertTrue(eventDto.getModifiedDirectories().isEmpty());
        assertTrue(eventDto.getCreatedFiles().isEmpty());
        assertTrue(eventDto.getModifiedFiles().isEmpty());
        assertTrue(eventDto.getRemovedFiles().isEmpty());

        assertEquals(eventDto.getRemovedDirectories().get(0), REMOVED_DIR_PATH);
    }

    @Test
    public void shouldDetectCreatedFileEvent() {
        addEventAndCreatePrecedingNodes(root, LoEvent.newInstance()
                                                     .withName(CREATED_FILE_NAME)
                                                     .withItemType(FILE)
                                                     .withPath(CREATED_FILE_PATH)
                                                     .withEventType(CREATED)
                                                     .withTime(System.currentTimeMillis()));

        final Optional<HiEvent<ArtikVfsModificationEventDto>> eventOptional = detector.detect(root);
        assertTrue(eventOptional.isPresent());
        final ArtikVfsModificationEventDto eventDto = eventOptional.get().getDto();

        assertEquals(eventDto.getCreatedFiles().size(), 1);
        assertTrue(eventDto.getCreatedDirectories().isEmpty());
        assertTrue(eventDto.getModifiedDirectories().isEmpty());
        assertTrue(eventDto.getRemovedDirectories().isEmpty());
        assertTrue(eventDto.getModifiedFiles().isEmpty());
        assertTrue(eventDto.getRemovedFiles().isEmpty());

        assertEquals(eventDto.getCreatedFiles().get(0), CREATED_FILE_PATH);
    }

    @Test
    public void shouldDetectModifiedFileEvent() {
        addEventAndCreatePrecedingNodes(root, LoEvent.newInstance()
                                                     .withName(MODIFIED_FILE_NAME)
                                                     .withItemType(FILE)
                                                     .withPath(MODIFIED_FILE_PATH)
                                                     .withEventType(MODIFIED)
                                                     .withTime(System.currentTimeMillis()));

        final Optional<HiEvent<ArtikVfsModificationEventDto>> eventOptional = detector.detect(root);
        assertTrue(eventOptional.isPresent());
        final ArtikVfsModificationEventDto eventDto = eventOptional.get().getDto();


        assertEquals(eventDto.getModifiedFiles().size(), 1);
        assertTrue(eventDto.getCreatedDirectories().isEmpty());
        assertTrue(eventDto.getModifiedDirectories().isEmpty());
        assertTrue(eventDto.getRemovedDirectories().isEmpty());
        assertTrue(eventDto.getCreatedFiles().isEmpty());
        assertTrue(eventDto.getRemovedFiles().isEmpty());

        assertEquals(eventDto.getModifiedFiles().get(0), MODIFIED_FILE_PATH);
    }

    @Test
    public void shouldDetectRemovedFileEvent() {
        addEventAndCreatePrecedingNodes(root, LoEvent.newInstance()
                                                     .withName(REMOVED_FILE_NAME)
                                                     .withItemType(FILE)
                                                     .withPath(REMOVED_FILE_PATH)
                                                     .withEventType(DELETED)
                                                     .withTime(System.currentTimeMillis()));

        final Optional<HiEvent<ArtikVfsModificationEventDto>> eventOptional = detector.detect(root);
        assertTrue(eventOptional.isPresent());
        final ArtikVfsModificationEventDto eventDto = eventOptional.get().getDto();


        assertEquals(eventDto.getRemovedFiles().size(), 1);
        assertTrue(eventDto.getCreatedDirectories().isEmpty());
        assertTrue(eventDto.getModifiedDirectories().isEmpty());
        assertTrue(eventDto.getRemovedDirectories().isEmpty());
        assertTrue(eventDto.getCreatedFiles().isEmpty());
        assertTrue(eventDto.getModifiedFiles().isEmpty());

        assertEquals(eventDto.getRemovedFiles().get(0), REMOVED_FILE_PATH);
    }

    @Test
    public void shouldDetectAllEvents() {
        LoEvent createdDir = LoEvent.newInstance()
                                    .withName(CREATED_DIR_NAME)
                                    .withItemType(DIR)
                                    .withPath(CREATED_DIR_PATH)
                                    .withEventType(CREATED)
                                    .withTime(System.currentTimeMillis());

        LoEvent modifiedDir = LoEvent.newInstance()
                                     .withName(MODIFIED_DIR_NAME)
                                     .withItemType(DIR)
                                     .withPath(MODIFIED_DIR_PATH)
                                     .withEventType(MODIFIED)
                                     .withTime(System.currentTimeMillis());

        LoEvent removedDir = LoEvent.newInstance()
                                    .withName(REMOVED_DIR_NAME)
                                    .withItemType(DIR)
                                    .withPath(REMOVED_DIR_PATH)
                                    .withEventType(DELETED)
                                    .withTime(System.currentTimeMillis());

        LoEvent createdFile = LoEvent.newInstance()
                                     .withName(CREATED_FILE_NAME)
                                     .withItemType(FILE)
                                     .withPath(CREATED_FILE_PATH)
                                     .withEventType(CREATED)
                                     .withTime(System.currentTimeMillis());

        LoEvent modifiedFile = LoEvent.newInstance()
                                      .withName(MODIFIED_FILE_NAME)
                                      .withItemType(FILE)
                                      .withPath(MODIFIED_FILE_PATH)
                                      .withEventType(MODIFIED)
                                      .withTime(System.currentTimeMillis());

        LoEvent removedFile = LoEvent.newInstance()
                                     .withName(REMOVED_FILE_NAME)
                                     .withItemType(FILE)
                                     .withPath(REMOVED_FILE_PATH)
                                     .withEventType(DELETED)
                                     .withTime(System.currentTimeMillis());

        addEventAndCreatePrecedingNodes(root, createdDir);
        addEventAndCreatePrecedingNodes(root, modifiedDir);
        addEventAndCreatePrecedingNodes(root, removedDir);
        addEventAndCreatePrecedingNodes(root, createdFile);
        addEventAndCreatePrecedingNodes(root, modifiedFile);
        addEventAndCreatePrecedingNodes(root, removedFile);


        final Optional<HiEvent<ArtikVfsModificationEventDto>> eventOptional = detector.detect(root);
        assertTrue(eventOptional.isPresent());
        final ArtikVfsModificationEventDto eventDto = eventOptional.get().getDto();


        assertEquals(eventDto.getCreatedDirectories().size(), 1);
        assertEquals(eventDto.getModifiedDirectories().size(), 1);
        assertEquals(eventDto.getRemovedDirectories().size(), 1);
        assertEquals(eventDto.getCreatedFiles().size(), 1);
        assertEquals(eventDto.getModifiedFiles().size(), 1);
        assertEquals(eventDto.getRemovedFiles().size(), 1);

        assertEquals(eventDto.getCreatedDirectories().get(0), CREATED_DIR_PATH);
        assertEquals(eventDto.getModifiedDirectories().get(0), MODIFIED_DIR_PATH);
        assertEquals(eventDto.getRemovedDirectories().get(0), REMOVED_DIR_PATH);
        assertEquals(eventDto.getCreatedFiles().get(0), CREATED_FILE_PATH);
        assertEquals(eventDto.getModifiedFiles().get(0), MODIFIED_FILE_PATH);
        assertEquals(eventDto.getRemovedFiles().get(0), REMOVED_FILE_PATH);
    }

}
