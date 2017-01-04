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
package org.eclipse.che.plugin.artik.ide.profile;

/**
 * Serves as enumeration of software required for development mode.
 *
 * @author Dmitry Kuleshov
 */
public enum Software {
    GDB_SERVER("gdb-gdbserver"),
    RSYNC("rsync");

    public final String name;

    private String installationCommand;
    private String verificationCommand;

    Software(String name) {
        this.name = name;
    }

    public String getVerificationCommand() {
        return verificationCommand;
    }

    public void setVerificationCommand(String verificationCommand) {
        this.verificationCommand = verificationCommand;
    }

    public String getInstallationCommand() {
        return installationCommand;
    }

    public void setInstallationCommand(String installationCommand) {
        this.installationCommand = installationCommand;
    }
}
