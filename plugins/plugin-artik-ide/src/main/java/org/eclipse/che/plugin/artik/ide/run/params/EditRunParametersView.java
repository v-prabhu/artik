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
package org.eclipse.che.plugin.artik.ide.run.params;

import com.google.inject.ImplementedBy;

import org.eclipse.che.ide.api.mvp.View;

/**
 * View for {@link EdiRunParametersPresenter}.
 */
@ImplementedBy(EditRunParametersViewImpl.class)
public interface EditRunParametersView extends View<EditRunParametersView.ActionDelegate> {

    /** Show the view. */
    void show();

    /** Close the view. */
    void close();

    /** Returns value of the 'Run parameters' field. */
    String getRunParameters();

    /** Sets value of the 'Run parameters' field. */
    void setRunParameters(String runParameters);

    interface ActionDelegate {

        /** Called when view has been closed. */
        void onClose();

        /** Called when 'Save' button has been clicked. */
        void onSave();
    }
}
