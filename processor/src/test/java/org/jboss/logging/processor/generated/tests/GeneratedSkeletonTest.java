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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Properties;

import org.jboss.logging.processor.generated.StringFormatLogger;
import org.jboss.logging.processor.generated.StringFormatMessages;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class GeneratedSkeletonTest {

    private static final String DIR = System.getProperty("test.skeleton.file.path");

    @Test
    public void testLoggerIndexes() throws Exception {
        final Properties properties = resolveProperties(StringFormatLogger.class);
        Assertions.assertEquals("String %1$s integer %2$d", properties.getProperty("stringInt"));
        Assertions.assertEquals("Duke's Birthday: %1$tm %<te,%<tY", properties.getProperty("dukesBirthday"));
        Assertions.assertEquals("The error is %1$s, I repeat %1$s", properties.getProperty("repeat"));
    }

    @Test
    public void testMessageBundleIndexes() throws Exception {
        final Properties properties = resolveProperties(StringFormatMessages.class);
        String foundFormat = properties.getProperty("stringInt");
        test("String %1$s integer %2$d", foundFormat, "value1", 2);
        Assertions.assertEquals(StringFormatMessages.MESSAGES.stringInt("value1", 2), String.format(foundFormat, "value1", 2));

        foundFormat = properties.getProperty("dukesBirthday");
        test("Duke's Birthday: %1$tm %<te,%<tY", foundFormat, new Date());
        final Date date = new Date();
        Assertions.assertEquals(StringFormatMessages.MESSAGES.dukesBirthday(date), String.format(foundFormat, date));

        foundFormat = properties.getProperty("repeat");
        test("The error is %1$s, I repeat %1$s", foundFormat, "value1");
        Assertions.assertEquals(StringFormatMessages.MESSAGES.repeat("value1"), String.format(foundFormat, "value1"));

        foundFormat = properties.getProperty("twoMixedIndexes");
        test("Second %2$s first %1$s", foundFormat, "second", "first");
        Assertions.assertEquals(StringFormatMessages.MESSAGES.twoMixedIndexes("second", "first"), String.format(foundFormat, "second", "first"));

        foundFormat = properties.getProperty("threeMixedIndexes");
        test("Third %3$s first %1$s second %2$s", foundFormat, 3, 1, 2);
        Assertions.assertEquals(StringFormatMessages.MESSAGES.threeMixedIndexes(3, 1, 2), String.format(foundFormat, 3, 1, 2));

        foundFormat = properties.getProperty("fourMixedIndexes");
        test("Third %3$s first %1$s second %2$s repeat second %2$s", foundFormat, 3, 1, 2);
        Assertions.assertEquals(StringFormatMessages.MESSAGES.fourMixedIndexes(3, 1, 2), String.format(foundFormat, 3, 1, 2));
    }

    private void test(final String expectedFormat, final String found, final Object... args) {
        Assertions.assertEquals(expectedFormat, found);
        Assertions.assertEquals(String.format(expectedFormat, args), String.format(found, args));
    }

    private static Properties resolveProperties(final Class<?> type) throws IOException {
        final Path file = findFile(type);
        final Properties properties = new Properties();
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        return properties;
    }
    private static Path findFile(final Class<?> type) {
        final String filePath = type.getName().replace('.', File.separatorChar) + ".i18n.properties";
        Assertions.assertNotNull(DIR, "Could not find the test.skeleton.file.path");
        final Path file = Paths.get(DIR, filePath);
        Assertions.assertTrue(Files.exists(file), String.format("File %s was not found.", file));
        return file;
    }
}
