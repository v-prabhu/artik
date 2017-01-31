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
const path = require('path');

function isWin() {
    return /^win/.test(process.platform);
}
function getUserHome() {
    return process.env[(process.platform === 'win32') ? 'USERPROFILE' : 'HOME'];
}

function convertToPosixPath(localPath) {
    if(isWin()) {
        let split = localPath.split(path.win32.sep);
        split[0] = split[0].charAt(0).toLowerCase();
        return '/' + split.join('/');
    }
    return localPath;
}

module.exports ={
    isWin : isWin,
    getUserHome : getUserHome,
    convertToPosixPath : convertToPosixPath
} ;