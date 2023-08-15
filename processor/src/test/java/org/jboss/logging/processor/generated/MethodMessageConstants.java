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

import org.jboss.logging.Messages;
import org.jboss.logging.annotations.Field;
import org.jboss.logging.annotations.Fields;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;
import org.jboss.logging.annotations.Properties;
import org.jboss.logging.annotations.Property;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@MessageBundle(projectCode = "CONSTANTS")
public interface MethodMessageConstants {
    String TEST_MSG = "Test method message constant";
    MethodMessageConstants MESSAGES = Messages.getBundle(MethodMessageConstants.class);

    // Properties

    @Message(TEST_MSG)
    @Property(name = "value", intValue = Integer.MAX_VALUE)
    IntTypeException intProperty();

    @Message(TEST_MSG)
    @Property(name = "value", longValue = Long.MAX_VALUE)
    LongTypeException longProperty();

    @Message(TEST_MSG)
    @Property(name = "value", shortValue = Short.MAX_VALUE)
    ShortTypeException shortProperty();

    @Message(TEST_MSG)
    @Property(name = "value", doubleValue = Double.MAX_VALUE)
    DoubleTypeException douleProperty();

    @Message(TEST_MSG)
    @Property(name = "value", floatValue = Float.MAX_VALUE)
    FloatTypeException floatProperty();

    @Message(TEST_MSG)
    @Property(name = "value", booleanValue = true)
    BooleanTypeException booleanProperty();

    @Message(TEST_MSG)
    @Property(name = "value", charValue = testChar)
    CharTypeException charProperty();

    @Message(TEST_MSG)
    @Property(name = "value", byteValue = (byte) 'x')
    ByteTypeException byteProperty();

    @Message(TEST_MSG)
    @Property(name = "value", classValue = ValueType.class)
    ClassTypeException classProperty();

    @Message(TEST_MSG)
    @Property(name = "value", stringValue = stringTest)
    StringTypeException stringProperty();

    @Message(TEST_MSG)
    @Property(name = "value", stringValue = stringTest)
    @Property(name = "type", classValue = String.class)
    TypeException repeatableProperty();

    @Message(TEST_MSG)
    @Properties({
            @Property(name = "value", stringValue = stringTest),
            @Property(name = "type", classValue = String.class)
    })
    TypeException multiProperty();

    // Fields

    @Message(TEST_MSG)
    @Field(name = "value", intValue = Integer.MAX_VALUE)
    IntTypeException intField();

    @Message(TEST_MSG)
    @Field(name = "value", longValue = Long.MAX_VALUE)
    LongTypeException longField();

    @Message(TEST_MSG)
    @Field(name = "value", shortValue = Short.MAX_VALUE)
    ShortTypeException shortField();

    @Message(TEST_MSG)
    @Field(name = "value", doubleValue = Double.MAX_VALUE)
    DoubleTypeException douleField();

    @Message(TEST_MSG)
    @Field(name = "value", floatValue = Float.MAX_VALUE)
    FloatTypeException floatField();

    @Message(TEST_MSG)
    @Field(name = "value", booleanValue = true)
    BooleanTypeException booleanField();

    char testChar = 'c';

    @Message(TEST_MSG)
    @Field(name = "value", charValue = testChar)
    CharTypeException charField();

    @Message(TEST_MSG)
    @Field(name = "value", byteValue = (byte) 'x')
    ByteTypeException byteField();

    @Message(TEST_MSG)
    @Field(name = "value", classValue = ValueType.class)
    ClassTypeException classField();

    String stringTest = "test";

    @Message(TEST_MSG)
    @Field(name = "value", stringValue = stringTest)
    StringTypeException stringField();

    @Message(TEST_MSG)
    @Field(name = "value", stringValue = stringTest)
    @Field(name = "type", classValue = String.class)
    TypeException repeatableField();

    @Message(TEST_MSG)
    @Fields({
            @Field(name = "value", stringValue = stringTest),
            @Field(name = "type", classValue = String.class)
    })
    TypeException multiField();

    @SuppressWarnings({ "InstanceVariableMayNotBeInitialized", "unused" })
    class TypeException extends RuntimeException {
        public Class<?> type;
        public Object value;

        public TypeException() {
        }

        public TypeException(final String msg) {
            super(msg);
        }

        public TypeException(final Throwable t) {
            super(t);
        }

        public TypeException(final String msg, final Throwable t) {
            super(msg, t);
        }

        public void setValue(final Object value) {
            this.value = value;
        }

        public void setType(final Class<?> type) {
            this.type = type;
        }
    }

    class IntTypeException extends RuntimeException {
        @SuppressWarnings("InstanceVariableMayNotBeInitialized")
        public int value;

        public IntTypeException(final String message) {
            super(message);
        }

        public void setValue(final Integer value) {
            this.value = value;
        }
    }

    class LongTypeException extends RuntimeException {
        @SuppressWarnings("InstanceVariableMayNotBeInitialized")
        public long value;

        public LongTypeException(final String message) {
            super(message);
        }

        public void setValue(final long value) {
            this.value = value;
        }
    }

    class ShortTypeException extends RuntimeException {
        @SuppressWarnings("InstanceVariableMayNotBeInitialized")
        public short value;

        public ShortTypeException(final String message) {
            super(message);
        }

        public void setValue(final short value) {
            this.value = value;
        }
    }

    class FloatTypeException extends RuntimeException {
        @SuppressWarnings("InstanceVariableMayNotBeInitialized")
        public float value;

        public FloatTypeException(final String message) {
            super(message);
        }

        public void setValue(final float value) {
            this.value = value;
        }
    }

    class DoubleTypeException extends RuntimeException {
        @SuppressWarnings("InstanceVariableMayNotBeInitialized")
        public double value;

        public DoubleTypeException(final String message) {
            super(message);
        }

        public void setValue(final double value) {
            this.value = value;
        }
    }

    class BooleanTypeException extends RuntimeException {
        @SuppressWarnings("InstanceVariableMayNotBeInitialized")
        public boolean value;

        public BooleanTypeException(final String message) {
            super(message);
        }

        public void setValue(final boolean value) {
            this.value = value;
        }
    }

    class ByteTypeException extends RuntimeException {
        @SuppressWarnings("InstanceVariableMayNotBeInitialized")
        public byte value;

        public ByteTypeException(final String message) {
            super(message);
        }

        public void setValue(final byte value) {
            this.value = value;
        }
    }

    class CharTypeException extends RuntimeException {
        @SuppressWarnings("InstanceVariableMayNotBeInitialized")
        public char value;

        public CharTypeException(final String message) {
            super(message);
        }

        public void setValue(final char value) {
            this.value = value;
        }
    }

    class ClassTypeException extends RuntimeException {
        @SuppressWarnings("InstanceVariableMayNotBeInitialized")
        public Class<?> value;

        public ClassTypeException(final String message) {
            super(message);
        }

        public void setValue(final Class<?> value) {
            this.value = value;
        }
    }

    class StringTypeException extends RuntimeException {
        @SuppressWarnings("InstanceVariableMayNotBeInitialized")
        public String value;

        public StringTypeException(final String message) {
            super(message);
        }

        public void setValue(final String value) {
            this.value = value;
        }
    }

    class ValueType {
    }
}
