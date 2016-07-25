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
package org.eclipse.che.plugin.artik.ide.resourcemonitor;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * View for {@link AbstractResourceIndicator}.
 *
 * @author Artem Zatsarynnyi
 */
public interface ResourceIndicatorView extends IsWidget {

    /** Set the value for displaying in view. */
    void setValue(String value);
}
