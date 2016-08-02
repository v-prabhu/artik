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
package org.eclipse.che.plugin.machine.artik.replication;

import com.google.common.annotations.Beta;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.commons.schedule.executor.ThreadPullLauncher;
import org.eclipse.che.plugin.machine.artik.replication.shell.JsonValueHelperFactory;
import org.eclipse.che.plugin.machine.artik.replication.shell.ShellCommandManager;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;
import static org.eclipse.che.api.core.model.machine.MachineStatus.RUNNING;
import static org.slf4j.LoggerFactory.getLogger;

/**
 *
 * @author Dmitry Kuleshov
 *
 * @since 4.5
 */
@Beta
@Singleton
public class RsyncService {
    private static final Logger LOG = getLogger(RsyncService.class);

    private static final String DEFAULT_PROJECT_LOCATION = "/projects";
    private static final String ARTIK                    = "artik";

    private final ApiRequestHelper       apiRequestHelper;
    private final ShellCommandManager    shellCommandManager;
    private final JsonValueHelperFactory jsonValueHelperFactory;

    @Inject
    public RsyncService(ApiRequestHelper apiRequestHelper,
                        ShellCommandManager shellCommandManager,
                        JsonValueHelperFactory jsonValueHelperFactory,
                        ThreadPullLauncher launcher) {
        this.apiRequestHelper = apiRequestHelper;
        this.shellCommandManager = shellCommandManager;
        this.jsonValueHelperFactory = jsonValueHelperFactory;

        LOG.info("LAUNCHING RSYNC SERVICE");
        launcher.scheduleWithFixedDelay(this::run, 3, 3, SECONDS);
    }

    private void run() {
        try {
            for (Machine machine : getMachines()) {
                shellCommandManager.rsync(jsonValueHelperFactory.create(machine.getId()), DEFAULT_PROJECT_LOCATION);
            }
        } catch (IOException | ApiException e) {
            LOG.error("Error trying to rsync to a machine files or folders.", e);
        }
    }

    private List<Machine> getMachines() throws ApiException, IOException {
        return apiRequestHelper.getMachines()
                               .stream()
                               .filter(it -> RUNNING.equals(it.getStatus()))
                               .filter(it -> ARTIK.equals(it.getConfig().getType()))
                               .collect(toList());
    }
}
