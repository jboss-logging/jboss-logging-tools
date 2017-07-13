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

package org.jboss.logging.processor.apt;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
            if ("adoc".equalsIgnoreCase(s)) {
                this.reportType = ReportType.ASCIIDOC;
            } else if ("xml".equalsIgnoreCase(s)) {
                this.reportType = ReportType.XML;
            } else {
                this.reportType = null;
                logger().warn(null, "Report type %s is invalid. No reports will be generated.", reportType);
            }
        }

    }

    @Override
    public void processTypeElement(final TypeElement annotation, final TypeElement element, final MessageInterface messageInterface) {
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
                        final ReportWriter reportWriter = ReportWriter.of(reportType, messageInterface, writer)
                ) {
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
            return new BufferedWriter(processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, packageName, fileName).openWriter());
        }
        final Path outputPath = Paths.get(reportPath, packageName.replace(".", FileSystems.getDefault().getSeparator()), fileName);
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
        // Ensure the messages are sorted by the id
        final List<MessageMethod> messageMethods = new ArrayList<>(messageInterface.methods());
        messageMethods.sort(MessageIdComparator.INSTANCE);
        return Collections.unmodifiableCollection(messageMethods);
    }

    private static class MessageIdComparator implements Comparator<MessageMethod> {
        static final MessageIdComparator INSTANCE = new MessageIdComparator();

        @Override
        public int compare(final MessageMethod o1, final MessageMethod o2) {
            return Integer.compare(o1.message().id(), o2.message().id());
        }
    }
}
