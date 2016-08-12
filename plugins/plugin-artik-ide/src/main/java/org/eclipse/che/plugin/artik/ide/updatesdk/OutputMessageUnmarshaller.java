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

import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;

import org.eclipse.che.ide.websocket.Message;
import org.eclipse.che.ide.websocket.rest.Unmarshallable;

/**
 * Unmarshaller for command's output.
 * Cuts leading '[STDOUT]' prefix.
 *
 * @author Artem Zatsarynnyi
 */
public class OutputMessageUnmarshaller implements Unmarshallable<String> {

    private String payload;

    @Override
    public void unmarshal(Message message) {
        payload = message.getBody();

        if (payload.startsWith("[STDOUT] ")) {
            payload = payload.substring(9);
        }
    }

    @Override
    public String getPayload() {
        return payload;
    }

}
