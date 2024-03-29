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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import org.jboss.logging.Logger;
import org.jboss.logging.processor.generated.DefaultLogger;
import org.jboss.logging.processor.generated.DefaultLogger.CustomFormatter;
import org.jboss.logging.processor.generated.StringFormatLogger;
import org.jboss.logging.processor.generated.TestConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class LoggerVerificationTest extends AbstractLoggerTest {
    private static final String NAME = System.getProperty("user.name");
    private static final String FILE_NAME_FORMAT = "DefaultLogger.i18n%s.properties";

    @AfterEach
    public void clearHandler() {
        HANDLER.close();
    }

    @Test
    public void defaultTest() throws Exception {
        final DefaultLogger logger = getLogger(Locale.ROOT);
        logger.hello(NAME);
        logger.howAreYou(NAME);
        logger.noFormat();
        logger.noFormatWithCause(new IllegalArgumentException("No format cause"));
        final String msg = "This is a test message";
        logger.formatWith(msg);

        final String[] values = { "A", "B", "C", "D" };
        logger.invalidSelection("G", values);
        logger.invalidSelection("A", "B", "C", "D");

        final Properties properties = findFile(String.format(FILE_NAME_FORMAT, ""));
        Assertions.assertEquals(HANDLER.size(), properties.size());
        compare("hello", properties, NAME);
        compare("howAreYou", properties, NAME);
        compare("noFormat", properties);
        compare("noFormatWithCause", properties);
        compare("formatWith", properties, new CustomFormatter(msg));
        compare("invalidSelection.2", properties, "G", Arrays.toString(values));
        compare("invalidSelection.1", properties, Arrays.toString(values));
    }

    @Test
    public void germanTest() throws Exception {
        DefaultLogger logger = getLogger(Locale.GERMAN);
        logger.hello(NAME);
        logger.howAreYou(NAME);
        final Properties properties = findFile(String.format(FILE_NAME_FORMAT, "_de"));
        Assertions.assertEquals(HANDLER.size(), properties.size());
        compare("hello", properties, NAME);
        compare("howAreYou", properties, NAME);
    }

    @Test
    public void frenchTest() throws Exception {
        DefaultLogger logger = getLogger(Locale.FRENCH);
        logger.hello(NAME);
        logger.howAreYou(NAME);
        final Properties properties = findFile(String.format(FILE_NAME_FORMAT, "_fr"));
        Assertions.assertEquals(HANDLER.size(), properties.size());
        compare("hello", properties, NAME);
        compare("howAreYou", properties, NAME);
    }

    @Test
    public void spanishTest() throws Exception {
        DefaultLogger logger = getLogger(new Locale("es"));
        logger.hello(NAME);
        logger.howAreYou(NAME);
        final Properties properties = findFile(String.format(FILE_NAME_FORMAT, "_es"));
        Assertions.assertEquals(HANDLER.size(), properties.size());
        compare("hello", properties, NAME);
        compare("howAreYou", properties, NAME);
    }

    @Test
    public void japaneseTest() throws Exception {
        DefaultLogger logger = getLogger(new Locale("ja"));
        logger.hello(NAME);
        logger.howAreYou(NAME);
        final Properties properties = findFile(String.format(FILE_NAME_FORMAT, "_ja"));
        Assertions.assertEquals(HANDLER.size(), properties.size());
        compare("hello", properties, NAME);
        compare("howAreYou", properties, NAME);
    }

    @Test
    public void testStringFormat() throws Exception {
        final String fileName = "StringFormatLogger.i18n%s.properties";
        final Properties en = findFile(String.format(fileName, ""));
        final Properties es = findFile(String.format(fileName, "_es"));
        final StringFormatLogger logger = Logger.getMessageLogger(StringFormatLogger.class, TestConstants.CATEGORY,
                new Locale("es"));
        final Date date = new Date();
        logger.dukesBirthday(date);
        logger.dukesBirthdayFailure(date);
        compare("dukesBirthday", es, date);
        compare("dukesBirthdayFailure", en, date);

        logger.stringInt("string", 1);
        logger.stringIntFailure("string", 1);
        compare("stringInt", es, "string", 1);
        compare("stringIntFailure", en, "string", 1);

        logger.repeat("invalid");
        logger.repeatFailure("invalid");
        compare("repeat", es, "invalid");
        compare("repeatFailure", en, "invalid");
    }

    private static DefaultLogger getLogger(final Locale locale) {
        return Logger.getMessageLogger(DefaultLogger.class, TestConstants.CATEGORY, locale);
    }

    private void compare(final String key, final Properties properties, final Object... params) throws InterruptedException {
        final String expectedMessage = getFormattedProperty(key, properties, params);
        final String loggedMessage = HANDLER.getMessage().replaceAll(LOGGER_ID_PATTERN, "");
        Assertions.assertEquals(expectedMessage, loggedMessage);
    }

    private String getFormattedProperty(final String key, final Properties properties, final Object... params) {
        final String format = properties.getProperty(key);
        if (format == null) {
            return null;
        }
        if (params != null && params.length > 0)
            return String.format(format, params);
        return format;
    }

    private static Properties findFile(final String fileName) throws IOException {
        final Properties properties = new Properties();
        final String name = TestConstants.CATEGORY.replace(".", File.separator) + File.separator + fileName;
        final InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
        Assertions.assertNotNull(in);
        properties.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        return properties;
    }

}
