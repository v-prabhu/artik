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
import com.google.inject.Inject;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.oauth.OAuth2Authenticator;
import org.eclipse.che.ide.api.oauth.OAuth2AuthenticatorUrlProvider;
import org.eclipse.che.ide.rest.RestContext;
import org.eclipse.che.security.oauth.JsOAuthWindow;
import org.eclipse.che.security.oauth.OAuthCallback;
import org.eclipse.che.security.oauth.OAuthStatus;

import java.util.Collections;

import static org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper.createFromAsyncRequest;

/**
 * @author Dmitry Kuleshov
 */
public class ArtikAuthenticator implements OAuth2Authenticator, OAuthCallback, ArtikCloudAuthenticatorViewImpl.ActionDelegate {
    private static final String ARTIK = "artik";

    private final ArtikCloudAuthenticatorView view;
    private final String                      restContext;
    private final AppContext                  appContext;

    private AsyncCallback<OAuthStatus> callback;
    private String                     authenticationUrl;

    @Inject
    public ArtikAuthenticator(@RestContext String restContext,
                              ArtikCloudAuthenticatorView view,
                              AppContext appContext) {
        this.view = view;
        this.view.setDelegate(this);
        this.restContext = restContext;
        this.appContext = appContext;
    }

    @Override
    public void authenticate(String authenticationUrl, AsyncCallback<OAuthStatus> callback) {
        this.authenticationUrl = authenticationUrl;
        this.callback = callback;
        view.showDialog();
    }

    public Promise<OAuthStatus> authenticate(String authenticationUrl) {
        this.authenticationUrl = authenticationUrl;

        return createFromAsyncRequest(new AsyncPromiseHelper.RequestCall<OAuthStatus>() {
            @Override
            public void makeCall(AsyncCallback<OAuthStatus> callback) {
                ArtikAuthenticator.this.callback = callback;
                view.showDialog();
            }
        });
    }

    @Override
    public String getProviderName() {
        return ARTIK;
    }

    @Override
    public void onCancelled() {
        callback.onFailure(new Exception("Authorization request rejected by user."));
    }

    @Override
    public void onAccepted() {
        showAuthenticationWindow();
    }

    @Override
    public void onAuthenticated(OAuthStatus authStatus) {
        callback.onSuccess(authStatus);
    }

    private void showAuthenticationWindow() {
        JsOAuthWindow authWindow;
        if (authenticationUrl == null) {
            authWindow = new JsOAuthWindow(getAuthUrl(), "error.url", 500, 980, this);
        } else {
            authWindow = new JsOAuthWindow(authenticationUrl, "error.url", 500, 980, this);
        }
        authWindow.loginWithOAuth();
    }

    private String getAuthUrl() {
        final String userId = appContext.getCurrentUser().getProfile().getUserId();
        return OAuth2AuthenticatorUrlProvider.get(restContext, ARTIK, userId, Collections.<String>emptyList());
    }
}
