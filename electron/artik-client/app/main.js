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
const {app, BrowserWindow, ipcMain, Menu, dialog} = require("electron");
const windowStateKeeper = require('electron-window-state');
const settings = require('electron-settings');
const {getUserHome} = require('./lib/util');
const path = require('path');
const {stopAllRunningWorkspaces} = require('./lib/cheWorkspace');

let mainWindow = null;
let artikWindow = null;

function init() {
    settings.defaults({
        "artik_workspace": getUserHome() + path.sep + "artik",
        "stopping-workspace": false
    });
}

function createMainWindow() {
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
    if (process.platform === 'darwin') {
        menuTemplate.unshift({
            label: app.getName(),
            submenu: [
                {
                    role: 'about'
                },
                {
                    type: 'separator'
                },
                {
                    role: 'services',
                    submenu: []
                },
                {
                    type: 'separator'
                },
                {
                    role: 'hide'
                },
                {
                    role: 'hideothers'
                },
                {
                    role: 'unhide'
                },
                {
                    type: 'separator'
                },
                {
                    role: 'quit'
                }
            ]
        })
    }
    const menu = Menu.buildFromTemplate(menuTemplate);
    Menu.setApplicationMenu(menu);
    // Load the previous state with fallback to defaults
    let mainWindowState = windowStateKeeper({
        defaultWidth: 966,
        defaultHeight: 535
    });

    mainWindow = new BrowserWindow({
        'x': mainWindowState.x,
        'y': mainWindowState.y,
        'width': mainWindowState.width,
        'height': mainWindowState.height,
        resizable: false,
        show: false,
        webPreferences: {webSecurity: false}
    });

    mainWindowState.manage(mainWindow);

    mainWindow.loadURL(`file://${__dirname}/main/index.html`);
    // mainWindow.setMenu(menu);
    mainWindow.once('ready-to-show', () => {
        mainWindow.show();
    });

    ipcMain.addListener('che_url', function (event, url) {
        console.log(url);
        artikWindow = new BrowserWindow({
            width: 1024,
            height: 768,
            show: false,
            webPreferences: {webSecurity: false, nodeIntegration: false}
        });
        artikWindow.loadURL(url);
        artikWindow.maximize();
        artikWindow.once('ready-to-show', () => {
            artikWindow.show();
        });
        artikWindow.on('close', () => {

        });
        artikWindow.on('closed', function () {
            if(settings.getSync('stopping-workspace')){
                // stopArtik(settings.getSync('artik_workspace')).then(()=>{
                //     artikWindow = null;
                // });
                stopAllRunningWorkspaces().then(()=>{
                    artikWindow = null;
                });
            }
        });
        mainWindow.close();
    });
    mainWindow.on('closed', function () {
        mainWindow = null;
    });
}

// Manage unhandled exceptions as early as possible
process.on('uncaughtException', (e) => {
    console.error(`Caught unhandled exception: ${e}`);
    dialog.showErrorBox('Caught unhandled exception', e.message || 'Unknown error message');
    app.quit()
});
function setStoppingWorkspace(isStop) {
    settings.setSync("stopping-workspace", isStop)
}

function addMacDockCommand() {
    if (settings.getSync("stopping-workspace")) {
        let dockMenu = Menu.buildFromTemplate([
            {label: 'Don\'t stop workspaces on close', click () { setStoppingWorkspace(false); addMacDockCommand(); }},
        ]);
        app.dock.setMenu(dockMenu);
    } else {
        let dockMenu = Menu.buildFromTemplate([
            {label: 'Stop workspaces on close', click () { setStoppingWorkspace(true); addMacDockCommand(); }},
        ]);
        app.dock.setMenu(dockMenu);
    }

}
app.on('ready', function () {
    init();
    createMainWindow();

    if(process.platform === 'darwin'){
        addMacDockCommand();
    }
});

app.on('activate', function () {
    // On OS X it's common to re-create a window in the app when the
    // dock icon is clicked and there are no other windows open.
    if (mainWindow === null && artikWindow === null) {
        createMainWindow()
    }
});

app.on('window-all-closed', function () {
    if (process.platform !== 'darwin') {
        app.quit();
    }
});

let shouldQuit = makeSingleInstance();
if (shouldQuit) return app.quit();

// Make this app a single instance app.
//
// The main window will be restored and focused instead of a second window
// opened when a person attempts to launch a second instance.
//
// Returns true if the current version of the app should quit instead of
// launching.
function makeSingleInstance() {
    return app.makeSingleInstance((args) => {
        if (process.platform === 'win32') {
            if (args[2] === "--disable-stopping-workspace") {
                setStoppingWorkspace(false)
            }

            if (args[2] === '--enable-stopping-workspace') {
                setStoppingWorkspace(true);
            }
        }

        if (mainWindow) {
            if (mainWindow.isMinimized()) mainWindow.restore();
            mainWindow.focus()
        }
    })
}

if (process.platform === 'win32') {
    if (settings.getSync("stopping-workspace")) {
        app.setUserTasks([
            {
                program: process.execPath,
                arguments: '--enable-stopping-workspace',
                iconPath: process.execPath,
                iconIndex: 0,
                title: 'Stop workspaces on close',
                description: 'When electron window is closed all running workspaces are stopped'
            }
        ]);

    } else {
        app.setUserTasks([
            {
                program: process.execPath,
                arguments: '--disable-stopping-workspace',
                iconPath: process.execPath,
                iconIndex: 0,
                title: "Don't stop workspaces on close",
                description: 'When electron window is closed workspaces keep running'
            }
        ]);
    }
}
