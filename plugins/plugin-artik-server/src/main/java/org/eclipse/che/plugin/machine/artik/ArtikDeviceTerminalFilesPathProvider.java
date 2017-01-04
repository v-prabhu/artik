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

import org.eclipse.che.api.agent.server.terminal.WebsocketTerminalFilesPathProvider;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.inject.ConfigurationProperties;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

/**
 * Provides path to websocket terminal archive.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class ArtikDeviceTerminalFilesPathProvider extends WebsocketTerminalFilesPathProvider {
    private static final String CONFIGURATION_PREFIX         = "artik.device.terminal.path_to_archive.";
    private static final String CONFIGURATION_PREFIX_PATTERN = "artik\\.device\\.terminal\\.path_to_archive\\..+";

    private Map<String, String> archivesPaths;

    @Inject
    public ArtikDeviceTerminalFilesPathProvider(ConfigurationProperties configurationProperties) {
        super(configurationProperties);
        archivesPaths = configurationProperties.getProperties(CONFIGURATION_PREFIX_PATTERN)
                                               .entrySet()
                                               .stream()
                                               .collect(toMap(entry -> entry.getKey().replaceFirst(CONFIGURATION_PREFIX, ""),
                                                              Map.Entry::getValue));
    }

    @Nullable
    @Override
    public String getPath(String architecture) {
        return archivesPaths.get(architecture);
    }
}
