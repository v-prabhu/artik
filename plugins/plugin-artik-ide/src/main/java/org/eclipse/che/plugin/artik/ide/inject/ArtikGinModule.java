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
package org.eclipse.che.plugin.artik.ide.inject;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.gwt.inject.client.multibindings.GinMapBinder;
import com.google.gwt.inject.client.multibindings.GinMultibinder;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.component.Component;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.api.machine.CheWsAgentLinksModifier;
import org.eclipse.che.ide.api.machine.CommandPropertyValueProvider;
import org.eclipse.che.ide.api.machine.WsAgentURLModifier;
import org.eclipse.che.ide.editor.orion.client.inject.OrionPlugin;
import org.eclipse.che.ide.extension.machine.client.command.producer.CommandProducer;
import org.eclipse.che.plugin.artik.ide.apidocs.DocsPartView;
import org.eclipse.che.plugin.artik.ide.apidocs.DocsPartViewImpl;
import org.eclipse.che.plugin.artik.ide.command.CompileCommandProducer;
import org.eclipse.che.plugin.artik.ide.command.RunCommandProducer;
import org.eclipse.che.plugin.artik.ide.command.macro.BinaryNameMacro;
import org.eclipse.che.plugin.artik.ide.command.macro.CompilationPropertiesMacro;
import org.eclipse.che.plugin.artik.ide.command.macro.ReplicationFolderMacroFactory;
import org.eclipse.che.plugin.artik.ide.debug.DebugBinaryActionFactory;
import org.eclipse.che.plugin.artik.ide.debug.DebugBinaryActionsManager;
import org.eclipse.che.plugin.artik.ide.discovery.DeviceDiscoveryServiceClient;
import org.eclipse.che.plugin.artik.ide.discovery.DeviceDiscoveryServiceClientImpl;
import org.eclipse.che.plugin.artik.ide.keyworddoc.KeywordDocsServiceClient;
import org.eclipse.che.plugin.artik.ide.keyworddoc.KeywordDocsServiceClientImpl;
import org.eclipse.che.plugin.artik.ide.orionplugin.ArtikOrionPlugin;
import org.eclipse.che.plugin.artik.ide.profile.ArtikModeActionFactory;
import org.eclipse.che.plugin.artik.ide.scp.action.PushToDeviceActionFactory;
import org.eclipse.che.plugin.artik.ide.updatesdk.UpdateSDKView;
import org.eclipse.che.plugin.artik.ide.updatesdk.UpdateSDKViewImpl;

/**
 * Gin module for Artik extension.
 *
 * @author Vitalii Parfonov
 * @author Artem Zatsarynnyi
 */
@ExtensionGinModule
public class ArtikGinModule extends AbstractGinModule {

    @Override
    protected void configure() {
        install(new GinFactoryModuleBuilder().build(PushToDeviceActionFactory.class));
        install(new GinFactoryModuleBuilder().build(ArtikModeActionFactory.class));

        bind(DeviceDiscoveryServiceClient.class).to(DeviceDiscoveryServiceClientImpl.class).in(Singleton.class);
        bind(WsAgentURLModifier.class).to(CheWsAgentLinksModifier.class);

        bind(UpdateSDKView.class).to(UpdateSDKViewImpl.class).in(Singleton.class);
        bind(DocsPartView.class).to(DocsPartViewImpl.class).in(Singleton.class);

        bind(KeywordDocsServiceClient.class).to(KeywordDocsServiceClientImpl.class).in(Singleton.class);

        GinMultibinder.newSetBinder(binder(), OrionPlugin.class).addBinding().to(ArtikOrionPlugin.class);

        GinMultibinder<CommandProducer> commandProducerMultibinder = GinMultibinder.newSetBinder(binder(), CommandProducer.class);
        commandProducerMultibinder.addBinding().to(CompileCommandProducer.class);
        commandProducerMultibinder.addBinding().to(RunCommandProducer.class);

        GinMultibinder<CommandPropertyValueProvider> macrosMultibinder = GinMultibinder.newSetBinder(binder(),
                                                                                                     CommandPropertyValueProvider.class);
        macrosMultibinder.addBinding().to(BinaryNameMacro.class);
        macrosMultibinder.addBinding().to(CompilationPropertiesMacro.class);

        install(new GinFactoryModuleBuilder().build(ReplicationFolderMacroFactory.class));

        GinMapBinder.newMapBinder(binder(), String.class, Component.class)
                    .addBinding("Debug binary actions manager")
                    .to(DebugBinaryActionsManager.class);
        install(new GinFactoryModuleBuilder().build(DebugBinaryActionFactory.class));
    }
}
