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
import com.google.common.collect.Lists;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.util.CommandLine;
import org.eclipse.che.api.core.util.ListLineConsumer;
import org.slf4j.Logger;

import javax.inject.Singleton;
import java.io.IOException;

import static java.lang.String.format;
import static org.eclipse.che.api.core.util.ProcessUtil.process;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Dmitry Kuleshov
 *
 * @since 4.5
 */
@Beta
@Singleton
public class ShellCommandExecutor {
    private static final Logger LOG = getLogger(ShellCommandExecutor.class);
    private static final int SUCCESS = 0;

    void execute(CommandLine commandLine) throws IOException, ServerException {
        final ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(commandLine.toShellCommand());
        try (ListLineConsumer stdout = new ListLineConsumer(); ListLineConsumer stderr = new ListLineConsumer()) {
            final Process process = processBuilder.start();
            final int status = process.waitFor();
            if (status != SUCCESS) {
                process(process, stdout, stderr);

                if (isHostAddedWarningMessage(stderr.getText())) {
                    execute(commandLine);
                } else {
                    LOG.error("ShellCommand execution failed: {}", commandLine);
                    throw new ServerException(format("ShellCommand execution failed: %s", commandLine));
                }
            } else {
                LOG.debug("ShellCommand execution succeeded: {}", commandLine);
            }
        } catch (InterruptedException e) {
            LOG.error("ShellCommand execution was interrupted: {}", commandLine, e);
            throw new ServerException(format("ShellCommand execution  was interrupted: %s", commandLine), e);
        }
    }

    private boolean isHostAddedWarningMessage(String error) {
        return error.startsWith("Warning: Permanently added") && error.endsWith("to the list of known hosts.");
    }
}
