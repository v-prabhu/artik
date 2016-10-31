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
package org.eclipse.che.plugin.machine.artik;

import com.google.inject.Singleton;

import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.workspace.server.WorkspaceFilesCleaner;

import java.io.IOException;

/**
 * Empty implementation of the {@link WorkspaceFilesCleaner}.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class EmptyWorkspaceFilesCleaner implements WorkspaceFilesCleaner {
    @Override
    public void clear(Workspace workspace) throws IOException {
    }
}
