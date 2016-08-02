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
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.commons.json.JsonParseException;
import org.eclipse.che.plugin.machine.artik.replication.ApiRequestHelper;
import org.everrest.core.impl.provider.json.JsonValue;
import org.slf4j.Logger;

import java.io.IOException;

import static org.eclipse.che.commons.json.JsonHelper.parseJson;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Helps getting host, port, username, password and synchornization folder data
 * out of a machine's script represented as json entity.
 *
 * @author Dmitry Kuleshov
 *
 * @since 4.5
 */
@Beta
public class JsonValueHelper {
    static final String REPLICATION_FOLDER = "replicationFolder";
    static final String HOST               = "host";
    static final String PORT               = "port";
    static final String USERNAME           = "username";
    static final String PASSWORD           = "password";

    private static final Logger LOG = getLogger(ShellCommandExecutor.class);

    private final JsonValue jsonValue;

    @Inject
    public JsonValueHelper(@Assisted String machineId, ApiRequestHelper apiRequestHelper) throws ServerException {
        try {
            final String script = apiRequestHelper.getScript(machineId);
            jsonValue = parseJson(script);
        } catch (IOException | ApiException | JsonParseException e) {
            LOG.error("Something went wrong when we tried to get or parse machine's script: {}", e.getMessage(), e);
            throw new ServerException(e.getMessage());
        }

    }

    String getReplicationRoot() {
        return jsonValue.getElement(REPLICATION_FOLDER).getStringValue();
    }

    String getHost() {
        return jsonValue.getElement(HOST).getStringValue();
    }

    String getPort() {
        return jsonValue.getElement(PORT).getStringValue();
    }

    String getUsername() {
        return jsonValue.getElement(USERNAME).getStringValue();
    }

    String getPassword() {
        return jsonValue.getElement(PASSWORD).getStringValue();
    }
}
