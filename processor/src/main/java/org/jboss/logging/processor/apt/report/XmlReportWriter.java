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
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.jboss.logging.processor.model.LoggerMessageMethod;
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
            if (messageMethod instanceof LoggerMessageMethod) {
                xmlWriter.writeAttribute("logLevel", getLogLevel((LoggerMessageMethod) messageMethod));
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
