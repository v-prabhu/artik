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
package org.eclipse.che.plugin.artik.ide.oauth;

import com.google.gwt.user.client.rpc.AsyncCallback;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.security.oauth.OAuthStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link ArtikAuthenticator}
 *
 * @author Dmitry Kuleshov
 */
@RunWith(MockitoJUnitRunner.class)
public class ArtikAuthenticatorTest {

    private final static String AUTHENTICATION_URL = "authentication_URL";

    @Mock
    private ArtikCloudAuthenticatorView view;

    @Mock
    private AppContext                  appContext;
    @InjectMocks
    private ArtikAuthenticator          authenticator;

    @Mock
    private AsyncCallback<OAuthStatus> callback;

    @Test
    public void shouldShowDialogOnAuthenticate() {
        authenticator.authenticate(AUTHENTICATION_URL, callback);

        verify(view).showDialog();
    }

    @Test
    public void shouldGetProviderName() {
        final String providerName = authenticator.getProviderName();

        assertEquals("artik", providerName);
    }

    @Test
    public void shouldRunOnFailureOnCancel() {
        authenticator.authenticate(AUTHENTICATION_URL, callback);

        authenticator.onCancelled();

        verify(callback).onFailure(any(Throwable.class));
    }

    @Test
    public void shouldRunOnSuccessOnAuthenticated() {
        authenticator.authenticate(AUTHENTICATION_URL, callback);
        final OAuthStatus status = OAuthStatus.LOGGED_IN;

        authenticator.onAuthenticated(status);

        verify(callback).onSuccess(eq(status));
    }
}
