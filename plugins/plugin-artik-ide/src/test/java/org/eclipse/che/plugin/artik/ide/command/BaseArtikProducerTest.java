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
package org.eclipse.che.plugin.artik.ide.command;

import com.google.common.base.Optional;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.extension.machine.client.command.custom.CustomCommandType;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** @author Artem Zatsarynnyi */
@RunWith(MockitoJUnitRunner.class)
public abstract class BaseArtikProducerTest {

    @Mock
    protected CustomCommandType customCommandType;
    @Mock
    protected DtoFactory        dtoFactory;
    @Mock
    protected AppContext        appContext;

    protected Project currentProject;
    protected File    currentResource;

    @Before
    public void setUp() {
        currentProject = mock(Project.class);

        currentResource = mock(File.class);
        when(currentResource.getRelatedProject()).thenReturn(Optional.of(currentProject));

        when(appContext.getResources()).thenReturn(new Resource[]{currentResource});
        when(appContext.getResource()).thenReturn(currentResource);
    }
}
