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
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.jboss.logging.processor.model.MessageInterface;
import org.jboss.logging.processor.model.MessageMethod;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
class XmlReportWriter extends ReportWriter {
    private static final String NAMESPACE = "urn:jboss:logging:report:1.0";
    private final XMLStreamWriter xmlWriter;

    XmlReportWriter(final MessageInterface messageInterface, final BufferedWriter writer) throws XMLStreamException {
        super(messageInterface);
        final XMLOutputFactory factory = XMLOutputFactory.newInstance();
        xmlWriter = new IndentingXmlWriter(factory.createXMLStreamWriter(writer));
    }

    @Override
    public void writeHeader(final String title) throws IOException {
        try {
            xmlWriter.writeStartDocument();
            xmlWriter.setDefaultNamespace(NAMESPACE);
            xmlWriter.writeStartElement("report");
            xmlWriter.writeNamespace(null, NAMESPACE);
            if (title != null) {
                xmlWriter.writeAttribute("title", title);
            }
            xmlWriter.writeStartElement("messages");
            xmlWriter.writeAttribute("interface", messageInterface.name());
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void writeDetail(final MessageMethod messageMethod) throws IOException {
        try {
            xmlWriter.writeStartElement("message");
            final MessageMethod.Message msg = messageMethod.message();
            final String url;
            if (msg.hasId()) {
                final String id = String.format(messageIdFormat, msg.id());
                xmlWriter.writeAttribute("id", id);
                url = getUrl(messageMethod, id);
            } else {
                url = getUrl(messageMethod, DEFAULT_ID);
            }
            if (!url.isEmpty()) {
                xmlWriter.writeAttribute("resolutionUrl", url);
            }
            if (messageMethod.isLoggerMethod()) {
                xmlWriter.writeAttribute("logLevel", getLogLevel(messageMethod));
            } else {
                xmlWriter.writeAttribute("returnType", messageMethod.returnType().name());
            }
            xmlWriter.writeCharacters(msg.value());
            xmlWriter.writeEndElement();
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void writeFooter() throws IOException {
        try {
            xmlWriter.writeEndElement(); // end <messages/>
            xmlWriter.writeEndElement(); // end <report/>
            xmlWriter.writeEndDocument();
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            if (xmlWriter != null) xmlWriter.close();
        } catch (XMLStreamException ignore) {
        }
    }

    @Override
    ReportType getReportType() {
        return ReportType.XML;
    }
}
