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

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.api.machine.shared.dto.MachineProcessDto;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.macro.MacroProcessor;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.project.ProjectServiceClient;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.CommandOutputConsole;
import org.eclipse.che.ide.extension.machine.client.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.util.UUID;
import org.eclipse.che.plugin.artik.ide.command.macro.ReplicationFolderMacro;
import org.eclipse.che.plugin.artik.ide.machine.DeviceServiceClient;
import org.eclipse.che.plugin.artik.ide.outputconsole.ArtikCommandConsoleFactory;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.plugin.artik.ide.command.macro.BinaryNameMacro.DEFAULT_BINARY_NAME;
import static org.eclipse.che.plugin.cpp.shared.Constants.BINARY_NAME_ATTRIBUTE;

/**
 * Run binary file.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class BinaryRunner {
    private final DtoFactory                 dtoFactory;
    private final AppContext                 appContext;
    private final NotificationManager        notificationManager;
    private final ProjectServiceClient       projectService;
    private final ArtikCommandConsoleFactory consoleFactory;
    private final DeviceServiceClient        deviceServiceClient;
    private final MacroProcessor             macroProcessor;
    private final ProcessesPanelPresenter    processesPanelPresenter;

    @Inject
    public BinaryRunner(DtoFactory dtoFactory,
                        AppContext appContext,
                        NotificationManager notificationManager,
                        ProjectServiceClient projectService,
                        ArtikCommandConsoleFactory consoleFactory,
                        MacroProcessor macroProcessor,
                        DeviceServiceClient deviceServiceClient,
                        ProcessesPanelPresenter processesPanelPresenter) {
        this.dtoFactory = dtoFactory;
        this.appContext = appContext;
        this.notificationManager = notificationManager;
        this.projectService = projectService;
        this.consoleFactory = consoleFactory;
        this.deviceServiceClient = deviceServiceClient;
        this.macroProcessor = macroProcessor;
        this.processesPanelPresenter = processesPanelPresenter;
    }


    /**
     * Run binary file.
     *
     * @param device
     *         current device.
     */
    public void run(final Machine device) {
        Optional<Project> projectOptional = getCurrentProject();
        if (!projectOptional.isPresent()) {
            return;
        }

        Project project = projectOptional.get();
        final Command command = buildCommand(project, device);
        final String outputChannel = "process:output:" + UUID.uuid();
        String binaryName = project.getAttribute(BINARY_NAME_ATTRIBUTE);
        Path binaryNamePath = project.getLocation().append(!isNullOrEmpty(binaryName) ? binaryName : DEFAULT_BINARY_NAME);
        projectService.getItem(binaryNamePath).then(itemReference -> {
            macroProcessor.expandMacros(command.getCommandLine()).then(arg -> {
                final CommandDto toExecute = dtoFactory.createDto(CommandDto.class)
                                                       .withName(command.getName())
                                                       .withCommandLine(arg)
                                                       .withType(command.getType());

                final Promise<MachineProcessDto> processPromise = deviceServiceClient.executeCommand(device.getId(),
                                                                                                     toExecute,
                                                                                                     outputChannel);
                processPromise.then(process -> {
                    final CommandOutputConsole commandOutputConsole = consoleFactory.create(new CommandImpl(toExecute), device);
                    commandOutputConsole.listenToOutput(outputChannel);
                    processesPanelPresenter.addCommandOutput(device.getId(), commandOutputConsole);

                    commandOutputConsole.attachToProcess(process);
                });
            });
        }).catchError(promiseError -> {
            notificationManager.notify("",
                                       "No binary file found. Compile your app and re-run.",
                                       StatusNotification.Status.FAIL,
                                       StatusNotification.DisplayMode.EMERGE_MODE);
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
