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
