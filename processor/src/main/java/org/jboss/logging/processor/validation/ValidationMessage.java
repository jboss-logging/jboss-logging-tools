/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.jboss.logging.processor.validation;

import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

import org.jboss.logging.processor.model.DelegatingElement;

/**
 * Date: 12.08.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public interface ValidationMessage {

    /**
     * Validation message type enum.
     */
    enum Type {
        ERROR,
        WARN
    }

    /**
     * The type of the message.
     *
     * @return the type of the message.
     */
    Type type();

    /**
     * Returns the element that caused the error.
     *
     * @return the element that caused the error.
     */
    Element getElement();

    /**
     * The annotation the error occurred on.
     *
     * @return the annotation the error occurred on or {@code null} if this was not an annotation error
     */
    AnnotationMirror getAnnotationMirror();

    /**
     * The value of the annotation which caused the error.
     *
     * @return the value of the annotation or {@code null}
     */
    AnnotationValue getAnnotationValue();

    /**
     * Returns the error message.
     *
     * @return the error message.
     */
    String getMessage();

    /**
     * Prints the message and returns {@code true} if the message was an error message.
     *
     * @param messager the messager used to print the message
     *
     * @return {@code true} if this was an error message otherwise {@code false}
     */
    default boolean printMessage(final Messager messager) {
        boolean error = false;
        Element element = getElement();
        element = (element instanceof DelegatingElement ? ((DelegatingElement) element).getDelegate() : element);
        final AnnotationMirror annotationMirror = getAnnotationMirror();
        final AnnotationValue annotationValue = getAnnotationValue();
        final Diagnostic.Kind kind;
        if (type() == Type.ERROR) {
            kind = Diagnostic.Kind.ERROR;
            error = true;
        } else {
            kind = Diagnostic.Kind.WARNING;
        }
        if (annotationMirror == null) {
            messager.printMessage(kind, getMessage(), element);
        } else if (annotationValue == null) {
            messager.printMessage(kind, getMessage(), element, annotationMirror);
        } else {
            messager.printMessage(kind, getMessage(), element, annotationMirror, annotationValue);
        }
        return error;
    }
}
