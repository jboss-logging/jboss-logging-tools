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

package org.jboss.logging.processor.generated.tests;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.function.Supplier;

import org.jboss.logging.processor.generated.MethodMessageConstants;
import org.jboss.logging.processor.generated.MethodMessageConstantsTypeException;
import org.jboss.logging.processor.generated.ValidMessages;
import org.jboss.logging.processor.generated.ValidMessagesCustomException;
import org.jboss.logging.processor.generated.ValidMessagesLoggingException;
import org.jboss.logging.processor.generated.ValidMessagesStringOnlyException;
import org.jboss.logging.processor.generated.ValidMessagesUncheckedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@SuppressWarnings("MagicNumber")
public class MessagesTest {

    @SuppressWarnings("RedundantStringFormatCall")
    private static final String FORMATTED_TEST_MSG = String.format(ValidMessages.TEST_MSG);

    @Test
    public void testFormats() {
        Assertions.assertEquals(FORMATTED_TEST_MSG, ValidMessages.MESSAGES.testWithNewLine());
        Assertions.assertEquals(ValidMessages.TEST_MSG, ValidMessages.MESSAGES.noFormat());
        Assertions.assertEquals(ValidMessages.TEST_MSG,
                ValidMessages.MESSAGES.noFormatException(new IllegalArgumentException()).getLocalizedMessage());

        final int value = 10;
        Assertions.assertEquals(value, ValidMessages.MESSAGES.fieldMessage(value).value);
        Assertions.assertEquals(value, ValidMessages.MESSAGES.paramMessage(value).value);
        Assertions.assertEquals(value, ValidMessages.MESSAGES.propertyMessage(value).value);

        final ValidMessagesStringOnlyException e = ValidMessages.MESSAGES.stringOnlyException(new RuntimeException());
        Assertions.assertEquals(FORMATTED_TEST_MSG, e.getMessage());
        Assertions.assertNotNull(e.getCause());

        Assertions.assertTrue(ValidMessages.MESSAGES.invalidCredentials() instanceof IllegalArgumentException,
                "Incorrect type constructed");

        final String arg1 = "value-1";
        final String arg2 = "value-2";
        final String messageFormatMessage = MessageFormat.format(ValidMessages.TEST_MESSAGE_FORMAT, arg1, arg2);
        Assertions.assertEquals(messageFormatMessage, ValidMessages.MESSAGES.testMessageFormat(arg1, arg2));
        Assertions.assertEquals(messageFormatMessage,
                ValidMessages.MESSAGES.testMessageFormatException(arg1, arg2).getMessage());
    }

    @Test
    public void testCauseInitialized() {
        final IOException exception = new IOException("Write failure");
        final ValidMessagesUncheckedException wrapped = ValidMessages.MESSAGES.wrapped(exception);
        Assertions.assertEquals(FORMATTED_TEST_MSG, wrapped.getMessage());
        Assertions.assertTrue((wrapped.getCause() instanceof IOException),
                "Expected the cause to be an IOException but was " + wrapped.getCause());
    }

    @Test
    public void testSuppressed() {
        final IllegalArgumentException e1 = new IllegalArgumentException("First exception");
        final IllegalStateException e2 = new IllegalStateException("Second exception");
        final RuntimeException e3 = new RuntimeException("Third exception");

        RuntimeException expected = new RuntimeException(ValidMessages.MULTIPLE_ERRORS);
        expected.addSuppressed(e1);
        expected.addSuppressed(e2);
        expected.addSuppressed(e3);

        RuntimeException actual = ValidMessages.MESSAGES.multipleErrors(e1, e2, e3);
        compare(expected.getSuppressed(), actual.getSuppressed());

        actual = ValidMessages.MESSAGES.multipleErrorsCollection(Arrays.asList(e1, e2, e3));
        compare(expected.getSuppressed(), actual.getSuppressed());

        expected = new RuntimeException(ValidMessages.SUPPRESSED_ERROR);
        expected.addSuppressed(e1);

        actual = ValidMessages.MESSAGES.suppressedError(e1);
        compare(expected.getSuppressed(), actual.getSuppressed());

        expected = new RuntimeException(ValidMessages.SUPPRESSED_ERRORS);
        expected.addSuppressed(e1);
        expected.addSuppressed(e2);

        actual = ValidMessages.MESSAGES.suppressedErrors(e1, e2);
        compare(expected.getSuppressed(), actual.getSuppressed());

    }

