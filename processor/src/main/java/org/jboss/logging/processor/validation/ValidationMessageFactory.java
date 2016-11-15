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

package org.jboss.logging.processor.validation;

import org.jboss.logging.processor.model.MessageObject;

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

    public static ValidationMessage createError(final MessageObject messageObject, final String message) {
        return new ValidationErrorMessage(messageObject, message);
    }

    public static ValidationMessage createError(final MessageObject messageObject, final String format, final Object... args) {
        return new ValidationErrorMessage(messageObject, String.format(format, args));
    }

    public static ValidationMessage createWarning(final MessageObject messageObject, final String message) {
        return new ValidationWarningMessage(messageObject, message);
    }

    public static ValidationMessage createWarning(final MessageObject messageObject, final String format, final Object... args) {
        return new ValidationWarningMessage(messageObject, String.format(format, args));
    }

    private static abstract class AbstractValidationMessage implements ValidationMessage {
        private final MessageObject messageObject;
        private final String message;

        AbstractValidationMessage(final MessageObject messageObject, final String message) {
            this.messageObject = messageObject;
            this.message = message;
        }

        @Override
        public final MessageObject getMessageObject() {
            return messageObject;
        }

        @Override
        public final String getMessage() {
            return message;
        }
    }

    private static class ValidationErrorMessage extends AbstractValidationMessage {

        private ValidationErrorMessage(final MessageObject messageObject, final String message) {
            super(messageObject, message);
        }

        @Override
        public Type type() {
            return Type.ERROR;
        }
    }

    private static class ValidationWarningMessage extends AbstractValidationMessage {

        private ValidationWarningMessage(final MessageObject messageObject, final String message) {
            super(messageObject, message);
        }

        @Override
        public Type type() {
            return Type.WARN;
        }
    }

}
