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
package org.eclipse.che.plugin.artik.ide.apidocs;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateHandler;
import org.eclipse.che.ide.api.parts.PartStackType;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.plugin.artik.ide.ArtikLocalizationConstant;
import org.eclipse.che.plugin.artik.ide.ArtikResources;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Presenter for displaying Artik API documentation.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class DocsPartPresenter extends BasePresenter {

    private final ArtikResources            resources;
    private final AppContext                appContext;
    private final ArtikLocalizationConstant localizationConstants;
    private final WorkspaceAgent            workspaceAgent;
    private final DocsPartView              view;
    private       String                    docURL;

    @Inject
    public DocsPartPresenter(ArtikResources resources,
                             AppContext appContext,
                             ArtikLocalizationConstant localizationConstants,
                             EventBus eventBus,
                             final WorkspaceAgent workspaceAgent,
                             DocsPartView view) {
        this.resources = resources;
        this.appContext = appContext;
        this.localizationConstants = localizationConstants;
        this.workspaceAgent = workspaceAgent;
        this.view = view;

        eventBus.addHandler(WsAgentStateEvent.TYPE, new WsAgentStateHandler() {
            @Override
            public void onWsAgentStarted(WsAgentStateEvent wsAgentStateEvent) {
                docURL = getDocURL();
            }

            @Override
            public void onWsAgentStopped(WsAgentStateEvent wsAgentStateEvent) {
                workspaceAgent.hidePart(DocsPartPresenter.this);
            }
        });
    }

    String getDocURL() {
        return appContext.getDevMachine().getAddress() + "/artikdocs";
    }

    /** Open this documentation page. */
    public void open() {
        workspaceAgent.openPart(this, PartStackType.TOOLING);
    }

    @Override
    public SVGResource getTitleImage() {
        return resources.artikIcon();
    }

    @Override
    public String getTitle() {
        return localizationConstants.showApiDocPageTitle();
    }

    @Override
    public void setVisible(boolean visible) {
        view.setVisible(visible);
    }

    @Override
    public IsWidget getView() {
        return view;
    }

    @Override
    public String getTitleToolTip() {
        return localizationConstants.showApiDocPageTooltip();
    }

    @Override
    public void go(AcceptsOneWidget container) {
        view.setURL(docURL);
        container.setWidget(view);
    }
}
