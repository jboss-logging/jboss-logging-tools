/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat, Inc., and individual contributors
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

package org.jboss.logging.processor.generated;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import org.jboss.logging.Logger;
import org.jboss.logging.processor.generated.DefaultLogger.CustomFormatter;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class LoggerVerificationTest extends AbstractLoggerTest {
    private static final String NAME = System.getProperty("user.name");
    private static final String FILE_NAME_FORMAT = "DefaultLogger.i18n%s.properties";

    @AfterMethod
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

        final String[] values = {"A", "B", "C", "D"};
        logger.invalidSelection("G", values);
        logger.invalidSelection("A", "B", "C", "D");

        final Properties properties = findFile(String.format(FILE_NAME_FORMAT, ""));
        Assert.assertEquals(properties.size(), HANDLER.size());
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
        Assert.assertEquals(properties.size(), HANDLER.size());
        compare("hello", properties, NAME);
        compare("howAreYou", properties, NAME);
    }

    @Test
    public void frenchTest() throws Exception {
        DefaultLogger logger = getLogger(Locale.FRENCH);
        logger.hello(NAME);
        logger.howAreYou(NAME);
        final Properties properties = findFile(String.format(FILE_NAME_FORMAT, "_fr"));
        Assert.assertEquals(properties.size(), HANDLER.size());
        compare("hello", properties, NAME);
        compare("howAreYou", properties, NAME);
    }

    @Test
    public void spanishTest() throws Exception {
        DefaultLogger logger = getLogger(new Locale("es"));
        logger.hello(NAME);
        logger.howAreYou(NAME);
        final Properties properties = findFile(String.format(FILE_NAME_FORMAT, "_es"));
        Assert.assertEquals(properties.size(), HANDLER.size());
        compare("hello", properties, NAME);
        compare("howAreYou", properties, NAME);
    }

    @Test
    public void japaneseTest() throws Exception {
        DefaultLogger logger = getLogger(new Locale("ja"));
        logger.hello(NAME);
        logger.howAreYou(NAME);
        final Properties properties = findFile(String.format(FILE_NAME_FORMAT, "_ja"));
        Assert.assertEquals(properties.size(), HANDLER.size());
        compare("hello", properties, NAME);
        compare("howAreYou", properties, NAME);
    }

    @Test
    public void testStringFormat() throws Exception {
        final String fileName = "StringFormatLogger.i18n%s.properties";
        final Properties en = findFile(String.format(fileName, ""));
        final Properties es = findFile(String.format(fileName, "_es"));
        final StringFormatLogger logger = Logger.getMessageLogger(StringFormatLogger.class, CATEGORY, new Locale("es"));
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
        return Logger.getMessageLogger(DefaultLogger.class, CATEGORY, locale);
    }

    private void compare(final String key, final Properties properties, final Object... params) throws InterruptedException {
        final String expectedMessage = getFormattedProperty(key, properties, params);
        final String loggedMessage = HANDLER.getMessage().replaceAll(LOGGER_ID_PATTERN, "");
        Assert.assertEquals(loggedMessage, expectedMessage);
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
        final String name = CATEGORY.replace(".", File.separator) + File.separator + fileName;
        final InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
        properties.load(new InputStreamReader(in, "utf-8"));
        return properties;
    }


}
