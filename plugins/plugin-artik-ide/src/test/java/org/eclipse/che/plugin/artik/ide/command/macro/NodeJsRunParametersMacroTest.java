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
package org.eclipse.che.plugin.artik.ide.command.macro;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import static org.eclipse.che.plugin.nodejs.shared.Constants.RUN_PARAMETERS_ATTRIBUTE;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class NodeJsRunParametersMacroTest {
    @InjectMocks
    private NodeJsRunParametersMacro macro;

    @Test
    public void attributeNameShouldBeReturned() throws Exception {
        assertEquals(RUN_PARAMETERS_ATTRIBUTE, macro.getAttribute());
    }

    @Test
    public void macroDefaultValueNameShouldBeReturned() throws Exception {
        assertEquals("", macro.getDefaultValue());
    }

    @Test
    public void macroNameShouldBeReturned() throws Exception {
        assertEquals("${node.run.parameters}", macro.getName());
    }

    @Test
    public void macroDescriptionShouldBeReturned() throws Exception {
        assertEquals("NodeJs Run Parameters", macro.getDescription());
    }
}
