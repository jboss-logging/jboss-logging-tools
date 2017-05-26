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

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.jboss.logging.processor.generated.ValidMessages.CustomException;
import org.jboss.logging.processor.generated.ValidMessages.LoggingException;
import org.jboss.logging.processor.generated.ValidMessages.StringOnlyException;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class MessagesTest {

    private static final String FORMATTED_TEST_MSG = String.format(ValidMessages.TEST_MSG);

    @Test
    public void testFormats() {
        Assert.assertEquals(ValidMessages.MESSAGES.testWithNewLine(), FORMATTED_TEST_MSG);
        Assert.assertEquals(ValidMessages.MESSAGES.noFormat(), ValidMessages.TEST_MSG);
        Assert.assertEquals(ValidMessages.MESSAGES.noFormatException(new IllegalArgumentException()).getLocalizedMessage(), ValidMessages.TEST_MSG);

        final int value = 10;
        Assert.assertEquals(ValidMessages.MESSAGES.fieldMessage(value).value, value);
        Assert.assertEquals(ValidMessages.MESSAGES.paramMessage(value).value, value);
        Assert.assertEquals(ValidMessages.MESSAGES.propertyMessage(value).value, value);

        final StringOnlyException e = ValidMessages.MESSAGES.stringOnlyException(new RuntimeException());
        Assert.assertEquals(e.getMessage(), FORMATTED_TEST_MSG);
        Assert.assertNotNull(e.getCause());

        Assert.assertTrue(ValidMessages.MESSAGES.invalidCredentials() instanceof IllegalArgumentException, "Incorrect type constructed");

        final String arg1 = "value-1";
        final String arg2 = "value-2";
        final String messageFormatMessage = MessageFormat.format(ValidMessages.TEST_MESSAGE_FORMAT, arg1, arg2);
        Assert.assertEquals(ValidMessages.MESSAGES.testMessageFormat(arg1, arg2), messageFormatMessage);
        Assert.assertEquals(ValidMessages.MESSAGES.testMessageFormatException(arg1, arg2).getMessage(), messageFormatMessage);
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
        Assert.assertEquals(MethodMessageConstants.MESSAGES.booleanProperty().value, true);
        Assert.assertEquals(MethodMessageConstants.MESSAGES.byteProperty().value, "x".getBytes()[0]);
        Assert.assertEquals(MethodMessageConstants.MESSAGES.charProperty().value, MethodMessageConstants.testChar);
        Assert.assertEquals(MethodMessageConstants.MESSAGES.classProperty().value, MethodMessageConstants.ValueType.class);
        Assert.assertEquals(MethodMessageConstants.MESSAGES.douleProperty().value, Double.MAX_VALUE);
        Assert.assertEquals(MethodMessageConstants.MESSAGES.floatProperty().value, Float.MAX_VALUE);
        Assert.assertEquals(MethodMessageConstants.MESSAGES.intProperty().value, Integer.MAX_VALUE);
        Assert.assertEquals(MethodMessageConstants.MESSAGES.longProperty().value, Long.MAX_VALUE);
        Assert.assertEquals(MethodMessageConstants.MESSAGES.shortProperty().value, Short.MAX_VALUE);
        Assert.assertEquals(MethodMessageConstants.MESSAGES.stringProperty().value, MethodMessageConstants.stringTest);
        MethodMessageConstants.TypeException exception = MethodMessageConstants.MESSAGES.multiProperty();
        Assert.assertEquals(exception.type, String.class);
        Assert.assertEquals(exception.value, MethodMessageConstants.stringTest);
        exception = MethodMessageConstants.MESSAGES.repeatableProperty();
        Assert.assertEquals(exception.type, String.class);
        Assert.assertEquals(exception.value, MethodMessageConstants.stringTest);
    }

    @Test
    public void testFieldConstants() {
        Assert.assertEquals(MethodMessageConstants.MESSAGES.booleanField().value, true);
        Assert.assertEquals(MethodMessageConstants.MESSAGES.byteField().value, "x".getBytes()[0]);
        Assert.assertEquals(MethodMessageConstants.MESSAGES.charField().value, MethodMessageConstants.testChar);
        Assert.assertEquals(MethodMessageConstants.MESSAGES.classField().value, MethodMessageConstants.ValueType.class);
        Assert.assertEquals(MethodMessageConstants.MESSAGES.douleField().value, Double.MAX_VALUE);
        Assert.assertEquals(MethodMessageConstants.MESSAGES.floatField().value, Float.MAX_VALUE);
        Assert.assertEquals(MethodMessageConstants.MESSAGES.intField().value, Integer.MAX_VALUE);
        Assert.assertEquals(MethodMessageConstants.MESSAGES.longField().value, Long.MAX_VALUE);
        Assert.assertEquals(MethodMessageConstants.MESSAGES.shortField().value, Short.MAX_VALUE);
        Assert.assertEquals(MethodMessageConstants.MESSAGES.stringField().value, MethodMessageConstants.stringTest);
        MethodMessageConstants.TypeException exception = MethodMessageConstants.MESSAGES.multiField();
        Assert.assertEquals(exception.type, String.class);
        Assert.assertEquals(exception.value, MethodMessageConstants.stringTest);
        exception = MethodMessageConstants.MESSAGES.repeatableField();
        Assert.assertEquals(exception.type, String.class);
        Assert.assertEquals(exception.value, MethodMessageConstants.stringTest);
    }

    @Test
    public void testSupplierReturnType() throws Exception {
        Supplier<RuntimeException> runtimeExceptionSupplier = ValidMessages.MESSAGES.testSupplierRuntimeException();
        Assert.assertNotNull(runtimeExceptionSupplier);
        RuntimeException runtimeException = runtimeExceptionSupplier.get();
        Assert.assertEquals(runtimeException.getMessage(), FORMATTED_TEST_MSG);
        Assert.assertEquals(runtimeException.getClass(), RuntimeException.class);

        Assert.assertEquals(FORMATTED_TEST_MSG, ValidMessages.MESSAGES.testSupplierString().get());

        runtimeExceptionSupplier = ValidMessages.MESSAGES.invalidCredentialsSupplier();
        Assert.assertNotNull(runtimeExceptionSupplier);
        runtimeException = runtimeExceptionSupplier.get();
        Assert.assertEquals(runtimeException.getClass(), IllegalArgumentException.class);

        // Test suppliers with fields/properties
        int value = 5;
        Supplier<CustomException> customExceptionSupplier = ValidMessages.MESSAGES.fieldMessageSupplier(value);
        Assert.assertNotNull(customExceptionSupplier);
        CustomException customException = customExceptionSupplier.get();
        Assert.assertEquals(customException.getMessage(), FORMATTED_TEST_MSG);
        Assert.assertEquals(customException.getClass(), CustomException.class);
        Assert.assertEquals(customException.value, value);

        value = 20;
        customExceptionSupplier = ValidMessages.MESSAGES.propertyMessageSupplier(value);
        Assert.assertNotNull(customExceptionSupplier);
        customException = customExceptionSupplier.get();
        Assert.assertEquals(customException.getMessage(), FORMATTED_TEST_MSG);
        Assert.assertEquals(customException.getClass(), CustomException.class);
        Assert.assertEquals(customException.value, value);

    }

    @Test
    public void testFunctionProducerMessages() throws Exception {
        RuntimeException runtimeException = ValidMessages.MESSAGES.operationFailed(IllegalArgumentException::new, "start");
        Assert.assertEquals(runtimeException.getClass(), IllegalArgumentException.class);
        Assert.assertEquals(runtimeException.getMessage(), String.format(ValidMessages.TEST_OP_FAILED_MSG, "start"));

        IOException ioException = ValidMessages.MESSAGES.operationFailed(IOException::new, "query");
        Assert.assertEquals(ioException.getMessage(), String.format(ValidMessages.TEST_OP_FAILED_MSG, "query"));

        final Supplier<IllegalStateException> supplier = ValidMessages.MESSAGES.supplierFunction(IllegalStateException::new);
        runtimeException = supplier.get();
        Assert.assertEquals(runtimeException.getClass(), IllegalStateException.class);
        Assert.assertEquals(runtimeException.getMessage(), FORMATTED_TEST_MSG);

        // Test functions with fields/properties
        int value = 5;
        CustomException customException = ValidMessages.MESSAGES.fieldMessageFunction(CustomException::new, value);
        Assert.assertEquals(customException.getMessage(), FORMATTED_TEST_MSG);
        Assert.assertEquals(customException.getClass(), CustomException.class);
        Assert.assertEquals(customException.value, value);

        value = 20;
        customException = ValidMessages.MESSAGES.propertyMessageFunction(CustomException::new, value);
        Assert.assertEquals(customException.getMessage(), FORMATTED_TEST_MSG);
        Assert.assertEquals(customException.getClass(), CustomException.class);
        Assert.assertEquals(customException.value, value);
    }

    @Test
    public void testBiFunctionProducerMessages() throws Exception {
        final RuntimeException cause = new RuntimeException("This is the cause");
        RuntimeException runtimeException = ValidMessages.MESSAGES.operationFailed(IllegalArgumentException::new, cause, "start");
        Assert.assertEquals(runtimeException.getClass(), IllegalArgumentException.class);
        Assert.assertEquals(runtimeException.getMessage(), String.format(ValidMessages.TEST_OP_FAILED_MSG, "start"));
        Assert.assertEquals(runtimeException.getCause(), cause);

        runtimeException = ValidMessages.MESSAGES.throwableStringBiFunction(LoggingException::new, cause);
        Assert.assertEquals(runtimeException.getClass(), LoggingException.class);
        Assert.assertEquals(runtimeException.getCause(), cause);

        final Supplier<RuntimeException> supplier = ValidMessages.MESSAGES.throwableStringBiFunctionSupplier(IllegalArgumentException::new, cause);
        runtimeException = supplier.get();
        Assert.assertEquals(runtimeException.getClass(), IllegalArgumentException.class);
        Assert.assertEquals(runtimeException.getMessage(), FORMATTED_TEST_MSG);
        Assert.assertEquals(runtimeException.getCause(), cause);

    }

    private <T> void compare(final T[] a1, final T[] a2) {
        Assert.assertTrue(equalsIgnoreOrder(a1, a2), String.format("Expected: %s%n Actual: %s", Arrays.toString(a1), Arrays.toString(a2)));
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
