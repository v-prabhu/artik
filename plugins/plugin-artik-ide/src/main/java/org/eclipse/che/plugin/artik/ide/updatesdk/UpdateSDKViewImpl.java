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
package org.eclipse.che.plugin.artik.ide.updatesdk;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.ui.listbox.CustomListBox;
import org.eclipse.che.ide.ui.window.Window;
import org.eclipse.che.plugin.artik.ide.ArtikLocalizationConstant;

import java.util.List;

/**
 * The implementation of {@link UpdateSDKView}.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class UpdateSDKViewImpl extends Window implements UpdateSDKView {

    @UiField(provided = true)
    final ArtikLocalizationConstant localizationConstants;

    private final Button cancelButton;
    private final Button installButton;

    @UiField(provided = true)
    CellTable<TargetForUpdate> targetsTable;

    @UiField
    CustomListBox versionsBox;

    private ActionDelegate delegate;

    private TableResources tableResources = GWT.create(TableResources.class);

    @Inject
    public UpdateSDKViewImpl(UpdateSDKViewImplUiBinder uiBinder, ArtikLocalizationConstant localizationConstants) {
        this.localizationConstants = localizationConstants;
        setTitle(localizationConstants.updateSDKViewTitle());

        targetsTable = new CellTable<>(5, tableResources);

        Column<TargetForUpdate, String> targetColumn = new Column<TargetForUpdate, String>(new TextCell()) {
            @Override
            public String getValue(TargetForUpdate target) {
                return target.getName();
            }
        };
        Column<TargetForUpdate, String> installedVersionColumn = new Column<TargetForUpdate, String>(new TextCell()) {
            @Override
            public String getValue(TargetForUpdate target) {
                return target.getCurrentVersion();
            }
        };

        targetsTable.addColumn(targetColumn, localizationConstants.updateSDKViewColumnTargetTitle());
        targetsTable.setColumnWidth(targetColumn, "60%");
        targetsTable.addColumn(installedVersionColumn, localizationConstants.updateSDKViewColumnInstalledVersionTitle());
        targetsTable.setColumnWidth(installedVersionColumn, "40%");

        setWidget(uiBinder.createAndBindUi(this));

        installButton = createButton(localizationConstants.updateSDKViewButtonInstallTitle(),
                                     "artik-updateSDK-install",
                                     new ClickHandler() {
                                         @Override
                                         public void onClick(ClickEvent event) {
                                             delegate.onInstallClicked();
                                         }
                                     });
        installButton.addStyleName(Window.resources.windowCss().primaryButton());
        addButtonToFooter(installButton);

        cancelButton = createButton(localizationConstants.updateSDKViewButtonCancelTitle(),
                                    "artik-updateSDK-cancel",
                                    new ClickHandler() {
                                        @Override
                                        public void onClick(ClickEvent event) {
                                            delegate.onCancelClicked();
                                        }
                                    });
        addButtonToFooter(cancelButton);
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void setTargets(List<TargetForUpdate> targets) {
        targetsTable.setRowData(targets);
    }

    @Override
    public void setAvailableVersions(List<String> versions) {
        versionsBox.clear();

        for (String version : versions) {
            versionsBox.addItem(version, version);
        }

        versionsBox.setSelectedIndex(versionsBox.getItemCount() - 1);
    }

    @Override
    public void close() {
        this.hide();
    }

    @Override
    public void setEnabledInstallButton(boolean enabled) {
        installButton.setEnabled(enabled);
    }

    @Override
    public String getSelectedVersion() {
        return versionsBox.getSelectedItemText();
    }

    interface UpdateSDKViewImplUiBinder extends UiBinder<Widget, UpdateSDKViewImpl> {
    }
}
