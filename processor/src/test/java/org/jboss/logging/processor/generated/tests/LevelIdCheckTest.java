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

import org.jboss.logging.processor.generated.ValidLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class LevelIdCheckTest extends AbstractLoggerTest {

    @AfterEach
    public void clearHandler() {
        HANDLER.close();
    }

    @SuppressWarnings("MagicNumber")
    @Test
    public void inheritedId() throws Exception {
        ValidLogger.LOGGER.processingError();
        ValidLogger.LOGGER.processingError(new IllegalArgumentException());
        ValidLogger.LOGGER.processingError(new IllegalArgumentException(), "generated");
        ValidLogger.LOGGER.processingError(this, "invalid reference");
        Assertions.assertEquals(203, parseLoggerId(HANDLER.getMessage()));
        Assertions.assertEquals(203, parseLoggerId(HANDLER.getMessage()));
        Assertions.assertEquals(203, parseLoggerId(HANDLER.getMessage()));
        Assertions.assertEquals(203, parseLoggerId(HANDLER.getMessage()));
    }

}
