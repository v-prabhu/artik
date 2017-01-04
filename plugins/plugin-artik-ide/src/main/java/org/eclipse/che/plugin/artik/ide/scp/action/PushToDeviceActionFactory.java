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
package org.eclipse.che.plugin.artik.ide.scp.action;

/**
 * The factory which creates actions to push file or folder to ssh machine.
 *
 * @author Dmitry Shnurenko
 */
public interface PushToDeviceActionFactory {

    /**
     * Creates action with specified name.
     *
     * @param actionName
     *         action name which will be created
     * @return an instance of {@link PushToDeviceAction}
     */
    PushToDeviceAction create(String actionName);
}
