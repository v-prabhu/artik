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
package org.eclipse.che.plugin.artik.ide.run;

import com.google.common.base.Optional;
import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.plugin.artik.ide.ArtikLocalizationConstant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.eclipse.che.plugin.cpp.shared.Constants.C_PROJECT_TYPE_ID;
import static org.eclipse.che.plugin.nodejs.shared.Constants.NODE_JS_PROJECT_TYPE_ID;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(GwtMockitoTestRunner.class)
public class RunActionTest {
    @Mock
    private ArtikLocalizationConstant localizationConstants;
    @Mock
    private AppContext                appContext;
    @Mock
    private Machine                   device;
    @Mock
    private NodeJsRunner              nodeJsRunner;
    @Mock
    private BinaryRunner              binaryRunner;
    @Mock
    private Resource                  resource;

    @Mock
    private MachineConfig machineConfig;
    @Mock
    private ActionEvent   event;
    @Mock
    private Presentation  presentation;
    @Mock
    private Project       project;

    private RunAction action;

    @Before
    public void setUp() {
        when(machineConfig.getName()).thenReturn("device_name");
        when(device.getConfig()).thenReturn(machineConfig);
        when(event.getPresentation()).thenReturn(presentation);

        Resource resource = Mockito.mock(Resource.class);
        Resource[] resources = {resource};
        when(appContext.getResources()).thenReturn(resources);
        when(appContext.getResource()).thenReturn(resource);
        when(resource.getName()).thenReturn("app.js");
        when(resource.getRelatedProject()).thenReturn(Optional.of(project));

        action = new RunAction(localizationConstants, appContext, device, nodeJsRunner, binaryRunner);
    }

    @Test
    public void shouldSetTitle() throws Exception {
        verify(device).getConfig();
        verify(machineConfig).getName();
        verify(localizationConstants).runActionDescription();
    }

    @Test
    public void shouldBeDisabledIfNoSelectedProject1() throws Exception {
        when(appContext.getResources()).thenReturn(null);

        action.updateInPerspective(event);

        verify(presentation).setEnabled(false);
    }

    @Test
    public void shouldBeDisabledIfNoSelectedProject2() throws Exception {
        Resource resource1 = Mockito.mock(Resource.class);
        Resource resource2 = Mockito.mock(Resource.class);
        Resource[] resources = {resource1, resource2};
        when(appContext.getResources()).thenReturn(resources);

        action.updateInPerspective(event);

        verify(presentation).setEnabled(false);
    }

    @Test
    public void shouldBeDisabledIfNoSelectedProject3() throws Exception {
        when(resource.getRelatedProject()).thenReturn(Optional.absent());

        action.updateInPerspective(event);

        verify(presentation).setEnabled(false);
    }

    @Test
    public void shouldBeDisabledIfSelectedProjectIsNotNodeOrC() throws Exception {
        when(project.isTypeOf(anyString())).thenReturn(false);

        action.updateInPerspective(event);

        verify(presentation).setEnabled(false);
    }

    @Test
    public void shouldBeEnabledIfSelectedProjectIsNodeJs() throws Exception {
        when(project.isTypeOf(NODE_JS_PROJECT_TYPE_ID)).thenReturn(true);

        action.updateInPerspective(event);

        verify(presentation).setEnabled(true);
    }

    @Test
    public void shouldBeEnabledIfSelectedProjectIsC() throws Exception {
        when(project.isTypeOf(C_PROJECT_TYPE_ID)).thenReturn(true);

        action.updateInPerspective(event);

        verify(presentation).setEnabled(true);
    }

    @Test
    public void nodeJsFileShouldBeRan() throws Exception {
        when(project.isTypeOf(NODE_JS_PROJECT_TYPE_ID)).thenReturn(true);

        action.actionPerformed(event);

        verify(nodeJsRunner).run(device);
    }

    @Test
    public void binaryFileShouldBeRan() throws Exception {
        when(project.isTypeOf(C_PROJECT_TYPE_ID)).thenReturn(true);

        action.actionPerformed(event);

        verify(binaryRunner).run(device);
    }

    @Test
    public void anyRunnerShouldNotBeRan() throws Exception {
        when(project.isTypeOf(C_PROJECT_TYPE_ID)).thenReturn(false);
        when(project.isTypeOf(NODE_JS_PROJECT_TYPE_ID)).thenReturn(false);

        action.actionPerformed(event);

        verify(binaryRunner, never()).run(device);
        verify(nodeJsRunner, never()).run(device);
    }
}
