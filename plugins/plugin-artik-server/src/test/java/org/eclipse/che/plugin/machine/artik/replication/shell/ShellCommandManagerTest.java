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

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.util.CommandLine;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;

import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;


/**
 * Tests for {@link ShellCommandManager}
 *
 * @author Dmitry Kuleshov
 *
 * @since 4.5
 */
@Listeners(value = {MockitoTestNGListener.class})
public class ShellCommandManagerTest {
    private static final String USERNAME    = "username";
    private static final String PASSWORD    = "password";
    private static final String HOST        = "host";
    private static final String PORT        = "port";
    private static final String REPLICATION = "replication";
    private final static String SOURCE      = "source";
    private final static String TARGET      = "target";

    private static final String SCP_COMMAND_LINE = format("sshpass -p %s scp -P %s -o StrictHostKeyChecking=no  %s %s@%s:%s%s",
                                                          PASSWORD, PORT, SOURCE, USERNAME, HOST, REPLICATION, TARGET);

    private static final String RSYNC_COMMAND_LINE = format("rsync.sh %s %s %s %s %s", USERNAME, PASSWORD, SOURCE, HOST, REPLICATION);
    @Mock
    private JsonValueHelper      jsonValueHelper;
    @Mock
    private ShellCommandExecutor shellCommandExecutor;
    @InjectMocks
    private ShellCommandManager  shellCommandManager;

    @BeforeMethod
    public void beforeMethod() {
        when(jsonValueHelper.getUsername()).thenReturn(USERNAME);
        when(jsonValueHelper.getPassword()).thenReturn(PASSWORD);
        when(jsonValueHelper.getHost()).thenReturn(HOST);
        when(jsonValueHelper.getPort()).thenReturn(PORT);
        when(jsonValueHelper.getReplicationRoot()).thenReturn(REPLICATION);
    }

    @Test
    public void shouldCallScpCommandOnExecutor() throws IOException, ServerException {
        shellCommandManager.scp(jsonValueHelper, SOURCE, TARGET, TRUE);

        final ArgumentCaptor<CommandLine> captor = forClass(CommandLine.class);

        verify(shellCommandExecutor, times(1)).execute(captor.capture());

        assertEquals(captor.getValue().toString(), SCP_COMMAND_LINE);
    }

    @Test
    public void shouldCallRsyncCommandOnExecutor() throws IOException, ServerException {
        shellCommandManager.rsync(jsonValueHelper, SOURCE);

        final ArgumentCaptor<CommandLine> captor = forClass(CommandLine.class);

        verify(shellCommandExecutor, times(1)).execute(captor.capture());

        assertEquals(captor.getValue().toString(), RSYNC_COMMAND_LINE);
    }
}
