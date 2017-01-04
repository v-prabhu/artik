/*******************************************************************************
 * Copyright (c) 2016-2017 Samsung Electronics Co., Ltd.
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


import org.eclipse.che.api.core.model.machine.ServerConf;
import org.eclipse.che.api.machine.server.model.impl.ServerConfImpl;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.net.URI;

/**
 * Provides server conf that describes websocket terminal server
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class TerminalServerConfProvider implements Provider<ServerConf> {
    public static final String TERMINAL_SERVER_REFERENCE = "terminal";

    @Inject
    @Named("che.api")
    private URI apiEndpoint;

    @Override
    public ServerConf get() {
        return new ServerConfImpl(TERMINAL_SERVER_REFERENCE, "4411/tcp", apiEndpoint.getScheme(), null);
    }
}
