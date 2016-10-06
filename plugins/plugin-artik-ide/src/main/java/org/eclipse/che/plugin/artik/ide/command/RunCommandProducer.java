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
package org.eclipse.che.plugin.artik.ide.command;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.extension.machine.client.command.custom.CustomCommandType;
import org.eclipse.che.plugin.artik.ide.command.macro.ReplicationFolderMacro;

import java.util.HashSet;
import java.util.Set;

/**
 * Produces command for running C-file.
 * Applicable when current project type is C and any file is selected.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class RunCommandProducer extends AbstractArtikProducer {

    static final String COMMAND_NAME     = "Run";
    static final String COMMAND_TEMPLATE =
            "cd " + ReplicationFolderMacro.KEY + "${explorer.current.file.parent.path} && ./${explorer.current.file.name}";

    @Inject
    public RunCommandProducer(CustomCommandType customCommandType, DtoFactory dtoFactory, AppContext appContext) {
        super(COMMAND_NAME, customCommandType, dtoFactory, appContext);
    }

    @Override
    public boolean isApplicable() {
        if (!super.isApplicable()) {
            return false;
        }

        Optional<Resource> selectedResourceOptional = getSelectedResource();
        if (selectedResourceOptional.isPresent()) {
            return selectedResourceOptional.get().isFile();
        }

        return false;
    }

    @Override
    protected String getCommandLine(Machine machine) {
        return COMMAND_TEMPLATE.replace("%machineId%", machine.getId());
    }

    @Override
    public Set<String> getMachineTypes() {
        Set<String> set = new HashSet<>();
        set.add("ssh");
        set.add("artik");

        return set;
    }
}
