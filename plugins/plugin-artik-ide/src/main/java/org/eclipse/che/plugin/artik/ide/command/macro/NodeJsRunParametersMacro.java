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
package org.eclipse.che.plugin.artik.ide.command.macro;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.app.AppContext;

import static org.eclipse.che.plugin.nodejs.shared.Constants.RUN_PARAMETERS_ATTRIBUTE;

/**
 * Macro for arguments, related to node command. They will be added to the end of the command.
 */
@Singleton
public class NodeJsRunParametersMacro extends ProjectAttributeMacro {
    public static final String DEFAULT_RUN_PARAMETERS = "";

    @Inject
    public NodeJsRunParametersMacro(AppContext appContext) {
        super(appContext);
    }

    @Override
    protected String getAttribute() {
        return RUN_PARAMETERS_ATTRIBUTE;
    }

    @Override
    protected String getDefaultValue() {
        return DEFAULT_RUN_PARAMETERS;
    }

    @Override
    public String getName() {
        return "${node.run.parameters}";
    }

    @Override
    public String getDescription() {
        return "NodeJs Run Parameters";
    }
}
