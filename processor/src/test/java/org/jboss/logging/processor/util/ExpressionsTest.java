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

package org.jboss.logging.processor.util;

import java.io.IOException;
import java.util.Properties;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class ExpressionsTest {

    private static final Properties PROPERTIES = new Properties();

    @BeforeAll
    public static void configureProperties() throws IOException {
        PROPERTIES.load(ExpressionsTest.class.getResourceAsStream("/expression.properties"));
    }

    @Test
    public void testEnvironmentVariables() {
        Assertions.assertEquals("envValue", Expressions.resolve(PROPERTIES, "${env.JBOSS_LOGGING_TEST_VAR}"));
        Assertions.assertEquals("defaultValue", Expressions.resolve(PROPERTIES, "${env.JBOSS_LOGGING_TEST_INVALID:defaultValue}"));
    }

    @Test
    public void testSystemProperties() {
        Assertions.assertEquals(System.getProperty("user.home"), Expressions.resolve(PROPERTIES, "${sys.user.home}"));
        Assertions.assertEquals("sysValue", Expressions.resolve(PROPERTIES, "${sys.test.property}"));
        Assertions.assertEquals("defaultValue", Expressions.resolve(PROPERTIES, "${sys.invalid.property:defaultValue}"));
    }

    @Test
    public void testProperties() {
        Assertions.assertEquals("test property value", Expressions.resolve(PROPERTIES, "${test.property}"));
        Assertions.assertEquals("defaultValue", Expressions.resolve(PROPERTIES, "${invalid.property:defaultValue}"));
    }
}
