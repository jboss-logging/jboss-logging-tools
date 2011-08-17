package org.jboss.logging.generator.validation;

import org.jboss.logging.generator.MessageObject;

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

        protected AbstractValidationMessage(final MessageObject messageObject, final String message) {
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
        public MessageType messageType() {
            return MessageType.ERROR;
        }
    }

    private static class ValidationWarningMessage extends AbstractValidationMessage {

        private ValidationWarningMessage(final MessageObject messageObject, final String message) {
            super(messageObject, message);
        }

        @Override
        public MessageType messageType() {
            return MessageType.WARN;
        }
    }

}
