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
import org.everrest.assured.EverrestJetty;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Random;

import static com.jayway.restassured.RestAssured.with;
import static java.lang.Integer.MAX_VALUE;
import static java.lang.System.currentTimeMillis;
import static javax.ws.rs.core.Response.Status.OK;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


/**
 * Tests for {@link PushToDeviceService}
 *
 * @author Dmitry Kuleshov
 *
 * @since 4.5
 */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class PushToDeviceServiceTest {

    private static final String WS_PATH_PARAM_NAME  = "ws-id";
    private static final String WS_PATH_PARAM_VALUE = "ws".concat(Integer.toString(new Random(currentTimeMillis()).nextInt(MAX_VALUE)));
    private static final String SCP_PATH            = "/scp/{" + WS_PATH_PARAM_NAME + "}";

    private static final String MACHINE_ID_PARAM_NAME  = "machine_id";
    private static final String SOURCE_PATH_PARAM_NAME = "source_file";
    private static final String TARGET_PATH_PARAM_NAME = "target_path";

    private static final String TARGET_PATH_PARAM_VALUE = "target_path_value";
    private static final String SOURCE_PATH_PARAM_VALUE = "source_path_value";
    private static final String MACHINE_ID_PARAM_VALUE  = "machine_id_value";
    @Mock
    private ReplicationManager replicationManager;

    @InjectMocks
    private PushToDeviceService pushToDeviceService;

    @Test
    public void shouldRespondWithOkStatus() throws ServerException, IOException, JsonParseException {
        with()
                .queryParameter(MACHINE_ID_PARAM_NAME, MACHINE_ID_PARAM_VALUE)
                .queryParameter(SOURCE_PATH_PARAM_NAME, SOURCE_PATH_PARAM_VALUE)
                .queryParameter(TARGET_PATH_PARAM_NAME, TARGET_PATH_PARAM_VALUE)
                .pathParameter(WS_PATH_PARAM_NAME, WS_PATH_PARAM_VALUE)

                .expect().statusCode(OK.getStatusCode())

                .when().post(SCP_PATH);
    }

    @Test
    public void shouldCallPtdManagerAndRespondWithOkStatus() throws ServerException, IOException, JsonParseException {
        with()
                .queryParameter(MACHINE_ID_PARAM_NAME, MACHINE_ID_PARAM_VALUE)
                .queryParameter(SOURCE_PATH_PARAM_NAME, SOURCE_PATH_PARAM_VALUE)
                .queryParameter(TARGET_PATH_PARAM_NAME, TARGET_PATH_PARAM_VALUE)
                .pathParameter(WS_PATH_PARAM_NAME, WS_PATH_PARAM_VALUE)

                .post(SCP_PATH);


        verify(replicationManager, times(1))
                .copy(MACHINE_ID_PARAM_VALUE, SOURCE_PATH_PARAM_VALUE, TARGET_PATH_PARAM_VALUE + "/" + SOURCE_PATH_PARAM_VALUE);
    }
}
