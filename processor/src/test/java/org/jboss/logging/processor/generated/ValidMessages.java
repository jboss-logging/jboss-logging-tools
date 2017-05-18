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

package org.jboss.logging.processor.generated;

import java.util.Collection;
import java.util.function.Supplier;

import org.jboss.logging.Messages;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.ConstructType;
import org.jboss.logging.annotations.Field;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.Message.Format;
import org.jboss.logging.annotations.MessageBundle;
import org.jboss.logging.annotations.Param;
import org.jboss.logging.annotations.Property;
import org.jboss.logging.annotations.Suppressed;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@MessageBundle(projectCode = "MSG")
public interface ValidMessages {
    final String TEST_MSG = "Test%n";

    final ValidMessages MESSAGES = Messages.getBundle(ValidMessages.class);

    @Message(value = TEST_MSG)
    String testWithNewLine();

    @Message(format = Format.NO_FORMAT, value = TEST_MSG)
    String noFormat();

    @Message(format = Format.NO_FORMAT, value = TEST_MSG)
    RuntimeException noFormatException(@Cause Throwable cause);

    @Message(TEST_MSG)
    CustomException fieldMessage(@Field(name = "value") int value);

    @Message(TEST_MSG)
    CustomException paramMessage(@Param int value);

    @Message(TEST_MSG)
    CustomException propertyMessage(@Property int value);

    @Message(TEST_MSG)
    LoggingException loggingException(@Cause Exception e);

    @Message(TEST_MSG)
    StringOnlyException stringOnlyException(@Cause Exception e);

    @ConstructType(IllegalArgumentException.class)
    @Message("Invalid user id or password")
    RuntimeException invalidCredentials();

    String SUPPRESSED_ERROR = "Single error occurred";
    @Message(SUPPRESSED_ERROR)
    RuntimeException suppressedError(@Suppressed Throwable t);

    String SUPPRESSED_ERRORS = "Two errors occurred";
    @Message(SUPPRESSED_ERRORS)
    RuntimeException suppressedErrors(@Suppressed Throwable error1, @Suppressed Throwable error2);

    String MULTIPLE_ERRORS = "Multiple errors occurred";
    @Message(MULTIPLE_ERRORS)
    RuntimeException multipleErrors(@Suppressed Throwable... errors);

    String MULTIPLE_ERRORS_COLLECTION = "Multiple errors occurred";
    @Message(MULTIPLE_ERRORS_COLLECTION)
    RuntimeException multipleErrorsCollection(@Suppressed Collection<? extends Throwable> errors);

    String TEST_MESSAGE_FORMAT = "A two argument message format test. Argument 1 is {0} argument 2 is {1}.";
    @Message(format = Format.MESSAGE_FORMAT, value = TEST_MESSAGE_FORMAT)
    String testMessageFormat(final String arg1, final String arg2);

    @Message(format = Format.MESSAGE_FORMAT, value = TEST_MESSAGE_FORMAT)
    RuntimeException testMessageFormatException(final String arg1, final String arg2);

    @Message(value = TEST_MSG)
    Supplier<RuntimeException> testSupplierRuntimeException();

    @Message(TEST_MSG)
    Supplier<CustomException> fieldMessageSupplier(@Field(name = "value") int value);

    @Message(TEST_MSG)
    Supplier<CustomException> propertyMessageSupplier(@Property int value);

    @ConstructType(IllegalArgumentException.class)
    @Message("Invalid user id or password")
    Supplier<RuntimeException> invalidCredentialsSupplier();

    @Message(value = TEST_MSG)
    Supplier<String> testSupplierString();

    class CustomException extends RuntimeException {
        public int value;

        public CustomException() {
        }

        public CustomException(final int value, final String msg) {
            super(msg);
            this.value = value;
        }

        public CustomException(final String msg) {
            super(msg);
        }

        public CustomException(final Throwable t) {
            super(t);
        }

        public CustomException(final String msg, final Throwable t) {
            super(msg, t);
        }

        public void setValue(final int value) {
            this.value = value;
        }
    }

    class LoggingException extends RuntimeException {

        public LoggingException(final Exception e) {
            super(e);
        }

        public LoggingException(final Exception e, final String msg) {
            super(msg, e);
        }
    }

    class StringOnlyException extends RuntimeException {
        public StringOnlyException(final String msg) {
            super(msg);
        }
    }
}
