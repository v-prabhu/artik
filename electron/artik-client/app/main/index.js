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
const dockerUtil = require("../lib/dockerUtil.js");
const check = require("./check.js");
const settings = require('electron-settings');
const path = require('path');
const {ipcRenderer}= require('electron');
const open = require("open");
const {convertToPosixPath, isWin} = require("../lib/util");

function createDockerCheck() {
    let step = $('#statusStep');
    return check(step, step.siblings(".stepLabel"), dockerUtil.isDockerRunning(), "docker", true);
}

function getWorkspacePath() {
    let path = settings.getSync("artik_workspace");
    return convertToPosixPath(path);
}

function createArtikCheck() {
    let step = $("#artikStep");
    return check(step, step.siblings(".stepLabel"), dockerUtil.checkAndRunArtik("codenvy/artik-server", getWorkspacePath()), "running");
}

function createConnectivityCheck(data) {
    let step = $("#connectivityStep");
    let promise = dockerUtil.runConnectivityTest(data);
    return check(step, step.siblings(".stepLabel"), promise, "online", true);
}


function sendCheUrl(url) {
    ipcRenderer.send('che_url', url);
}

function dockerNotRunning() {
    let infoPanel = $('#infoPanel');
    infoPanel.hide();
    $('#errorPanel').show();
    setTimeout(startChecks, 5000)
}
function showInfoPanel() {
    let infoPanel = $('#infoPanel');
    infoPanel.show();
    $('#errorPanel').hide();
}
function startChecks() {
    createDockerCheck().then((data) => {

        if (data && data.docker) {
            showInfoPanel();
            createArtikCheck().then(createConnectivityCheck).then(data => {
                if (data.running && data.online) {
                    //connect to artik
                    sendCheUrl('http://localhost:8080', true);
                } else if (!data.running && data.online) {
                    //launch artic and connect to it
                }

            }).catch((err) => {
                console.log(err);
                alert("Ups should never happen!!!");
            });
        } else {
            dockerNotRunning();
        }
    }).catch(error => {
        console.error(error);
    });

}
$(document).ready(() => {
    settings.get("artik_workspace").then(pa => {
        $("#pathLabel").text(pa);
    });

    $('#docLink').click((e) => {
        e.preventDefault();
        if (isWin()) {
            open('https://docs.docker.com/docker-for-windows/#/shared-drives');
        } else {
            open(('https://docs.docker.com/docker-for-mac/#/file-sharing'));
        }
    });


    startChecks();

});
