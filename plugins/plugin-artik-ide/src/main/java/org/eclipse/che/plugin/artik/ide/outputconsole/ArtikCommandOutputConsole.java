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
package org.eclipse.che.plugin.artik.ide.outputconsole;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.machine.shared.Constants;
import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.api.machine.shared.dto.MachineProcessDto;
import org.eclipse.che.api.machine.shared.dto.event.MachineProcessEvent;
import org.eclipse.che.api.machine.shared.dto.execagent.ProcessSubscribeResponseDto;
import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessDiedEventDto;
import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessStartedEventDto;
import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessStdErrEventDto;
import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessStdOutEventDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.machine.CommandOutputMessageUnmarshaller;
import org.eclipse.che.ide.api.macro.MacroProcessor;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.extension.machine.client.MachineResources;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.CommandOutputConsole;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.OutputConsoleView;
import org.eclipse.che.ide.extension.machine.client.processes.ProcessFinishedEvent;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.StringUnmarshaller;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.websocket.MessageBusProvider;
import org.eclipse.che.ide.websocket.WebSocketException;
import org.eclipse.che.ide.websocket.events.MessageHandler;
import org.eclipse.che.ide.websocket.rest.SubscriptionHandler;
import org.eclipse.che.ide.websocket.rest.Unmarshallable;
import org.eclipse.che.plugin.artik.ide.machine.DeviceServiceClient;
import org.vectomatic.dom.svg.ui.SVGResource;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.ide.extension.machine.client.command.edit.EditCommandsPresenter.PREVIEW_URL_ATTR;

/**
 * Console for Artik command output.
 */
public class ArtikCommandOutputConsole implements CommandOutputConsole, OutputConsoleView.ActionDelegate {

    private final OutputConsoleView      view;
    private final DtoFactory             dtoFactory;
    private final DtoUnmarshallerFactory dtoUnmarshallerFactory;
    private final MessageBusProvider     messageBusProvider;
    private final DeviceServiceClient    deviceServiceClient;
    private final MachineResources       resources;
    private final AsyncRequestFactory    asyncRequestFactory;
    private final CommandImpl            command;
    private final EventBus               eventBus;
    private final Machine                device;

    private int            pid;
    private String         outputChannel;
    private MessageHandler outputHandler;
    private boolean        finished;

    /** Wrap text or not */
    private boolean wrapText = false;

    /** Follow output when printing text */
    private boolean followOutput = true;

    private final List<ActionDelegate> actionDelegates = new ArrayList<>();

    @Inject
    public ArtikCommandOutputConsole(final OutputConsoleView view,
                                     final DtoFactory dtoFactory,
                                     final DtoUnmarshallerFactory dtoUnmarshallerFactory,
                                     final MessageBusProvider messageBusProvider,
                                     final DeviceServiceClient deviceServiceClient,
                                     final MachineResources resources,
                                     final MacroProcessor macroProcessor,
                                     final EventBus eventBus,
                                     final AsyncRequestFactory asyncRequestFactory,
                                     @Assisted CommandImpl command,
                                     @Assisted Machine device) {
        this.view = view;
        this.dtoFactory = dtoFactory;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.messageBusProvider = messageBusProvider;
        this.deviceServiceClient = deviceServiceClient;
        this.resources = resources;
        this.asyncRequestFactory = asyncRequestFactory;
        this.command = command;
        this.device = device;
        this.eventBus = eventBus;

        view.setDelegate(this);

        final String previewUrl = command.getAttributes().get(PREVIEW_URL_ATTR);
        if (!isNullOrEmpty(previewUrl)) {
            macroProcessor.expandMacros(previewUrl).then(new Operation<String>() {
                @Override
                public void apply(String arg) throws OperationException {
                    view.showPreviewUrl(arg);
                }
            });
        } else {
            view.hidePreview();
        }
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }

    @Override
    public CommandImpl getCommand() {
        return command;
    }

    @Override
    public String getTitle() {
        return command.getName();
    }

    @Override
    public SVGResource getTitleIcon() {
        return resources.output();
    }

    @Override
    public void listenToOutput(String wsChannel) {
        view.enableStopButton(true);
        view.toggleScrollToEndButton(true);

        outputChannel = wsChannel;
        outputHandler = new SubscriptionHandler<String>(new CommandOutputMessageUnmarshaller(device.getConfig().getName())) {
            @Override
            protected void onMessageReceived(String result) {
                view.print(result, result.endsWith("\r"));

                for (ActionDelegate actionDelegate : actionDelegates) {
                    actionDelegate.onConsoleOutput(ArtikCommandOutputConsole.this);
                }
            }

            @Override
            protected void onErrorReceived(Throwable exception) {
                wsUnsubscribe(outputChannel, this);
            }
        };

        wsSubscribe(outputChannel, outputHandler);
    }

