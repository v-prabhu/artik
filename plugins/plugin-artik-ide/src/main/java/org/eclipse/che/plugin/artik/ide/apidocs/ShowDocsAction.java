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
package org.eclipse.che.plugin.artik.ide.apidocs;

import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.plugin.artik.ide.ArtikLocalizationConstant;

/**
 * Action for opening Artik API documentation page.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class ShowDocsAction extends AbstractPerspectiveAction {

    private final DocsPartPresenter docsPartPresenter;

    @Inject
    public ShowDocsAction(DocsPartPresenter docsPartPresenter, ArtikLocalizationConstant localizationConstants) {
        super(null, localizationConstants.showApiDocActionTitle(), localizationConstants.showApiDocActionDescription());

        this.docsPartPresenter = docsPartPresenter;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Window.open(docsPartPresenter.getDocURL(), "_blank", null);
    }

    @Override
    public void updateInPerspective(ActionEvent e) {
        e.getPresentation().setEnabledAndVisible(true);
    }
}
