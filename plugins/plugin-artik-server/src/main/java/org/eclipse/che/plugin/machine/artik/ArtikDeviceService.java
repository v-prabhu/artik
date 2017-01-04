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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import com.google.common.io.CharStreams;
import com.google.inject.Inject;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.machine.server.DtoConverter;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.machine.shared.dto.MachineProcessDto;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Artik device service API
 *
 * @author Valeriy Svydenko
 */
@Api(value = "/artik", description = "Artik REST API")
@Path("/artik")
public class ArtikDeviceService extends Service {
    private final ArtikDeviceManager              artikDeviceManager;
    private final ArtikDeviceServiceLinksInjector linksInjector;

    @Inject
    public ArtikDeviceService(ArtikDeviceManager artikDeviceManager, ArtikDeviceServiceLinksInjector linksInjector) {
        this.artikDeviceManager = artikDeviceManager;
        this.linksInjector = linksInjector;
    }

    @POST
    @Path("/connect")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create a new device based on the configuration")
    @ApiResponses({@ApiResponse(code = 500, message = "Internal server error occurred"),
                   @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid")})
    public MachineDto connect(@ApiParam(value = "The new device configuration", required = true)
                                  MachineConfigDto machineConfig) throws ServerException, BadRequestException {
        requiredNotNull(machineConfig, "Device configuration");
        return artikDeviceManager.connect(machineConfig);
    }

    @POST
    @Path("/restore")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Restore devices based on the configuration")
    @ApiResponses({@ApiResponse(code = 500, message = "Internal server error occurred"),
                   @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid")})
    public List<MachineDto> restore(@ApiParam(value = "Devices' configuration", required = true)
                                      List<MachineConfigDto> devicesConfigs) throws ServerException, BadRequestException {
        requiredNotNull(devicesConfigs, "Device configuration");
        return artikDeviceManager.restoreDevices(devicesConfigs);
    }

    @GET
    @Path("/connect/{deviceId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Connect to existing device")
    @ApiResponses({@ApiResponse(code = 500, message = "Internal server error occurred"),
                   @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
                   @ApiResponse(code = 404, message = "Device with specified id does not exist")})
    public MachineDto connectById(@ApiParam(value = "Device ID") @PathParam("deviceId") String deviceId) throws ServerException,
                                                                                                                BadRequestException,
                                                                                                                NotFoundException {
        requiredNotNull(deviceId, "Device Id");
        final MachineDto device = artikDeviceManager.connectById(deviceId);
        return linksInjector.injectLinks(device, getServiceContext());
    }

    @GET
    @Path("/{deviceId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Get device by ID", response = MachineDto.class)
    @ApiResponses({@ApiResponse(code = 404, message = "Device with specified id does not exist")})
    public MachineDto getDeviceById(@ApiParam(value = "Device ID") @PathParam("deviceId") String deviceId) throws NotFoundException {
        final MachineDto device = artikDeviceManager.getDeviceById(deviceId);
        return linksInjector.injectLinks(device, getServiceContext());
    }

    @GET
    @Path("/devices")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get all connected devices", response = MachineDto.class, responseContainer = "List")
    public List<MachineDto> getDevices() {
        final List<MachineDto> devices = artikDeviceManager.getDevices();
        for (MachineDto device : devices) {
            linksInjector.injectLinks(device, getServiceContext());
        }
        return devices;
    }

    @GET
    @Path("/processes/{deviceId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Get processes of device",
            response = MachineProcessDto.class,
            responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "The response contains device process entities"),
                   @ApiResponse(code = 404, message = "Device with specified ID does not exist"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public List<MachineProcessDto> getProcesses(@ApiParam(value = "Device ID")
                                                @PathParam("deviceId") String deviceId) throws ServerException, NotFoundException {
        return artikDeviceManager.getProcessesById(deviceId)
                                 .stream()
                                 .map(DtoConverter::asDto)
                                 .map(machineProcess -> linksInjector.injectLinks(machineProcess,
                                                                                  deviceId,
                                                                                  getServiceContext()))
                                 .collect(Collectors.toList());
    }

    @GET
    @Path("/{deviceId}/process/{pid}/logs")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Get logs of device process")
    @ApiResponses({@ApiResponse(code = 200, message = "The response contains logs"),
                   @ApiResponse(code = 404, message = "Device or process with specified ID does not exist"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public void getProcessLogs(@ApiParam(value = "Device ID")
                               @PathParam("deviceId") String machineId,
                               @ApiParam(value = "Process ID")
                               @PathParam("pid") int pid,
                               @Context HttpServletResponse httpServletResponse)
            throws NotFoundException,
                   ForbiddenException,
                   ServerException,
                   IOException {

        addProcessLogsToResponse(machineId, pid, httpServletResponse);
    }


    @DELETE
    @Path("/{deviceId}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Disconnect device")
    @ApiResponses({@ApiResponse(code = 404, message = "Device with specified id does not exist"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public MachineDto disconnect(@ApiParam(value = "Device ID")
                                 @PathParam("deviceId") String deviceId,
                                 @ApiParam(value = "True if device should be removed from the storage")
                                 @DefaultValue("false")
                                 @QueryParam("remove") boolean remove) throws ServerException, NotFoundException {
        return artikDeviceManager.disconnect(deviceId, remove);
    }

    @POST
    @Path("/{deviceId}/command")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Start specified command in device")
    @ApiResponses({@ApiResponse(code = 200, message = "The response contains entity of created device process"),
                   @ApiResponse(code = 400, message = "Command entity is invalid"),
                   @ApiResponse(code = 404, message = "Device with specified ID does not exist"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public MachineProcessDto executeCommandInDevice(@ApiParam(value = "Device ID")
                                                    @PathParam("deviceId") String deviceId,
                                                    @ApiParam(value = "Command to execute", required = true)
                                                    final CommandDto command,
                                                    @ApiParam(value = "Channel for command output")
                                                    @QueryParam("outputChannel") String outputChannel) throws NotFoundException,
                                                                                                              ServerException,
                                                                                                              BadRequestException {
        requiredNotNull(command, "Command description");
        requiredNotNull(command.getCommandLine(), "Commandline");
        return linksInjector.injectLinks(DtoConverter.asDto(artikDeviceManager.exec(deviceId,
                                                                                    command,
                                                                                    outputChannel)),
                                         deviceId,
                                         getServiceContext());
    }

    @DELETE
    @Path("/{deviceId}/process/{processId}")
    @ApiOperation(value = "Stop process in device")
    @ApiResponses({@ApiResponse(code = 204, message = "Process was successfully stopped"),
                   @ApiResponse(code = 404, message = "Device with specified ID does not exist"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public void stopProcess(@ApiParam(value = "Device ID")
                            @PathParam("deviceId") String deviceId,
                            @ApiParam(value = "Process ID")
                            @PathParam("processId") int processId) throws NotFoundException,
                                                                          ServerException {
        artikDeviceManager.stopProcess(deviceId, processId);
    }

    private void requiredNotNull(Object object, String message) throws BadRequestException {
        if (object == null) {
            throw new BadRequestException(message + " required");
        }
    }

    private void addProcessLogsToResponse(String machineId, int pid, HttpServletResponse httpServletResponse) throws IOException,
                                                                                                                     NotFoundException,
                                                                                                                     MachineException {
        try (Reader logsReader = artikDeviceManager.getProcessLogReader(machineId, pid)) {
            // Response is written directly to the servlet request stream
            httpServletResponse.setContentType("text/plain");
            CharStreams.copy(logsReader, httpServletResponse.getWriter());
            httpServletResponse.getWriter().flush();
        }
    }
}

