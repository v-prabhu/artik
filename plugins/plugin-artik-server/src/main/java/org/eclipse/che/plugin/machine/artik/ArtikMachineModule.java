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

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

import org.eclipse.che.api.machine.server.event.MachineProcessMessenger;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.plugin.machine.ssh.SshMachineFactory;

/**
 * Provides bindings needed for artik machine implementation usage.
 *
 * @author Alexander Garagatyi
 */
@DynaModule
public class ArtikMachineModule extends AbstractModule {
    @Override
    protected void configure() {
        Multibinder<org.eclipse.che.api.machine.server.spi.InstanceProvider> machineProviderMultibinder =
                Multibinder.newSetBinder(binder(),
                                         org.eclipse.che.api.machine.server.spi.InstanceProvider.class);
        machineProviderMultibinder.addBinding()
                                  .to(org.eclipse.che.plugin.machine.artik.ArtikDeviceInstanceProvider.class);

        install(new FactoryModuleBuilder()
                        .implement(org.eclipse.che.api.machine.server.spi.Instance.class,
                                   org.eclipse.che.plugin.machine.ssh.SshMachineInstance.class)
                        .implement(org.eclipse.che.api.machine.server.spi.InstanceProcess.class,
                                   org.eclipse.che.plugin.machine.ssh.SshMachineProcess.class)
                        .implement(org.eclipse.che.plugin.machine.ssh.SshClient.class,
                                   org.eclipse.che.plugin.machine.ssh.jsch.JschSshClient.class)
                        .build(SshMachineFactory.class));

        Multibinder<org.eclipse.che.api.core.model.machine.ServerConf> machineServers =
                Multibinder.newSetBinder(binder(),
                                         org.eclipse.che.api.core.model.machine.ServerConf.class,
                                         Names.named("machine.ssh.machine_servers"));
        machineServers.addBinding().toProvider(TerminalServerConfProvider.class);

        bind(MachineProcessMessenger.class).asEagerSingleton();
        bind(ArtikDeviceStateMessenger.class).asEagerSingleton();

        Multibinder.newSetBinder(binder(), org.eclipse.che.api.agent.server.launcher.AgentLauncher.class)
                   .addBinding().to(ArtikTerminalLauncher.class);

        bind(ArtikDeviceService.class);
    }
}
