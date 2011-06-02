/*
 *  JBoss, Home of Professional Open Source Copyright 2011, Red Hat, Inc., and
 *  individual contributors by the @authors tag. See the copyright.txt in the
 *  distribution for a full listing of individual contributors.
 *
 *  This is free software; you can redistribute it and/or modify it under the
 *  terms of the GNU Lesser General Public License as published by the Free
 *  Software Foundation; either version 2.1 of the License, or (at your option)
 *  any later version.
 *
 *  This software is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 *  details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this software; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 *  site: http://www.fsf.org.
 */
package org.jboss.logging.generator;

import org.jboss.logging.generator.Annotations.FormatType;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a> - 20.Feb.2011
 */
final class Message {

    private final int id;
    private final boolean hasId;
    private final String value;
    private final FormatType format;

    /**
     * Create the message descriptor.
     *
     * @param id     the message id.
     * @param hasId  {@code true} if message has an id, otherwise {@code false}.
     * @param value  the message value.
     * @param format the message format type.
     */
    private Message(int id, boolean hasId, String value, FormatType format) {
        this.id = id;
        this.hasId = hasId;
        this.value = value;
        this.format = format;
    }

    /**
     * Creates   the message descriptor.
     *
     * @param id     the message id.
     * @param hasId  {@code true} if message has an id, otherwise {@code false}.
     * @param value  the message value.
     * @param format the message format type.
     *
     * @return the message that was created.
     */
    public static Message of(int id, boolean hasId, String value, FormatType format) {
        return new Message(id, hasId, value, format);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 0;
        result = prime * result + id;
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        result = prime * result + ((format == null) ? 0 : format.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return false;
        }
        if (!(obj instanceof Message)) {
            return false;
        }
        final Message other = (Message) obj;
        if (this.id != other.id) {
            return false;
        }
        if ((this.value == null) ? (other.value != null) : !this.value.equals(other.value)) {
            return false;
        }
        if (this.format != other.format) {
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getClass().getSimpleName()).
                append("(id=").
                append(id).
                append(",hasId=").
                append(hasId).
                append(",value=").
                append(value).
                append(",format=").
                append(format).
                append(")");
        return stringBuilder.toString();
    }

    public int id() {
        return id;
    }

    public boolean hasId() {
        return hasId;
    }

    public String value() {
        return value;
    }

    public FormatType format() {
        return format;
    }
}
