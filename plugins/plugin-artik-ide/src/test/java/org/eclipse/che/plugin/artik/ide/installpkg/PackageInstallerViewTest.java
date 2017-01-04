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
package org.eclipse.che.plugin.artik.ide.installpkg;

import com.google.gwt.core.shared.GWT;
import com.google.gwtmockito.GwtMockitoTestRunner;
import org.eclipse.che.plugin.artik.ide.ArtikLocalizationConstant;
import org.eclipse.che.plugin.artik.ide.installpkg.PackageInstallerViewImpl.PackageInstallerViewBinder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link PackageInstallerViewImpl}.
 *
 * @author Lijuan Xue
 */
@RunWith(GwtMockitoTestRunner.class)
public class PackageInstallerViewTest {

    @Mock
    private ArtikLocalizationConstant locale;

    private PackageInstallerViewImpl view;

    @Before
    public void setUp() {
        PackageInstallerViewBinder uiBinder = GWT.create(PackageInstallerViewBinder.class);
        view = new PackageInstallerViewImpl(uiBinder, locale);
    }

    @Test
    public void shouldCloseDialogue() throws Exception {
        view.closeDialog();
        verify(view.pkgName).setValue(eq(""));
    }

    @Test
    public void shouldGetPackageName() throws Exception {
        view.getPackageName();
        verify(view.pkgName).getValue();
    }

    @Test
    public void shouldGetName() throws Exception {
        view.getName();
        verify(view.pkgName).getText();
    }

}
