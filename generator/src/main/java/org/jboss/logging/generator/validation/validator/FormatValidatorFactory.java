package org.jboss.logging.generator.validation.validator;

import org.jboss.logging.generator.Annotations;
import org.jboss.logging.generator.MessageMethod;

/**
 * Date: 12.08.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public final class FormatValidatorFactory {
    private FormatValidatorFactory() {
    }

    public static FormatValidator create(final MessageMethod method) throws IllegalStateException {
        if (method.message() == null) {
            return InvalidFormatValidator.of("No message annotation found.");
        }
        final String msg = method.message().value();
        final Annotations.FormatType format = method.message().format();
        if (msg == null) {
            return InvalidFormatValidator.of("A message is required for the format.");
        }
        if (format == null) {
            return InvalidFormatValidator.of("A format is required for the message.");
        }
        switch (format) {
            case MESSAGE_FORMAT:
                return MessageFormatValidator.of(msg);
            case PRINTF:
                return StringFormatValidator.of(msg);
        }
        return InvalidFormatValidator.of(String.format("Format %s is invalid.", format));
    }


    private static final class InvalidFormatValidator extends AbstractFormatValidator {

        private InvalidFormatValidator() {
            super();
        }

        static FormatValidator of(final String summaryMessage) {
            final InvalidFormatValidator result = new InvalidFormatValidator();
            result.setSummaryMessage(summaryMessage);
            return result;
        }

        static FormatValidator of(final String summaryMessage, final String detailMessage) {
            final InvalidFormatValidator result = new InvalidFormatValidator();
            result.setDetailMessage(detailMessage);
            result.setSummaryMessage(summaryMessage);
            return result;
        }

        @Override
        public String format() {
            return "";
        }

        @Override
        public int argumentCount() {
            return 0;
        }

        @Override
        public boolean isValid() {
            return false;
        }
    }
}