    @Test
    public void testPropertyConstants() {
        Assertions.assertTrue(MethodMessageConstants.MESSAGES.booleanProperty().value);
        Assertions.assertEquals("x".getBytes()[0], MethodMessageConstants.MESSAGES.byteProperty().value);
        Assertions.assertEquals(MethodMessageConstants.testChar, MethodMessageConstants.MESSAGES.charProperty().value);
        Assertions.assertEquals(MethodMessageConstants.ValueType.class, MethodMessageConstants.MESSAGES.classProperty().value);
        Assertions.assertEquals(Double.MAX_VALUE, MethodMessageConstants.MESSAGES.douleProperty().value, 0);
        Assertions.assertEquals(Float.MAX_VALUE, MethodMessageConstants.MESSAGES.floatProperty().value, 0);
        Assertions.assertEquals(Integer.MAX_VALUE, MethodMessageConstants.MESSAGES.intProperty().value);
        Assertions.assertEquals(Long.MAX_VALUE, MethodMessageConstants.MESSAGES.longProperty().value);
        Assertions.assertEquals(Short.MAX_VALUE, MethodMessageConstants.MESSAGES.shortProperty().value);
        Assertions.assertEquals(MethodMessageConstants.stringTest, MethodMessageConstants.MESSAGES.stringProperty().value);
        MethodMessageConstantsTypeException exception = MethodMessageConstants.MESSAGES.multiProperty();
        Assertions.assertEquals(String.class, exception.type);
        Assertions.assertEquals(MethodMessageConstants.stringTest, exception.value);
        exception = MethodMessageConstants.MESSAGES.repeatableProperty();
        Assertions.assertEquals(String.class, exception.type);
        Assertions.assertEquals(MethodMessageConstants.stringTest, exception.value);
    }

    @Test
    public void testFieldConstants() {
        Assertions.assertTrue(MethodMessageConstants.MESSAGES.booleanField().value);
        Assertions.assertEquals("x".getBytes()[0], MethodMessageConstants.MESSAGES.byteField().value);
        Assertions.assertEquals(MethodMessageConstants.testChar, MethodMessageConstants.MESSAGES.charField().value);
        Assertions.assertEquals(MethodMessageConstants.ValueType.class, MethodMessageConstants.MESSAGES.classField().value);
        Assertions.assertEquals(Double.MAX_VALUE, MethodMessageConstants.MESSAGES.douleField().value, 0);
        Assertions.assertEquals(Float.MAX_VALUE, MethodMessageConstants.MESSAGES.floatField().value, 0);
        Assertions.assertEquals(Integer.MAX_VALUE, MethodMessageConstants.MESSAGES.intField().value);
        Assertions.assertEquals(Long.MAX_VALUE, MethodMessageConstants.MESSAGES.longField().value);
        Assertions.assertEquals(Short.MAX_VALUE, MethodMessageConstants.MESSAGES.shortField().value);
        Assertions.assertEquals(MethodMessageConstants.stringTest, MethodMessageConstants.MESSAGES.stringField().value);
        MethodMessageConstantsTypeException exception = MethodMessageConstants.MESSAGES.multiField();
        Assertions.assertEquals(String.class, exception.type);
        Assertions.assertEquals(MethodMessageConstants.stringTest, exception.value);
        exception = MethodMessageConstants.MESSAGES.repeatableField();
        Assertions.assertEquals(String.class, exception.type);
        Assertions.assertEquals(MethodMessageConstants.stringTest, exception.value);
    }

