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
    MethodMessageConstantsIntTypeException intProperty();

    @Message(TEST_MSG)
    @Property(name = "value", longValue = Long.MAX_VALUE)
    MethodMessageConstantsLongTypeException longProperty();

    @Message(TEST_MSG)
    @Property(name = "value", shortValue = Short.MAX_VALUE)
    MethodMessageConstantsShortTypeException shortProperty();

    @Message(TEST_MSG)
    @Property(name = "value", doubleValue = Double.MAX_VALUE)
    MethodMessageConstantsDoubleTypeException douleProperty();

    @Message(TEST_MSG)
    @Property(name = "value", floatValue = Float.MAX_VALUE)
    MethodMessageConstantsFloatTypeException floatProperty();

    @Message(TEST_MSG)
    @Property(name = "value", booleanValue = true)
    MethodMessageConstantsBooleanTypeException booleanProperty();

    @Message(TEST_MSG)
    @Property(name = "value", charValue = testChar)
    MethodMessageConstantsCharTypeException charProperty();

    @Message(TEST_MSG)
    @Property(name = "value", byteValue = (byte) 'x')
    MethodMessageConstantsByteTypeException byteProperty();

    @Message(TEST_MSG)
    @Property(name = "value", classValue = ValueType.class)
    MethodMessageConstantsClassTypeException classProperty();

    @Message(TEST_MSG)
    @Property(name = "value", stringValue = stringTest)
    MethodMessageConstantsStringTypeException stringProperty();

    @Message(TEST_MSG)
    @Property(name = "value", stringValue = stringTest)
    @Property(name = "type", classValue = String.class)
    MethodMessageConstantsTypeException repeatableProperty();

    @Message(TEST_MSG)
    @Properties({
            @Property(name = "value", stringValue = stringTest),
            @Property(name = "type", classValue = String.class)
    })
    MethodMessageConstantsTypeException multiProperty();

    // Fields

    @Message(TEST_MSG)
    @Field(name = "value", intValue = Integer.MAX_VALUE)
    MethodMessageConstantsIntTypeException intField();

    @Message(TEST_MSG)
    @Field(name = "value", longValue = Long.MAX_VALUE)
    MethodMessageConstantsLongTypeException longField();

    @Message(TEST_MSG)
    @Field(name = "value", shortValue = Short.MAX_VALUE)
    MethodMessageConstantsShortTypeException shortField();

    @Message(TEST_MSG)
    @Field(name = "value", doubleValue = Double.MAX_VALUE)
    MethodMessageConstantsDoubleTypeException douleField();

    @Message(TEST_MSG)
    @Field(name = "value", floatValue = Float.MAX_VALUE)
    MethodMessageConstantsFloatTypeException floatField();

    @Message(TEST_MSG)
    @Field(name = "value", booleanValue = true)
    MethodMessageConstantsBooleanTypeException booleanField();

    char testChar = 'c';

    @Message(TEST_MSG)
    @Field(name = "value", charValue = testChar)
    MethodMessageConstantsCharTypeException charField();

    @Message(TEST_MSG)
    @Field(name = "value", byteValue = (byte) 'x')
    MethodMessageConstantsByteTypeException byteField();

    @Message(TEST_MSG)
    @Field(name = "value", classValue = ValueType.class)
    MethodMessageConstantsClassTypeException classField();

    String stringTest = "test";

    @Message(TEST_MSG)
    @Field(name = "value", stringValue = stringTest)
    MethodMessageConstantsStringTypeException stringField();

    @Message(TEST_MSG)
    @Field(name = "value", stringValue = stringTest)
    @Field(name = "type", classValue = String.class)
    MethodMessageConstantsTypeException repeatableField();

    @Message(TEST_MSG)
    @Fields({
            @Field(name = "value", stringValue = stringTest),
            @Field(name = "type", classValue = String.class)
    })
    MethodMessageConstantsTypeException multiField();

    class ValueType {
    }
}
