/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2026 Red Hat, Inc., and individual contributors
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

import org.jboss.logging.processor.generated.ThrottledLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * @author Tristan Tarrant
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ThrottledTest extends AbstractLoggerTest {

    @AfterEach
    public void clearHandler() {
        HANDLER.close();
    }

    @Test
    @Order(1)
    public void countThrottled() {
        // With count=5, every 5th invocation should log (0th, 5th, 10th, ...)
        for (int i = 0; i < 10; i++) {
            ThrottledLogger.LOGGER.countThrottled("test");
        }
        Assertions.assertEquals(2, HANDLER.size(), "Should have logged at invocations 0 and 5");
    }

    @Test
    @Order(2)
    public void countThrottledOverload() {
        // Overloaded methods share the same counter.
        // The counter continues from the previous test (static field), so current count is 10.
        // Next log at count 10 (10 % 5 == 0).
        ThrottledLogger.LOGGER.countThrottled("old", "new");
        Assertions.assertEquals(1, HANDLER.size(), "Overloaded method should share counter and log");

        // Advance to 14 (no more logs)
        for (int i = 0; i < 4; i++) {
            ThrottledLogger.LOGGER.countThrottled("test");
        }
        Assertions.assertEquals(1, HANDLER.size(), "No additional logs expected before next threshold");

        // At count 15, should log again
        ThrottledLogger.LOGGER.countThrottled("test");
        Assertions.assertEquals(2, HANDLER.size(), "Should log at count 15");
    }

    @Test
    @Order(3)
    public void timeThrottled() throws Exception {
        // With period=1s, only the first call should log immediately
        ThrottledLogger.LOGGER.timeThrottled("first");
        Assertions.assertEquals(1, HANDLER.size(), "First call should log");

        // Rapid calls should not log
        for (int i = 0; i < 10; i++) {
            ThrottledLogger.LOGGER.timeThrottled("rapid");
        }
        Assertions.assertEquals(1, HANDLER.size(), "Rapid calls within 1s should not log");

        // Wait for the period to elapse
        Thread.sleep(1100);

        ThrottledLogger.LOGGER.timeThrottled("after-wait");
        Assertions.assertEquals(2, HANDLER.size(), "Should log again after period elapses");
    }

    @Test
    @Order(4)
    public void combinedThrottled() throws Exception {
        // With count=3, period=1s: both conditions must be satisfied.
        // First call: count 0 % 3 == 0, and time elapsed (first call always passes) -> logs
        ThrottledLogger.LOGGER.combinedThrottled("first");
        Assertions.assertEquals(1, HANDLER.size(), "First call should log");

        // Calls 1-2: count doesn't match -> no log
        ThrottledLogger.LOGGER.combinedThrottled("second");
        ThrottledLogger.LOGGER.combinedThrottled("third");
        Assertions.assertEquals(1, HANDLER.size(), "Count not met, should not log");

        // Call 3: count 3 % 3 == 0, but within 1s period -> no log
        ThrottledLogger.LOGGER.combinedThrottled("fourth");
        Assertions.assertEquals(1, HANDLER.size(), "Count met but period not elapsed, should not log");

        // Wait for period to elapse
        Thread.sleep(1100);

        // Calls 4-5: count doesn't match -> no log even though period elapsed
        ThrottledLogger.LOGGER.combinedThrottled("fifth");
        ThrottledLogger.LOGGER.combinedThrottled("sixth");
        Assertions.assertEquals(1, HANDLER.size(), "Period elapsed but count not met, should not log");

        // Call 6: count 6 % 3 == 0, and period elapsed -> logs
        ThrottledLogger.LOGGER.combinedThrottled("seventh");
        Assertions.assertEquals(2, HANDLER.size(), "Both count and period met, should log");
    }
}
