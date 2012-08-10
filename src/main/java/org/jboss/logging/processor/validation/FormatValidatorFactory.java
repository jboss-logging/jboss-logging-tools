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

import org.jboss.logging.processor.apt.Annotations.FormatType;
import org.jboss.logging.processor.intf.model.MessageMethod;

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

    public static FormatValidator create(final FormatType format, final String message) throws IllegalStateException {
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

