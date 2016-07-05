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

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

/**
 * @author Dmitry Kuleshov
 *
 * @since 4.5
 */
@Beta
class MkdirCommandContext extends ShellCommandContext {
    private final String path;

    MkdirCommandContext(String username,
                        String password,
                        String host,
                        String port,
                        String path) {
        super(username,
              password,
              host,
              port);

        this.path = path;
    }

    MkdirCommandContext(JsonValueHelper jsonValueHelper,
                        String path) {
        this(jsonValueHelper.getUsername(),
             jsonValueHelper.getPassword(),
             jsonValueHelper.getHost(),
             jsonValueHelper.getPort(),
             jsonValueHelper.getReplicationRoot().concat(path));
    }

    public String getPath() {
        return path;
    }
}
