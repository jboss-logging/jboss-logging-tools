/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

package org.jboss.logging.generator.validation;

import javax.lang.model.element.Element;

/**
 * Date: 09.04.2011
 *
 * @author <a href="mailto:jrperkinsjr@gmail.com">James R. Perkins</a>
 */
public class ValidationWarningMessage implements ValidationMessage {

    private final Element element;
    private final String message;

    public ValidationWarningMessage(final Element element, final String message) {
        this.element = element;
        this.message = message;
    }

    /**
     * Creates a new validation warning message.
     *
     * @param element the element to create the message for.
     * @param message the message for the warning.
     *
     * @return a new validation warning message.
     */
    public static ValidationWarningMessage of(final Element element, final String message) {
        return new ValidationWarningMessage(element, message);
    }

    /**
     * Creates a new formatted validation warning message.
     *
     * @param element       the element to create the message for.
     * @param messageFormat the message format.
     * @param args          the replacement arguments for {@link String#format(java.lang.String, java.lang.Object[])}.
     *
     * @return a new validation warning message.
     */
    public static ValidationWarningMessage of(final Element element, final String messageFormat, final Object... args) {
        return new ValidationWarningMessage(element, String.format(messageFormat, args));
    }

    @Override
    public MessageType type() {
        return MessageType.WARN;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int hash = 1;
        hash = prime * hash + ((element == null) ? 0 : element.hashCode());
        hash = prime * hash + ((message == null) ? 0 : message.hashCode());
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ValidationWarningMessage)) {
            return false;
        }
        final ValidationWarningMessage other = (ValidationWarningMessage) obj;
        if ((this.element == null) ? (other.element != null) : !this.element.equals(other.message)) {
            return false;
        }
        if ((this.message == null) ? (other.message != null) : !this.message.equals(other.message)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        result.append(getClass().getSimpleName()).
                append("(element=").
                append(element).
                append(", message=").
                append(message).
                append(")");
        return result.toString();
    }

    @Override
    public Element getElement() {
        return element;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
