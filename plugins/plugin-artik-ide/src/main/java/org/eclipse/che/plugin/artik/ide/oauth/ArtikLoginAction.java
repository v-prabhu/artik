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
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.oauth.OAuth2Authenticator;
import org.eclipse.che.ide.api.oauth.OAuth2AuthenticatorRegistry;
import org.eclipse.che.ide.api.oauth.OAuth2AuthenticatorUrlProvider;
import org.eclipse.che.ide.rest.RestContext;
import org.eclipse.che.plugin.artik.ide.ArtikLocalizationConstant;
import org.eclipse.che.security.oauth.OAuthStatus;

import java.util.Collections;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;

/**
 * @author Dmitry Kuleshov
 */
@Singleton
public class ArtikLoginAction extends Action {
    private static final String ARTIK = "artik";

    private final OAuth2AuthenticatorRegistry   auth2AuthenticatorRegistry;
    private final String                        restContext;
    private final AppContext                    appContext;
    private final Provider<NotificationManager> notificationManagerProvider;

    @Inject
    public ArtikLoginAction(@RestContext String restContext,
                            AppContext appContext,
                            ArtikLocalizationConstant localizationConstants,
                            OAuth2AuthenticatorRegistry auth2AuthenticatorRegistry,
                            Provider<NotificationManager> notificationManagerProvider) {
        super(localizationConstants.artikCloudLoginActionTitle(), localizationConstants.artikCloudLoginActionDescription());

        this.restContext = restContext;
        this.appContext = appContext;
        this.auth2AuthenticatorRegistry = auth2AuthenticatorRegistry;
        this.notificationManagerProvider = notificationManagerProvider;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final OAuth2Authenticator authenticator = auth2AuthenticatorRegistry.getAuthenticator(ARTIK);
        if (authenticator != null) {
            final String userId = appContext.getCurrentUser().getProfile().getUserId();
            final String url = OAuth2AuthenticatorUrlProvider.get(restContext, ARTIK, userId, Collections.<String>emptyList());
            final AsyncCallback<OAuthStatus> callback = new AsyncCallback<OAuthStatus>() {
                @Override
                public void onFailure(Throwable caught) {
                    notificationManagerProvider.get().notify("Authentication failed", FAIL, EMERGE_MODE);
                }

                @Override
                public void onSuccess(OAuthStatus result) {
                    switch (result) {
                        case FAILED:
                            notificationManagerProvider.get().notify("Authentication failed", FAIL, EMERGE_MODE);
                            break;
                        case LOGGED_IN:
                            notificationManagerProvider.get().notify("Logged in to Artik Cloud", SUCCESS, EMERGE_MODE);
                            break;
                        case LOGGED_OUT:
                            notificationManagerProvider.get().notify("Logged out from Artik Cloud", SUCCESS, EMERGE_MODE);
                            break;
                    }
                }
            };

            authenticator.authenticate(url, callback);
        } else {
            notificationManagerProvider.get().notify("Failed to find authenticator", FAIL, EMERGE_MODE);
        }
    }
}
