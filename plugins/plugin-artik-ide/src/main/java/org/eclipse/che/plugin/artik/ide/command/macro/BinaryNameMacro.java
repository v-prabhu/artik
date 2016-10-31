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
package org.eclipse.che.plugin.artik.ide.command.macro;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.app.AppContext;

import static org.eclipse.che.plugin.cpp.shared.Constants.BINARY_NAME_ATTRIBUTE;

/**
 * Macro that provides binary name value from the project's attributes.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class BinaryNameMacro extends ProjectAttributeMacro {

    public static final String DEFAULT_BINARY_NAME = "a.out";

    @Inject
    public BinaryNameMacro(AppContext appContext) {
        super(appContext);
    }

    @Override
    public String getName() {
        return "${binary.name}";
    }

    @Override
    public String getDescription() {
        return "provides binary name";
    }

    @Override
    protected String getAttribute() {
        return BINARY_NAME_ATTRIBUTE;
    }

    @Override
    protected String getDefaultValue() {
        return DEFAULT_BINARY_NAME;
    }
}
