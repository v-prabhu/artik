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

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineSource;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.api.machine.CommandPropertyValueProvider;

/**
 * Macro which provides path to the replication folder on Artik device.
 *
 * @author Artem Zatsarynnyi
 */
public class ReplicationFolderMacro implements CommandPropertyValueProvider {

    public static final String KEY = "${artik.replication.folder.%machineId%}";

    private final Machine machine;
    private final String  key;

    @Inject
    public ReplicationFolderMacro(@Assisted Machine machine) {
        this.machine = machine;
        this.key = KEY.replaceAll("%machineId%", machine.getId());
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public Promise<String> getValue() {
        final MachineSource source = machine.getConfig().getSource();
        if (!"ssh-config".equals(source.getType())) {
            return Promises.resolve("");
        }

        final String location = source.getLocation();

        final RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, location);

        return AsyncPromiseHelper.createFromAsyncRequest(new AsyncPromiseHelper.RequestCall<String>() {
            @Override
            public void makeCall(final AsyncCallback<String> callback) {
                requestBuilder.setCallback(new RequestCallback() {
                    @Override
                    public void onResponseReceived(Request request, Response response) {
                        try {
                            JSONValue jsonValue = JSONParser.parseStrict(response.getText());
                            JSONValue replicationFolder = jsonValue.isObject().get("replicationFolder");

                            callback.onSuccess(replicationFolder.isString().stringValue());
                        } catch (Exception e) {
                            callback.onFailure(e);
                        }
                    }

                    @Override
                    public void onError(Request request, Throwable exception) {
                        callback.onFailure(exception);
                    }
                });

                try {
                    requestBuilder.send();
                } catch (RequestException e) {
                    callback.onFailure(e);
                }
            }
        });
    }
}
