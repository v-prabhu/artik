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

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.plugin.artik.ide.ArtikLocalizationConstant;

/**
 * Action for updating Artik SDK on Artik device.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class UpdateSDKAction extends Action {

    private final UpdateSDKPresenter presenter;

    @Inject
    public UpdateSDKAction(UpdateSDKPresenter presenter, ArtikLocalizationConstant localizationConstants) {
        super(localizationConstants.updateSDKActionTitle(), localizationConstants.updateSDKActionDescription());
        this.presenter = presenter;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        presenter.show();
    }
}
