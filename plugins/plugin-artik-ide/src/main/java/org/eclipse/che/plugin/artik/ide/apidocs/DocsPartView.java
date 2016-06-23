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

import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.base.BaseActionDelegate;

/**
 * View for {@link DocsPartPresenter}.
 *
 * @author Artem Zatsarynnyi
 */
public interface DocsPartView extends View<DocsPartView.ActionDelegate> {

    /** Set the URL to load content into view. */
    void setURL(String url);

    /** Sets whether the view is visible. */
    void setVisible(boolean visible);

    interface ActionDelegate extends BaseActionDelegate {
    }
}
