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

import com.google.gwtmockito.GwtMockitoTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

/** @author Artem Zatsarynnyi */
@RunWith(GwtMockitoTestRunner.class)
public class ResourceIndicatorViewImplTest {

    private ResourceIndicatorViewImpl view;

    @Before
    public void setUp() throws Exception {
        view = new ResourceIndicatorViewImpl();
    }

    @Test
    public void shouldSetValue() throws Exception {
        view.setValue("value");

        verify(view.valueLabel).setText(eq("value"));
    }
}
