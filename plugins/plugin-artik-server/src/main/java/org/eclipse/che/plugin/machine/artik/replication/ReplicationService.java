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
package org.eclipse.che.plugin.machine.artik.replication;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.commons.json.JsonParseException;
import org.eclipse.che.plugin.artik.shared.dto.ArtikVfsModificationEventDto;
import org.eclipse.che.plugin.machine.artik.replication.event.ArtikVfsModificationEventQueueHolder;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.stream.Collectors.toList;
import static org.eclipse.che.api.core.model.machine.MachineStatus.RUNNING;
import static org.eclipse.che.plugin.machine.artik.replication.ReplicationManager.USE_REPLICATION_ROOT;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * It is responsible for synchronization of content of a local machine's file system
 * with a remote.
 *
 * Basically what it does is:
 * <ul>
 *     <li>Uses {@link ArtikVfsModificationEventQueueHolder} to populate the {@link List} of
 *     pending events</li>
 *     <li>Process the {@link List} of pending events to get accumulated lists of</li>
 *     <ul>
 *         <li>Created folders</li>
 *         <li>Created files</li>
 *         <li>Modified files</li>
 *     </ul>
 *     <li>
 *         Creates on a remote machine new folders, copy modified and newly craated files
 *     </li>
 * </ul>
 * <p>
 *     Please note two things:
 *     <ul>
 *         <li>The replicator does not cover file removal operations (both sides)</li>
 *         <li>The synchronization process is a one-side process: from a local machine to a
 *         remote</li>
 *     </ul>
 * </p>
 *
 * @author Dmitry Kuleshov
 *
 * @since 4.5
 */
@Beta
@Singleton
public class ReplicationService {
    private static final Logger LOG   = getLogger(ReplicationService.class);
    private static final String ARTIK = "artik";

    private final ArtikVfsModificationEventQueueHolder queueHolder;
    private final ReplicationManager                   replicationManager;
    private final ApiRequestHelper                     apiRequestHelper;
    private final ExecutorService                      executor;
    private final AtomicBoolean                        running;

    @Inject
    public ReplicationService(ArtikVfsModificationEventQueueHolder queueHolder,
                              ReplicationManager replicationManager,
                              ApiRequestHelper apiRequestHelper) {
        this.queueHolder = queueHolder;
        this.apiRequestHelper = apiRequestHelper;
        this.replicationManager = replicationManager;

        final String threadName = getClass().getSimpleName().concat("Thread-%d");
        final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat(threadName)
                                                                      .setDaemon(TRUE)
                                                                      .build();

        this.executor = newSingleThreadExecutor(threadFactory);
        this.running = new AtomicBoolean(FALSE);
    }

    @PostConstruct
    void postConstruct() {
        running.compareAndSet(FALSE, TRUE);
        executor.execute(this::doTheThings);
    }

    @PreDestroy
    void preDestroy() {
        running.compareAndSet(TRUE, FALSE);
        executor.shutdown();
    }

    private void doTheThings() {
            while (running.get()) {
            run();
        }
    }

    void run() {
        final Optional<ArtikVfsModificationEventDto> optional = queueHolder.take();
        if (optional.isPresent()) {
            try {
                for (Machine machine : getMachines()) {
                    replicateDirectories(machine, optional.get().getCreatedDirectories());
                    replicateDirectories(machine, optional.get().getModifiedDirectories());
                    replicateFiles(machine, optional.get().getCreatedFiles());
                    replicateFiles(machine, optional.get().getModifiedFiles());
                }
            } catch (JsonParseException | IOException | ApiException e) {
                LOG.error("Error trying to push to a machine files or folders.", e);
                preDestroy();
            }
        }
    }

    private void replicateFiles(Machine machine, List<String> itemPaths) throws IOException, ServerException, JsonParseException {
        for (String itemPath : itemPaths) {
            replicationManager.makeDirectory(machine.getId(), itemPath.substring(0, itemPath.lastIndexOf('/')));
            replicationManager.copy(machine.getId(), itemPath, itemPath, USE_REPLICATION_ROOT);
        }
    }

    private void replicateDirectories(Machine machine, List<String> itemPaths) throws IOException, ServerException, JsonParseException {
        for (String itemPath : itemPaths) {
            replicationManager.makeDirectory(machine.getId(), itemPath);
        }
    }

    private List<Machine> getMachines() throws ApiException, IOException {
        return apiRequestHelper.getMachines()
                               .stream()
                               .filter(o -> RUNNING.equals(o.getStatus()))
                               .filter(o -> ARTIK.equals(o.getConfig().getType()))
                               .collect(toList());
    }
}
