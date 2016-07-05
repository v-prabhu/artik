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

import com.google.common.annotations.Beta;
import com.google.inject.Inject;

import org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType;
import org.eclipse.che.api.vfs.impl.file.event.HiEvent;
import org.eclipse.che.api.vfs.impl.file.event.HiEventBroadcaster;
import org.eclipse.che.api.vfs.impl.file.event.HiEventDetector;
import org.eclipse.che.api.vfs.impl.file.event.HiEventServerPublisher;
import org.eclipse.che.api.vfs.impl.file.event.LoEvent.ItemType;
import org.eclipse.che.api.vfs.impl.file.event.EventTreeNode;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.plugin.artik.shared.dto.ArtikVfsModificationEventDto;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.*;
import static org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType.CREATED;
import static org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType.DELETED;
import static org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType.MODIFIED;
import static org.eclipse.che.api.vfs.impl.file.event.HiEvent.Category.UNDEFINED;
import static org.eclipse.che.api.vfs.impl.file.event.LoEvent.ItemType.FILE;
import static org.eclipse.che.api.vfs.impl.file.event.LoEvent.ItemType.DIR;

/**
 * Detects if artik related events happens. Tracks file and folder modifications and if yes
 * generates a DTO object to store all modified items paths. Uses {@link HiEventServerPublisher}
 * to broadcast an event as {@link ArtikVfsModificationEventDto} instance.
 *
 * @author Dmitry Kuleshov
 *
 * @since 4.5
 */
@Beta
public class ArtikVfsModificationEventDetector implements HiEventDetector<ArtikVfsModificationEventDto> {

    private final HiEventBroadcaster broadcaster;

    @Inject
    public ArtikVfsModificationEventDetector(HiEventServerPublisher broadcaster) {
        this.broadcaster = broadcaster;
    }

    @Override
    public Optional<HiEvent<ArtikVfsModificationEventDto>> detect(EventTreeNode loVfsEventTreeNode) {
        return Optional.of(HiEvent.newInstance(ArtikVfsModificationEventDto.class)
                                                   .withCategory(UNDEFINED)
                                                   .withBroadcaster(broadcaster)
                                                   .withDto(DtoHelper.newDto(loVfsEventTreeNode)));
    }

    private static class DtoHelper {

        private final EventTreeNode loVfsEventTreeNode;

        private DtoHelper(EventTreeNode loVfsEventTreeNode) {
            this.loVfsEventTreeNode = loVfsEventTreeNode;
        }

        private static ArtikVfsModificationEventDto newDto(EventTreeNode eventTreeNode) {
            return new DtoHelper(eventTreeNode).getDto();
        }

        private ArtikVfsModificationEventDto getDto() {
            return DtoFactory.newDto(ArtikVfsModificationEventDto.class)
                             .withCreatedDirectories(getListOfFoldersOf(CREATED))
                             .withModifiedDirectories(getListOfFoldersOf(MODIFIED))
                             .withRemovedDirectories(getListOfFoldersOf(DELETED))
                             .withCreatedFiles(getListOfFilesOf(CREATED))
                             .withModifiedFiles(getListOfFilesOf(MODIFIED))
                             .withRemovedFiles(getListOfFilesOf(DELETED));
        }

        private List<String> getListOfFilesOf(FileWatcherEventType eventType) {
            return getListOfItemsOfEventType(FILE, eventType);
        }

        private List<String> getListOfFoldersOf(FileWatcherEventType eventType) {
            return getListOfItemsOfEventType(DIR, eventType);
        }

        private List<String> getListOfItemsOfEventType(ItemType itemType, FileWatcherEventType eventType) {
            return loVfsEventTreeNode.stream()
                                     .filter(EventTreeNode::modificationOccurred)
                                     .filter(o -> itemType.equals(o.getType()))
                                     .filter(o -> eventType.equals(o.getLastEventType()))
                                     .map(EventTreeNode::getPath)
                                     .collect(toList());
        }
    }
}
