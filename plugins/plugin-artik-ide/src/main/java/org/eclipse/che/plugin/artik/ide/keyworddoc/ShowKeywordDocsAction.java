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

import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ProjectAction;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.text.TextRange;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.plugin.artik.ide.ArtikLocalizationConstant;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

/**
 * Action for showing documentation for keyword.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class ShowKeywordDocsAction extends ProjectAction {

    private final EditorAgent              editorAgent;
    private final KeywordDocsServiceClient keywordDocsServiceClient;
    private final NotificationManager      notificationManager;
    private final AppContext               appContext;

    @Inject
    public ShowKeywordDocsAction(EditorAgent editorAgent,
                                 ArtikLocalizationConstant localizationConstants,
                                 KeywordDocsServiceClient keywordDocsServiceClient,
                                 NotificationManager notificationManager,
                                 AppContext appContext) {
        super(localizationConstants.showKeywordDocActionTitle(), localizationConstants.showKeywordDocActionDescription());
        this.editorAgent = editorAgent;
        this.keywordDocsServiceClient = keywordDocsServiceClient;
        this.notificationManager = notificationManager;
        this.appContext = appContext;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        EditorPartPresenter activeEditor = editorAgent.getActiveEditor();
        if (activeEditor instanceof TextEditor) {
            final Document document = ((TextEditor)activeEditor).getDocument();
            final TextRange textRange = document.getSelectedTextRange();
            final String selectedContent = document.getContentRange(textRange);

            keywordDocsServiceClient.getLink(selectedContent).then(new Operation<String>() {
                @Override
                public void apply(String link) throws OperationException {
                    Window.open(appContext.getDevMachine().getAddress() + link, "_blank", null);
                }
            }).catchError(new Operation<PromiseError>() {
                @Override
                public void apply(PromiseError promiseError) throws OperationException {
                    notificationManager.notify("", promiseError.getMessage(), FAIL, FLOAT_MODE);
                }
            });
        }
    }

    @Override
    protected void updateProjectAction(ActionEvent e) {
        e.getPresentation().setVisible(true);

        EditorPartPresenter activeEditor = editorAgent.getActiveEditor();
        if (activeEditor instanceof TextEditor) {
            final Document document = ((TextEditor)activeEditor).getDocument();
            final TextRange textRange = document.getSelectedTextRange();
            final String selectedContent = document.getContentRange(textRange);

            e.getPresentation().setEnabled(!selectedContent.isEmpty());
            return;
        }

        e.getPresentation().setEnabled(false);
    }
}
