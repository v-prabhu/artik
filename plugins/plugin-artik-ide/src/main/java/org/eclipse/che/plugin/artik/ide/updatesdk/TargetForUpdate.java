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
package org.eclipse.che.plugin.artik.ide.updatesdk;

/**
 * Represents the target for updating Artik SDK.
 *
 * @author Artem Zatsarynnyi
 */
public class TargetForUpdate {
    private final String id;
    private final String name;
    private       String currentVersion;

    public TargetForUpdate(String id, String name) {
        this.id = id;
        this.name = name;
    }

    /** Returns target's identifier. */
    public String getId() {
        return id;
    }

    /** Returns target's name. */
    public String getName() {
        return name;
    }

    /** Returns version of the Artik SDK which is currently installed on this target. */
    public String getCurrentVersion() {
        return currentVersion;
    }

    /** Sets version of the Artik SDK which is currently installed on this target. */
    public void setCurrentVersion(String version) {
        currentVersion = version;
    }
}
