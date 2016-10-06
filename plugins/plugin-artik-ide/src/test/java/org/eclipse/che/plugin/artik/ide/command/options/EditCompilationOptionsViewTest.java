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
package org.eclipse.che.plugin.artik.ide.command.options;

import com.google.gwt.core.shared.GWT;
import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.plugin.artik.ide.ArtikLocalizationConstant;
import org.eclipse.che.plugin.artik.ide.command.options.EditCompilationOptionsViewImpl;
import org.eclipse.che.plugin.artik.ide.command.options.EditCompilationOptionsViewImpl.EditCompilationOptionsViewImplUiBinder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link EditCompilationOptionsViewImpl}.
 *
 * @author Artem Zatsarynnyi
 */
@RunWith(GwtMockitoTestRunner.class)
public class EditCompilationOptionsViewTest {

    @Mock
    private CoreLocalizationConstant  coreLocale;
    @Mock
    private ArtikLocalizationConstant locale;

    private EditCompilationOptionsViewImpl view;

    @Before
    public void setUp() {
        EditCompilationOptionsViewImplUiBinder uiBinder = GWT.create(EditCompilationOptionsViewImplUiBinder.class);
        view = new EditCompilationOptionsViewImpl(uiBinder, locale, coreLocale);
    }

    @Test
    public void shouldGetBinaryName() {
        view.getBinaryName();

        verify(view.binaryName).getValue();
    }

    @Test
    public void shouldSetBinaryName() {
        view.setBinaryName("abc.out");

        verify(view.binaryName).setValue(eq("abc.out"));
    }

    @Test
    public void shouldGetCompilationOptions() {
        view.getCompilationOptions();

        verify(view.compilationOptions).getValue();
    }

    @Test
    public void shouldSetCompilationOptions() {
        view.setCompilationOptions("-a -b -c");

        verify(view.compilationOptions).setValue(eq("-a -b -c"));
    }
}
