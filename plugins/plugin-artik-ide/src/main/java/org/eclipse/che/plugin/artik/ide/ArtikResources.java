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
package org.eclipse.che.plugin.artik.ide;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.TextResource;

import org.vectomatic.dom.svg.ui.SVGResource;

public interface ArtikResources extends ClientBundle {

    @Source("svg/artik-icon.svg")
    SVGResource artikIcon();

    /** Returns the CSS resource for the Artik extension. */
    @Source({"artik-plugin.css", "org/eclipse/che/ide/api/ui/style.css"})
    Css getCss();

    @Source("updatesdk/checkAvailableSdkVersionsCommand")
    TextResource getCheckAvailableSDKVersionsCommand();

    @Source("updatesdk/checkSdkVersionOnDeviceCommand")
    TextResource getCheckSDKVersionOnDeviceCommand();

    @Source("updatesdk/checkSdkVersionOnWsAgentCommand")
    TextResource getCheckSDKVersionOnAgentCommand();

    @Source("updatesdk/installSdkOnDeviceCommand")
    TextResource getInstallSDKOnDeviceCommand();

    @Source("updatesdk/recipe")
    TextResource recipe();

    @Source("resourcemonitor/cpu")
    TextResource getCpuCommand();

    @Source("resourcemonitor/memTotal")
    TextResource getTotalMemoryCommand();

    @Source("resourcemonitor/memUsed")
    TextResource getUsedMemoryCommand();

    @Source("resourcemonitor/storageTotal")
    TextResource getTotalStorageSpaceCommand();

    @Source("resourcemonitor/storageUsed")
    TextResource getUsedStorageSpaceCommand();

    /** The CssResource interface for the Artik extension. */
    interface Css extends CssResource {

        String deviceTitle();
    }
}
