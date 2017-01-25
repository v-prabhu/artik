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
const {app, BrowserWindow, ipcMain, Menu} = require("electron");
//const CheLauncher_1 = require("./CheLauncher");
var mainWindow = null;
var artikWindow = null;
app.on('window-all-closed', function () {
    if (process.platform !== 'darwin') {
        app.quit();
    }
});
app.on('ready', function () {

    const menuTemplate = [
        {
            label: 'View',
            submenu: [
                {
                    label: 'Toggle Developer Tools',
                    accelerator: process.platform === 'darwin' ? 'Alt+Command+I' : 'Ctrl+Shift+I',
                    click(item, focusedWindow) {
                        if (focusedWindow) {
                            focusedWindow.webContents.toggleDevTools();
                        }
                    }
                },
            ]
        }
    ];

    const menu = Menu.buildFromTemplate(menuTemplate);
    Menu.setApplicationMenu(menu);
    const display = require("electron").screen.getPrimaryDisplay();

    mainWindow = new BrowserWindow({ width: 966, height: 535,/* x: display.bounds.x+50, y: display.bounds.y+50,*/ resizable:false, show: false, webPreferences: { webSecurity: false } });
    mainWindow.loadURL(`file://${__dirname}/main/index.html`);
    // mainWindow.setMenu(menu);
    mainWindow.once('ready-to-show', () => {
        mainWindow.show();
    });

    ipcMain.addListener('che_url', function (event, url, needStopChe) {
        console.log(url, needStopChe);
        artikWindow = new BrowserWindow({
            width: 1024,
            height: 768,
            show: false,
            webPreferences: { webSecurity: false, nodeIntegration: false }
        });
        artikWindow.loadURL(url);
        artikWindow.maximize();
        artikWindow.once('ready-to-show', () => {
            artikWindow.show();
        });
        artikWindow.on('close', () => {
            if (needStopChe) {
                let choice = electron_1.dialog.showMessageBox({
                    type: 'question',
                    buttons: ['Yes', 'No'],
                    title: 'Stop Artik IDE',
                    message: 'Do you wont to stop Artik IDE?'
                });
                // if (choice === 0) {
                //     CheLauncher_1.stopChe();
                // }
            }
        });
        artikWindow.on('closed', function () {
            artikWindow = null;
        });
        mainWindow.close();
    });
    mainWindow.on('closed', function () {
        mainWindow = null;
    });
});
