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

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

import org.eclipse.che.api.machine.server.event.MachineProcessMessenger;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.plugin.machine.ssh.SshMachineModule;

/**
 * Provides bindings needed for artik machine implementation usage.
 *
 * @author Alexander Garagatyi
 */
@DynaModule
public class ArtikMachineModule extends AbstractModule {
    @Override
    protected void configure() {

        bindConstant().annotatedWith(Names.named("machine.terminal_agent.run_command"))
                      .to("$HOME/che/terminal/che-websocket-terminal " +
                          "-addr :4411 " +
                          "-cmd ${SHELL_INTERPRETER} " +
                          "-static $HOME/che/terminal/ " +
                          "-logs-dir $HOME/che/exec-agent/logs");

        install(new SshMachineModule());
        bind(ArtikDeviceInstanceProvider.class);

        Multibinder<org.eclipse.che.api.core.model.machine.ServerConf> machineServers =
                Multibinder.newSetBinder(binder(),
                                         org.eclipse.che.api.core.model.machine.ServerConf.class,
                                         Names.named("machine.ssh.machine_servers"));
        machineServers.addBinding().toProvider(TerminalServerConfProvider.class);

        bind(MachineProcessMessenger.class).asEagerSingleton();
        bind(ArtikDeviceStateMessenger.class).asEagerSingleton();

        bind(ArtikTerminalLauncher.class);

        bind(ArtikDeviceService.class);
    }
}
