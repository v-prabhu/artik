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
package org.eclipse.che.plugin.artik.ide.command;

import org.eclipse.che.api.core.model.machine.Machine;
import org.junit.Before;
import org.junit.Test;

import static org.eclipse.che.plugin.artik.ide.command.CompileCommandProducer.COMMAND_NAME;
import static org.eclipse.che.plugin.artik.ide.command.CompileCommandProducer.COMMAND_TEMPLATE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link CompileCommandProducer}.
 *
 * @author Artem Zatsarynnyi
 */
public class CompileCommandProducerTest extends BaseArtikProducerTest {

    private CompileCommandProducer producer;

    @Before
    public void setUp() {
        super.setUp();

        producer = new CompileCommandProducer(customCommandType, dtoFactory, appContext);
    }

    @Test
    public void testGetName() throws Exception {
        assertEquals(COMMAND_NAME, producer.getName());
    }

    @Test
    public void shouldBeApplicableWhenC_FileIsSelected() throws Exception {
        when(currentProject.isTypeOf(eq("c"))).thenReturn(true);
        when(currentResource.isFile()).thenReturn(true);
        when(currentResource.getExtension()).thenReturn("c");

        assertTrue(producer.isApplicable());
    }

    @Test
    public void shouldNotBeApplicableWhenNonC_FileIsSelected() throws Exception {
        when(currentProject.isTypeOf(eq("c"))).thenReturn(true);
        when(currentResource.isFile()).thenReturn(true);
        when(currentResource.getExtension()).thenReturn("h");

        assertFalse(producer.isApplicable());
    }

    @Test
    public void testGetCommand() throws Exception {
        assertEquals(COMMAND_TEMPLATE, producer.getCommandLine(mock(Machine.class)));
    }
}