    @Override
    public void attachToProcess(MachineProcessDto process) {
        this.pid = process.getPid();
        view.showCommandLine(process.getCommandLine());
        //try to restore previous log of the process
        final Link link = process.getLink(Constants.LINK_REL_GET_PROCESS_LOGS);
        if (link != null) {
            asyncRequestFactory.createGetRequest(link.getHref()).send(new StringUnmarshaller()).then(
                    new Operation<String>() {
                        @Override
                        public void apply(String arg) throws OperationException {
                            view.print(arg.replaceAll("\\[STDOUT\\] ", ""), false);
                            handelProcessEvents();
                        }
                    }).catchError(new Operation<PromiseError>() {
                @Override
                public void apply(PromiseError arg) throws OperationException {
                    Log.error(getClass(), arg);
                    //if logs not found will handel incoming events any way
                    handelProcessEvents();
                }
            });
        } else {
            handelProcessEvents();
        }
    }

    @Override
    public Operation<ProcessStdErrEventDto> getStdErrOperation() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Operation<ProcessStdOutEventDto> getStdOutOperation() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Operation<ProcessStartedEventDto> getProcessStartedOperation() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Operation<ProcessDiedEventDto> getProcessDiedOperation() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Operation<ProcessSubscribeResponseDto> getProcessSubscribeOperation() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void printOutput(String output) {
        throw new UnsupportedOperationException();
    }

    private void handelProcessEvents() {
        final Unmarshallable<MachineProcessEvent> unmarshaller = dtoUnmarshallerFactory.newWSUnmarshaller(MachineProcessEvent.class);
        final String processStateChannel = "machine:process:" + device.getId();
        final MessageHandler handler = new SubscriptionHandler<MachineProcessEvent>(unmarshaller) {
            @Override
            protected void onMessageReceived(MachineProcessEvent result) {
                final int processId = result.getProcessId();
                if (pid != processId) {
                    return;
                }
                switch (result.getEventType()) {
                    case STOPPED:
                        finished = true;
                        view.enableStopButton(false);
                        eventBus.fireEvent(new ProcessFinishedEvent(processId));
                        break;
                    case ERROR:
                        finished = true;
                        view.enableStopButton(false);
                        eventBus.fireEvent(new ProcessFinishedEvent(processId));
                        wsUnsubscribe(processStateChannel, this);
                        wsUnsubscribe(outputChannel, outputHandler);
                        String error = result.getError();
                        if (error == null) {
                            return;
                        }
                        view.print(error, false);
                        break;
                }
            }

            @Override
            protected void onErrorReceived(Throwable exception) {
                finished = true;
                view.enableStopButton(false);
                wsUnsubscribe(processStateChannel, this);
                wsUnsubscribe(outputChannel, outputHandler);
            }
        };
        wsSubscribe(processStateChannel, handler);
    }

    private void wsSubscribe(String wsChannel, MessageHandler handler) {
        try {
            messageBusProvider.getMachineMessageBus().subscribe(wsChannel, handler);
        } catch (WebSocketException e) {
            Log.error(getClass(), e);
        }
    }

    private void wsUnsubscribe(String wsChannel, MessageHandler handler) {
        try {
            messageBusProvider.getMachineMessageBus().unsubscribe(wsChannel, handler);
        } catch (WebSocketException e) {
            Log.error(getClass(), e);
        }
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public void stop() {
        deviceServiceClient.stopProcess(device.getId(), pid);
    }

    @Override
    public void close() {
        actionDelegates.clear();
    }

    @Override
    public void addActionDelegate(ActionDelegate actionDelegate) {
        actionDelegates.add(actionDelegate);
    }

    @Override
    public void reRunProcessButtonClicked() {
        final CommandDto commandDto = dtoFactory.createDto(CommandDto.class)
                                                .withName(command.getName())
                                                .withCommandLine(command.getCommandLine())
                                                .withType(command.getType());

        if (isFinished()) {
            deviceServiceClient.executeCommand(device.getId(), commandDto, outputChannel);
        } else {
            deviceServiceClient.stopProcess(device.getId(), pid).then(new Operation<Void>() {
                @Override
                public void apply(Void arg) throws OperationException {
                    deviceServiceClient.executeCommand(device.getId(), commandDto, outputChannel);
                }
            });
        }
    }

    @Override
    public void stopProcessButtonClicked() {
        stop();
    }

    @Override
    public void clearOutputsButtonClicked() {
        view.clearConsole();
    }

    @Override
    public void downloadOutputsButtonClicked() {
        for (ActionDelegate actionDelegate : actionDelegates) {
            actionDelegate.onDownloadOutput(this);
        }
    }

    @Override
    public void wrapTextButtonClicked() {
        wrapText = !wrapText;
        view.wrapText(wrapText);
        view.toggleWrapTextButton(wrapText);
    }

    @Override
    public void scrollToBottomButtonClicked() {
        followOutput = !followOutput;

        view.toggleScrollToEndButton(followOutput);
        view.enableAutoScroll(followOutput);
    }

    @Override
    public void onOutputScrolled(boolean bottomReached) {
        followOutput = bottomReached;
        view.toggleScrollToEndButton(bottomReached);
    }

    /**
     * Returns the console text.
     *
     * @return console text
     */
    public String getText() {
        return view.getText();
    }

}
