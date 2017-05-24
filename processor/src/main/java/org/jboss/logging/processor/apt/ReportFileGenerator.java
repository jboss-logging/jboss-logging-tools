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
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.element.TypeElement;
import javax.tools.StandardLocation;

import org.jboss.logging.processor.apt.report.ReportType;
import org.jboss.logging.processor.apt.report.ReportWriter;
import org.jboss.logging.processor.model.MessageInterface;

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
    public static final String REPORT_TYPE = "reportType";
    public static final String REPORT_PATH = "reportPath";
    public static final String REPORT_TITLE = "reportTitle";

    private final ReportType reportType;
    private final String reportPath;
    private final Optional<String> reportTitle;

    public ReportFileGenerator(final ProcessingEnvironment processingEnv) {
        super(processingEnv);
        Map<String, String> options = processingEnv.getOptions();
        final String reportType = options.get(REPORT_TYPE);
        reportPath = options.get(REPORT_PATH);
        reportTitle = Optional.ofNullable(options.get(REPORT_TITLE));
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
                        final ReportWriter reportWriter = ReportWriter.of(reportType, writer)
                ) {
                    reportWriter.writeStart(reportTitle);
                    reportWriter.write(messageInterface);
                    reportWriter.writeEnd();
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
        return Files.newBufferedWriter(Paths.get(reportPath, packageName.replace(".", FileSystems.getDefault().getSeparator()), fileName), StandardCharsets.UTF_8);
    }
}
