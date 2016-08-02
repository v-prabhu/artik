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

import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static org.eclipse.che.plugin.machine.artik.replication.shell.CommandBuilder.buildCommandWithContext;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;


/**
 * Tests for {@link CommandBuilder}
 *
 * @author Dmitry Kuleshov
 *
 * @since 4.5
 */
@Listeners(value = {MockitoTestNGListener.class})
public class CommandBuilderTest {
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String HOST     = "host";
    private static final String PORT     = "port";
    private static final String SOURCE   = "source";
    private static final String TARGET   = "target";

    private static final String SCP_COMMAND_LINE   = format("sshpass -p %s scp -P %s -o StrictHostKeyChecking=no -r %s %s@%s:%s",
                                                            PASSWORD, PORT, SOURCE, USERNAME, HOST, TARGET);

    private static final String RSYNC_COMMAND_LINE   = format("rsync.sh %s %s %s %s %s", USERNAME, PASSWORD, SOURCE, HOST, TARGET);

    @Mock
    private ScpCommandContext   scpCommandContext;
    @Mock
    private RsyncCommandContext   rsyncCommandContext;

    @Test
    public void shouldBuildProperScpCommand() {
        when(scpCommandContext.getUsername()).thenReturn(USERNAME);
        when(scpCommandContext.getPassword()).thenReturn(PASSWORD);
        when(scpCommandContext.getHost()).thenReturn(HOST);
        when(scpCommandContext.getPort()).thenReturn(PORT);
        when(scpCommandContext.getSourcePath()).thenReturn(SOURCE);
        when(scpCommandContext.getTargetPath()).thenReturn(TARGET);
        when(scpCommandContext.isDirectory()).thenReturn(TRUE);

        assertEquals(buildCommandWithContext(scpCommandContext).toString(), SCP_COMMAND_LINE);
    }

    @Test
    public void shouldBuildProperRsyncCommand() {
        when(rsyncCommandContext.getUsername()).thenReturn(USERNAME);
        when(rsyncCommandContext.getPassword()).thenReturn(PASSWORD);
        when(rsyncCommandContext.getHost()).thenReturn(HOST);
        when(rsyncCommandContext.getPort()).thenReturn(PORT);
        when(rsyncCommandContext.getSourcePath()).thenReturn(SOURCE);
        when(rsyncCommandContext.getTargetPath()).thenReturn(TARGET);

        assertEquals(buildCommandWithContext(rsyncCommandContext).toString(), RSYNC_COMMAND_LINE);
    }
}
