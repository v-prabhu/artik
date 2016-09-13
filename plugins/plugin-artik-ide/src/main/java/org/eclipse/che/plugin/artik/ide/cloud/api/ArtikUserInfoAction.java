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
package org.eclipse.che.plugin.artik.ide.cloud.api;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.eclipse.che.api.auth.shared.dto.OAuthToken;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.RestContext;
import org.eclipse.che.ide.rest.StringUnmarshaller;
import org.eclipse.che.ide.rest.Unmarshallable;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.plugin.artik.ide.ArtikLocalizationConstant;
import org.eclipse.che.plugin.artik.shared.dto.ArtikUserDto;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

/**
 * @author Dmitry Kuleshov
 */
@Singleton
public class ArtikUserInfoAction extends Action {
    private static final String OAUTH_TOKEN_CONTEXT      = "/oauth/token?oauth_provider=artik";
    private static final String ARTIK_API_USER_SELF_PATH = "https://api.artik.cloud/v1.1/users/self";

    private final Provider<NotificationManager> notificationManagerProvider;
    private final DtoUnmarshallerFactory        dtoUnmarshallerFactory;
    private final AsyncRequestFactory           asyncRequestFactory;
    private final UserInfoPresenter             userInfoPresenter;
    private final DialogFactory                 dialogFactory;
    private final String                        restContext;
    private final DtoFactory                    dtoFactory;


    @Inject
    public ArtikUserInfoAction(@RestContext String restContext,
                               Provider<NotificationManager> notificationManagerProvider,
                               ArtikLocalizationConstant localizationConstants,
                               DtoUnmarshallerFactory dtoUnmarshallerFactory,
                               AsyncRequestFactory asyncRequestFactory,
                               UserInfoPresenter userInfoPresenter,
                               DialogFactory dialogFactory,
                               DtoFactory dtoFactory) {
        super(localizationConstants.artikCloudGetUserInfoActionTitle(), localizationConstants.artikCloudGetUserInfoActionDescription());

        this.notificationManagerProvider = notificationManagerProvider;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.userInfoPresenter = userInfoPresenter;
        this.dtoFactory = dtoFactory;
        this.asyncRequestFactory = asyncRequestFactory;
        this.dialogFactory = dialogFactory;
        this.restContext = restContext;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final Unmarshallable<OAuthToken> unmarshaller = dtoUnmarshallerFactory.newUnmarshaller(OAuthToken.class);
        final String url = restContext + OAUTH_TOKEN_CONTEXT;

        asyncRequestFactory.createGetRequest(url)
                           .send(unmarshaller)
                           .then(new Function<OAuthToken, Void>() {
                               @Override
                               public Void apply(OAuthToken arg) throws FunctionException {
                                   return processUserInfo(arg);
                               }
                           })
                           .catchError(new Function<PromiseError, Void>() {
                               @Override
                               public Void apply(PromiseError arg) throws FunctionException {
                                   return processError();
                               }
                           });
    }

    private Void processError() {
        final String message = "Could not retrieve registered devises list because of authorization issues";
        notificationManagerProvider.get().notify(message, FAIL, EMERGE_MODE);
        return null;
    }

    private Void processUserInfo(OAuthToken arg) {
        final Unmarshallable<ArtikUserDto> unmarshaller = dtoUnmarshallerFactory.newUnmarshaller(ArtikUserDto.class);
        final String token = arg.getToken();

        asyncRequestFactory.createGetRequest(ARTIK_API_USER_SELF_PATH)
                           .header("Authorization", "Bearer " + token)
                           .send(new StringUnmarshaller())
                           .then(new Function<String, Void>() {
                               @Override
                               public Void apply(String data) throws FunctionException {
                                   final ArtikUserDto artikUserDto = dtoFactory.createDtoFromJson(data, ArtikUserDto.class);
                                   userInfoPresenter.showUserInfo(artikUserDto.getData());

                                   return null;
                               }
                           })
                           .catchError(new Function<PromiseError, Void>() {
                               @Override
                               public Void apply(PromiseError arg) throws FunctionException {
                                   Log.error(getClass(), arg);
                                   return null;
                               }
                           });
        return null;
    }

}
