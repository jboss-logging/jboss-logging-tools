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

package org.jboss.logging.processor.apt.report;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.jboss.logging.annotations.BaseUrl;
import org.jboss.logging.annotations.ResolutionDoc;
import org.jboss.logging.processor.model.LoggerMessageMethod;
import org.jboss.logging.processor.model.MessageInterface;
import org.jboss.logging.processor.model.MessageMethod;
import org.jboss.logging.processor.util.Expressions;

/**
 * Writes reports based on a {@link MessageInterface}. These reports could be used for documented messages from logging
 * or message bundle interfaces.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public abstract class ReportWriter implements Closeable {
    static final String DEFAULT_ID = "--";

    private final String baseUrl;
    final MessageInterface messageInterface;
    final String messageIdFormat;

    ReportWriter(final MessageInterface messageInterface) {
        this.messageInterface = messageInterface;
        final int idLen = messageInterface.getIdLength();
        if (idLen > 0) {
            messageIdFormat = messageInterface.projectCode() + "%0" + messageInterface.getIdLength() + "d";
        } else {
            messageIdFormat = messageInterface.projectCode() + "%d";
        }
        if (messageInterface.isAnnotatedWith(BaseUrl.class)) {
            baseUrl = messageInterface.getAnnotation(BaseUrl.class).value();
        } else {
            baseUrl = null;
        }
    }

    /**
     * Creates a new report writer based on the report type.
     *
     * @param reportType the report type to create the writer for
     * @param writer     the used to write the contents to
     *
     * @return the report writer to use
     *
     * @throws IllegalStateException    if there was an error creating the report writer
     * @throws IllegalArgumentException if the {@code reportType} is invalid
     */
    public static ReportWriter of(final ReportType reportType, final MessageInterface messageInterface,
            final BufferedWriter writer) {
        if (reportType == ReportType.ASCIIDOC) {
            return new AsciidocReportWriter(messageInterface, writer);
        } else if (reportType == ReportType.XML) {
            try {
                return new XmlReportWriter(messageInterface, writer);
            } catch (XMLStreamException e) {
                throw new IllegalStateException("Failed to create XML report writer.", e);
            }
        }
        throw new IllegalArgumentException("Type " + reportType + " is not a known report type.");
    }

    /**
     * Writes the header for the report.
     *
     * @param title the title of the header
     *
     * @throws IOException if an I/O error occurs
     */
    public abstract void writeHeader(String title) throws IOException;

    /**
     * Writes a detail line for the report.
     *
     * @param messageMethod the method to write the details for
     *
     * @throws IOException if an I/O error occurs
     */
    public abstract void writeDetail(MessageMethod messageMethod) throws IOException;

    /**
     * Writes the footer for the report.
     *
     * @throws IOException if an I/O error occurs
     */
    public abstract void writeFooter() throws IOException;

    /**
     * The report type for this writer.
     *
     * @return the report type
     */
    abstract ReportType getReportType();

    /**
     * Gets the log level from the {@code @Message} annotation.
     *
     * @param method the method to get the log level from
     *
     * @return the log level or an empty string
     */
    String getLogLevel(final LoggerMessageMethod method) {
        final String logLevel = method.logLevel();
        final int index = logLevel.lastIndexOf('.');
        if (index > 0) {
            return logLevel.substring(index + 1);
        }
        return logLevel;
    }

    String getUrl(final MessageMethod messageMethod, final String id) {
        final ResolutionDoc resolutionDoc = getResolutionDoc(messageMethod);
        if (resolutionDoc == null || resolutionDoc.skip() || DEFAULT_ID.equals(id)) {
            return "";
        }

        final StringBuilder result = new StringBuilder();

        String base = baseUrl == null ? "" : baseUrl;
        final String path = resolutionDoc.path();
        final String suffix = resolutionDoc.suffix();
        final String url = resolutionDoc.url();
        if (!url.isEmpty()) {
            base = url;
        }

        if (!base.isEmpty()) {
            result.append(base);
            if (!base.endsWith("/") && !base.endsWith("#")) {
                result.append('/');
            }
        }

        if (path.isEmpty()) {
            result.append(id);
        } else {
            result.append(path);
        }

        if (suffix.isEmpty()) {
            result.append(getReportType().getExtension());
        } else {
            result.append(suffix);
        }

        return Expressions.resolve(messageInterface.expressionProperties(), result.toString());
    }

    private ResolutionDoc getResolutionDoc(final MessageMethod messageMethod) {
        if (messageMethod.isAnnotatedWith(ResolutionDoc.class)) {
            return messageMethod.getAnnotation(ResolutionDoc.class);
        }
        return messageInterface.getAnnotation(ResolutionDoc.class);
    }
}
