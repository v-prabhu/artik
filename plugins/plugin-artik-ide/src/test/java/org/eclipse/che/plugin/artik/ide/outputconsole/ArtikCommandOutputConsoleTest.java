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
package org.eclipse.che.plugin.artik.ide.outputconsole;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwtmockito.GwtMockitoTestRunner;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.machine.shared.Constants;
import org.eclipse.che.api.machine.shared.dto.MachineProcessDto;
import org.eclipse.che.api.machine.shared.dto.event.MachineProcessEvent;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.macro.MacroProcessor;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.extension.machine.client.MachineResources;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.OutputConsoleView;
import org.eclipse.che.ide.rest.AsyncRequest;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.StringUnmarshaller;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.MessageBusProvider;
import org.eclipse.che.ide.websocket.rest.SubscriptionHandler;
import org.eclipse.che.plugin.artik.ide.machine.DeviceServiceClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.Map;

import static org.eclipse.che.ide.extension.machine.client.command.edit.EditCommandsPresenter.PREVIEW_URL_ATTR;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(GwtMockitoTestRunner.class)
public class ArtikCommandOutputConsoleTest {
    public static final String PREVIEW_URL     = "preview_url";
    public static final String DEVICE_ID       = "device_id";
    public static final String PROCESS_CHANNEL = "machine:process:" + DEVICE_ID;
    public static final String COMMAND_LINE    = "command-line";
    @Mock
    private OutputConsoleView      view;
    @Mock
    private DtoFactory             dtoFactory;
    @Mock
    private DtoUnmarshallerFactory dtoUnmarshallerFactory;
    @Mock
    private MessageBusProvider     messageBusProvider;
    @Mock
    private DeviceServiceClient    deviceServiceClient;
    @Mock
    private MachineResources       resources;
    @Mock
    private MacroProcessor         macroProcessor;
    @Mock
    private EventBus               eventBus;
    @Mock
    private AsyncRequestFactory    asyncRequestFactory;
    @Mock
    private CommandImpl            command;
    @Mock
    private Machine                device;

    private ArtikCommandOutputConsole console;

    @Mock
    private Link              link;
    @Mock
    private AsyncRequest      asyncRequest;
    @Mock
    private MessageBus        messageBus;
    @Mock
    private MachineProcessDto deviceProcessDto;

    @Mock
    private Promise<String> macroProcessorPromise;
    @Mock
    private Promise<String> asyncRequestPromise;

    @Captor
    private ArgumentCaptor<Operation<String>>       stringOperationCapture;
    @Captor
    private ArgumentCaptor<Operation<PromiseError>> promiseErrorOperationCapture;
    @Captor
    private ArgumentCaptor<SubscriptionHandler<MachineProcessEvent>> subscriptionCapture;

    @Before
    public void setUp() throws Exception {
        Map<String, String> attributes = new HashMap<>(1);
        attributes.put(PREVIEW_URL_ATTR, PREVIEW_URL);
        when(command.getAttributes()).thenReturn(attributes);

        when(messageBusProvider.getMachineMessageBus()).thenReturn(messageBus);
        when(macroProcessor.expandMacros(anyString())).thenReturn(macroProcessorPromise);
        when(macroProcessorPromise.then(Matchers.<Operation<String>>any())).thenReturn(macroProcessorPromise);
        when(device.getId()).thenReturn(DEVICE_ID);
        when(deviceProcessDto.getPid()).thenReturn(1);
        when(deviceProcessDto.getCommandLine()).thenReturn(COMMAND_LINE);
        when(deviceProcessDto.getLink(Constants.LINK_REL_GET_PROCESS_LOGS)).thenReturn(link);
        when(link.getHref()).thenReturn("link-href");

        //for attachToProcess method
        when(asyncRequestFactory.createGetRequest(anyString())).thenReturn(asyncRequest);
        when(asyncRequest.send(Matchers.<StringUnmarshaller>any())).thenReturn(asyncRequestPromise);
        when(asyncRequestPromise.then(Matchers.<Operation<String>>any())).thenReturn(asyncRequestPromise);
        when(asyncRequestPromise.catchError(Matchers.<Operation<PromiseError>>any())).thenReturn(asyncRequestPromise);

        console = new ArtikCommandOutputConsole(view,
                                                dtoFactory,
                                                dtoUnmarshallerFactory,
                                                messageBusProvider,
                                                deviceServiceClient,
                                                resources,
                                                macroProcessor,
                                                eventBus,
                                                asyncRequestFactory,
                                                command,
                                                device);

    }

