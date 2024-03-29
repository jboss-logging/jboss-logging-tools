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
import java.io.IOException;

import org.jboss.logging.processor.model.LoggerMessageMethod;
import org.jboss.logging.processor.model.MessageInterface;
import org.jboss.logging.processor.model.MessageMethod;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
class AsciidocReportWriter extends ReportWriter {

    private final BufferedWriter writer;

    AsciidocReportWriter(final MessageInterface messageInterface, final BufferedWriter writer) {
        super(messageInterface);
        this.writer = writer;
    }

    @Override
    public void writeHeader(final String title) throws IOException {
        // Write the title for the document
        final String escapedTitle;
        if (title != null) {
            escapedTitle = escape(title).toString();
        } else {
            escapedTitle = "Messages";
        }
        writer.write(escapedTitle);
        writer.newLine();
        for (int i = 0; i < escapedTitle.length(); i++) {
            writer.append('=');
        }
        writer.newLine();
        writer.newLine();

        // Write the table title
        writer.append('.').append(messageInterface.name());
        writer.newLine();
        // Write the table configuration, 4 columns
        writer.write("[cols=\"1,5,^1,2m\"]");
        writer.newLine();
        // Write the table header
        writer.write("|===");
        writer.newLine();
        writer.write("|Message Id |Message |Log Level |Return Type");
        writer.newLine();
        writer.newLine();
    }

    @Override
    public void writeDetail(final MessageMethod messageMethod) throws IOException {
        final MessageMethod.Message msg = messageMethod.message();
        final String id = (msg.hasId() ? String.format(messageIdFormat, msg.id()) : DEFAULT_ID);
        final String url = getUrl(messageMethod, id);
        if (url.isEmpty()) {
            writer.append('|').append(escape(id));
        } else {
            writer.append("|link:").append(url).append('[').append(id).append(']');
        }
        writer.newLine();
        writer.append('|').append(escape(msg.value()));
        writer.newLine();
        if (messageMethod instanceof LoggerMessageMethod) {
            writer.append('|').append(getLogLevel((LoggerMessageMethod) messageMethod));
            writer.newLine();
            writer.append("|void");
        } else {
            writer.append("|--");
            writer.newLine();
            writer.append('|').append(messageMethod.returnType().name());
        }
        writer.newLine();
        writer.newLine();
    }

    @Override
    public void writeFooter() throws IOException {
        // End the table
        writer.write("|===");
        writer.newLine();
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }

    @Override
    ReportType getReportType() {
        return ReportType.ASCIIDOC;
    }

    private CharSequence escape(final CharSequence s) {
        final StringBuilder sb = new StringBuilder();
        final int len = s.length();
        int aPos = -1;
        int offset = 0;
        char previous = 0x00;
        for (int i = 0; i < len; i++) {
            final char c = s.charAt(i);
            switch (c) {
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
