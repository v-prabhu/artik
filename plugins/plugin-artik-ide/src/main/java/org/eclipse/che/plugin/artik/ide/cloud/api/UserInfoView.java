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

import org.eclipse.che.ide.api.mvp.View;

/**
 * @author Dmitry Kuleshov
 */
public interface UserInfoView extends View<UserInfoView.ActionDelegate> {

    interface ActionDelegate {

        void onOkClicked();
    }

    void close();

    void showDialog();

    void setName(String name);

    void setFullName(String fullName);

    void setEmail(String email);

    void setIdentity(String identity);

    void setAccountType(String accountType);
}