    @Test
    public void testSupplierReturnType() {
        Supplier<RuntimeException> runtimeExceptionSupplier = ValidMessages.MESSAGES.testSupplierRuntimeException();
        Assertions.assertNotNull(runtimeExceptionSupplier);
        RuntimeException runtimeException = runtimeExceptionSupplier.get();
        Assertions.assertEquals(FORMATTED_TEST_MSG, runtimeException.getMessage());
        Assertions.assertEquals(RuntimeException.class, runtimeException.getClass());

        Assertions.assertEquals(ValidMessages.MESSAGES.testSupplierString().get(), FORMATTED_TEST_MSG);

        runtimeExceptionSupplier = ValidMessages.MESSAGES.invalidCredentialsSupplier();
        Assertions.assertNotNull(runtimeExceptionSupplier);
        runtimeException = runtimeExceptionSupplier.get();
        Assertions.assertEquals(IllegalArgumentException.class, runtimeException.getClass());

        // Test suppliers with fields/properties
        int value = 5;
        Supplier<ValidMessagesCustomException> customExceptionSupplier = ValidMessages.MESSAGES.fieldMessageSupplier(value);
        Assertions.assertNotNull(customExceptionSupplier);
        ValidMessagesCustomException customException = customExceptionSupplier.get();
        Assertions.assertEquals(FORMATTED_TEST_MSG, customException.getMessage());
        Assertions.assertEquals(ValidMessagesCustomException.class, customException.getClass());
        Assertions.assertEquals(value, customException.value);

        value = 20;
        customExceptionSupplier = ValidMessages.MESSAGES.propertyMessageSupplier(value);
        Assertions.assertNotNull(customExceptionSupplier);
        customException = customExceptionSupplier.get();
        Assertions.assertEquals(FORMATTED_TEST_MSG, customException.getMessage());
        Assertions.assertEquals(ValidMessagesCustomException.class, customException.getClass());
        Assertions.assertEquals(value, customException.value);

    }

    @Test
    public void testFunctionProducerMessages() {
        RuntimeException runtimeException = ValidMessages.MESSAGES.operationFailed(IllegalArgumentException::new, "start");
        Assertions.assertEquals(IllegalArgumentException.class, runtimeException.getClass());
        Assertions.assertEquals(String.format(ValidMessages.TEST_OP_FAILED_MSG, "start"), runtimeException.getMessage());

        IOException ioException = ValidMessages.MESSAGES.operationFailed(IOException::new, "query");
        Assertions.assertEquals(String.format(ValidMessages.TEST_OP_FAILED_MSG, "query"), ioException.getMessage());

        final Supplier<IllegalStateException> supplier = ValidMessages.MESSAGES.supplierFunction(IllegalStateException::new);
        runtimeException = supplier.get();
        Assertions.assertEquals(IllegalStateException.class, runtimeException.getClass());
        Assertions.assertEquals(FORMATTED_TEST_MSG, runtimeException.getMessage());

        // Test functions with fields/properties
        int value = 5;
        ValidMessagesCustomException customException = ValidMessages.MESSAGES
                .fieldMessageFunction(ValidMessagesCustomException::new, value);
        Assertions.assertEquals(FORMATTED_TEST_MSG, customException.getMessage());
        Assertions.assertEquals(ValidMessagesCustomException.class, customException.getClass());
        Assertions.assertEquals(customException.value, value);

        value = 20;
        customException = ValidMessages.MESSAGES.propertyMessageFunction(ValidMessagesCustomException::new, value);
        Assertions.assertEquals(FORMATTED_TEST_MSG, customException.getMessage());
        Assertions.assertEquals(ValidMessagesCustomException.class, customException.getClass());
        Assertions.assertEquals(value, customException.value);
    }

