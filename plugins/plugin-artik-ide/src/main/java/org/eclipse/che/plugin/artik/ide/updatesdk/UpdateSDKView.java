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
package org.eclipse.che.plugin.artik.ide.updatesdk;

import org.eclipse.che.ide.api.mvp.View;

import java.util.List;

/**
 * The view of {@link UpdateSDKPresenter}.
 *
 * @author Artem Zatsarynnyi
 */
public interface UpdateSDKView extends View<UpdateSDKView.ActionDelegate> {

    /** Show the view. */
    void show();

    /** Set the targets list. */
    void setTargets(List<TargetForUpdate> targets);

    /** Set the list of the available versions. */
    void setAvailableVersions(List<String> versions);

    /** Close the view. */
    void close();

    /** Enable/disable the 'Install' button. */
    void setEnabledInstallButton(boolean enabled);

    /** Returns the selected version. */
    String getSelectedVersion();

    interface ActionDelegate {

        /** Called when 'Cancel' button clicked. */
        void onCancelClicked();

        /** Called when 'Install' button clicked. */
        void onInstallClicked();
    }
}
