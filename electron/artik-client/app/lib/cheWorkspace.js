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
"use strict";
const {Client} = require('node-rest-client');

const client = new Client();

client.registerMethod('getWorkspaces', 'http://localhost:8080/api/workspace?skipCount=0&maxItems=30', 'GET');
client.registerMethod('stopWorkspace', 'http://localhost:8080/api/workspace/${id}/runtime', 'DELETE');

function getWorkspaces() {
    return new Promise((resolve, reject) => {
        client.methods.getWorkspaces((data, response) => {
            resolve(data);
        }).on('error', err => {
            reject(err);
        });
    });
}

function stopWorkspace(data) {
    data.forEach(ws => {
        if (ws.status === 'RUNNING') {
            const arg = {
                path: {"id": ws.id}
            };
            client.methods.stopWorkspace(arg,()=>{});
        }
    });
}

exports.stopAllRunningWorkspaces = function () {
    return new Promise(resolve =>{
        getWorkspaces().then(stopWorkspace).then(resolve).catch(err =>{
            console.log(err);
            resolve();
        });
    });


};