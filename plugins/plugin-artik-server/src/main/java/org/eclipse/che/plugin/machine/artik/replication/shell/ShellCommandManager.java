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

import org.eclipse.che.api.core.ServerException;

import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.Files.isDirectory;
import static org.eclipse.che.plugin.machine.artik.replication.shell.CommandBuilder.buildCommandWithContext;

/**
 * @author Dmitry Kuleshov
 *
 * @since 4.5
 */
@Beta
@Singleton
public class ShellCommandManager {
    private final ShellCommandExecutor shellCommandExecutor;

    @Inject
    public ShellCommandManager(ShellCommandExecutor shellCommandExecutor, JsonValueHelperFactory jsonValueHelperFactory) {
        this.shellCommandExecutor = shellCommandExecutor;
    }

    public void scp(JsonValueHelper jsonValueHelper, String sourcePath, String targetPath, boolean useReplicationRoot)
            throws IOException, ServerException {
        final Path path = Paths.get(sourcePath);
        final boolean isDirectory = isDirectory(path);

        final ScpCommandContext context = new ScpCommandContext(jsonValueHelper,
                                                                isDirectory,
                                                                sourcePath,
                                                                useReplicationRoot,
                                                                targetPath);

        shellCommandExecutor.execute(buildCommandWithContext(context));

    }

    public void rsync(JsonValueHelper jsonValueHelper, String from) throws IOException, ServerException {
        final RsyncCommandContext context = new RsyncCommandContext(jsonValueHelper, from);
        shellCommandExecutor.execute(buildCommandWithContext(context));
    }
}
