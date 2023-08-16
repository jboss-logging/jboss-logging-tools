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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.logging.processor.generated.TestConstants;
import org.jboss.logging.processor.generated.ValidLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class LoggerTest extends AbstractLoggerTest {

    private Level currentLevel = Level.ALL;

    @BeforeEach
    public void setup() {
        currentLevel = Logger.getLogger(TestConstants.CATEGORY).getLevel();
    }

    @AfterEach
    public void cleanup() {
        Logger.getLogger(TestConstants.CATEGORY).setLevel(currentLevel);
    }

    @Test
    public void testSupplierLogger() throws Exception {
        ValidLogger.LOGGER.expensiveLog(() -> "supplier value");
        ValidLogger.LOGGER.expensiveLogArray(() -> new String[] { "value1", "value2" });
        ValidLogger.LOGGER.expectedValues("value1", "value2", "value3");

        // Check the expected logs
        Assertions.assertEquals("Error: supplier value", HANDLER.getMessage());
        Assertions.assertEquals("Error: [value1, value2]", HANDLER.getMessage());
        Assertions.assertEquals("Expected: [value1, value2, value3]", HANDLER.getMessage());

        Logger.getLogger(TestConstants.CATEGORY).setLevel(Level.INFO);
        ValidLogger.LOGGER.debugValues(() -> {
            Assertions.fail("This should not be invoked.");
            return "debug values";
        });
        // The message should not exist
        Assertions.assertEquals(0, HANDLER.size(), () -> String.format("More than one message was found: %s", HANDLER));
    }
}
