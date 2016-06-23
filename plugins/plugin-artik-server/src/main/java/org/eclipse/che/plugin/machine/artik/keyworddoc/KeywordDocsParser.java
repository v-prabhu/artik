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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Parser for Artik API documentation pages.
 *
 * @author Artem Zatsarynnyi
 */
public class KeywordDocsParser {

    private static Pattern regexp = Pattern.compile("\"[a-zA-Z0-9_#.]+\"");

    /**
     * Parses all documentation pages in the specified directory.
     *
     * @param path
     *         path to the directory that contains documentation pages
     * @return map of the keywords to the according pages
     * @throws IOException
     *         if any error occurred while parsing
     */
    public static Map<String, String> parseFolder(Path path) throws IOException {
        List<Path> docFiles = Files.list(path)
                                   .filter(p -> p.toString().endsWith(".js"))
                                   .collect(Collectors.toList());

        Map<String, String> links = new HashMap<>();

        for (Path docFile : docFiles) {
            links.putAll(parseFile(docFile));
        }

        return links;
    }

    /**
     * Parses the specified documentation page.
     *
     * @param path
     *         path to the documentation page
     * @return map of the keywords to the according pages
     * @throws IOException
     *         if any error occurred while parsing
     */
    public static Map<String, String> parseFile(Path path) throws IOException {
        Map<String, String> links = new HashMap<>();

        List<String> lines = Files.readAllLines(path);

        for (String line : lines) {
            Matcher matcher = regexp.matcher(line);

            String operator = null;
            String link = null;

            if (matcher.find()) {
                operator = matcher.group().replace("\"", "");
            }
            if (matcher.find()) {
                link = matcher.group().replace("\"", "");
            }

            if (operator != null && link != null) {
                links.put(operator, link);
            }
        }

        return links;
    }
}
