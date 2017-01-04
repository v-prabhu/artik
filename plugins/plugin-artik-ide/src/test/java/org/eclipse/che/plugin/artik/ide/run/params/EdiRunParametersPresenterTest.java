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

import com.google.common.base.Optional;

import org.eclipse.che.api.core.model.project.SourceStorage;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.plugin.artik.ide.command.macro.NodeJsRunParametersMacro;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;

import static org.eclipse.che.plugin.nodejs.shared.Constants.RUN_PARAMETERS_ATTRIBUTE;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EdiRunParametersPresenterTest {
    @Mock
    private AppContext            appContext;
    @Mock
    private EditRunParametersView view;

    private EdiRunParametersPresenter presenter;

    @Mock
    private Resource               resource;
    @Mock
    private Project                project;
    @Mock
    private Project.ProjectRequest projectRequest;
    @Mock
    private Promise<Project>       projectPromise;

    @Captor
    private ArgumentCaptor<Operation<Project>> updatedProjectCaptor;

    @Before
    public void setUp() throws Exception {
        Resource[] resources = {resource};
        when(appContext.getResources()).thenReturn(resources);
        when(appContext.getResource()).thenReturn(resource);
        when(resource.getRelatedProject()).thenReturn(Optional.of(project));

        presenter = new EdiRunParametersPresenter(appContext, view);
    }

    @Test
    public void delegateShouldBeSet() throws Exception {
        verify(view).setDelegate(presenter);
    }

    @Test
    public void windowShouldBeShowed() throws Exception {
        presenter.show();

        verify(view).show();
    }

    @Test
    public void parametersShouldNotBeSetIfNoAnyProject() throws Exception {
        when(resource.getRelatedProject()).thenReturn(Optional.absent());

        presenter.show();

        verify(view).show();
        verify(view, never()).setRunParameters(anyString());
    }

    @Test
    public void defaultParametersShouldBeAddedIfAttributeIsEmpty() throws Exception {
        when(project.getAttribute(RUN_PARAMETERS_ATTRIBUTE)).thenReturn("");

        presenter.show();

        verify(view).setRunParameters(NodeJsRunParametersMacro.DEFAULT_RUN_PARAMETERS);
    }

    @Test
    public void defaultParametersShouldBeAddedIfAttributeIsNull() throws Exception {
        when(project.getAttribute(RUN_PARAMETERS_ATTRIBUTE)).thenReturn(null);

        presenter.show();

        verify(view).setRunParameters(NodeJsRunParametersMacro.DEFAULT_RUN_PARAMETERS);
    }

    @Test
    public void parametersShouldBeSetFromAttributes() throws Exception {
        final String parameters = "parameters";
        when(project.getAttribute(RUN_PARAMETERS_ATTRIBUTE)).thenReturn(parameters);

        presenter.show();

        verify(view).setRunParameters(parameters);
    }

    @Test
    public void windowShouldBeClosed() throws Exception {
        presenter.onClose();

        verify(view).close();
    }


    @Test
    public void parametersShouldBeSaved() throws Exception {
        final HashMap attributes = new HashMap(1);
        final String parametersValue = "parameters' value";
        final SourceStorage sourceStorage = mock(SourceStorage.class);
        when(project.getAttributes()).thenReturn(attributes);
        when(project.getSource()).thenReturn(sourceStorage);
        when(view.getRunParameters()).thenReturn(parametersValue);
        when(project.update()).thenReturn(projectRequest);
        when(projectRequest.withBody(anyObject())).thenReturn(projectRequest);
        when(projectRequest.send()).thenReturn(projectPromise);
        when(projectPromise.then(Matchers.<Operation<Project>>any())).thenReturn(projectPromise);

        presenter.onSave();

        verify(view).getRunParameters();
        verify(projectRequest).send();
        verify(projectPromise).then(updatedProjectCaptor.capture());
        updatedProjectCaptor.getValue().apply(project);
        verify(view).close();
    }
}
