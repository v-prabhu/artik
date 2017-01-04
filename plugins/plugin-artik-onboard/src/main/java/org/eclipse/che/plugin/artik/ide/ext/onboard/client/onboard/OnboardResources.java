/*******************************************************************************
 * Copyright (c) 2016-2017 Samsung Electronics Co., Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - Initial implementation
 *   Samsung Electronics Co., Ltd. - Initial implementation
 *******************************************************************************/
package org.eclipse.che.plugin.artik.ide.ext.onboard.client.onboard;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import org.vectomatic.dom.svg.ui.SVGResource;

public interface OnboardResources extends ClientBundle {

    @Source("org/eclipse/che/plugin/artik/ide/ext/onboard/client/onboard/onboard.svg")
    SVGResource onboard();

    @Source("org/eclipse/che/plugin/artik/ide/ext/onboard/client/onboard/onboard.css")
    OnboardCss onboardCss();

    interface OnboardCss extends CssResource {
        String emptyBorder();

        String label();

        String spacing();

        String value();

        String mainText();

        String logo();
    }
}
