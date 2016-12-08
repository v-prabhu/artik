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
package org.eclipse.che.plugin.artik.ide.outputconsole;

import com.google.inject.name.Named;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.CommandOutputConsole;

public interface ArtikCommandConsoleFactory {

    /**
     * Create the instance of {@link CommandOutputConsole} for the given {@code command}.
     *
     * @param command
     *         command to execute
     * @param machine
     *         device where the command will be executed
     */
    @Named("artik-command-console")
    CommandOutputConsole create(CommandImpl command, Machine machine);
}
