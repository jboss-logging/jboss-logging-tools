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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.logging.Logger;
import org.jboss.logging.processor.generated.LogOnceLogger;
import org.jboss.logging.processor.generated.TestConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LogOnceTest extends AbstractLoggerTest {

    @AfterEach
    public void clearHandler() {
        HANDLER.close();
    }

    @Test
    @Order(1)
    public void logOnce() throws Exception {
        LogOnceLogger.LOGGER.deprecated("test.property");
        LogOnceLogger.LOGGER.deprecated("test.property");
        Assertions.assertEquals(1, HANDLER.size(), "Only one message should have been logged");

        LogOnceLogger.LOGGER.deprecated("test.property", "new.test.property");
        Assertions.assertEquals(1, HANDLER.size(), "Only one message should have been logged");

        final Method method = LogOnceTest.class.getMethod("logOnce");
        LogOnceLogger.LOGGER.deprecated(method);
        LogOnceLogger.LOGGER.deprecated(method);
        Assertions.assertEquals(3, HANDLER.size(), "The message should have been logged three times");
    }

    @Test
    @Order(2)
    public void newLogger() {
        final LogOnceLogger logger = Logger.getMessageLogger(LogOnceLogger.class, TestConstants.CATEGORY);
        logger.deprecated("test.property");
        Assertions.assertEquals(0, HANDLER.size(), "No messages should have been logged");

        logger.deprecated("test.property", "new.test.property");
        Assertions.assertEquals(0, HANDLER.size(), "No messages should have been logged");

    }

    @Test
    @Order(3)
    public void transformTests() {
        final List<String> listCache = Arrays.asList("item1", "item2", "item3");
        LogOnceLogger.LOGGER.cacheSizeChanged(listCache);
        LogOnceLogger.LOGGER.cacheSizeChanged(listCache);
        Assertions.assertEquals(1, HANDLER.size(), "Only one message should have been logged");

        final Map<String, Object> mapCache = new HashMap<>();
        for (int i = 0; i < 5; i++) {
            mapCache.put("item" + i, "value " + i);
        }
        LogOnceLogger.LOGGER.cacheSizeChanged(mapCache);
        Assertions.assertEquals(1, HANDLER.size(), "Only one message should have been logged");

        LogOnceLogger.LOGGER.cacheSizeChanged("item1", "item2", "item3", "item4");
        LogOnceLogger.LOGGER.cacheSizeChanged("item1", "item2", "item3", "item4", "item5", "item6");
        Assertions.assertEquals(3, HANDLER.size(), "The message should have been logged twice");
    }
}
