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
package org.eclipse.che.plugin.machine.artik.replication;

import io.swagger.annotations.ApiOperation;

import com.google.common.annotations.Beta;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.commons.json.JsonParseException;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.file.Paths;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.OK;

/**
 * The service allows to copy files and folders to ssh machines using secure copy protocol.
 *
 * @author Dmitry Shnurenko
 * @author Dmitry Kuleshov
 *
 */
@Path("/scp/{ws-id}")
@Singleton
@Beta
public class PushToDeviceService extends Service {
    private final ReplicationManager replicationManager;

    @Inject
    public PushToDeviceService(ReplicationManager replicationManager) {
        this.replicationManager = replicationManager;
    }

    /**
     * Copies file or folder to ssh machine using secure copy protocol. It is necessary to install 'sshpass' to machine from which
     * file or folder will be copied.
     *
     * @param machineId
     *         the machine id on which the file will be copied
     * @param sourcePath
     *         path to file which will be copied
     * @param targetPath
     *         path to folder into which file will be copied
     * @return response with status 204 if no error happened during copying
     * @throws ServerException
     *         thrown when some error happened during sending request to other services or if error occurs during execution
     *         of process
     * @throws IOException
     *         thrown when some error occurs during reading from process output
     * @throws JsonParseException
     *         thrown when some error occurs during parsing machine recipe script
     */
    @POST
    @ApiOperation(value = "Copy file or folder to ssh machine")
    public Response pushToDevice(@QueryParam("machine_id") String machineId,
                                 @QueryParam("source_file") String sourcePath,
                                 @QueryParam("target_path") String targetPath) throws ServerException, IOException, JsonParseException {
        final java.nio.file.Path path = Paths.get(sourcePath).getFileName();
        if (path != null) {
            final String s = path.toString();
            replicationManager.copy(machineId, sourcePath, targetPath + "/" + s);
            return Response.status(OK).build();
        } else {
            return Response.status(INTERNAL_SERVER_ERROR).build();
        }
    }
}
