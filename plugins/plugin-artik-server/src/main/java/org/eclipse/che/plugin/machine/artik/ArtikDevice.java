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

import org.eclipse.che.api.core.model.machine.MachineStatus;
import org.eclipse.che.api.machine.server.spi.Instance;

import static org.eclipse.che.api.core.model.machine.MachineStatus.DESTROYING;
import static org.eclipse.che.api.core.model.machine.MachineStatus.RUNNING;

/**
 * Describes Artik device model.
 *
 * @author Valeriy Svydenko
 */
public class ArtikDevice {
    private Instance    instance;

    ArtikDevice(Instance instance) {
        this.instance = instance;
    }

    /** Returns instance of {@link Instance} */
    public Instance getInstance() {
        return instance;
    }

    /** Set {@link MachineStatus#DESTROYING} status of the device. */
    public void disconnect() {
        instance.setStatus(DESTROYING);
    }

    /** Set {@link MachineStatus#RUNNING} status of the device. */
    public void connect() {
        instance.setStatus(RUNNING);
    }
}
