/*
 *  JBoss, Home of Professional Open Source Copyright 2010, Red Hat, Inc., and
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
package org.jboss.logging.validation;

import javax.lang.model.element.Element;

/**
 * An exception used to indicate a validation error has been found.
 * 
 * @author James R. Perkins Jr. (jrp)
 * 
 */
public class ValidationException extends RuntimeException {
    /**
     * 
     */
    private static final long serialVersionUID = -8868861814545681091L;
    private final Element element;

    /**
     * Creates a validation exception.
     * 
     * @param element
     *            the element that caused the exception.
     */
    public ValidationException(final Element element) {
        super();
        this.element = element;
    }

    /**
     * Creates a validation exception.
     * 
     * @param message
     *            the detail message. The detail message is saved for later
     *            retrieval by the {@link Throwable.getMessage()} method.
     * @param element
     *            the element that caused the exception.
     */
    public ValidationException(final String message, final Element element) {
        super(message);
        this.element = element;
    }

    /**
     * Creates a validation exception.
     * 
     * @param cause
     *            the cause (which is saved for later retrieval by the {@link
     *            Throwable.getCause()} method). (A null value is permitted, and
     *            indicates that the cause is nonexistent or unknown.)
     * @param element
     *            the element that caused the exception.
     */
    public ValidationException(final Throwable cause, final Element element) {
        super(cause);
        this.element = element;
    }

    /**
     * Creates a validation exception.
     * 
     * @param message
     *            the detail message. The detail message is saved for later
     *            retrieval by the {@link Throwable.getMessage()} method.
     * @param cause
     *            the cause (which is saved for later retrieval by the {@link
     *            Throwable.getCause()} method). (A null value is permitted, and
     *            indicates that the cause is nonexistent or unknown.)
     * @param element
     *            the element that caused the exception.
     */
    public ValidationException(final String message, final Throwable cause,
            final Element element) {
        super(message, cause);
        this.element = element;
    }

    /**
     * Returns the element that was the cause of the exception.
     * 
     * @return the element that was the cause of the exception.
     */
    public Element getElement() {
        return element;
    }

}
