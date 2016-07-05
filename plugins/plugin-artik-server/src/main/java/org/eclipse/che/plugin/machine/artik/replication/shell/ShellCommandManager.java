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

import static java.nio.file.Files.isDirectory;
import static java.nio.file.Paths.get;
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
        final Path path = get(targetPath);
        final boolean isDirectory = isDirectory(path);

        final ScpCommandContext context = new ScpCommandContext(jsonValueHelper,
                                                                isDirectory,
                                                                sourcePath,
                                                                useReplicationRoot,
                                                                targetPath);

        shellCommandExecutor.execute(buildCommandWithContext(context));

    }

    public void mkdir(JsonValueHelper jsonValueHelper, String path) throws IOException, ServerException {
        final MkdirCommandContext context = new MkdirCommandContext(jsonValueHelper, path);
        shellCommandExecutor.execute(buildCommandWithContext(context));
    }
}
