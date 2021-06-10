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
        ValidLogger.LOGGER.expensiveLogArray(() -> new String[]{"value1", "value2"});
        ValidLogger.LOGGER.expectedValues("value1", "value2", "value3");

        // Check the expected logs
        Assertions.assertEquals("Error: supplier value", HANDLER.getMessage());
        Assertions.assertEquals("Error: [value1, value2]", HANDLER.getMessage());
        Assertions.assertEquals("Expected: [value1, value2, value3]", HANDLER.getMessage());

        Logger.getLogger(TestConstants.CATEGORY).setLevel(Level.INFO);
        ValidLogger.LOGGER.debugValues(() -> {
            Assertions.fail("This should not be invoked.");
           return  "debug values";
        });
        // The message should not exist
        Assertions.assertEquals(0, HANDLER.size(), () -> String.format("More than one message was found: %s", HANDLER));
    }
}
