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

import com.google.common.base.Optional;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.macro.Macro;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Macro that provides value from the project's attributes.
 *
 * @author Artem Zatsarynnyi
 */
public abstract class ProjectAttributeMacro implements Macro {

    protected final AppContext appContext;

    protected ProjectAttributeMacro(AppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public Promise<String> expand() {
        String binaryName = getAttrValue(getAttribute(), getDefaultValue());

        return Promises.resolve(binaryName);
    }

    /** Returns name of the attribute which value should be read. */
    protected abstract String getAttribute();

    /** Returns the value that should be returned by macro if no value in attribute. */
    protected abstract String getDefaultValue();

    /** Returns the specified project attribute value or the given {@code defaultValue} if none. */
    private String getAttrValue(String attrName, String defaultValue) {
        Optional<Project> projectOptional = getCurrentProject();
        if (!projectOptional.isPresent()) {
            return defaultValue;
        }

        final Project project = projectOptional.get();
        final String attrValue = project.getAttribute(attrName);
        if (isNullOrEmpty(attrValue)) {
            return defaultValue;
        }

        return attrValue;
    }

    private Optional<Project> getCurrentProject() {
        final Resource[] resources = appContext.getResources();
        if (resources == null || resources.length != 1) {
            return Optional.absent();
        }

        Resource resource = appContext.getResource();
        return resource.getRelatedProject();
    }
}
