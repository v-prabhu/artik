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
package org.eclipse.che.plugin.artik.ide.apidocs;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.DevMachine;
import org.eclipse.che.ide.api.parts.PartStackType;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.plugin.artik.ide.ArtikLocalizationConstant;
import org.eclipse.che.plugin.artik.ide.ArtikResources;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** @author Artem Zatsarynnyi */
@RunWith(MockitoJUnitRunner.class)
public class DocsPartPresenterTest {

    @Mock
    private DocsPartView              view;
    @Mock
    private WorkspaceAgent            workspaceAgent;
    @Mock
    private ArtikResources            resources;
    @Mock
    private AppContext                appContext;
    @Mock
    private ArtikLocalizationConstant localizationConstants;
    @Mock
    private EventBus                  eventBus;

    @InjectMocks
    private DocsPartPresenter presenter;

    @Test
    public void shouldOpenPart() throws Exception {
        presenter.open();

        verify(workspaceAgent).openPart(presenter, PartStackType.TOOLING);
    }

    @Test
    public void shouldReturnTitle() throws Exception {
        presenter.getTitle();

        verify(localizationConstants).showApiDocPageTitle();
    }

    @Test
    public void shouldReturnTitleTooltip() throws Exception {
        presenter.getTitleToolTip();

        verify(localizationConstants).showApiDocPageTooltip();
    }

    @Test
    public void shouldReturnTitleImage() throws Exception {
        presenter.getTitleImage();

        verify(resources).artikIcon();
    }

    @Test
    public void shouldReturnView() throws Exception {
        assertEquals(view, presenter.getView());
    }

    @Test
    public void shouldChangeVisibility() throws Exception {
        presenter.setVisible(true);

        verify(view).setVisible(true);
    }

    @Test
    public void shouldShowView() throws Exception {
        AcceptsOneWidget container = mock(AcceptsOneWidget.class);
        presenter.go(container);

        verify(view).setURL(anyString());
        verify(container).setWidget(view);
    }

    @Test
    public void shouldReturnDosURL() throws Exception {
        DevMachine devMachine = mock(DevMachine.class);
        when(devMachine.getAddress()).thenReturn("http://localhost:32806");
        when(appContext.getDevMachine()).thenReturn(devMachine);

        String docURL = presenter.getDocURL();

        verify(appContext).getDevMachine();
        verify(devMachine).getAddress();

        assertEquals("http://localhost:32806/artikdocs", docURL);
    }
}
