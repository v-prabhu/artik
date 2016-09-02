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
package org.eclipse.che.plugin.artik.ide.orionplugin;

import com.google.inject.Inject;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.editor.orion.client.inject.OrionPlugin;

/**
 * Provides path to the Orion plugin that adds functionality needed for Artik.
 * <p>
 * Also responsible for setting WS-agent base URL to the global JS object 'window',
 * which is used by Orion Artik plugin (artikPlugin.html).
 *
 * @author Artem Zatsarynnyi
 */
public class ArtikOrionPlugin implements OrionPlugin {

    @Inject
    public ArtikOrionPlugin(ArtikOrionPluginResource artikOrionPluginResource, final AppContext appContext) {
        artikOrionPluginResource.style().ensureInjected();

        // plugin is loading on Orion initialization stage
        // so WS-agent have to be running
        setWsAgentBaseUrl(appContext.getDevMachine().getWsAgentBaseUrl());
    }

    private final native void setWsAgentBaseUrl(String url) /*-{
        $wnd.wsAgentURL = url;
    }-*/;

    @Override
    public String getRelPath() {
        return "artikOrionPlugin/artikPlugin.html";
    }
}
