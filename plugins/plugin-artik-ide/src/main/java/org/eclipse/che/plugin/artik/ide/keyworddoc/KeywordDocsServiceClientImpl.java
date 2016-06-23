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
package org.eclipse.che.plugin.artik.ide.keyworddoc;

import com.google.inject.Inject;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.DevMachine;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.StringUnmarshaller;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;

/**
 * Implementation of {@link KeywordDocsServiceClient}.
 *
 * @author Artem Zatsarynnyi
 */
public class KeywordDocsServiceClientImpl implements KeywordDocsServiceClient {

    private final AsyncRequestFactory asyncRequestFactory;
    private final LoaderFactory       loaderFactory;
    private final AppContext          appContext;

    @Inject
    public KeywordDocsServiceClientImpl(AsyncRequestFactory asyncRequestFactory,
                                        LoaderFactory loaderFactory,
                                        AppContext appContext) {
        this.asyncRequestFactory = asyncRequestFactory;
        this.loaderFactory = loaderFactory;
        this.appContext = appContext;
    }

    @Override
    public Promise<String> getLink(String keyword) {
        final DevMachine devMachine = appContext.getDevMachine();
        final String url = devMachine.getWsAgentBaseUrl() + "/artikdoc/" + appContext.getWorkspaceId() + "/" + keyword;

        return asyncRequestFactory.createGetRequest(url)
                                  .loader(loaderFactory.newLoader("Opening documentation page..."))
                                  .send(new StringUnmarshaller());
    }
}
