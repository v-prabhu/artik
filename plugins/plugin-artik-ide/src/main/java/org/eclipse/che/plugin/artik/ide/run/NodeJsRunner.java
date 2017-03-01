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
package org.eclipse.che.plugin.artik.ide.run;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.machine.ExecAgentCommandManager;
import org.eclipse.che.ide.api.macro.MacroProcessor;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.CommandConsoleFactory;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.CommandOutputConsole;
import org.eclipse.che.ide.extension.machine.client.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.ide.part.explorer.project.macro.ExplorerCurrentFileNameMacro;
import org.eclipse.che.ide.part.explorer.project.macro.ExplorerCurrentFileParentPathMacro;
import org.eclipse.che.plugin.artik.ide.command.macro.NodeJsRunParametersMacro;
import org.eclipse.che.plugin.artik.ide.command.macro.ReplicationFolderMacro;

/**
 * Run NodeJs file.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class NodeJsRunner {
    private final DtoFactory                         dtoFactory;
    private final ExecAgentCommandManager            execAgentCommandManager;
    private final CommandConsoleFactory              consoleFactory;
    private final MacroProcessor                     macroProcessor;
    private final NodeJsRunParametersMacro           nodeJsRunParametersMacro;
    private final ExplorerCurrentFileParentPathMacro currentFileParentPathMacro;
    private final ExplorerCurrentFileNameMacro       currentFileNameMacro;
    private final ProcessesPanelPresenter            processesPanelPresenter;

    @Inject
    public NodeJsRunner(DtoFactory dtoFactory,
                        ExecAgentCommandManager execAgentCommandManager,
                        MacroProcessor macroProcessor,
                        CommandConsoleFactory consoleFactory,
                        NodeJsRunParametersMacro nodeJsRunOptionsMacro,
                        ExplorerCurrentFileParentPathMacro currentFileParentPathMacro,
                        ExplorerCurrentFileNameMacro currentFileNameMacro,
                        ProcessesPanelPresenter processesPanelPresenter) {
        this.dtoFactory = dtoFactory;
        this.execAgentCommandManager = execAgentCommandManager;
        this.consoleFactory = consoleFactory;
        this.macroProcessor = macroProcessor;
        this.nodeJsRunParametersMacro = nodeJsRunOptionsMacro;
        this.currentFileParentPathMacro = currentFileParentPathMacro;
        this.currentFileNameMacro = currentFileNameMacro;
        this.processesPanelPresenter = processesPanelPresenter;
    }


    /**
     * Run NodeJs file.
     *
     * @param device
     *         current device.
     */
    public void run(final Machine device) {
        final Command command = buildCommand(device);

        macroProcessor.expandMacros(command.getCommandLine()).then(commandLine -> {
            final CommandDto toExecute = dtoFactory.createDto(CommandDto.class)
                                                   .withName(command.getName())
                                                   .withCommandLine(commandLine)
                                                   .withType(command.getType());

            final CommandOutputConsole commandOutputConsole = consoleFactory.create(new CommandImpl(toExecute), device);
            processesPanelPresenter.addCommandOutput(device.getId(), commandOutputConsole);

            execAgentCommandManager.startProcess(device.getId(), toExecute)
                                   .thenIfProcessStartedEvent(commandOutputConsole.getProcessStartedOperation())
                                   .thenIfProcessStdErrEvent(commandOutputConsole.getStdErrOperation())
                                   .thenIfProcessStdOutEvent(commandOutputConsole.getStdOutOperation())
                                   .thenIfProcessDiedEvent(commandOutputConsole.getProcessDiedOperation());

        });
    }

    private Command buildCommand(Machine machine) {
        String commandLine = "cd " +
                             ReplicationFolderMacro.KEY.replace("%machineId%", machine.getId()) +
                             currentFileParentPathMacro.getName() +
                             " && node " +
                             currentFileNameMacro.getName() +
                             ' ' + nodeJsRunParametersMacro.getName();

        return new CommandImpl("run", commandLine, "custom");
    }

}
