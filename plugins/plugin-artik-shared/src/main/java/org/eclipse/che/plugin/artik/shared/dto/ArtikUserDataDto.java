/*******************************************************************************
 * Copyright (c) 2016 Samsung Electronics Co., Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - Initial implementation
 *   Samsung Electronics Co., Ltd. - Initial implementation
 *******************************************************************************/
package org.eclipse.che.plugin.artik.shared.dto;

import org.eclipse.che.dto.shared.DTO;
import org.eclipse.che.security.oauth.shared.User;

/**
 * @author Dmitry Kuleshov
 */
@DTO
public interface ArtikUserDataDto extends User {
    String getFullName();

    void setFullName(String fullName);

    String getSaIdentity();

    void setSaIdentity(String saIdentity);

    String getAccountType();

    void setAccountType(String accountType);

    String getCreatedOn();

    void setCreatedOn(String createdOn);

    String getModifiedOn();

    void setModifiedOn(String modifiedOn);
}
