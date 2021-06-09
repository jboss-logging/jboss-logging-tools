/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2021, Red Hat, Inc., and individual contributors
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

package org.jboss.logging.processor.generated.tests;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.function.Supplier;

import org.jboss.logging.processor.generated.MethodMessageConstants;
import org.jboss.logging.processor.generated.ValidMessages;
import org.jboss.logging.processor.generated.ValidMessages.CustomException;
import org.jboss.logging.processor.generated.ValidMessages.LoggingException;
import org.jboss.logging.processor.generated.ValidMessages.StringOnlyException;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@SuppressWarnings("MagicNumber")
public class MessagesTest {

    @SuppressWarnings("RedundantStringFormatCall")
    private static final String FORMATTED_TEST_MSG = String.format(ValidMessages.TEST_MSG);

    @Test
    public void testFormats() {
        Assert.assertEquals(FORMATTED_TEST_MSG, ValidMessages.MESSAGES.testWithNewLine());
        Assert.assertEquals(ValidMessages.TEST_MSG, ValidMessages.MESSAGES.noFormat());
        Assert.assertEquals(ValidMessages.TEST_MSG, ValidMessages.MESSAGES.noFormatException(new IllegalArgumentException()).getLocalizedMessage());

        final int value = 10;
        Assert.assertEquals(value, ValidMessages.MESSAGES.fieldMessage(value).value);
        Assert.assertEquals(value, ValidMessages.MESSAGES.paramMessage(value).value);
        Assert.assertEquals(value, ValidMessages.MESSAGES.propertyMessage(value).value);

        final StringOnlyException e = ValidMessages.MESSAGES.stringOnlyException(new RuntimeException());
        Assert.assertEquals(FORMATTED_TEST_MSG, e.getMessage());
        Assert.assertNotNull(e.getCause());

        Assert.assertTrue("Incorrect type constructed", ValidMessages.MESSAGES.invalidCredentials() instanceof IllegalArgumentException);

        final String arg1 = "value-1";
        final String arg2 = "value-2";
        final String messageFormatMessage = MessageFormat.format(ValidMessages.TEST_MESSAGE_FORMAT, arg1, arg2);
        Assert.assertEquals(messageFormatMessage, ValidMessages.MESSAGES.testMessageFormat(arg1, arg2));
        Assert.assertEquals(messageFormatMessage, ValidMessages.MESSAGES.testMessageFormatException(arg1, arg2).getMessage());
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
        Assert.assertTrue(MethodMessageConstants.MESSAGES.booleanProperty().value);
        Assert.assertEquals("x".getBytes()[0], MethodMessageConstants.MESSAGES.byteProperty().value);
        Assert.assertEquals(MethodMessageConstants.testChar, MethodMessageConstants.MESSAGES.charProperty().value);
        Assert.assertEquals(MethodMessageConstants.ValueType.class, MethodMessageConstants.MESSAGES.classProperty().value);
        Assert.assertEquals(Double.MAX_VALUE, MethodMessageConstants.MESSAGES.douleProperty().value, 0);
        Assert.assertEquals(Float.MAX_VALUE, MethodMessageConstants.MESSAGES.floatProperty().value, 0);
        Assert.assertEquals(Integer.MAX_VALUE, MethodMessageConstants.MESSAGES.intProperty().value);
        Assert.assertEquals(Long.MAX_VALUE, MethodMessageConstants.MESSAGES.longProperty().value);
        Assert.assertEquals(Short.MAX_VALUE, MethodMessageConstants.MESSAGES.shortProperty().value);
        Assert.assertEquals(MethodMessageConstants.stringTest, MethodMessageConstants.MESSAGES.stringProperty().value);
        MethodMessageConstants.TypeException exception = MethodMessageConstants.MESSAGES.multiProperty();
        Assert.assertEquals(String.class, exception.type);
        Assert.assertEquals(MethodMessageConstants.stringTest, exception.value);
        exception = MethodMessageConstants.MESSAGES.repeatableProperty();
        Assert.assertEquals(String.class, exception.type);
        Assert.assertEquals(MethodMessageConstants.stringTest, exception.value);
    }

    @Test
    public void testFieldConstants() {
        Assert.assertTrue(MethodMessageConstants.MESSAGES.booleanField().value);
        Assert.assertEquals("x".getBytes()[0], MethodMessageConstants.MESSAGES.byteField().value);
        Assert.assertEquals(MethodMessageConstants.testChar, MethodMessageConstants.MESSAGES.charField().value);
        Assert.assertEquals(MethodMessageConstants.ValueType.class, MethodMessageConstants.MESSAGES.classField().value);
        Assert.assertEquals(Double.MAX_VALUE, MethodMessageConstants.MESSAGES.douleField().value, 0);
        Assert.assertEquals(Float.MAX_VALUE, MethodMessageConstants.MESSAGES.floatField().value, 0);
        Assert.assertEquals(Integer.MAX_VALUE, MethodMessageConstants.MESSAGES.intField().value);
        Assert.assertEquals(Long.MAX_VALUE, MethodMessageConstants.MESSAGES.longField().value);
        Assert.assertEquals(Short.MAX_VALUE, MethodMessageConstants.MESSAGES.shortField().value);
        Assert.assertEquals(MethodMessageConstants.stringTest, MethodMessageConstants.MESSAGES.stringField().value);
        MethodMessageConstants.TypeException exception = MethodMessageConstants.MESSAGES.multiField();
        Assert.assertEquals(String.class, exception.type);
        Assert.assertEquals(MethodMessageConstants.stringTest, exception.value);
        exception = MethodMessageConstants.MESSAGES.repeatableField();
        Assert.assertEquals(String.class, exception.type);
        Assert.assertEquals(MethodMessageConstants.stringTest, exception.value);
    }

