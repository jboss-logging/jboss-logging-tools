/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.logging.processor.report;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.SchemaFactory;

import org.jboss.logging.processor.apt.report.ReportType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class ReportGenerationTest {
    private static String TEST_REPORT_PATH = null;

    @BeforeAll
    public static void setUp() {
        TEST_REPORT_PATH = System.getProperty("test.report.path");
    }

    @Test
    public void testAsciidoc() throws Exception {
        final Collection<Path> paths = findFiles(ReportType.ASCIIDOC);
        // Just ensure they were generated
        Assertions.assertFalse(paths.isEmpty(), "No asciidoc files found");
    }

    @Test
    public void testXml() throws Exception {
        final Collection<Path> paths = findFiles(ReportType.XML);
        Assertions.assertFalse(paths.isEmpty(), "No XML files found");
        final SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);

        final SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        factory.setSchema(schemaFactory.newSchema(getClass().getResource("/schema/logging-report_1_0.xsd")));

        // Crudely test each XML file
        for (Path path : paths) {
            final SAXParser parser = factory.newSAXParser();

            final XMLReader reader = parser.getXMLReader();
            reader.setErrorHandler(new ErrorHandler() {
                @Override
                public void warning(final SAXParseException exception) {
                    // ignore
                }

                @Override
                public void error(final SAXParseException exception) {
                    fail(exception);
                }

                @Override
                public void fatalError(final SAXParseException exception) {
                    fail(exception);
                }

                private void fail(final SAXParseException exception) {
                    Assertions.fail(String.format("%s - Line: %d Column: %d%nPath: %s", exception.getMessage(), exception.getLineNumber(), exception.getColumnNumber(), path));
                }
            });
            try (final Reader fileReader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                reader.parse(new InputSource(fileReader));
            }
        }
    }

    private static Collection<Path> findFiles(final ReportType reportType) throws IOException {

        final Path dir = Paths.get(TEST_REPORT_PATH);
        final List<Path> paths = new ArrayList<>();

        Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) {
                if (file.toString().endsWith(reportType.getExtension())) {
                    paths.add(file);
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return paths;
    }
}
