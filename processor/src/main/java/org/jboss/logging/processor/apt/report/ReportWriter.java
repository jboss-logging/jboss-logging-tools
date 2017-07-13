/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat, Inc., and individual contributors
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

package org.jboss.logging.processor.apt.report;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import javax.xml.stream.XMLStreamException;

import org.jboss.logging.processor.model.MessageInterface;
import org.jboss.logging.processor.model.MessageMethod;

/**
 * Writes reports based on a {@link MessageInterface}. These reports could be used for documented messages from logging
 * or message bundle interfaces.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public abstract class ReportWriter implements Closeable {

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
    public static ReportWriter of(final ReportType reportType, final MessageInterface messageInterface, final BufferedWriter writer) {
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
     * Gets the log level from the {@code @Message} annotation.
     *
     * @param method the method to get the log level from
     *
     * @return the log level or an empty string
     */
    String getLogLevel(final MessageMethod method) {
        if (method.isLoggerMethod()) {
            final String logLevel = method.logLevel();
            final int index = logLevel.lastIndexOf('.');
            if (index > 0) {
                return logLevel.substring(index + 1);
            }
            return logLevel;
        }
        return "";
    }
}
