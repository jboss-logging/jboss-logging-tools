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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.logging.processor.generated.TestConstants;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public abstract class AbstractLoggerTest {

    static final QueuedMessageHandler HANDLER = new QueuedMessageHandler();
    static final String LOGGER_ID_PATTERN = "LOG.*[0-9]:\\s";

    private static final org.jboss.logmanager.Logger LOGGER = org.jboss.logmanager.Logger.getLogger(TestConstants.CATEGORY);

    @BeforeAll
    public static void installHandler() {
        LOGGER.addHandler(HANDLER);
    }

    @AfterAll
    public static void uninstallHandler() {
        LOGGER.removeHandler(HANDLER);
        HANDLER.close();
    }

    protected String parseStringLoggerId(final String message) {
        final Pattern p = Pattern.compile(LOGGER_ID_PATTERN);
        final Matcher m = p.matcher(message);
        if (m.find()) {
            return m.group();
        }
        return null;
    }

    protected int parseLoggerId(final String message) {
        final String stringId = parseStringLoggerId(message);
        if (stringId != null) {
            final String s = message.replaceAll("([a-zA-z]|:.*)", "");
            return Integer.parseInt(s);
        }
        return 0;
    }
}
