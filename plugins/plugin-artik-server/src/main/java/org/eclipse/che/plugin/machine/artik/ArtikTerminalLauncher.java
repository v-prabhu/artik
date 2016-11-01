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
package org.eclipse.che.plugin.machine.artik;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.util.AbstractLineConsumer;
import org.eclipse.che.api.core.util.ListLineConsumer;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.model.impl.CommandImpl;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.server.spi.InstanceProcess;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Launch websocket terminal in Artik device.
 *
 * @author Valeriy Svydenko
 */
public class ArtikTerminalLauncher {
    private static final Logger LOG = getLogger(ArtikTerminalLauncher.class);

    public static final String TERMINAL_LAUNCH_COMMAND_PROPERTY = "artik.terminal.run_command";
    public static final String TERMINAL_LOCATION_PROPERTY       = "artik.terminal.location";

    private final String                               runTerminalCommand;
    private final String                               terminalLocation;
    private final ArtikDeviceTerminalFilesPathProvider archivePathProvider;

    @Inject
    public ArtikTerminalLauncher(@Named(TERMINAL_LAUNCH_COMMAND_PROPERTY) String runTerminalCommand,
                                 @Named(TERMINAL_LOCATION_PROPERTY) String terminalLocation,
                                 ArtikDeviceTerminalFilesPathProvider terminalPathProvider) {
        this.runTerminalCommand = runTerminalCommand;
        this.terminalLocation = terminalLocation;
        this.archivePathProvider = terminalPathProvider;
    }

    public void launchTerminal(Instance artik) throws MachineException {
        try {
            if (!isWebsocketTerminalRunning(artik)) {
                artik.copy(archivePathProvider.getPath("linux_arm7"), terminalLocation);
                startTerminal(artik);
            }
        } catch (ConflictException e) {
            throw new MachineException("Internal server error occurs on terminal launching.");
        }
    }

    private boolean isWebsocketTerminalRunning(Instance artik) throws MachineException, ConflictException {
        InstanceProcess checkTerminalAlive = artik.createProcess(
                new CommandImpl("check if che websocket terminal is running",
                                "ps ax | grep 'che-websocket-terminal' | grep -q -v 'grep che-websocket-terminal' && echo 'found' || echo" +
                                " 'not found'",
                                null),
                null);
        ListLineConsumer lineConsumer = new ListLineConsumer();
        checkTerminalAlive.start(lineConsumer);
        String checkAliveText = lineConsumer.getText();
        if ("[STDOUT] not found".equals(checkAliveText)) {
            return false;
        } else if (!"[STDOUT] found".equals(checkAliveText)) {
            LOG.error("Unexpected output of websocket terminal check. Output:" + checkAliveText);
            return false;
        }
        return true;
    }

    private void startTerminal(Instance artik) throws MachineException, ConflictException {
        InstanceProcess startTerminal = artik.createProcess(new CommandImpl("websocket terminal",
                                                                            runTerminalCommand,
                                                                            null),
                                                            null);

        startTerminal.start(new DeviceLineConsumer(artik));
    }

    private static class DeviceLineConsumer extends AbstractLineConsumer {
        private final Instance instance;

        DeviceLineConsumer(Instance instance) {
            this.instance = instance;
        }

        @Override
        public void writeLine(String line) throws IOException {
            instance.getLogger().writeLine("[Terminal] " + line);
        }
    }
}