    @Test
    public void previewUrlShouldBeShown() throws Exception {
        verify(view).setDelegate(console);
        verify(macroProcessor).expandMacros(PREVIEW_URL);
        verify(macroProcessorPromise).then(stringOperationCapture.capture());
        stringOperationCapture.getValue().apply(PREVIEW_URL);
        verify(view).showPreviewUrl(PREVIEW_URL);
    }

    @Test
    public void widgetShouldBeSet() throws Exception {
        AcceptsOneWidget container = mock(AcceptsOneWidget.class);
        console.go(container);

        verify(container).setWidget(view);
    }

    @Test
    public void commandShouldBeReturned() throws Exception {
        assertEquals(command, console.getCommand());
    }

    @Test
    public void titleShouldBeReturned() throws Exception {
        String title = "command_title";
        when(command.getName()).thenReturn(title);
        assertEquals(title, console.getTitle());
    }

    @Test
    public void iconShouldBeReturned() throws Exception {
        console.getTitleIcon();
        verify(resources).output();
    }

    @Test
    public void outputTextShouldBeReturned() throws Exception {
        console.getText();

        verify(view).getText();
    }

    @Test
    public void viewShouldBeScrolled() throws Exception {
        console.onOutputScrolled(true);

        verify(view).toggleScrollToEndButton(eq(true));
    }

    @Test
    public void autoScrollShouldBeEnabled() throws Exception {
        console.scrollToBottomButtonClicked();

        verify(view).toggleScrollToEndButton(false);
        verify(view).enableAutoScroll(false);
    }

    @Test
    public void textShouldBeWrapped() throws Exception {
        console.wrapTextButtonClicked();

        verify(view).wrapText(true);
        verify(view).toggleWrapTextButton(true);
    }

    @Test
    public void consoleShouldBeClear() throws Exception {
        console.clearOutputsButtonClicked();

        verify(view).clearConsole();
    }

    @Test
    public void previousLogsShouldBeRestored() throws Exception {
        console.attachToProcess(deviceProcessDto);

        verify(view).showCommandLine(COMMAND_LINE);
        verify(deviceProcessDto).getLink(Constants.LINK_REL_GET_PROCESS_LOGS);

        verify(asyncRequestPromise).then(stringOperationCapture.capture());
        stringOperationCapture.getValue().apply("[STDOUT] text");
        verify(view).print("text", false);
    }

    @Test
    public void processShouldSubscribesToTheWsChannelIfProcessLinkIsNull() throws Exception {
        when(deviceProcessDto.getLink(Constants.LINK_REL_GET_PROCESS_LOGS)).thenReturn(null);

        console.attachToProcess(deviceProcessDto);

        verify(messageBus).subscribe(eq(PROCESS_CHANNEL), subscriptionCapture.capture());
    }

    @Test
    public void processShouldSubscribesToTheWsChannelIfCanNotGetPreviousLogs() throws Exception {
        PromiseError promiseError = mock(PromiseError.class);
        console.attachToProcess(deviceProcessDto);

        verify(asyncRequestPromise).catchError(promiseErrorOperationCapture.capture());
        promiseErrorOperationCapture.getValue().apply(promiseError);

        verify(messageBus).subscribe(eq(PROCESS_CHANNEL), subscriptionCapture.capture());
    }

    @Test
    public void processShouldSubscribesToTheWsChannel() throws Exception {
        console.attachToProcess(deviceProcessDto);

        verify(asyncRequestPromise).then(stringOperationCapture.capture());
        stringOperationCapture.getValue().apply("[STDOUT] text");
        verify(view).print("text", false);

        verify(messageBus).subscribe(eq(PROCESS_CHANNEL), subscriptionCapture.capture());
    }

    @Test
    public void processShouldBeStop() throws Exception {
        console.attachToProcess(deviceProcessDto);
        console.stopProcessButtonClicked();

        verify(deviceServiceClient).stopProcess(DEVICE_ID, 1);
    }
}
