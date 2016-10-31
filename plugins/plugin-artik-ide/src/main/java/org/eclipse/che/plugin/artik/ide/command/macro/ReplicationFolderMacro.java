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

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineSource;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.api.macro.Macro;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.gwt.json.client.JSONParser.parseLenient;

/**
 * Macro which provides path to the replication folder on Artik device.
 *
 * @author Artem Zatsarynnyi
 * @author Valeriy Svydenko
 */
public class ReplicationFolderMacro implements Macro {

    public static final  String KEY                = "${artik.replication.folder.%machineId%}";
    private static final String REPLICATION_FOLDER = "replicationFolder";

    private final Machine machine;
    private final String  key;

    @Inject
    public ReplicationFolderMacro(@Assisted Machine machine) {
        this.machine = machine;
        this.key = KEY.replaceAll("%machineId%", machine.getId());
    }

    @Override
    public String getName() {
        return key;
    }

    @Override
    public String getDescription() {
        return "provides path to the replication folder on Artik device";
    }

    @Override
    public Promise<String> expand() {
        final MachineSource source = machine.getConfig().getSource();
        if (!"ssh-config".equals(source.getType())) {
            return Promises.resolve("");
        }

        final String content = source.getContent();
        if (isNullOrEmpty(content)) {
            return Promises.resolve("");
        }
        final JSONObject jsonObject = parseLenient(content).isObject();

        final JSONValue replicationFolderValue = jsonObject.get(REPLICATION_FOLDER);
        final JSONString replicationFolderString = replicationFolderValue.isString();
        final String replicationFolder = replicationFolderString.stringValue();

        return Promises.resolve(replicationFolder);
    }
}
