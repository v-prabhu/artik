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

import cloud.artik.api.UsersApi;
import cloud.artik.client.ApiClient;
import cloud.artik.client.ApiException;
import cloud.artik.client.Configuration;
import cloud.artik.client.auth.OAuth;
import cloud.artik.model.UserEnvelope;

import com.google.api.client.auth.oauth2.AuthorizationCodeResponseUrl;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.util.store.MemoryDataStoreFactory;

import org.eclipse.che.api.auth.shared.dto.OAuthToken;
import org.eclipse.che.plugin.artik.shared.dto.ArtikUserDataDto;
import org.eclipse.che.security.oauth.OAuthAuthenticationException;
import org.eclipse.che.security.oauth.OAuthAuthenticator;
import org.eclipse.che.security.oauth.shared.User;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.slf4j.LoggerFactory.getLogger;

/** OAuth authentication for artik account. */
@Singleton
public class ArtikOAuthAuthenticator extends OAuthAuthenticator {
    private static final Logger LOG = getLogger(ArtikOAuthAuthenticator.class);

    private static final Pattern statePattern  = Pattern.compile("(?:.*)(?:state=)([^&]+)(?:.*)");
    private static final Pattern userIdPattern = Pattern.compile("(?:.*)(?:userId=)([^$]+)(?:.*)");

    @Inject
    public ArtikOAuthAuthenticator(@Named("oauth.artik.clientid") String clientId,
                                   @Named("oauth.artik.clientsecret") String clientSecret,
                                   @Named("oauth.artik.redirecturis") String[] redirectUris,
                                   @Named("oauth.artik.authuri") String authUri,
                                   @Named("oauth.artik.tokenuri") String tokenUri) throws IOException {
        if (!isNullOrEmpty(clientId)
            && !isNullOrEmpty(clientSecret)
            && !isNullOrEmpty(authUri)
            && !isNullOrEmpty(tokenUri)
            && redirectUris != null && redirectUris.length != 0) {

            configure(clientId, clientSecret, redirectUris, authUri, tokenUri, new MemoryDataStoreFactory());
        }
    }

    @Override
    public User getUser(OAuthToken accessToken) throws OAuthAuthenticationException {
        configureArtikCloudApiClients(accessToken);

        final UsersApi userApiClient = new UsersApi();
        try {
            final UserEnvelope result = userApiClient.getSelf();

            final String id = result.getData().getId();
            final String email = result.getData().getEmail();
            final String name = result.getData().getName();

            final User user = newDto(ArtikUserDataDto.class);
            user.setId(id);
            user.setName(name);
            user.setEmail(email);

            return user;
        } catch (ApiException e) {
            LOG.error("Exception when calling UsersApi#getSelf");
        }
        return null;
    }

    private void configureArtikCloudApiClients(OAuthToken accessToken) {
        final ApiClient defaultClient = Configuration.getDefaultApiClient();
        final OAuth artikCloudOAuth = (OAuth)defaultClient.getAuthentication("artikcloud_oauth");
        artikCloudOAuth.setAccessToken(accessToken.getToken());
    }

    @Override
    public String callback(URL requestUrl, List<String> scopes) throws OAuthAuthenticationException {
        if (!isConfigured()) {
            throw new OAuthAuthenticationException("Authenticator is not configured");
        }

        AuthorizationCodeResponseUrl authorizationCodeResponseUrl = new AuthorizationCodeResponseUrl(requestUrl.toString());
        final String error = authorizationCodeResponseUrl.getError();
        if (error != null) {
            throw new OAuthAuthenticationException("Authentication failed: " + error);
        }
        final String code = authorizationCodeResponseUrl.getCode();
        if (code == null) {
            throw new OAuthAuthenticationException("Missing authorization code. ");
        }

        try {
            TokenResponse tokenResponse = flow.newTokenRequest(code).setRequestInitializer(request -> {
                if (request.getParser() == null) {
                    request.setParser(flow.getJsonFactory().createJsonObjectParser());
                }
                request.getHeaders().setAccept(MediaType.APPLICATION_JSON);
            }).setRedirectUri(findRedirectUrl(requestUrl)).setScopes((scopes == null || scopes.isEmpty()) ? null : scopes).execute();
            String userId = getUserId(requestUrl);
            if (userId == null) {
                userId = getUser(newDto(OAuthToken.class).withToken(tokenResponse.getAccessToken())).getId();
            }
            flow.createAndStoreCredential(tokenResponse, userId);
            return userId;
        } catch (IOException ioe) {
            throw new OAuthAuthenticationException(ioe.getMessage());
        }
    }

    private String getUserId(URL requestUrl) {
        try {
            final String queryParams = requestUrl.getQuery();
            final Matcher stateMatcher = statePattern.matcher(queryParams);
            if (stateMatcher.matches()){
                final String state = stateMatcher.group(1);
                final String decodedState = URLDecoder.decode(state, "UTF-8");

                final Matcher userIdMatcher = userIdPattern.matcher(decodedState);
                if (userIdMatcher.matches()){
                    return userIdMatcher.group(1);
                }
            }
        } catch (UnsupportedEncodingException e) {
            LOG.error("Error during decoding of URL state query param", e);
        }

        return null;
    }

    @Override
    public final String getOAuthProvider() {
        return "artik";
    }

    @Override
    public OAuthToken getToken(String userId) throws IOException {
        return super.getToken(userId);
    }

    @Override
    protected String prepareState(URL requestUrl) {
        try {
            final String encoded = URLEncoder.encode(super.prepareState(requestUrl).replace("&", "$"), StandardCharsets.UTF_8.name());
            return URLEncoder.encode(encoded, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            LOG.error("Error during encoding of URL state query param", e);
        }
        return null;
    }
}
