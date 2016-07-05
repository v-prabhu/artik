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

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Dmitry Kuleshov
 *
 * @since 4.5
 */
@Beta
class ShellCommandContext {
    private final String username;
    private final String password;
    private final String host;
    private final String port;

    ShellCommandContext(String username, String password, String host, String port) {
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = port;
    }

    String getUsername() {
        return username;
    }

    String getPassword() {
        return password;
    }

    String getHost() {
        return host;
    }

    String getPort() {
        return port;
    }
}
