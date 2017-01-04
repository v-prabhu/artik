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
package org.eclipse.che.plugin.artik.ide.scp.action;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.api.selection.SelectionAgent;
import org.eclipse.che.ide.resources.tree.ResourceNode;
import org.eclipse.che.plugin.artik.ide.ArtikLocalizationConstant;
import org.eclipse.che.plugin.artik.ide.scp.PushToDeviceManager;

/**
 * The class which represents action to copy file or folder to ssh machine. By default all files and folders will be copied to
 * '/root' directory. To change destination path you have to call {@link ChooseTargetAction}.
 *
 * @author Dmitry Shnurenko
 */
public class PushToDeviceAction extends AbstractPerspectiveAction {

    private final SelectionAgent      selectionAgent;
    private final PushToDeviceManager scpManager;
    private final String              machineName;

    @Inject
    public PushToDeviceAction(SelectionAgent selectionAgent,
                              ArtikLocalizationConstant locale,
                              PushToDeviceManager scpManager,
                              @Assisted String machineName) {
        super(null, machineName, locale.pushToDeviceDescription(), null, null);

        this.selectionAgent = selectionAgent;
        this.machineName = machineName;
        this.scpManager = scpManager;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        scpManager.pushToDevice(machineName, getSelectedElementPath(), "/root");
    }

    private String getSelectedElementPath() {
        Selection<?> selection = selectionAgent.getSelection();
        if (selection.isEmpty()) {
            return "";
        }

        Object selectedElement = selection.getHeadElement();

        if (selectedElement instanceof ResourceNode) {
            return ((ResourceNode)selectedElement).getData().getLocation().toString();
        }

        return "";
    }

    @Override
    public void updateInPerspective(ActionEvent e) {
        e.getPresentation().setEnabledAndVisible(true);
    }
}
