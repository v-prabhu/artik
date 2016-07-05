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
package org.eclipse.che.plugin.artik.shared.dto;

import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * @author Dmitry Kuleshov
 *
 * @since 4.5
 */
@DTO
public interface ArtikVfsModificationEventDto {
    /**
     * @return a list of created folders paths
     */
    List<String> getCreatedDirectories();

    /**
     * @return a list of modified folders paths
     */
    List<String> getModifiedDirectories();

    /**
     * @return a list of removed folders paths
     */
    List<String> getRemovedDirectories();

    /**
     * @return a list of created files paths
     */
    List<String> getCreatedFiles();

    /**
     * @return a list of modified files paths
     */
    List<String> getModifiedFiles();

    /**
     * @return a list of removed files paths
     */
    List<String> getRemovedFiles();

    /**
     * @param created a list of created folders paths
     */
    ArtikVfsModificationEventDto withCreatedDirectories(List<String> created);

    /**
     *
     * @param modified list of modified folders paths
     */
    ArtikVfsModificationEventDto withModifiedDirectories(List<String> modified);

    /**
     *
     * @param removed list of removed folders paths
     */
    ArtikVfsModificationEventDto withRemovedDirectories(List<String> removed);

    /**
     *
     * @param created list of created files paths
     */
    ArtikVfsModificationEventDto withCreatedFiles(List<String> created);

    /**
     *
     * @param modified list of modified files paths
     */
    ArtikVfsModificationEventDto withModifiedFiles(List<String> modified);

    /**
     *
     * @param removed list of removed files paths
     */
    ArtikVfsModificationEventDto withRemovedFiles(List<String> removed);
}
