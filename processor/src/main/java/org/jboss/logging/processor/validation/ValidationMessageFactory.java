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

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;

/**
 * Date: 12.08.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public final class ValidationMessageFactory {

    /**
     * Private constructor for factory.
     */
    private ValidationMessageFactory() {

    }

    public static ValidationMessage createError(final Element element, final String message) {
        return new ValidationErrorMessage(element, message, null, null);
    }

    public static ValidationMessage createError(final Element element, final String format, final Object... args) {
        return new ValidationErrorMessage(element, String.format(format, args), null, null);
    }

    public static ValidationMessage createError(final Element element, final AnnotationMirror annotationMirror, final String message) {
        return new ValidationErrorMessage(element, message, annotationMirror, null);
    }

    public static ValidationMessage createError(final Element element, final AnnotationMirror annotationMirror, final String format, final Object... args) {
        return new ValidationErrorMessage(element, String.format(format, args), annotationMirror, null);
    }

    public static ValidationMessage createError(final Element element, final AnnotationMirror annotationMirror, final AnnotationValue annotationValue, final String message) {
        return new ValidationErrorMessage(element, message, annotationMirror, annotationValue);
    }

    public static ValidationMessage createError(final Element element, final AnnotationMirror annotationMirror, final AnnotationValue annotationValue, final String format, final Object... args) {
        return new ValidationErrorMessage(element, String.format(format, args), annotationMirror, annotationValue);
    }

    public static ValidationMessage createWarning(final Element element, final String message) {
        return new ValidationWarningMessage(element, message, null, null);
    }

    public static ValidationMessage createWarning(final Element element, final String format, final Object... args) {
        return new ValidationWarningMessage(element, String.format(format, args), null, null);
    }

    private static abstract class AbstractValidationMessage implements ValidationMessage {
        private final Element element;
        private final String message;
        private final AnnotationMirror annotationMirror;
        private final AnnotationValue annotationValue;

        AbstractValidationMessage(final Element element, final String message, final AnnotationMirror annotationMirror, final AnnotationValue annotationValue) {
            this.element = element;
            this.message = message;
            this.annotationMirror = annotationMirror;
            this.annotationValue = annotationValue;
        }

        @Override
        public final Element getElement() {
            return element;
        }

        @Override
        public final String getMessage() {
            return message;
        }

        @Override
        public AnnotationMirror getAnnotationMirror() {
            return annotationMirror;
        }

        @Override
        public AnnotationValue getAnnotationValue() {
            return annotationValue;
        }
    }

    private static class ValidationErrorMessage extends AbstractValidationMessage {

        private ValidationErrorMessage(final Element element, final String message, final AnnotationMirror annotationMirror, final AnnotationValue annotationValue) {
            super(element, message, annotationMirror, annotationValue);
        }

        @Override
        public Type type() {
            return Type.ERROR;
        }
    }

    private static class ValidationWarningMessage extends AbstractValidationMessage {

        private ValidationWarningMessage(final Element element, final String message, final AnnotationMirror annotationMirror, final AnnotationValue annotationValue) {
            super(element, message, annotationMirror, annotationValue);
        }

        @Override
        public Type type() {
            return Type.WARN;
        }
    }

}