    @Test
    public void testBiFunctionProducerMessages() {
        final RuntimeException cause = new RuntimeException("This is the cause");
        RuntimeException runtimeException = ValidMessages.MESSAGES.operationFailed(IllegalArgumentException::new, cause,
                "start");
        Assertions.assertEquals(IllegalArgumentException.class, runtimeException.getClass());
        Assertions.assertEquals(String.format(ValidMessages.TEST_OP_FAILED_MSG, "start"), runtimeException.getMessage());
        Assertions.assertEquals(cause, runtimeException.getCause());

        runtimeException = ValidMessages.MESSAGES.throwableStringBiFunction(ValidMessagesLoggingException::new, cause);
        Assertions.assertEquals(ValidMessagesLoggingException.class, runtimeException.getClass());
        Assertions.assertEquals(cause, runtimeException.getCause());

        final Supplier<RuntimeException> supplier = ValidMessages.MESSAGES
                .throwableStringBiFunctionSupplier(IllegalArgumentException::new, cause);
        runtimeException = supplier.get();
        Assertions.assertEquals(IllegalArgumentException.class, runtimeException.getClass());
        Assertions.assertEquals(FORMATTED_TEST_MSG, runtimeException.getMessage());
        Assertions.assertEquals(cause, runtimeException.getCause());

    }

    @Test
    public void testTransformException() {
        final SocketAddress address = new InetSocketAddress("localhost", 9990);
        IOException cause = new BindException("Address already in use");
        IOException e = ValidMessages.MESSAGES.bindFailed(address, cause);
        Assertions.assertTrue(e instanceof BindException);
        Assertions.assertEquals(String.format("Binding to %s failed: %s", address, cause.getLocalizedMessage()),
                e.getMessage());
        Assertions.assertArrayEquals(cause.getStackTrace(), e.getStackTrace());

        cause = new SocketException("Address already in use");
        e = ValidMessages.MESSAGES.bindFailed(address, cause);
        Assertions.assertTrue(e instanceof SocketException);
        Assertions.assertEquals(String.format("Binding to %s failed: %s", address, cause.getLocalizedMessage()),
                e.getMessage());
        Assertions.assertArrayEquals(cause.getStackTrace(), e.getStackTrace());
    }

    @Test
    public void testTransformExceptionNewStackTrace() {
        final SocketAddress address = new InetSocketAddress("localhost", 9990);
        final IOException cause = new BindException("Address already in use");
        final IOException e = ValidMessages.MESSAGES.bindFailedNewStackTrace(address, cause);
        Assertions.assertEquals(IOException.class, e.getClass());
        Assertions.assertEquals(String.format("Binding to %s failed: %s", address, cause.getLocalizedMessage()),
                e.getMessage());
        Assertions.assertFalse(Arrays.equals(cause.getStackTrace(), e.getStackTrace()), "The stack traces should not match.");
        Assertions.assertEquals(cause, e.getCause());
    }

    @Test
    public void testTransformSpecialException() {
        final IOException cause = new BindException("Address already in use");
        final UncheckedIOException e = ValidMessages.MESSAGES.uncheckedIO(cause);
        Assertions.assertEquals(String.format("Unchecked IO: %s", cause.getLocalizedMessage()), e.getMessage());
        Assertions.assertEquals(cause, e.getCause());
    }

    private <T> void compare(final T[] a1, final T[] a2) {
        Assertions.assertTrue(equalsIgnoreOrder(a1, a2),
                String.format("Expected: %s%n Actual: %s", Arrays.toString(a1), Arrays.toString(a2)));
    }

    private <T> boolean equalsIgnoreOrder(final T[] a1, final T[] a2) {
        if (a1.length != a2.length) {
            return false;
        }
        final T[] ca2 = Arrays.copyOf(a2, a2.length);
        boolean found = false;
        for (T t : a1) {
            final int i = search(ca2, t);
            if (i >= 0) {
                found = true;
                ca2[i] = null;
            } else {
                found = false;
            }
            if (!found) {
                break;
            }
        }
        return found;
    }

    private <T> int search(final T[] a, final T key) {
        for (int i = 0; i < a.length; i++) {
            if (key.equals(a[i])) {
                return i;
            }
        }
        return -1;
    }
}
