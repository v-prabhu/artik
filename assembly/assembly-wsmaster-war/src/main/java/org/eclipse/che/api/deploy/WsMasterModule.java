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
package org.eclipse.che.api.deploy;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

import org.eclipse.che.account.api.AccountModule;
import org.eclipse.che.api.agent.server.launcher.AgentLauncher;
import org.eclipse.che.api.core.rest.CheJsonProvider;
import org.eclipse.che.api.core.rest.MessageBodyAdapter;
import org.eclipse.che.api.core.rest.MessageBodyAdapterInterceptor;
import org.eclipse.che.api.machine.server.jpa.MachineJpaModule;
import org.eclipse.che.api.machine.shared.Constants;
import org.eclipse.che.api.ssh.server.jpa.SshJpaModule;
import org.eclipse.che.api.user.server.TokenValidator;
import org.eclipse.che.api.user.server.jpa.UserJpaModule;
import org.eclipse.che.api.workspace.server.jpa.WorkspaceJpaModule;
import org.eclipse.che.core.db.schema.SchemaInitializer;
import org.eclipse.che.inject.DynaModule;
import org.everrest.guice.ServiceBindingHelper;
import org.flywaydb.core.internal.util.PlaceholderReplacer;

import javax.sql.DataSource;

import static com.google.inject.matcher.Matchers.subclassesOf;
import static org.eclipse.che.inject.Matchers.names;

