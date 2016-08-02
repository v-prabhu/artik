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
package org.eclipse.che.plugin.machine.artik.replication.shell;

import com.google.common.annotations.Beta;

import org.eclipse.che.api.core.util.CommandLine;

import static java.lang.String.format;

/**
 * @author Dmitry Kuleshov
 *
 * @since 4.5
 */
@Beta
class CommandBuilder {
    static CommandLine buildCommandWithContext(ScpCommandContext ctx) {
        return new CommandLine().add(SSHPASS.NAME)
                                .add(SSHPASS.PASSWORD_KEY, ctx.getPassword())

                                .add(SCP.NAME)
                                .add(SCP.PORT_KEY, ctx.getPort())

                                .add(SCP.OPTIONS_KEY)
                                .addPair(Options.STRICT_HOST_KEY_CHECKING, Options.NO)

                                .add(ctx.isDirectory() ? SCP.RECURSIVE_KEY : "")
                                .add(ctx.getSourcePath())
                                .add(format("%s@%s:%s", ctx.getUsername(), ctx.getHost(), ctx.getTargetPath()));
    }

    static CommandLine buildCommandWithContext(RsyncCommandContext ctx) {
        return new CommandLine().add("rsync.sh")
                                .add(ctx.getUsername())
                                .add(ctx.getPassword())
                                .add(ctx.getSourcePath())
                                .add(ctx.getHost())
                                .add(ctx.getTargetPath());
    }

    /**
     * scp copies files between hosts on a network.  It uses ssh(1) for data
     * transfer, and uses the same authentication and provides the same security
     * as ssh(1).  Unlike rcp(1), scp will ask for passwords or passphrases if
     * they are needed for authentication.

     * @see <a href="http://man7.org/linux/man-pages/man1/scp.1.html">SCP</a>
     */
    private interface SCP {
        String NAME          = "scp";
        /**
         * Defies a list of SSH protocol options to be applied.
         */
        String OPTIONS_KEY   = "-o";
        /**
         * Specifies the port to connect to on the remote host.  Note that
         * this option is written with a capital 'P', because -p is already
         * reserved for preserving the times and modes of the file.
         */
        String PORT_KEY      = "-P";
        /**
         * Recursively copy entire directories.  Note that scp follows
         * symbolic links encountered in the tree traversal.
         */
        String RECURSIVE_KEY = "-r";
    }

    /**
     * ssh (SSH client) is a program for logging into a remote machine and for
     * executing commands on a remote machine.  It is intended to replace rlogin
     * and rsh, and provide secure encrypted communications between two
     * untrusted hosts over an insecure network.  X11 connections and arbitrary
     * TCP/IP ports can also be forwarded over the secure channel.
     *
     * @see <a href="http://man7.org/linux/man-pages/man1/ssh.1.html">SSH</a>
     */
    private interface SSH {
        String NAME        = "ssh";
        /**
         * Port to connect to on the remote host.  This can be specified
         * on a per-host basis in the configuration file.
         */
        String PORT_KEY    = "-p";
        /**
         * Can be used to give options in the format used in the configuration
         * file.  This is useful for specifying options for which there is no
         * separate command-line flag.  For full details of the options listed
         * below, and their possible values
         */
        String OPTIONS_KEY = "-o";
    }

    /**
     * sshpass is a utility designed for running ssh using the mode referred to
     * as "keyboard-interactive" password authentication, but in non-interactive
     * mode. ssh uses direct TTY access to make sure that the password is indeed
     * issued by an interactive keyboard user.
     *
     * @see
     */
    private interface SSHPASS {
        String NAME         = "sshpass";
        /**
         * The value of the key defines a password to be passed further.
         */
        String PASSWORD_KEY = "-p";
    }

    private interface Options {
        /**
         * Enables/disables strict host key checking during SSH protocol
         * interactions. Is set to 'yes' by default.
         */
        String STRICT_HOST_KEY_CHECKING = "StrictHostKeyChecking";
        String NO                       = "no";
    }
}
