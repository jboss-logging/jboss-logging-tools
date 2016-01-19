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

package org.jboss.logging.processor.apt.report;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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
    /**
     * The backing writer to use.
     */
    protected final BufferedWriter writer;

    protected ReportWriter(final BufferedWriter writer) {
        this.writer = writer;
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
    public static ReportWriter of(final ReportType reportType, final BufferedWriter writer) {
        if (reportType == ReportType.ASCIIDOC) {
            return new AsciidocReportWriter(writer);
        } else if (reportType == ReportType.XML) {
            try {
                return new XmlReportWriter(writer);
            } catch (XMLStreamException e) {
                throw new IllegalStateException("Failed to create XML report writer.", e);
            }
        }
        throw new IllegalArgumentException("Type " + reportType + " is not a known report type.");
    }

    /**
     * Writes the message interface to the reports ouput.
     *
     * @param messageInterface the message interface to write
     *
     * @throws IOException if a write failure occurs
     */
    public abstract void write(MessageInterface messageInterface) throws IOException;

    /**
     * Writes a header for the report.
     *
     * @param title the optional title for the report
     *
     * @throws IOException if a write failure occurs
     */
    public void writeStart(final Optional<String> title) throws IOException {
        // Do nothing
    }

    /**
     * Writes any ending information for the report.
     *
     * @throws IOException if a write failure occurs
     */
    public void writeEnd() throws IOException {
        // Do nothing
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }

    /**
     * Gets the log level from the {@code @Message} annotation.
     *
     * @param method the method to get the log level from
     *
     * @return the log level or an empty string
     */
    protected String getLogLevel(final MessageMethod method) {
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

    /**
     * Returns a sorted collection of the message methods on the interface. The methods are sorted by the message id.
     *
     * @param messageInterface the message interface to get the methods for
     *
     * @return a sorted collection of message methods
     */
    protected Collection<MessageMethod> getSortedMessageMethods(final MessageInterface messageInterface) {
        // Ensure the messages are sorted by the id
        final List<MessageMethod> messageMethods = new ArrayList<>(messageInterface.methods());
        Collections.sort(messageMethods, MessageIdComparator.INSTANCE);
        return Collections.unmodifiableCollection(messageMethods);
    }

    /**
     * Creates a {@linkplain java.util.Formatter format} string for creating message id's.
     *
     * @param messageInterface the message interface to create the format string for
     *
     * @return the format string
     */
    static String createMessageIdFormat(final MessageInterface messageInterface) {
        final int idLen = messageInterface.getIdLength();
        if (idLen > 0) {
            return messageInterface.projectCode() + "%0" + messageInterface.getIdLength() + "d";
        }
        return messageInterface.projectCode() + "%d";
    }

    static class MessageIdComparator implements Comparator<MessageMethod> {
        static final MessageIdComparator INSTANCE = new MessageIdComparator();

        @Override
        public int compare(final MessageMethod o1, final MessageMethod o2) {
            return Integer.compare(o1.message().id(), o2.message().id());
        }
    }
}
