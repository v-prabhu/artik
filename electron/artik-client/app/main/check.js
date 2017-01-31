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


function addLoader(statusElement, titleElement) {
    statusElement.children().remove();
    $('<img src="../assets/oval.svg">').appendTo(statusElement);
    titleElement.siblings('.retryingLabel').text("");

    statusElement.removeClass('failStatus');
    statusElement.removeClass("gray");
    titleElement.removeClass('failLabel');
    titleElement.addClass("readyStep");
}

function setSuccess(statusElement, titleElement) {
    titleElement.removeClass("readyStep");
    titleElement.removeClass('failLabel')
    titleElement.addClass("successLabel");
    titleElement.siblings('.retryingLabel').text("");
    statusElement.children().remove();
    statusElement.addClass("successStatus");
    statusElement.removeClass('failStatus')
    $('<i class="fa fa-check mark" aria-hidden="true"></i>').appendTo(statusElement);
}

function setRetryLabel(element, count) {

    element.text(`Retrying check in: ${count} seconds`);
    let id = setInterval(() => {
        count--;
        element.text(`Retrying check in: ${count} seconds`);
        if (count == 0) {
            clearInterval(id);
        }
    }, 1000);
}
function setFail(statusElement, titleElement, addRetry) {
    titleElement.removeClass("readyStep");
    titleElement.addClass("failLabel");
    if (addRetry) {
        setRetryLabel(titleElement.siblings('.retryingLabel'), 5);
    }
    statusElement.children().remove();
    statusElement.addClass("failStatus");
    $('<i class="fa fa-exclamation mark" aria-hidden="true"></i>').appendTo(statusElement);
}
function check(statusElement, titleElement, promise, prop, addRetry) {
    return new Promise((resolve) => {

        addLoader(statusElement, titleElement);
        promise.then((data) => {
            if (data) {
                if (data[prop]) {
                    setSuccess(statusElement, titleElement);
                    resolve(data);
                } else {
                    setFail(statusElement, titleElement, addRetry);
                    resolve(data);
                }
            }
        }).catch(error => {
            console.log(JSON.stringify(error));
        })
    });
}


module.exports = check;