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
package org.eclipse.che.plugin.artik.ide.run;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.api.machine.shared.dto.MachineProcessDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.macro.MacroProcessor;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.CommandConsoleFactory;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.DefaultOutputConsole;
import org.eclipse.che.ide.extension.machine.client.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.ide.util.UUID;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.MessageBusProvider;
import org.eclipse.che.ide.websocket.WebSocketException;
import org.eclipse.che.ide.websocket.events.MessageHandler;
import org.eclipse.che.plugin.artik.ide.command.macro.ReplicationFolderMacro;
import org.eclipse.che.plugin.artik.ide.debug.ProcessListener;
import org.eclipse.che.plugin.artik.ide.machine.DeviceServiceClient;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.plugin.artik.ide.command.macro.BinaryNameMacro.DEFAULT_BINARY_NAME;
import static org.eclipse.che.plugin.cpp.shared.Constants.BINARY_NAME_ATTRIBUTE;

/**
 * Run binary file.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class BinaryFileRunner {
    private final DtoFactory              dtoFactory;
    private final AppContext              appContext;
    private final DeviceServiceClient     deviceServiceClient;
    private final MacroProcessor          macroProcessor;
    private final ProcessListener         processListener;
    private final MessageBus              messageBus;
    private final ProcessesPanelPresenter processesPanelPresenter;
    private final CommandConsoleFactory   commandConsoleFactory;

    @Inject
    public BinaryFileRunner(DtoFactory dtoFactory,
                            AppContext appContext,
                            MacroProcessor macroProcessor,
                            DeviceServiceClient deviceServiceClient,
                            ProcessListener processListener,
                            MessageBusProvider messageBusProvider,
                            ProcessesPanelPresenter processesPanelPresenter,
                            CommandConsoleFactory commandConsoleFactory) {
        this.dtoFactory = dtoFactory;
        this.appContext = appContext;
        this.deviceServiceClient = deviceServiceClient;
        this.macroProcessor = macroProcessor;
        this.processListener = processListener;
        this.messageBus = messageBusProvider.getMachineMessageBus();
        this.processesPanelPresenter = processesPanelPresenter;
        this.commandConsoleFactory = commandConsoleFactory;
    }


    /**
     * Run binary file.
     *
     * @param machine
     *         current device.
     */
    public void run(final Machine machine) {
        Optional<Project> projectOptional = getCurrentProject();
        if (!projectOptional.isPresent()) {
            return;
        }

        Project project = projectOptional.get();
        final Command command = buildCommand(project, machine);
        final String outputChannel = "process:output:" + UUID.uuid();
        final DefaultOutputConsole outputConsole = (DefaultOutputConsole)commandConsoleFactory.create(command.getName());

        final MessageHandler messageHandler = new MessageHandler() {
            @Override
            public void onMessage(String message) {
                outputConsole.printText(message);
            }
        };
        try {
            messageBus.subscribe(outputChannel, messageHandler);
        } catch (WebSocketException e) {
            //do nothing
        }

        macroProcessor.expandMacros(command.getCommandLine()).then(new Operation<String>() {
            @Override
            public void apply(String arg) throws OperationException {
                final CommandDto toExecute = dtoFactory.createDto(CommandDto.class)
                                                       .withName(command.getName())
                                                       .withCommandLine(arg)
                                                       .withType(command.getType());

                final Promise<MachineProcessDto> processPromise = deviceServiceClient.executeCommand(machine.getId(),
                                                                                                     toExecute,
                                                                                                     outputChannel);
                processPromise.then(new Operation<MachineProcessDto>() {
                    @Override
                    public void apply(MachineProcessDto process) throws OperationException {
                        processesPanelPresenter.addCommandOutput(machine.getId(), outputConsole);
                        processListener.attachToProcess(process, machine, outputChannel, messageHandler);
                    }
                });
            }
        });
    }

    private Command buildCommand(Project project, Machine machine) {
        StringBuilder commandLine = new StringBuilder("cd ").append(ReplicationFolderMacro.KEY.replace("%machineId%", machine.getId()))
                                                            .append("/${current.project.path} && ");

        String binaryName = project.getAttribute(BINARY_NAME_ATTRIBUTE);
        commandLine.append("./").append(!isNullOrEmpty(binaryName) ? binaryName : DEFAULT_BINARY_NAME);

        return new CommandImpl("run", commandLine.toString(), "custom");
    }

    private Optional<Project> getCurrentProject() {
        final Resource[] resources = appContext.getResources();
        if (resources == null || resources.length != 1) {
            return Optional.absent();
        }

        Resource resource = appContext.getResource();
        return resource.getRelatedProject();
    }
}
