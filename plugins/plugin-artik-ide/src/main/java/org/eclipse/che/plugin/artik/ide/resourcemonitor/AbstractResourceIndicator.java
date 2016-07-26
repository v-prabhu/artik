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

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.CustomComponentAction;
import org.eclipse.che.ide.api.action.Presentation;

/**
 * Abstract class for all resource indicators.
 * Implementors should override {@link #getValue()} method.
 *
 * @author Artem Zatsarynnyi
 */
public abstract class AbstractResourceIndicator extends Action implements CustomComponentAction {

    private static final int RESOURCE_POLLING_PERIOD = 3000;

    private final ResourceIndicatorView view;

    @Inject
    public AbstractResourceIndicator(final ResourceIndicatorView view) {
        this.view = view;

        new Timer() {
            @Override
            public void run() {
                getValue().then(new Operation<String>() {
                    @Override
                    public void apply(String value) throws OperationException {
                        view.setValue(value);
                    }
                }).catchError(new Operation<PromiseError>() {
                    @Override
                    public void apply(PromiseError promiseError) throws OperationException {
                        view.setValue("N/A");
                    }
                });
            }
        }.scheduleRepeating(RESOURCE_POLLING_PERIOD);
    }

    @Override
    public Widget createCustomComponent(Presentation presentation) {
        return view.asWidget();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    /** Returns value for displaying in indicator's view. */
    protected abstract Promise<String> getValue();
}
