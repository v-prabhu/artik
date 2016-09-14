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
package org.eclipse.che.plugin.machine.artik.oauth;

import org.mockito.Mockito;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

/**
 * Tests for {@link ArtikOAuthAuthenticationService}
 *
 * @author Dmitry Kuleshov
 */
public class ArtikOAuthAuthenticationServiceTest {

    private static final String VALUE_1 = "value1";
    private static final String VALUE_2 = "value2";
    private static final String PARAMETER_1 = "parameter1";
    private static final String PARAMETER_2 = "parameter2";
    private static final String STATE_PARAMETER_SEPARATOR = "$";

    private ArtikOAuthAuthenticationService service;

    @BeforeMethod
    public void setUp(){
        service = new ArtikOAuthAuthenticationService();
    }

    @AfterMethod
    public void tearDown(){
        service = null;
    }

    @Test
    public void shouldProperlyGetRequestParameters(){
        final String state = PARAMETER_1 + "=" + VALUE_1 + STATE_PARAMETER_SEPARATOR + PARAMETER_2 + "=" + VALUE_2;

        final Map<String, List<String>> requestParameters = service.getRequestParameters(state);

        final String value_1 = requestParameters.get(PARAMETER_1).get(0);
        final String value_2 = requestParameters.get(PARAMETER_2).get(0);

        assertEquals(value_1, VALUE_1);
        assertEquals(value_2, VALUE_2);
    }

    @Test
    public void shouldProperlyGetState() throws MalformedURLException {
        final String query_1 = "param=value&param=value&state=state_value&param&value";
        final String query_2 = "state=state_value&param&value";
        final String query_3 = "param=value&param=value&state=state_value";

        final String state_1 = service.getState( new URL("http://localhost:8080/?" + query_1));
        final String state_2 = service.getState( new URL("http://localhost:8080/?" + query_2));
        final String state_3 = service.getState( new URL("http://localhost:8080/?" + query_3));


        assertEquals(state_1, "state_value");
        assertEquals(state_2, "state_value");
        assertEquals(state_3, "state_value");
    }
}
