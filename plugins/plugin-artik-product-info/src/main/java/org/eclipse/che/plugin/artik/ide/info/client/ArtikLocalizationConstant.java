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
package org.eclipse.che.plugin.artik.ide.info.client;

import com.google.gwt.i18n.client.Messages;


/**
 * Che product information constant.
 *
 * @author Oleksii Orel
 */
public interface ArtikLocalizationConstant extends Messages {

    @Key("che.tab.title")
    String cheTabTitle();

    @Key("che.tab.title.with.workspace.name")
    String cheTabTitle(String workspaceName);

    @Key("get.support.link")
    String getSupportLink();

    @Key("get.product.name")
    String getProductName();

    @Key("support.title")
    String supportTitle();
}
