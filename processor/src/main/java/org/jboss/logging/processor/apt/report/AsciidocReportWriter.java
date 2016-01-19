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
import java.io.IOException;
import java.util.Optional;

import org.jboss.logging.processor.model.MessageInterface;
import org.jboss.logging.processor.model.MessageMethod;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
class AsciidocReportWriter extends ReportWriter {

    protected AsciidocReportWriter(final BufferedWriter writer) {
        super(writer);
    }

    @Override
    public void write(final MessageInterface messageInterface) throws IOException {
        writeTableHeader(messageInterface);

        final String idFormat = createMessageIdFormat(messageInterface);

        // Process the methods
        for (MessageMethod messageMethod : getSortedMessageMethods(messageInterface)) {
            writeTableRow(idFormat, messageMethod);
        }

        // End the table
        writeTableEnd();
    }

    @Override
    public void writeStart(final Optional<String> title) throws IOException {
        // Write a title
        writer.write("= ");
        writer.write(escape(title.orElse("Messages")).toString());
        writer.newLine();
        writer.newLine();
    }

    private void writeTableHeader(final MessageInterface messageInterface) throws IOException {
        // Write the table title
        writer.append('.').append(messageInterface.name());
        writer.newLine();
        // Write the table header
        writer.write("|===");
        writer.newLine();
        writer.write("|Message Id |Message |Log Level |Return Type");
        writer.newLine();
        writer.newLine();
    }

    private void writeTableRow(final String idFormat, final MessageMethod method) throws IOException {
        final MessageMethod.Message msg = method.message();
        final String id = (msg.hasId() ? String.format(idFormat, msg.id()) : "none");
        writer.append('|').append(escape(id));
        writer.newLine();
        writer.append('|').append(escape(msg.value()));
        writer.newLine();
        if (method.isLoggerMethod()) {
            writer.append('|').append(getLogLevel(method));
            writer.newLine();
            writer.append("|void");
        } else {
            writer.append("|--");
            writer.newLine();
            writer.append('|').append(method.returnType().name());
        }
        writer.newLine();
        writer.newLine();
    }

    private void writeTableEnd() throws IOException {
        writer.write("|===");
        writer.newLine();
    }

    private CharSequence escape(final CharSequence s) {
        final StringBuilder sb = new StringBuilder();
        final int len = s.length();
        int sqPos = -1;
        int aPos = -1;
        int offset = 0;
        char previous = 0x00;
        for (int i = 0; i < len; i++) {
            final char c = s.charAt(i);
            switch (c) {
                // Surrounding quotes will print emphasize text if not escaped
                // Example: 'some test' should be changed to \'some text'
                case '\'':
                    if (sqPos >= 0) {
                        sb.insert(sqPos + offset++, '\\');
                        sqPos = -1;
                    } else {
                        sqPos = i;
                    }
                    break;
                // Asterisks around text make it bold, unless there is a space after a beginning asterisk or a space
                // before an ending asterisk
                case '*':
                    if (aPos >= 0) {
                        if (previous != ' ') {
                            sb.insert(aPos + offset++, '\\');
                        }
                        aPos = -1;
                    } else if ((i + 1) < len) {
                        final char next = s.charAt(i + 1);
                        if (next != ' ') {
                            aPos = i;
                        }
                    }
                    break;
            }
            previous = c;
            sb.append(c);
        }
        return sb;
    }
}
