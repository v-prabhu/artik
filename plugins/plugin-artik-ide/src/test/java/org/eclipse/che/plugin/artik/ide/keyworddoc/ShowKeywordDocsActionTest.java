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
package org.eclipse.che.plugin.artik.ide.keyworddoc;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.text.TextRange;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.plugin.artik.ide.ArtikLocalizationConstant;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** @author Artem Zatsarynnyi */
@RunWith(MockitoJUnitRunner.class)
public class ShowKeywordDocsActionTest {

    @Mock
    private EditorAgent               editorAgent;
    @Mock
    private KeywordDocsServiceClient  keywordDocsServiceClient;
    @Mock
    private ArtikLocalizationConstant localizationConstants;
    @Mock
    private NotificationManager       notificationManager;
    @Mock
    private AppContext                appContext;

    @Mock
    private Promise<String>                         promise;
    @Captor
    private ArgumentCaptor<Operation<PromiseError>> errorCaptor;
    @Mock
    private PromiseError                            promiseError;

    @InjectMocks
    private ShowKeywordDocsAction presenter;

    @Test
    public void shouldSetTitle() throws Exception {
        verify(localizationConstants).showKeywordDocActionTitle();
        verify(localizationConstants).showKeywordDocActionDescription();
    }

    @Test
    public void shouldNotWorkForkForNonTextEditor() throws Exception {
        when(editorAgent.getActiveEditor()).thenReturn(mock(EditorPartPresenter.class));

        presenter.actionPerformed(mock(ActionEvent.class));

        verify(keywordDocsServiceClient, never()).getLink(anyString());
    }

    @Test
    public void shouldShowNotificationOnError() throws Exception {
        String selectedText = "keyword";

        TextEditor textEditor = mock(TextEditor.class);
        Document document = mock(Document.class);
        TextRange selectedTextRange = mock(TextRange.class);
        when(document.getSelectedTextRange()).thenReturn(selectedTextRange);
        when(document.getContentRange(selectedTextRange)).thenReturn(selectedText);
        when(textEditor.getDocument()).thenReturn(document);
        when(editorAgent.getActiveEditor()).thenReturn(textEditor);

        when(promise.then((Operation)anyObject())).thenReturn(promise);
        when(promise.catchError(Matchers.<Operation<PromiseError>>anyObject())).thenReturn(promise);
        when(keywordDocsServiceClient.getLink(anyString())).thenReturn(promise);

        presenter.actionPerformed(mock(ActionEvent.class));

        verify(keywordDocsServiceClient).getLink(anyString());
        verify(promise).catchError(errorCaptor.capture());
        errorCaptor.getValue().apply(promiseError);

        verify(notificationManager).notify(anyString(), anyString(), eq(FAIL), eq(FLOAT_MODE));
    }
}
