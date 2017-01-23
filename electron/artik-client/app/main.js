"use strict";
const {app, dialog} = require("electron");
//const CheLauncher_1 = require("./CheLauncher");
var mainWindow = null;
var mainWebWindow = null;
app.on('window-all-closed', function () {
    if (process.platform !== 'darwin') {
        app.quit();
    }
});
app.on('ready', function () {
    dialog.showMessageBox({"message": "Hello World!"});
    app.quit();
});
