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
package org.eclipse.che.plugin.artik.ide.debug;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineSource;
import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.JsPromiseError;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.debug.DebugConfiguration;
import org.eclipse.che.ide.api.debug.DebugConfigurationType;
import org.eclipse.che.ide.api.debug.DebugConfigurationsManager;
import org.eclipse.che.ide.api.machine.ExecAgentCommandManager;
import org.eclipse.che.ide.api.macro.MacroProcessor;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.project.ProjectServiceClient;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.CommandConsoleFactory;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.DefaultOutputConsole;
import org.eclipse.che.ide.extension.machine.client.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.plugin.artik.ide.command.macro.ReplicationFolderMacro;
import org.eclipse.che.plugin.debugger.ide.configuration.DebugConfigurationTypeRegistry;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.gwt.json.client.JSONParser.parseLenient;
import static org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper.createFromAsyncRequest;
import static org.eclipse.che.plugin.artik.ide.command.macro.BinaryNameMacro.DEFAULT_BINARY_NAME;
import static org.eclipse.che.plugin.cpp.shared.Constants.BINARY_NAME_ATTRIBUTE;

/**
 * Connects to the debugger for debugging project's binary file.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class DebuggerConnector {

    private final DtoFactory                     dtoFactory;
    private final ProjectServiceClient           projectService;
    private final AppContext                     appContext;
    private final ExecAgentCommandManager        execAgentCommandManager;
    private final MacroProcessor                 macroProcessor;
    private final DebugConfigurationTypeRegistry debugConfigurationTypeRegistry;
    private final DebugConfigurationsManager     debugConfigurationsManager;
    private final NotificationManager            notificationManager;

    private final ProcessesPanelPresenter processesPanelPresenter;
    private final CommandConsoleFactory   commandConsoleFactory;

    private AsyncCallback<Integer> runCallback;

    @Inject
    public DebuggerConnector(DtoFactory dtoFactory,
                             ProjectServiceClient projectService,
                             AppContext appContext,
                             MacroProcessor macroProcessor,
                             ExecAgentCommandManager execAgentCommandManager,
                             DebugConfigurationTypeRegistry debugConfigurationTypeRegistry,
                             DebugConfigurationsManager debugConfigurationsManager,
                             NotificationManager notificationManager,
                             ProcessesPanelPresenter processesPanelPresenter,
                             CommandConsoleFactory commandConsoleFactory) {
        this.dtoFactory = dtoFactory;
        this.projectService = projectService;
        this.appContext = appContext;
        this.execAgentCommandManager = execAgentCommandManager;
        this.macroProcessor = macroProcessor;
        this.debugConfigurationTypeRegistry = debugConfigurationTypeRegistry;
        this.debugConfigurationsManager = debugConfigurationsManager;
        this.notificationManager = notificationManager;
        this.processesPanelPresenter = processesPanelPresenter;
        this.commandConsoleFactory = commandConsoleFactory;
    }

    /**
     * Run GDB in the specified {@code machine} and connects
     * to the debugger on the specified {@code machine}
     * for debugging project's binary file.
     */
    public void debug(final Machine machine) {
        Optional<Project> projectOptional = getCurrentProject();
        if (!projectOptional.isPresent()) {
            return;
        }

        Project project = projectOptional.get();

        String binaryName = project.getAttribute(BINARY_NAME_ATTRIBUTE);
        projectService.getItem(project.getLocation().append(!isNullOrEmpty(binaryName) ? binaryName : DEFAULT_BINARY_NAME))
                      .then(itemReference -> {
                          checkPortOccupation(machine, project);
                      }).catchError(noFileError -> {
            notificationManager.notify("",
                                       "No binary file found. Compile your app and re-run debug.",
                                       StatusNotification.Status.FAIL,
                                       StatusNotification.DisplayMode.EMERGE_MODE);
        });
    }

    private void checkPortOccupation(final Machine device, final Project project) {
        final String commandLine = "process=$(netstat -nlp | grep :1234 | awk '{print $7}' | sed 's/\\/gdbserver//')\n" +
                                   "        if [[ $process ]]; then\n" +
                                   "          echo 'Stopping GDB server...'\n" +
                                   "          kill $process\n" +
                                   "          echo 'GDB server stopped'\n" +
                                   "        else\n" +
                                   "          echo 'GDB server is not running'\n" +
                                   "      fi";
        final String commandName = "kill process";
        final String commandType = "custom";
        final CommandDto commandDto = dtoFactory.createDto(CommandDto.class)
                                                .withName(commandName)
                                                .withCommandLine(commandLine)
                                                .withType(commandType);

        final String deviceId = device.getId();
        execAgentCommandManager.startProcess(deviceId, commandDto)
                               .thenIfProcessDiedEvent(processDied -> runGdbServer(device, project).then(port -> {
                                   if (port != null) {
                                       connect(device, port);
                                   }
                               }).catchError(promiseError -> {
                                   notificationManager.notify("", promiseError.getMessage());
                               }));
    }

    /** Runs GDB server and returns the listened port. */
    private Promise<Integer> runGdbServer(final Machine machine, final Project project) {
        final int debugPort = 1234;

        final Promise<Integer> promise = createFromAsyncRequest(callback -> runCallback = callback);

        final Command command = buildCommand(project, machine, debugPort);
        final DefaultOutputConsole console = (DefaultOutputConsole)commandConsoleFactory.create(command.getName());

        macroProcessor.expandMacros(command.getCommandLine()).then(arg -> {
            final CommandDto commandDto = dtoFactory.createDto(CommandDto.class)
                                                    .withName(command.getName())
                                                    .withCommandLine(arg)
                                                    .withType(command.getType());

            execAgentCommandManager.startProcess(machine.getId(), commandDto)
                                   .then(process -> processesPanelPresenter.addCommandOutput(machine.getId(), console))
                                   .thenIfProcessStdOutEvent(processStdOut -> {
                                       if (isSuccessMessage(processStdOut.getText())) {
                                           runCallback.onSuccess(debugPort);
                                       }
                                       console.printText(processStdOut.getText());
                                   })
                                   .thenIfProcessStdErrEvent(processStdErr -> {
                                       if (isSuccessMessage(processStdErr.getText())) {
                                           runCallback.onSuccess(debugPort);
                                       }
                                       console.printText(processStdErr.getText());
                                   });
        });


        return promise;
    }

    private static boolean isSuccessMessage(String message) {
        return message.contains("Listening on port");
    }

    /** Connect the debugger to the specified device. */
    private void connect(final Machine machine, final int debugPort) {
        final DebugConfigurationType gdbType = debugConfigurationTypeRegistry.getConfigurationTypeById("gdb");
        if (gdbType == null) {
            return;
        }

        final Map<String, String> connectionProperties = new HashMap<>();

        macroProcessor.expandMacros("${current.project.path}/${binary.name}").then(cmd -> {
            connectionProperties.put("BINARY", cmd);

            getDeviceIP(machine).then(ip -> {
                DebugConfiguration debugConfiguration = new DebugConfiguration(gdbType,
                                                                               "debug",
                                                                               ip,
                                                                               debugPort,
                                                                               connectionProperties);
                debugConfigurationsManager.apply(debugConfiguration);
            }).catchError(arg -> {
                notificationManager.notify("", arg.getMessage());
            });
        });
    }

    /** Read the specified devices's IP from it's config. */
    private Promise<String> getDeviceIP(Machine machine) {
        return getValueOfMachineContent(machine, "host");
    }

    private Promise<String> getValueOfMachineContent(Machine machine, String key) {
        final MachineSource source = machine.getConfig().getSource();
        if (!"ssh-config".equals(source.getType())) {
            return Promises.reject(JsPromiseError.create(new Exception("Can't get machine's address. Machine " +
                                                                       machine.getConfig().getName() +
                                                                       " isn't Artik device.")));
        }

        final String content = machine.getConfig().getSource().getContent();
        if (Strings.isNullOrEmpty(content)) {
            return Promises.resolve("");
        }
        final JSONObject jsonObject = parseLenient(content).isObject();
        final String value = jsonObject.get(key).isString().stringValue();
        return Promises.resolve(value);
    }

    private Command buildCommand(Project project, Machine machine, int debugPort) {
        final String commandName = "run gdbserver";
        final String commandType = "custom";

        StringBuilder commandLine = new StringBuilder("cd ").append(ReplicationFolderMacro.KEY.replace("%machineId%", machine.getId()))
                                                            .append("/${current.project.path} && gdbserver :")
                                                            .append(debugPort);

        String binaryName = project.getAttribute(BINARY_NAME_ATTRIBUTE);
        commandLine.append(' ').append(!isNullOrEmpty(binaryName) ? binaryName : DEFAULT_BINARY_NAME);

        return new CommandImpl(commandName, commandLine.toString(), commandType);
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
