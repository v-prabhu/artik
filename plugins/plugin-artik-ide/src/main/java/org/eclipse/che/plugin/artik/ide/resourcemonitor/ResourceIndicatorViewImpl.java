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
package org.eclipse.che.plugin.artik.ide.resourcemonitor;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Implementation of {@link ResourceIndicatorView}.
 *
 * @author Artem Zatsarynnyi
 */
public class ResourceIndicatorViewImpl implements ResourceIndicatorView {

    private static final ResourceIndicatorViewImplUiBinder UI_BINDER = GWT.create(ResourceIndicatorViewImplUiBinder.class);

    @UiField
    FlowPanel mainPanel;
    @UiField
    Label     valueLabel;

    private FlowPanel rootElement;

    @Inject
    public ResourceIndicatorViewImpl() {
        rootElement = UI_BINDER.createAndBindUi(this);
    }

    @Override
    public Widget asWidget() {
        return rootElement;
    }

    @Override
    public void setValue(String value) {
        valueLabel.setText(value);
    }

    interface ResourceIndicatorViewImplUiBinder extends UiBinder<FlowPanel, ResourceIndicatorViewImpl> {
    }
}
