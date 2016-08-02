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

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.plugin.machine.artik.replication.ApiRequestHelper;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Random;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.System.currentTimeMillis;
import static org.eclipse.che.plugin.machine.artik.replication.shell.JsonValueHelper.HOST;
import static org.eclipse.che.plugin.machine.artik.replication.shell.JsonValueHelper.PASSWORD;
import static org.eclipse.che.plugin.machine.artik.replication.shell.JsonValueHelper.PORT;
import static org.eclipse.che.plugin.machine.artik.replication.shell.JsonValueHelper.REPLICATION_FOLDER;
import static org.eclipse.che.plugin.machine.artik.replication.shell.JsonValueHelper.USERNAME;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;


/**
 * Tests for {@link JsonValueHelper}
 *
 * @author Dmitry Kuleshov
 *
 * @since 4.5
 */
@Listeners(value = {MockitoTestNGListener.class})
public class JsonValueHelperTest {
    private static final String MACHINE_ID = Integer.toString(new Random(currentTimeMillis()).nextInt(MAX_VALUE));
    private static final String JSON       = "{" + "\n" +
                                             "  \"username\" : \"username\"," + "\n" +
                                             "  \"password\" : \"password\"," + "\n" +
                                             "  \"port\" : \"port\"," + "\n" +
                                             "  \"host\" : \"host\"," + "\n" +
                                             "  \"replicationFolder\" : \"replicationFolder\"" + "\n" +
                                             "}";
    @Mock
    private ApiRequestHelper apiRequestHelper;

    private JsonValueHelper jsonValueHelper;

    @BeforeMethod
    public void beforeMethod() throws ApiException, IOException {
        when(apiRequestHelper.getScript(anyString())).thenReturn(JSON);

        jsonValueHelper = new JsonValueHelper(MACHINE_ID, apiRequestHelper);
    }

    @Test
    public void shouldProvideCorrectJsonElementValues() throws ServerException {
        assertNotNull(jsonValueHelper);
        assertEquals(jsonValueHelper.getUsername(), USERNAME);
        assertEquals(jsonValueHelper.getPassword(), PASSWORD);
        assertEquals(jsonValueHelper.getHost(), HOST);
        assertEquals(jsonValueHelper.getPort(), PORT);
        assertEquals(jsonValueHelper.getReplicationRoot(), REPLICATION_FOLDER);

    }
}
