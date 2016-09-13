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
package org.eclipse.che.plugin.artik.ide.oauth;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.dialogs.CancelCallback;
import org.eclipse.che.ide.api.dialogs.ConfirmCallback;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.plugin.artik.ide.ArtikLocalizationConstant;

/**
 * @author Dmitry Kuleshov
 */
@Singleton
public class ArtikCloudAuthenticatorViewImpl implements ArtikCloudAuthenticatorView {
    private final DialogFactory             dialogFactory;
    private final ArtikLocalizationConstant locale;
    private final DockLayoutPanel           contentPanel;

    private ActionDelegate delegate;

    @Inject
    public ArtikCloudAuthenticatorViewImpl(DialogFactory dialogFactory, ArtikLocalizationConstant locale) {
        this.dialogFactory = dialogFactory;
        this.locale = locale;

        this.contentPanel = new DockLayoutPanel(Style.Unit.PX);
        this.contentPanel.addNorth(new InlineHTML(locale.artikAuthorizationDialogText()), 20);
    }

    @Override
    public void showDialog() {
        final String title = locale.artikAuthorizationDialogTitle();
        final ConfirmCallback confirmCallback = new ConfirmCallback() {
            @Override
            public void accepted() {
                delegate.onAccepted();
            }
        };
        final CancelCallback cancelCallback = new CancelCallback() {
            @Override
            public void cancelled() {
                delegate.onCancelled();
            }
        };

        dialogFactory.createConfirmDialog(title, contentPanel, confirmCallback, cancelCallback).show();
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public Widget asWidget() {
        return contentPanel;
    }
}
