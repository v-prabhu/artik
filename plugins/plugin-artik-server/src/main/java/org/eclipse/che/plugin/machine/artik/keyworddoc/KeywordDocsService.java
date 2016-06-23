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
package org.eclipse.che.plugin.machine.artik.keyworddoc;

import com.google.inject.Singleton;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.rest.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Artik API keyword documentation service.
 *
 * @author Artem Zatsarynnyi
 */
@Path("/artikdoc/{ws-id}")
@Singleton
public class KeywordDocsService extends Service {

    private static final Logger LOG = LoggerFactory.getLogger(KeywordDocsService.class);

    private final Map<String, String> docLinks;
    private final String              docsPath;

    @Inject
    public KeywordDocsService(@Named("artik.apidocs.path") String docsPath) {
        this.docsPath = docsPath;
        docLinks = new HashMap<>();
    }

    @PostConstruct
    private void start() {
        final java.nio.file.Path docPath = Paths.get(docsPath);

        try {
            Map<String, String> links = KeywordDocsParser.parseFolder(docPath);
            for (Map.Entry<String, String> entry : links.entrySet()) {
                docLinks.put(entry.getKey(), "/artikdocs/" + entry.getValue());
            }
        } catch (IOException e) {
            LOG.info("Unable to parse Artik API documentation files", e);
        }
    }

    @GET
    @Path("/{keyword}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public String getLink(@PathParam("keyword") String keyword) throws NotFoundException {
        return Optional.ofNullable(docLinks.get(keyword))
                       .orElseThrow(() -> new NotFoundException("Documentation not found for " + keyword));
    }
}
