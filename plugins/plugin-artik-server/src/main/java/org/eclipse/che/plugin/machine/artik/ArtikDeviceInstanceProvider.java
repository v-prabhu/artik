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

import com.google.gson.Gson;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.machine.MachineSource;
import org.eclipse.che.api.core.model.machine.MachineStatus;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.machine.server.exception.InvalidRecipeException;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.exception.SnapshotException;
import org.eclipse.che.api.machine.server.exception.UnsupportedRecipeException;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.server.spi.InstanceProvider;
import org.eclipse.che.plugin.machine.ssh.SshClient;
import org.eclipse.che.plugin.machine.ssh.SshMachineFactory;
import org.eclipse.che.plugin.machine.ssh.SshMachineInstance;
import org.eclipse.che.plugin.machine.ssh.SshMachineRecipe;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * Implementation of {@link InstanceProvider} based on communication with machine over ssh protocol.
 * Performs command execution in Artik device.
 *
 * @author Valeriy Svydenko
 */
public class ArtikDeviceInstanceProvider implements InstanceProvider {
    private static final Gson GSON = new Gson();

    private final Set<String>       supportedRecipeTypes;
    private final SshMachineFactory sshMachineFactory;

    @Inject
    public ArtikDeviceInstanceProvider(SshMachineFactory sshMachineFactory) throws IOException {
        this.sshMachineFactory = sshMachineFactory;
        this.supportedRecipeTypes = Collections.singleton("ssh-config");
    }

    @Override
    public String getType() {
        return "artik";
    }

    @Override
    public Set<String> getRecipeTypes() {
        return supportedRecipeTypes;
    }

    /**
     * Creates instance from scratch or by reusing a previously one by using specified {@link MachineSource}
     * data in {@link MachineConfig}.
     *
     * @param machine
     *         machine description
     * @param lineConsumer
     *         output for instance creation logs
     * @return newly created {@link Instance}
     * @throws UnsupportedRecipeException
     *         if specified {@code recipe} is not supported
     * @throws InvalidRecipeException
     *         if {@code recipe} is invalid
     * @throws NotFoundException
     *         if instance described by {@link MachineSource} doesn't exists
     * @throws MachineException
     *         if other error occurs
     */
    @Override
    public Instance createInstance(Machine machine, LineConsumer lineConsumer) throws NotFoundException, MachineException {
        requireNonNull(machine, "Non null machine required");
        requireNonNull(lineConsumer, "Non null logs consumer required");
        requireNonNull(machine.getConfig().getSource().getContent(), "Location in machine source is required");

        SshMachineRecipe sshMachineRecipe = GSON.fromJson(machine.getConfig().getSource().getContent(), SshMachineRecipe.class);

        SshClient sshClient = sshMachineFactory.createSshClient(sshMachineRecipe,
                                                                machine.getConfig().getEnvVariables());
        sshClient.start();

        SshMachineInstance instance = sshMachineFactory.createInstance(machine,
                                                                       sshClient,
                                                                       lineConsumer);

        instance.setStatus(MachineStatus.RUNNING);
        return instance;
    }

    @Override
    public void removeInstanceSnapshot(MachineSource machineSource) throws SnapshotException {
        throw new SnapshotException("Snapshot feature is unsupported for ssh machine implementation");
    }

}
