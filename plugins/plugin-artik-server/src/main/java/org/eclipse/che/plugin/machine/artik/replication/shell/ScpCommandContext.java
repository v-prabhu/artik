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
package org.eclipse.che.plugin.machine.artik.replication.shell;

import com.google.common.annotations.Beta;

/**
 * @author Dmitry Kuleshov
 *
 * @since 4.5
 */
@Beta
class ScpCommandContext extends ShellCommandContext {
    private final boolean isDirectory;
    private final String  sourcePath;
    private final String  targetPath;

    ScpCommandContext(String username,
                      String password,
                      String host,
                      String port,
                      boolean isDirectory,
                      String sourcePath,
                      String targetPath) {

        super(username,
              password,
              host,
              port);

        this.isDirectory = isDirectory;
        this.sourcePath = sourcePath;
        this.targetPath = targetPath;
    }

    ScpCommandContext(JsonValueHelper jsonValueHelper,
                      boolean isDirectory,
                      String sourcePath,
                      boolean useReplicationRoot,
                      String targetPath) {

        this(jsonValueHelper.getUsername(),
             jsonValueHelper.getPassword(),
             jsonValueHelper.getHost(),
             jsonValueHelper.getPort(),
             isDirectory,
             sourcePath,
             (useReplicationRoot ? jsonValueHelper.getReplicationRoot() : "").concat(targetPath));
    }

    String getSourcePath() {
        return sourcePath;
    }

    boolean isDirectory() {
        return isDirectory;
    }

    String getTargetPath() {
        return targetPath;
    }
}
