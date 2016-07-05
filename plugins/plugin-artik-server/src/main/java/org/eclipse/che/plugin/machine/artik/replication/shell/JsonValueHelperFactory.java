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

import org.eclipse.che.api.core.model.machine.Machine;

/**
 *  Creates {@link JsonValueHelper} instances and passes machine's
 *  script data represented by an instance of {@link String}.
 *
 *  @author Dmitry Kuleshov
 *
 *  @since 4.5
 */
@Beta
public interface JsonValueHelperFactory {

    /**
     * Creates json helper to parse machine script with specified name.
     *
     * @param machineId
     *         if of the machine where we will parse the script represented by json
     * @return an instance of {@link JsonValueHelper}
     */
    JsonValueHelper create(String machineId);
}
