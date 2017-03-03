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
package org.eclipse.che.plugin.artik.ide.machine;

import com.google.inject.Inject;

import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.machine.shared.dto.MachineProcessDto;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.DevMachine;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;

import java.util.List;

/**
 * Implementation of {@link DeviceServiceClient}
 *
 * @author Valeriy Svydenko
 */
public class DeviceServiceClientImpl implements DeviceServiceClient {
    private final AppContext             appContext;
    private final LoaderFactory          loaderFactory;
    private final AsyncRequestFactory    asyncRequestFactory;
    private final DtoUnmarshallerFactory dtoUnmarshallerFactory;

    @Inject
    public DeviceServiceClientImpl(AppContext appContext,
                                   LoaderFactory loaderFactory,
                                   AsyncRequestFactory asyncRequestFactory,
                                   DtoUnmarshallerFactory dtoUnmarshallerFactory) {
        this.appContext = appContext;
        this.loaderFactory = loaderFactory;
        this.asyncRequestFactory = asyncRequestFactory;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
    }

    @Override
    public Promise<MachineDto> connect(MachineConfigDto config) {
        final DevMachine devMachine = appContext.getDevMachine();
        final String url = devMachine.getWsAgentBaseUrl() + "/artik/connect";

        return asyncRequestFactory.createPostRequest(url, config)
                                  .loader(loaderFactory.newLoader())
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(MachineDto.class));
    }

    @Override
    public Promise<List<MachineDto>> restore(List<MachineConfigDto> configs) {
        final DevMachine devMachine = appContext.getDevMachine();
        final String url = devMachine.getWsAgentBaseUrl() + "/artik/restore";

        return asyncRequestFactory.createPostRequest(url, configs)
                                  .loader(loaderFactory.newLoader())
                                  .send(dtoUnmarshallerFactory.newListUnmarshaller(MachineDto.class));
    }

    @Override
    public Promise<MachineDto> connectById(String deviceId) {
        final DevMachine devMachine = appContext.getDevMachine();
        final String url = devMachine.getWsAgentBaseUrl() + "/artik/connect/" + deviceId;

        return asyncRequestFactory.createGetRequest(url)
                                  .loader(loaderFactory.newLoader())
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(MachineDto.class));
    }

    @Override
    public Promise<MachineDto> getDevice(String machineId) {
        final DevMachine devMachine = appContext.getDevMachine();
        final String url = devMachine.getWsAgentBaseUrl() + "/artik/" + machineId;

        return asyncRequestFactory.createGetRequest(url)
                                  .loader(loaderFactory.newLoader())
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(MachineDto.class));
    }

    @Override
    public Promise<List<MachineDto>> getDevices() {
        final DevMachine devMachine = appContext.getDevMachine();
        final String url = devMachine.getWsAgentBaseUrl() + "/artik/devices";

        return asyncRequestFactory.createGetRequest(url)
                                  .loader(loaderFactory.newLoader())
                                  .send(dtoUnmarshallerFactory.newListUnmarshaller(MachineDto.class));
    }

    @Override
    public Promise<MachineDto> disconnect(String machineId, boolean remove) {
        final DevMachine devMachine = appContext.getDevMachine();
        final String url = devMachine.getWsAgentBaseUrl() + "/artik/" + machineId + "?remove=" + remove;

        return asyncRequestFactory.createDeleteRequest(url)
                                  .loader(loaderFactory.newLoader())
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(MachineDto.class));
    }

    @Override
    public Promise<List<MachineProcessDto>> getProcesses(String machineId) {
        final DevMachine devMachine = appContext.getDevMachine();
        final String url = devMachine.getWsAgentBaseUrl() + "/artik/processes/" + machineId;

        return asyncRequestFactory.createGetRequest(url)
                                  .loader(loaderFactory.newLoader())
                                  .send(dtoUnmarshallerFactory.newListUnmarshaller(MachineProcessDto.class));
    }

    @Override
    public Promise<MachineProcessDto> executeCommand(String machineId, Command command, String outputChannel) {
        final DevMachine devMachine = appContext.getDevMachine();
        final String url = devMachine.getWsAgentBaseUrl() + "/artik/" + machineId + "/command?outputChannel=" + outputChannel;

        return asyncRequestFactory.createPostRequest(url, command)
                                  .loader(loaderFactory.newLoader())
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(MachineProcessDto.class));
    }

    @Override
    public Promise<Void> stopProcess(String machineId, int processId) {
        final DevMachine devMachine = appContext.getDevMachine();
        final String url = devMachine.getWsAgentBaseUrl() + "/artik/" + machineId + "/process/" + processId;

        return asyncRequestFactory.createDeleteRequest(url)
                                  .loader(loaderFactory.newLoader())
                                  .send();
    }

}
