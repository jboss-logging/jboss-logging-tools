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

package org.jboss.logging.processor.generated;

import java.util.concurrent.TimeUnit;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.logging.annotations.Throttled;

/**
 * @author Tristan Tarrant
 */
@MessageLogger(projectCode = TestConstants.PROJECT_CODE)
public interface ThrottledLogger {

    ThrottledLogger LOGGER = Logger.getMessageLogger(ThrottledLogger.class, ThrottledLogger.class.getName());

    @LogMessage(level = Level.WARN)
    @Throttled(count = 5)
    @Message("Count throttled: '%s'")
    void countThrottled(String msg);

    @LogMessage(level = Level.WARN)
    @Throttled(count = 5)
    @Message("Count throttled overload: '%s' -> '%s'")
    void countThrottled(String msg, String replacement);

    @LogMessage(level = Level.WARN)
    @Throttled(period = 1, unit = TimeUnit.SECONDS)
    @Message("Time throttled: '%s'")
    void timeThrottled(String msg);

    @LogMessage(level = Level.WARN)
    @Throttled(count = 3, period = 1, unit = TimeUnit.SECONDS)
    @Message("Combined throttled: '%s'")
    void combinedThrottled(String msg);
}
