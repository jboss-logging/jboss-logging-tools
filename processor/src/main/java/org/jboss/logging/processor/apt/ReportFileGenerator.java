/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2023 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.logging.processor.apt;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.element.TypeElement;
import javax.tools.StandardLocation;

import org.jboss.logging.processor.apt.report.ReportType;
import org.jboss.logging.processor.apt.report.ReportWriter;
import org.jboss.logging.processor.model.MessageInterface;
import org.jboss.logging.processor.model.MessageMethod;

/**
 * Generates reports for logging interfaces and message bundles.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@SupportedOptions({
        ReportFileGenerator.REPORT_TYPE,
        ReportFileGenerator.REPORT_PATH,
        ReportFileGenerator.REPORT_TITLE
})
public class ReportFileGenerator extends AbstractGenerator {
    static final String REPORT_TYPE = "org.jboss.logging.tools.report.type";
    static final String REPORT_PATH = "org.jboss.logging.tools.report.path";
    static final String REPORT_TITLE = "org.jboss.logging.tools.report.title";

    private final ReportType reportType;
    private final String reportPath;
    private final String reportTitle;

    ReportFileGenerator(final ProcessingEnvironment processingEnv) {
        super(processingEnv);
        Map<String, String> options = processingEnv.getOptions();
        final String reportType = options.get(REPORT_TYPE);
        reportPath = options.get(REPORT_PATH);
        reportTitle = options.get(REPORT_TITLE);
        if (reportType == null) {
            this.reportType = null;
        } else {
            final String s = reportType.toLowerCase(Locale.ROOT);
            if ("adoc".equals(s) || "asciidoc".equals(s)) {
                this.reportType = ReportType.ASCIIDOC;
            } else if ("xml".equals(s)) {
                this.reportType = ReportType.XML;
            } else {
                this.reportType = null;
                logger().warn(null, "Report type %s is invalid. No reports will be generated.", reportType);
            }
        }

    }

    @Override
    public void processTypeElement(final TypeElement annotation, final TypeElement element,
            final MessageInterface messageInterface) {
        if (reportType != null) {
            try {
                // Don't generate empty interfaces
                if (messageInterface.methods().isEmpty()) {
                    logger().debug(element, "Skipping reports for interface %s with no methods.", messageInterface.name());
                    return;
                }
                logger().debug(element, "Writing reports for interface %s.", messageInterface.name());

                final String fileName = messageInterface.simpleName() + reportType.getExtension();
                try (
                        final BufferedWriter writer = createWriter(messageInterface.packageName(), fileName);
                        final ReportWriter reportWriter = ReportWriter.of(reportType, messageInterface, writer)) {
                    reportWriter.writeHeader(reportTitle);
                    // Process the methods
                    for (MessageMethod messageMethod : getSortedMessageMethods(messageInterface)) {
                        reportWriter.writeDetail(messageMethod);
                    }
                    reportWriter.writeFooter();
                }
            } catch (IOException e) {
                logger().error(element, e, "Failed to generate %s report", reportType);
            }
        }
    }

    private BufferedWriter createWriter(final String packageName, final String fileName) throws IOException {
        if (reportPath == null) {
            return new BufferedWriter(processingEnv.getFiler()
                    .createResource(StandardLocation.SOURCE_OUTPUT, packageName, fileName).openWriter());
        }
        final Path outputPath = Paths.get(reportPath, packageName.replace(".", FileSystems.getDefault().getSeparator()),
                fileName);
        Files.createDirectories(outputPath.getParent());
        return Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
    }

    /**
     * Returns a sorted collection of the message methods on the interface. The methods are sorted by the message id.
     *
     * @param messageInterface the message interface to get the methods for
     *
     * @return a sorted collection of message methods
     */
    private static Collection<MessageMethod> getSortedMessageMethods(final MessageInterface messageInterface) {
        final Collection<MessageMethod> messageMethods = new TreeSet<>(MessageMethodSortComparator.INSTANCE);
        messageMethods.addAll(messageInterface.methods());
        return messageMethods;
    }

    private static class MessageMethodSortComparator implements Comparator<MessageMethod> {
        static final MessageMethodSortComparator INSTANCE = new MessageMethodSortComparator();

        @Override
        public int compare(final MessageMethod o1, final MessageMethod o2) {
            // First sort by message id, then message to ensure uniqueness
            int result = Integer.compare(o1.message().id(), o2.message().id());
            if (result == 0) {
                result = o1.message().value().compareTo(o2.message().value());
            }
            return result;
        }
    }
}
