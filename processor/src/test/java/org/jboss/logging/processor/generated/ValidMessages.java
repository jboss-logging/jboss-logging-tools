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

package org.jboss.logging.processor.generated;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.BindException;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jboss.logging.Messages;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.ConstructType;
import org.jboss.logging.annotations.Field;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.Message.Format;
import org.jboss.logging.annotations.MessageBundle;
import org.jboss.logging.annotations.Param;
import org.jboss.logging.annotations.Producer;
import org.jboss.logging.annotations.Property;
import org.jboss.logging.annotations.Signature;
import org.jboss.logging.annotations.Suppressed;
import org.jboss.logging.annotations.TransformException;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@SuppressWarnings("unused")
@MessageBundle(projectCode = "MSG")
public interface ValidMessages {
    String TEST_MSG = "Test%n";

    ValidMessages MESSAGES = Messages.getBundle(ValidMessages.class);

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

    String TEST_OP_FAILED_MSG = "The operation failed due to %s";

    @Message(TEST_OP_FAILED_MSG)
    <T extends Throwable> T operationFailed(@Producer Function<String, T> function, String op);

    <T extends Throwable> T operationFailed(@Producer BiFunction<String, Throwable, T> function, @Cause Throwable cause,
            String op);

    @Message(TEST_MSG)
    <T extends RuntimeException> Supplier<T> supplierFunction(@Producer Function<String, T> function);

    @Message(TEST_MSG)
    <T extends CustomException> T fieldMessageFunction(@Producer Function<String, T> function,
            @Field(name = "value") int value);

    @Message(TEST_MSG)
    <T extends CustomException> T propertyMessageFunction(@Producer Function<String, T> function, @Property int value);

    @Message(TEST_MSG)
    LoggingException throwableStringBiFunction(@Producer BiFunction<Exception, String, LoggingException> function,
            @Cause Exception cause);

    @Message(TEST_MSG)
    <T extends RuntimeException> Supplier<T> throwableStringBiFunctionSupplier(
            @Producer BiFunction<String, Exception, T> function, @Cause Exception cause);

    @Message(TEST_MSG)
    UncheckedException wrapped(@Cause Throwable cause);

    @Message("Binding to %s failed: %s")
    IOException bindFailed(SocketAddress address,
            @TransformException({ BindException.class, SocketException.class }) IOException toCopy);

    @Message("Binding to %s failed: %s")
    IOException bindFailedNewStackTrace(SocketAddress address,
            @Cause @TransformException(copyStackTrace = false) IOException toCopy);

    @Message("Unchecked IO: %s")
    @Signature(value = { String.class, IOException.class }, causeIndex = 1)
    UncheckedIOException uncheckedIO(@Cause @TransformException(copyStackTrace = false) IOException toCopy);

    @SuppressWarnings({ "InstanceVariableMayNotBeInitialized", "unused" })
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

    @SuppressWarnings("unused")
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

    class UncheckedException extends RuntimeException {

        public UncheckedException(final String msg) {
            super(msg);
        }

        public UncheckedException(final Exception e) {
            super(e);
        }
    }
}
