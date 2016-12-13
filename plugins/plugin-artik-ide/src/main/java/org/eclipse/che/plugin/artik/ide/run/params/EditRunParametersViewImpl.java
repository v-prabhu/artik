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
package org.eclipse.che.plugin.artik.ide.run.params;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.ui.window.Window;
import org.eclipse.che.plugin.artik.ide.ArtikLocalizationConstant;

import static org.eclipse.che.plugin.artik.ide.command.macro.NodeJsRunParametersMacro.DEFAULT_RUN_PARAMETERS;

/**
 * Implementation of {@link EditRunParametersView}.
 */
@Singleton
public class EditRunParametersViewImpl extends Window implements EditRunParametersView {

    @UiField(provided = true)
    ArtikLocalizationConstant locale;

    @UiField
    TextArea runParameters;

    private ActionDelegate actionDelegate;

    @Inject
    public EditRunParametersViewImpl(EditRunParametersViewImplUiBinder uiBinder,
                                     ArtikLocalizationConstant locale,
                                     CoreLocalizationConstant coreLocale) {
        this.locale = locale;

        setWidget(uiBinder.createAndBindUi(this));
        setTitle(locale.editRunParamsViewTitle());

        Button closeButton = createButton(coreLocale.cancel(), "runParameters.button.cancel",
                                          new ClickHandler() {
                                              @Override
                                              public void onClick(ClickEvent event) {
                                                  actionDelegate.onClose();
                                              }
                                          });
        addButtonToFooter(closeButton);

        Button saveButton = createButton(coreLocale.save(), "runParameters.button.save",
                                         new ClickHandler() {
                                             @Override
                                             public void onClick(ClickEvent event) {
                                                 actionDelegate.onSave();
                                             }
                                         });
        saveButton.addStyleName(resources.windowCss().primaryButton());
        addButtonToFooter(saveButton);

        runParameters.getElement().setAttribute("placeholder", DEFAULT_RUN_PARAMETERS);
    }

    @Override
    public void setDelegate(ActionDelegate actionDelegate) {
        this.actionDelegate = actionDelegate;
    }

    @Override
    public void show() {
        super.show();

        setRunParameters("");
    }

    @Override
    public void close() {
        hide();
    }

    public String getRunParameters() {
        return runParameters.getValue();
    }

    public void setRunParameters(String runParameters) {
        this.runParameters.setValue(runParameters);
    }

    interface EditRunParametersViewImplUiBinder extends UiBinder<Widget, EditRunParametersViewImpl> {
    }
}