    @Test
    public void testSupplierReturnType() {
        Supplier<RuntimeException> runtimeExceptionSupplier = ValidMessages.MESSAGES.testSupplierRuntimeException();
        Assert.assertNotNull(runtimeExceptionSupplier);
        RuntimeException runtimeException = runtimeExceptionSupplier.get();
        Assert.assertEquals(FORMATTED_TEST_MSG, runtimeException.getMessage());
        Assert.assertEquals(RuntimeException.class, runtimeException.getClass());

        Assert.assertEquals(ValidMessages.MESSAGES.testSupplierString().get(), FORMATTED_TEST_MSG);

        runtimeExceptionSupplier = ValidMessages.MESSAGES.invalidCredentialsSupplier();
        Assert.assertNotNull(runtimeExceptionSupplier);
        runtimeException = runtimeExceptionSupplier.get();
        Assert.assertEquals(IllegalArgumentException.class, runtimeException.getClass());

        // Test suppliers with fields/properties
        int value = 5;
        Supplier<CustomException> customExceptionSupplier = ValidMessages.MESSAGES.fieldMessageSupplier(value);
        Assert.assertNotNull(customExceptionSupplier);
        CustomException customException = customExceptionSupplier.get();
        Assert.assertEquals(FORMATTED_TEST_MSG, customException.getMessage());
        Assert.assertEquals(CustomException.class, customException.getClass());
        Assert.assertEquals(value, customException.value);

        value = 20;
        customExceptionSupplier = ValidMessages.MESSAGES.propertyMessageSupplier(value);
        Assert.assertNotNull(customExceptionSupplier);
        customException = customExceptionSupplier.get();
        Assert.assertEquals(FORMATTED_TEST_MSG, customException.getMessage());
        Assert.assertEquals(CustomException.class, customException.getClass());
        Assert.assertEquals(value, customException.value);

    }

    @Test
    public void testFunctionProducerMessages() {
        RuntimeException runtimeException = ValidMessages.MESSAGES.operationFailed(IllegalArgumentException::new, "start");
        Assert.assertEquals(IllegalArgumentException.class, runtimeException.getClass());
        Assert.assertEquals(String.format(ValidMessages.TEST_OP_FAILED_MSG, "start"), runtimeException.getMessage());

        IOException ioException = ValidMessages.MESSAGES.operationFailed(IOException::new, "query");
        Assert.assertEquals(String.format(ValidMessages.TEST_OP_FAILED_MSG, "query"), ioException.getMessage());

        final Supplier<IllegalStateException> supplier = ValidMessages.MESSAGES.supplierFunction(IllegalStateException::new);
        runtimeException = supplier.get();
        Assert.assertEquals(IllegalStateException.class, runtimeException.getClass());
        Assert.assertEquals(FORMATTED_TEST_MSG, runtimeException.getMessage());

        // Test functions with fields/properties
        int value = 5;
        CustomException customException = ValidMessages.MESSAGES.fieldMessageFunction(CustomException::new, value);
        Assert.assertEquals(FORMATTED_TEST_MSG, customException.getMessage());
        Assert.assertEquals(CustomException.class, customException.getClass());
        Assert.assertEquals(customException.value, value);

        value = 20;
        customException = ValidMessages.MESSAGES.propertyMessageFunction(CustomException::new, value);
        Assert.assertEquals(FORMATTED_TEST_MSG, customException.getMessage());
        Assert.assertEquals(CustomException.class, customException.getClass());
        Assert.assertEquals(value, customException.value);
    }

    @Test
    public void testBiFunctionProducerMessages() {
        final RuntimeException cause = new RuntimeException("This is the cause");
        RuntimeException runtimeException = ValidMessages.MESSAGES.operationFailed(IllegalArgumentException::new, cause, "start");
        Assert.assertEquals(IllegalArgumentException.class, runtimeException.getClass());
        Assert.assertEquals(String.format(ValidMessages.TEST_OP_FAILED_MSG, "start"), runtimeException.getMessage());
        Assert.assertEquals(cause, runtimeException.getCause());

        runtimeException = ValidMessages.MESSAGES.throwableStringBiFunction(LoggingException::new, cause);
        Assert.assertEquals(LoggingException.class, runtimeException.getClass());
        Assert.assertEquals(cause, runtimeException.getCause());

        final Supplier<RuntimeException> supplier = ValidMessages.MESSAGES.throwableStringBiFunctionSupplier(IllegalArgumentException::new, cause);
        runtimeException = supplier.get();
        Assert.assertEquals(IllegalArgumentException.class, runtimeException.getClass());
        Assert.assertEquals(FORMATTED_TEST_MSG, runtimeException.getMessage());
        Assert.assertEquals(cause, runtimeException.getCause());

    }

    private <T> void compare(final T[] a1, final T[] a2) {
        Assert.assertTrue(String.format("Expected: %s%n Actual: %s", Arrays.toString(a1), Arrays.toString(a2)), equalsIgnoreOrder(a1, a2));
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
