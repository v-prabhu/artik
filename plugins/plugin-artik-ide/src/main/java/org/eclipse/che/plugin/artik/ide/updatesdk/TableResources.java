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
package org.eclipse.che.plugin.artik.ide.updatesdk;

/** @author Artem Zatsarynnyi */
public interface TableResources extends org.eclipse.che.ide.Resources {

    @Source({"org/eclipse/che/ide/ui/cellview/cellTable.css", "org/eclipse/che/ide/api/ui/style.css", "table.css"})
    TableStyles cellTableStyle();

    interface TableStyles extends CellTableStyle {
    }
}
