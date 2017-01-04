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
package org.eclipse.che.plugin.machine.artik;

import org.eclipse.che.api.agent.server.AgentRegistry;
import org.eclipse.che.api.agent.server.exception.AgentException;
import org.eclipse.che.api.agent.server.model.impl.AgentKeyImpl;
import org.eclipse.che.api.agent.shared.model.Agent;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.plugin.machine.ssh.SshMachineImplTerminalLauncher;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Launch websocket terminal in Artik device.
 *
 * @author Valeriy Svydenko
 */
public class ArtikTerminalLauncher extends SshMachineImplTerminalLauncher {
    public static final  String TERMINAL_LOCATION_PROPERTY       = "artik.terminal.location";
    private static final String ARTIK_TERMINAL_AGENT_RUN_COMMAND = "artik.terminal_agent.run_command";
    private static final String ARTIK_MACHINE_TYPE               = "artik";
    private static final long   TERMINAL_AGENT_MAX_START_TIME_MS = 120_000;
    private static final long   TERMINAL_AGENT_PING_DELAY_MS     = 2000;

    private final AgentRegistry agentRegistry;

    @Inject
    public ArtikTerminalLauncher(@Named(TERMINAL_LOCATION_PROPERTY) String terminalLocation,
                                 @Named(ARTIK_TERMINAL_AGENT_RUN_COMMAND) String terminalRunCommand,
                                 ArtikDeviceTerminalFilesPathProvider terminalPathProvider,
                                 AgentRegistry agentRegistry) {
        super(TERMINAL_AGENT_MAX_START_TIME_MS, TERMINAL_AGENT_PING_DELAY_MS, terminalLocation, terminalRunCommand, terminalPathProvider);
        this.agentRegistry = agentRegistry;
    }

    public void launch(Instance machine) throws ServerException {
        Agent agent;
        try {
            agent = agentRegistry.getAgent(AgentKeyImpl.parse("org.eclipse.che.terminal"));
        } catch (AgentException e) {
            throw new ServerException("org.eclipse.che.terminal agent not found");
        }

        super.launch(machine, agent);
    }

    @Override
    public String getMachineType() {
        return ARTIK_MACHINE_TYPE;
    }
}
