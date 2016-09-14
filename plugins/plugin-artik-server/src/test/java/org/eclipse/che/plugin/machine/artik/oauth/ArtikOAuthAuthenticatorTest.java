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

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link ArtikOAuthAuthenticator}
 *
 * @author Dmitry Kuleshov
 */
public class ArtikOAuthAuthenticatorTest {
    private ArtikOAuthAuthenticator authenticator;

    @BeforeMethod
    public void setUp() throws IOException {
        authenticator = new ArtikOAuthAuthenticator("client_id", "client_secret", new String[]{"redirect_uri"}, "auth_uri", "http://localhost");
    }

    @AfterMethod
    public void tearDown() {
        authenticator = null;
    }

    @Test
    public void shouldProperlyGetUserId() throws MalformedURLException {
        final String userId = authenticator.getUserId(new URL("http://localhost:8080/?state=userId%3DuserIdValue"));

        assertEquals(userId, "userIdValue");
    }

    @Test
    public void shouldProperlyGetOAuthProvider() {
        final String oAuthProvider = authenticator.getOAuthProvider();

        assertEquals(oAuthProvider, "artik");
    }

    @Test
    public void shouldProperlyPrepareState() throws MalformedURLException {
        final String state = authenticator.prepareState(new URL("http://localhost:8080/?userId=userIdValue"));

        assertEquals(state, "userId%253DuserIdValue");
    }
}
