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
package org.eclipse.che.plugin.artik.ide.debug;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.plugin.artik.ide.ArtikLocalizationConstant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** @author Artem Zatsarynnyi */
@RunWith(MockitoJUnitRunner.class)
public class DebugBinaryActionTest {

    @Mock
    private ArtikLocalizationConstant localizationConstants;
    @Mock
    private AppContext                appContext;
    @Mock
    private Machine                   machine;
    @Mock
    private DebuggerConnector         debuggerConnector;

    @Mock
    private MachineConfig machineConfig;

    private DebugBinaryAction action;

    @Before
    public void setUp() {
        when(machineConfig.getName()).thenReturn("machine_name");
        when(machine.getConfig()).thenReturn(machineConfig);

        action = new DebugBinaryAction(localizationConstants, appContext, machine, debuggerConnector);
    }

    @Test
    public void shouldSetTitle() throws Exception {
        verify(machine).getConfig();
        verify(machineConfig).getName();
        verify(localizationConstants).debugActionDescription();
    }

    @Test
    public void shouldCallConnector() throws Exception {
        action.actionPerformed(null);

        verify(debuggerConnector).debug(eq(machine));
    }
}