/** @author andrew00x */
@DynaModule
public class WsMasterModule extends AbstractModule {
    @Override
    protected void configure() {
        // db configuration
        bind(DataSource.class).toProvider(org.eclipse.che.core.db.h2.H2DataSourceProvider.class);
        bind(SchemaInitializer.class).to(org.eclipse.che.core.db.schema.impl.flyway.FlywaySchemaInitializer.class);
        bind(org.eclipse.che.core.db.DBInitializer.class).asEagerSingleton();
        bind(PlaceholderReplacer.class).toProvider(org.eclipse.che.core.db.schema.impl.flyway.PlaceholderReplacerProvider.class);
        install(new com.google.inject.persist.jpa.JpaPersistModule("main"));

        bind(org.eclipse.che.api.user.server.CheUserCreator.class);

        install(new UserJpaModule());
        install(new SshJpaModule());
        install(new WorkspaceJpaModule());
        install(new AccountModule());
        install(new MachineJpaModule());
        bind(TokenValidator.class).to(org.eclipse.che.api.local.DummyTokenValidator.class);
        bind(org.eclipse.che.api.local.LocalDataMigrator.class).asEagerSingleton();

        bind(org.eclipse.che.api.core.rest.ApiInfoService.class);
        bind(org.eclipse.che.api.project.server.template.ProjectTemplateDescriptionLoader.class).asEagerSingleton();
        bind(org.eclipse.che.api.project.server.template.ProjectTemplateRegistry.class);
        bind(org.eclipse.che.api.project.server.template.ProjectTemplateService.class);
        bind(org.eclipse.che.api.ssh.server.SshService.class);
        bind(org.eclipse.che.api.machine.server.recipe.RecipeService.class);
        bind(org.eclipse.che.api.user.server.UserService.class);
        bind(org.eclipse.che.api.user.server.ProfileService.class);
        bind(org.eclipse.che.api.user.server.PreferencesService.class);
        bind(org.eclipse.che.api.workspace.server.stack.StackLoader.class);
        bind(org.eclipse.che.api.workspace.server.stack.StackService.class);
        bind(org.eclipse.che.api.workspace.server.WorkspaceService.class);
        bind(org.eclipse.che.api.workspace.server.event.WorkspaceMessenger.class).asEagerSingleton();
        bind(org.eclipse.che.api.workspace.server.TemporaryWorkspaceRemover.class);
        bind(org.everrest.core.impl.async.AsynchronousJobPool.class).to(org.eclipse.che.everrest.CheAsynchronousJobPool.class);
        bind(ServiceBindingHelper.bindingKey(org.everrest.core.impl.async.AsynchronousJobService.class, "/async/{ws-id}"))
                .to(org.everrest.core.impl.async.AsynchronousJobService.class);
        bind(org.eclipse.che.plugin.docker.machine.ext.DockerMachineExtServerChecker.class);
        bind(org.eclipse.che.plugin.docker.machine.ext.DockerMachineTerminalChecker.class);
        bind(org.eclipse.che.everrest.EverrestDownloadFileResponseFilter.class);
        bind(org.eclipse.che.everrest.ETagResponseFilter.class);

        bind(org.eclipse.che.security.oauth.OAuthAuthenticatorProvider.class)
                .to(org.eclipse.che.security.oauth.OAuthAuthenticatorProviderImpl.class);
        bind(org.eclipse.che.api.auth.oauth.OAuthTokenProvider.class)
                .to(org.eclipse.che.security.oauth.OAuthAuthenticatorTokenProvider.class);
        bind(org.eclipse.che.security.oauth.OAuthAuthenticationService.class);

        bind(org.eclipse.che.api.core.notification.WSocketEventBusServer.class);
        // additional ports for development of extensions
        Multibinder<org.eclipse.che.api.core.model.machine.ServerConf> machineServers = Multibinder.newSetBinder(binder(),
                                                                                   org.eclipse.che.api.core.model.machine.ServerConf.class,
                                                                                   Names.named("machine.docker.dev_machine.machine_servers"));
        machineServers.addBinding().toInstance(
                new org.eclipse.che.api.machine.server.model.impl.ServerConfImpl(Constants.WSAGENT_DEBUG_REFERENCE, "4403/tcp", "http",
                                                                                 null));

        bind(org.eclipse.che.api.machine.server.recipe.RecipeLoader.class);
        Multibinder.newSetBinder(binder(), String.class, Names.named("predefined.recipe.path"))
                   .addBinding()
                   .toInstance("predefined-recipes.json");

        bindConstant().annotatedWith(Names.named("machine.ws_agent.run_command"))
                      .to("export JPDA_ADDRESS=\"4403\" && ~/che/ws-agent/bin/catalina.sh jpda run");
        bindConstant().annotatedWith(Names.named("machine.terminal_agent.run_command"))
                      .to("$HOME/che/terminal/che-websocket-terminal " +
                          "-addr :4411 " +
                          "-cmd ${SHELL_INTERPRETER} " +
                          "-static $HOME/che/terminal/ " +
                          "-logs-dir $HOME/che/exec-agent/logs");

        bind(org.eclipse.che.api.workspace.server.WorkspaceValidator.class)
                .to(org.eclipse.che.api.workspace.server.DefaultWorkspaceValidator.class);

        bind(org.eclipse.che.api.workspace.server.event.MachineStateListener.class).asEagerSingleton();

        bind(org.eclipse.che.api.agent.server.AgentRegistry.class)
                .to(org.eclipse.che.api.agent.server.impl.LocalAgentRegistryImpl.class);

        Multibinder<AgentLauncher> agentLaunchers = Multibinder.newSetBinder(binder(), AgentLauncher.class);
        agentLaunchers.addBinding().to(org.eclipse.che.api.workspace.server.launcher.WsAgentLauncherImpl.class);
        agentLaunchers.addBinding().to(org.eclipse.che.api.workspace.server.launcher.TerminalAgentLauncherImpl.class);
        agentLaunchers.addBinding().to(org.eclipse.che.api.workspace.server.launcher.SshAgentLauncherImpl.class);

        bind(org.eclipse.che.api.deploy.WsMasterAnalyticsAddresser.class);

        Multibinder<org.eclipse.che.api.machine.server.spi.InstanceProvider> machineImageProviderMultibinder =
                Multibinder.newSetBinder(binder(), org.eclipse.che.api.machine.server.spi.InstanceProvider.class);
        machineImageProviderMultibinder.addBinding().to(org.eclipse.che.plugin.docker.machine.DockerInstanceProvider.class);

        bind(org.eclipse.che.api.environment.server.MachineInstanceProvider.class)
                .to(org.eclipse.che.plugin.docker.machine.MachineProviderImpl.class);

        install(new org.eclipse.che.api.core.rest.CoreRestModule());
        install(new org.eclipse.che.api.core.util.FileCleaner.FileCleanerModule());
        install(new org.eclipse.che.plugin.docker.machine.local.LocalDockerModule());
        install(new org.eclipse.che.api.machine.server.MachineModule());
        install(new org.eclipse.che.api.agent.server.AgentModule());
        install(new org.eclipse.che.plugin.docker.machine.ext.DockerExtServerModule());
        install(new org.eclipse.che.swagger.deploy.DocsModule());
        install(new org.eclipse.che.plugin.machine.ssh.SshMachineModule());
        install(new org.eclipse.che.plugin.docker.machine.proxy.DockerProxyModule());
        install(new org.eclipse.che.commons.schedule.executor.ScheduleModule());

        final Multibinder<MessageBodyAdapter> adaptersMultibinder = Multibinder.newSetBinder(binder(), MessageBodyAdapter.class);
        adaptersMultibinder.addBinding().to(org.eclipse.che.api.workspace.server.WorkspaceConfigMessageBodyAdapter.class);
        adaptersMultibinder.addBinding().to(org.eclipse.che.api.workspace.server.WorkspaceMessageBodyAdapter.class);
        adaptersMultibinder.addBinding().to(org.eclipse.che.api.workspace.server.stack.StackMessageBodyAdapter.class);

        final MessageBodyAdapterInterceptor interceptor = new MessageBodyAdapterInterceptor();
        requestInjection(interceptor);
        bindInterceptor(subclassesOf(CheJsonProvider.class), names("readFrom"), interceptor);
        bind(org.eclipse.che.api.workspace.server.WorkspaceFilesCleaner.class)
                .to(org.eclipse.che.plugin.docker.machine.cleaner.LocalWorkspaceFilesCleaner.class);
        bind(org.eclipse.che.api.agent.server.WsAgentHealthChecker.class)
                .to(org.eclipse.che.api.agent.server.WsAgentHealthCheckerImpl.class);
        bind(org.eclipse.che.api.environment.server.InfrastructureProvisioner.class)
                .to(org.eclipse.che.plugin.docker.machine.local.LocalCheInfrastructureProvisioner.class);
    }
}
