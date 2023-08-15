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

import org.jboss.logging.annotations.Message.Format;
import org.jboss.logging.processor.model.MessageMethod;

/**
 * Date: 12.08.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public final class FormatValidatorFactory {
    private FormatValidatorFactory() {
    }

    public static FormatValidator create(final MessageMethod messageMethod) throws IllegalStateException {
        if (messageMethod.message() == null) {
            return InvalidFormatValidator.of("No message annotation found.");
        }
        return create(messageMethod.message().format(), messageMethod.message().value());
    }

    public static FormatValidator create(final Format format, final String message) throws IllegalStateException {
        if (message == null) {
            return InvalidFormatValidator.of("A message is required for the format.");
        }
        if (format == null) {
            return InvalidFormatValidator.of("A format is required for the message.");
        }
        switch (format) {
            case MESSAGE_FORMAT:
                return MessageFormatValidator.of(message);
            case PRINTF:
                return StringFormatValidator.of(message);
            case NO_FORMAT:
                return NoFormatValidator.INSTANCE;
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

