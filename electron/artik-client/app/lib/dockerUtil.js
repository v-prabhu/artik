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
const Docker = require('dockerode');
const {isWin} = require('./util');

let docker = new Docker();

function isDockerRunning() {
    return new Promise((resolve) => {
        docker.info((err, result) => {
            let isRunning = !err;
            if (isRunning) {
                let bind = '/Users:/test';
                if (isWin()) {
                    bind = '/c:/c';
                }

                docker.run("alpine:3.4", ["uname", "-a"], process.stdout, {
                    'Binds': [bind]
                }, function (err, data, container) {
                    if (err) {
                        console.error(err);
                    }
                    resolve({"docker": data.StatusCode === 0});
                });

            } else {
                resolve({"docker": isRunning});
            }
        });
    });
}

function checkAndRunArtik(containerName, path) {
    return new Promise((resolve, reject) => {
        docker.listContainers((err, containers) => {
            if (err) {
                reject(err);
                return;
            }
            let containerRunning = false;
            containers.forEach(info => {
                if (info.Image.startsWith(containerName)) {
                    containerRunning = true;
                }
            });
            if (containerRunning) {
                resolve(createRunningResult(containerRunning));
            } else {
                runArtik(path).then(data => {
                    resolve(data);
                })
            }
        });
    });
}

function runArtik(path) {
    return executeArtikCommand(path, 'start');
}
function stopArtik(path){
    return executeArtikCommand(path, 'stop');
}

function executeArtikCommand(path, command){
    return new Promise(resolve => {
        docker.run('codenvy/artik-cli:1.3.1', [command], process.stdout, {
            'Binds': [path + ':/data', '/var/run/docker.sock:/var/run/docker.sock']
        }, function (err) {
            if (err) {
                console.error(err);
                resolve(createRunningResult(false))
            }
            resolve(createRunningResult(true));
        });
    });
}

function createRunningResult(isRunning) {
    return {"running": isRunning};
}
function runConnectivityTest(result) {
    return new Promise((resolve) => {
        docker.run("alpine:3.4", ["sh", "-c", "wget google.com"], process.stdout, (err, data, container) => {
            if (err) {
                console.error(err);
            }
            result.online = data.StatusCode === 0;
            resolve(result);
        });
    })
}

module.exports = {
    isDockerRunning: isDockerRunning,
    checkAndRunArtik: checkAndRunArtik,
    runConnectivityTest: runConnectivityTest,
    stopArtik: stopArtik
};
