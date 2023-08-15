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

    public static ValidationMessage createError(final Element element, final AnnotationMirror annotationMirror,
            final String message) {
        return new ValidationErrorMessage(element, message, annotationMirror, null);
    }

    public static ValidationMessage createError(final Element element, final AnnotationMirror annotationMirror,
            final String format, final Object... args) {
        return new ValidationErrorMessage(element, String.format(format, args), annotationMirror, null);
    }

    public static ValidationMessage createError(final Element element, final AnnotationMirror annotationMirror,
            final AnnotationValue annotationValue, final String message) {
        return new ValidationErrorMessage(element, message, annotationMirror, annotationValue);
    }

    public static ValidationMessage createError(final Element element, final AnnotationMirror annotationMirror,
            final AnnotationValue annotationValue, final String format, final Object... args) {
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

        AbstractValidationMessage(final Element element, final String message, final AnnotationMirror annotationMirror,
                final AnnotationValue annotationValue) {
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

        private ValidationErrorMessage(final Element element, final String message, final AnnotationMirror annotationMirror,
                final AnnotationValue annotationValue) {
            super(element, message, annotationMirror, annotationValue);
        }

        @Override
        public Type type() {
            return Type.ERROR;
        }
    }

    private static class ValidationWarningMessage extends AbstractValidationMessage {

        private ValidationWarningMessage(final Element element, final String message, final AnnotationMirror annotationMirror,
                final AnnotationValue annotationValue) {
            super(element, message, annotationMirror, annotationValue);
        }

        @Override
        public Type type() {
            return Type.WARN;
        }
    }

}
