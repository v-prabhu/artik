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
package org.eclipse.che.plugin.artik.ide.run.params;

import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.plugin.artik.ide.ArtikLocalizationConstant;
import org.eclipse.che.plugin.nodejs.shared.Constants;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(GwtMockitoTestRunner.class)
public class EditRunParamsActionTest {
    @Mock
    private ArtikLocalizationConstant locale;
    @Mock
    private EdiRunParametersPresenter presenter;
    @Mock
    private AppContext                appContext;

    private EditRunParamsAction action;

    @Mock
    private ActionEvent event;
    @Mock
    private Project rootProject;
    @Mock
    private Presentation presentation;

    @Before
    public void setUp() throws Exception {
        when(event.getPresentation()).thenReturn(presentation);
        action = new EditRunParamsAction(locale, presenter, appContext);
    }

    @Test
    public void actionShouldBeInitialized() throws Exception {
        verify(locale).editRunParamsActionTitle();
        verify(locale).editRunParamsActionDescription();
    }

    @Test
    public void actionShouldBePerformed() throws Exception {
        action.actionPerformed(event);

        verify(presenter).show();
    }

    @Test
    public void actionShouldBeDisabledIfRootProjectIsNull() throws Exception {
        when(appContext.getRootProject()).thenReturn(null);

        action.updateInPerspective(event);

        verify(presentation).setEnabled(false);
    }

    @Test
    public void actionShouldBeDisabledIfRootProjectIsNotNodeJs() throws Exception {
        when(rootProject.isTypeOf(Constants.NODE_JS_PROJECT_TYPE_ID)).thenReturn(false);

        action.updateInPerspective(event);

        verify(presentation).setEnabled(false);
    }

    @Test
    public void actionShouldBeEnable() throws Exception {
        when(appContext.getRootProject()).thenReturn(rootProject);
        when(rootProject.isTypeOf(Constants.NODE_JS_PROJECT_TYPE_ID)).thenReturn(true);

        action.updateInPerspective(event);

        verify(presentation).setEnabled(true);
    }
}
