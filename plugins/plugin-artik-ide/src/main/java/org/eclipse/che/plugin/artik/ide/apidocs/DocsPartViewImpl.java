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

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.parts.PartStackUIResources;
import org.eclipse.che.ide.api.parts.base.BaseView;
import org.eclipse.che.plugin.artik.ide.ArtikLocalizationConstant;

/**
 * Implementation of {@link DocsPartView}.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class DocsPartViewImpl extends BaseView<DocsPartView.ActionDelegate> implements DocsPartView {

    @UiField
    Frame frame;

    @Inject
    public DocsPartViewImpl(PartStackUIResources resources,
                            DocsPartViewImplUiBinder uiBinder,
                            ArtikLocalizationConstant localizationConstants) {
        super(resources);

        setTitle(localizationConstants.showApiDocPageTitle());
        setContentWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void setURL(String url) {
        frame.setUrl(url);
    }

    interface DocsPartViewImplUiBinder extends UiBinder<Widget, DocsPartViewImpl> {
    }
}
