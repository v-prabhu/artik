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

import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.plugin.artik.ide.ArtikLocalizationConstant;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link EditCompilationOptionsAction}.
 *
 * @author Artem Zatsarynnyi
 */
@RunWith(MockitoJUnitRunner.class)
public class EditCompilationOptionsActionTest {

    @Mock
    private AppContext                      appContext;
    @Mock
    private ArtikLocalizationConstant       locale;
    @Mock
    private EditCompilationOptionsPresenter presenter;

    @InjectMocks
    private EditCompilationOptionsAction action;

    @Test
    public void shouldOpenPresenter() {
        action.actionPerformed(mock(ActionEvent.class));

        verify(presenter).show();
    }
}
