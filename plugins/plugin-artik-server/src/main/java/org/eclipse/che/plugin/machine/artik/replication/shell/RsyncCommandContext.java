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
class RsyncCommandContext extends ShellCommandContext {
    private final String sourcePath;
    private final String targetPath;

    RsyncCommandContext(String username,
                        String password,
                        String host,
                        String port,
                        String sourcePath,
                        String targetPath) {
        super(username,
              password,
              host,
              port);

        this.sourcePath = sourcePath;
        this.targetPath = targetPath;
    }

    RsyncCommandContext(JsonValueHelper jsonValueHelper,
                        String sourcePath) {
        this(jsonValueHelper.getUsername(),
             jsonValueHelper.getPassword(),
             jsonValueHelper.getHost(),
             jsonValueHelper.getPort(),
             sourcePath,
             jsonValueHelper.getReplicationRoot());
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public String getTargetPath() {
        return targetPath;
    }
}
