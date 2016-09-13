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
import com.google.inject.Singleton;

import org.eclipse.che.plugin.artik.shared.dto.ArtikUserDataDto;

/**
 * @author Dmitry Kuleshov
 */
@Singleton
public class UserInfoPresenter implements UserInfoView.ActionDelegate {
    private UserInfoView view;

    @Inject
    public UserInfoPresenter(UserInfoView view) {
        this.view = view;
        view.setDelegate(this);
    }

    public void showUserInfo(ArtikUserDataDto userDataDto) {
        view.setName(userDataDto.getName());
        view.setFullName(userDataDto.getFullName());
        view.setEmail(userDataDto.getEmail());
        view.setIdentity(userDataDto.getSaIdentity());
        view.setAccountType(userDataDto.getAccountType());

        view.showDialog();
    }

    @Override
    public void onOkClicked() {
        view.close();
    }

}
