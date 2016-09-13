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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.ProductInfoDataProvider;
import org.eclipse.che.ide.ui.window.Window;
import org.eclipse.che.plugin.artik.ide.ArtikLocalizationConstant;
import org.vectomatic.dom.svg.ui.SVGImage;

/**
 * @author Dmitry Kuleshov
 */
@Singleton
public class UserInfoViewImpl extends Window implements UserInfoView {
    interface UserInfoViewImplUiBinder extends UiBinder<Widget, UserInfoViewImpl> {
    }

    Button btnOk;

    @UiField
    Label                     userInfoName;
    @UiField
    Label                     userInfoFullName;
    @UiField
    Label                     userInfoEmail;
    @UiField
    Label                     userInfoIdentity;
    @UiField
    Label                     userInfoAccountType;

    @UiField(provided = true)
    ArtikLocalizationConstant locale;

    private ActionDelegate delegate;

    @Inject
    public UserInfoViewImpl(UserInfoViewImplUiBinder uiBinder,
                            ArtikLocalizationConstant locale,
                            UserInfoResources userInfoResources) {
        this.locale = locale;

        userInfoResources.userInfoCss().ensureInjected();
        String title = locale.artikUserInfoTitle();
        this.setTitle(title);
        this.setWidget(uiBinder.createAndBindUi(this));
        this.ensureDebugId("userInfoView-window");

        btnOk = createButton(locale.artikUserInfoOk(), "user-info-ok", new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                delegate.onOkClicked();
            }
        });
        addButtonToFooter(btnOk);
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
        this.hide();
    }

    /** {@inheritDoc} */
    @Override
    public void showDialog() {
        this.show(btnOk);
    }

    @Override
    public void setName(String name) {
        this.userInfoName.setText(name);
    }

    @Override
    public void setFullName(String fullName) {
        this.userInfoFullName.setText(fullName);

    }

    @Override
    public void setEmail(String email) {
        this.userInfoEmail.setText(email);

    }

    @Override
    public void setIdentity(String identity) {
        this.userInfoIdentity.setText(identity);

    }

    @Override
    public void setAccountType(String accountType) {
        this.userInfoAccountType.setText(accountType);

    }

    @Override
    protected void onEnterClicked() {
        delegate.onOkClicked();
    }
}
