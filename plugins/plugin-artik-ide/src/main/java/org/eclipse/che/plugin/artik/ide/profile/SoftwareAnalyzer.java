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
package org.eclipse.che.plugin.artik.ide.profile;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.machine.ExecAgentCommandManager;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.plugin.artik.ide.ArtikResources;

import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper.createFromAsyncRequest;
import static org.eclipse.che.plugin.artik.ide.profile.Software.GDB_SERVER;
import static org.eclipse.che.plugin.artik.ide.profile.Software.RSYNC;

/**
 * @author Dmitry Kuleshov
 */
@Singleton
public class SoftwareAnalyzer {
    private static Software[] REQUIRED_SOFTWARE = Software.values();

    private final DtoFactory              dtoFactory;
    private final ExecAgentCommandManager execAgentCommandManager;

    @Inject
    public SoftwareAnalyzer(DtoFactory dtoFactory,
                            ArtikResources artikResources,
                            ExecAgentCommandManager execAgentCommandManager) {
        this.dtoFactory = dtoFactory;
        this.execAgentCommandManager = execAgentCommandManager;


        GDB_SERVER.setVerificationCommand(artikResources.gdbServerVerificationCommand().getText());
        RSYNC.setVerificationCommand(artikResources.rsyncVerificationCommand().getText());
    }

    public Promise<Set<Software>> getMissingSoft(final String machineId) {
        Log.debug(getClass(), "Verifying software for machine: " + machineId);

        final Command command = getCommand();

        final Promise<Set<Software>> promise = createFromAsyncRequest(callback -> {
            final Set<Software> missingSoftware = new HashSet<>(asList(REQUIRED_SOFTWARE));

            execAgentCommandManager.startProcess(machineId, command)
                                   .thenIfProcessStdOutEvent(psStdOut -> {
                                       String message = psStdOut.getText();
                                       if (message.contains(GDB_SERVER.name)) {
                                           missingSoftware.remove(GDB_SERVER);

                                           Log.debug(getClass(), "Debug: " + machineId + ", " + message);
                                       } else if (message.contains(RSYNC.name)) {
                                           missingSoftware.remove(RSYNC);

                                           Log.debug(getClass(), "Debug: " + machineId + ", " + message);
                                       }
                                   })
                                   .thenIfProcessStdErrEvent(
                                           psStdErr -> Log.error(getClass(), "Error: " + machineId + ", " + psStdErr.getText()))
                                   .thenIfProcessDiedEvent(psDied -> callback.onSuccess(missingSoftware));
        });


        Log.debug(getClass(), "Verification command: " + command);

        return promise;
    }

    private Command getCommand() {
        final StringBuilder commandLineBuilder = new StringBuilder();
        for (Software softwareType : REQUIRED_SOFTWARE) {
            final String checkCommand = softwareType.getVerificationCommand();
            commandLineBuilder.append(checkCommand);
            commandLineBuilder.append("\n");
        }
        final String commandLine = commandLineBuilder.toString();
        final String commandName = "get-missing-software";
        final String commandType = "custom";

        return dtoFactory.createDto(CommandDto.class)
                         .withName(commandName)
                         .withType(commandType)
                         .withCommandLine(commandLine);
    }

}
