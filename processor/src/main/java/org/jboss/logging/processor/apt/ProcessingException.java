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

import javax.lang.model.element.Element;

/**
 * An exception that can be used to log which element caused the error.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
class ProcessingException extends RuntimeException {
    private final Element element;

    /**
     * Creates a new exception.
     *
     * @param element the element the error occurs on
     * @param message the message
     */
    public ProcessingException(final Element element, final String message) {
        super(message);
        this.element = element;
    }

    /**
     * Creates a new exception.
     *
     * @param element the element the error occurs on
     * @param format  the format for the message
     * @param args    the arguments for the format
     */
    public ProcessingException(final Element element, final String format, final Object... args) {
        super(String.format(format, args));
        this.element = element;
    }

    /**
     * The element the error occurred on.
     *
     * @return the element
     */
    public Element getElement() {
        return element;
    }
}
