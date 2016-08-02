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

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.commons.json.JsonParseException;
import org.eclipse.che.plugin.machine.artik.replication.shell.JsonValueHelper;
import org.eclipse.che.plugin.machine.artik.replication.shell.JsonValueHelperFactory;
import org.eclipse.che.plugin.machine.artik.replication.shell.ShellCommandManager;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Random;

import static java.io.File.separator;
import static java.lang.Integer.MAX_VALUE;
import static java.lang.System.currentTimeMillis;
import static org.eclipse.che.plugin.machine.artik.replication.ReplicationManager.DEFAULT_PROJECT_LOCATION;
import static org.eclipse.che.plugin.machine.artik.replication.ReplicationManager.USE_REPLICATION_ROOT;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ReplicationManager}
 *
 * @author Dmitry Kuleshov
 *
 * @since 4.5
 */
@Listeners(value = {MockitoTestNGListener.class})
public class ReplicationManagerTest {
    private static final String MACHINE_ID = Integer.toString(new Random(currentTimeMillis()).nextInt(MAX_VALUE));

    private static final String PATH_TO_DIRECTORY = separator + "path" + separator + "to" + separator + "directory";

    @Mock
    private ShellCommandManager    shellCommandManager;
    @Mock
    private JsonValueHelperFactory jsonValueHelperFactory;
    @InjectMocks
    private ReplicationManager     replicationManager;

    @Test
    public void shouldCallShellCommandManagerScpOnCopyWithUseReplicationRoot() throws ServerException, IOException, JsonParseException {
        when(jsonValueHelperFactory.create(any(String.class))).thenReturn(mock(JsonValueHelper.class));

        replicationManager.copy(MACHINE_ID, PATH_TO_DIRECTORY, PATH_TO_DIRECTORY, USE_REPLICATION_ROOT);

        verify(shellCommandManager, times(1))
                .scp(any(JsonValueHelper.class),
                     eq(DEFAULT_PROJECT_LOCATION.concat(PATH_TO_DIRECTORY)),
                     eq(PATH_TO_DIRECTORY),
                     eq(USE_REPLICATION_ROOT));
    }

    @Test
    public void shouldCallShellCommandManagerScpOnCopy() throws ServerException, IOException, JsonParseException {
        when(jsonValueHelperFactory.create(any(String.class))).thenReturn(mock(JsonValueHelper.class));

        replicationManager.copy(MACHINE_ID, PATH_TO_DIRECTORY, PATH_TO_DIRECTORY);

        verify(shellCommandManager, times(1))
                .scp(any(JsonValueHelper.class),
                     eq(DEFAULT_PROJECT_LOCATION.concat(PATH_TO_DIRECTORY)),
                     eq(PATH_TO_DIRECTORY),
                     eq(!USE_REPLICATION_ROOT));
    }
}
