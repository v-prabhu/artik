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
package org.eclipse.che.plugin.artik.ide.ext.help.client;


import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.action.IdeActions;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.plugin.artik.ide.ext.help.client.about.ShowAboutAction;
import org.eclipse.che.plugin.artik.ide.ext.help.client.support.RedirectToSupportAction;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Vitalii Parfonov
 * @author Oleksii Orel
 */
@Singleton
@Extension(title = "Help Extension", version = "3.0.0")
public class HelpAboutExtension {


    @Inject
    public HelpAboutExtension(ActionManager actionManager,
                              final ShowAboutAction showAboutAction,
                              final RedirectToSupportAction redirectToSupportAction) {

        // Compose Help menu
        DefaultActionGroup helpGroup = (DefaultActionGroup)actionManager.getAction(IdeActions.GROUP_HELP);
        actionManager.registerAction("showAbout", showAboutAction);
        actionManager.registerAction("redirectToSupport", redirectToSupportAction);

        helpGroup.addSeparator();
        helpGroup.add(redirectToSupportAction);
        helpGroup.add(showAboutAction);
    }
}
