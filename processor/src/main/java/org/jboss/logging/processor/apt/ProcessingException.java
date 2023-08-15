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

package org.jboss.logging.processor.apt;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;

/**
 * An exception that can be used to log which element caused the error.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class ProcessingException extends RuntimeException {
    private final Element element;
    private final AnnotationMirror annotation;
    private final AnnotationValue annotationValue;

    /**
     * Creates a new exception.
     *
     * @param element the element the error occurs on
     * @param message the message
     */
    public ProcessingException(final Element element, final String message) {
        this(element, null, null, message);
    }

    /**
     * Creates a new exception.
     *
     * @param element    the element the error occurs on
     * @param annotation the annotation the error occurred on
     * @param message    the message
     */
    public ProcessingException(final Element element, final AnnotationMirror annotation, final String message) {
        this(element, annotation, null, message);
    }

    /**
     * Creates a new exception.
     *
     * @param element         the element the error occurs on
     * @param annotation      the annotation the error occurred on
     * @param annotationValue the annotation value
     * @param message         the message
     */
    public ProcessingException(final Element element, final AnnotationMirror annotation, final AnnotationValue annotationValue, final String message) {
        super(message);
        this.element = element;
        this.annotation = annotation;
        this.annotationValue = annotationValue;
    }

    /**
     * Creates a new exception.
     *
     * @param element the element the error occurs on
     * @param format  the format for the message
     * @param args    the arguments for the format
     */
    public ProcessingException(final Element element, final String format, final Object... args) {
        this(element, null, null, format, args);
    }

    /**
     * Creates a new exception.
     *
     * @param element    the element the error occurs on
     * @param annotation the annotation the error occurred on
     * @param format     the format for the message
     * @param args       the arguments for the format
     */
    public ProcessingException(final Element element, final AnnotationMirror annotation, final String format, final Object... args) {
        this(element, annotation, null, format, args);
    }

    /**
     * Creates a new exception.
     *
     * @param element         the element the error occurs on
     * @param annotation      the annotation the error occurred on
     * @param annotationValue the annotation value
     * @param format          the format for the message
     * @param args            the arguments for the format
     */
    public ProcessingException(final Element element, final AnnotationMirror annotation, final AnnotationValue annotationValue, final String format, final Object... args) {
        super(String.format(format, args));
        this.element = element;
        this.annotation = annotation;
        this.annotationValue = annotationValue;
    }

    /**
     * The element the error occurred on.
     *
     * @return the element
     */
    public Element getElement() {
        return element;
    }

    /**
     * The annotation where the error occurred.
     *
     * @return the annotation or {@code null} if the error did not occur on an annotation
     */
    public AnnotationMirror getAnnotation() {
        return annotation;
    }

    /**
     * The value for the annotation that is invalid.
     *
     * @return the annotation value or {@code null}
     */
    public AnnotationValue getAnnotationValue() {
        return annotationValue;
    }
}
