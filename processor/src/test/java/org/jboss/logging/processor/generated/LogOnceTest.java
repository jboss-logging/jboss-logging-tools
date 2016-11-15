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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.logging.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class LogOnceTest extends AbstractLoggerTest {

    @AfterMethod
    public void clearHandler() {
        HANDLER.close();
    }

    @Test
    public void logOnce() throws Exception {
        LogOnceLogger.LOGGER.deprecated("test.property");
        LogOnceLogger.LOGGER.deprecated("test.property");
        Assert.assertEquals(HANDLER.size(), 1, "Only one message should have been logged");

        LogOnceLogger.LOGGER.deprecated("test.property", "new.test.property");
        Assert.assertEquals(HANDLER.size(), 1, "Only one message should have been logged");

        final Method method = LogOnceTest.class.getMethod("logOnce");
        LogOnceLogger.LOGGER.deprecated(method);
        LogOnceLogger.LOGGER.deprecated(method);
        Assert.assertEquals(HANDLER.size(), 3, "The message should have been logged twice");
    }

    @Test
    public void newLogger() throws Exception {
        final LogOnceLogger logger = Logger.getMessageLogger(LogOnceLogger.class, CATEGORY);
        logger.deprecated("test.property");
        Assert.assertEquals(HANDLER.size(), 0, "No messages should have been logged");

        logger.deprecated("test.property", "new.test.property");
        Assert.assertEquals(HANDLER.size(), 0, "No messages should have been logged");

    }

    @Test
    public void transformTests() throws Exception {
        final List<String> listCache = Arrays.asList("item1", "item2", "item3");
        LogOnceLogger.LOGGER.cacheSizeChanged(listCache);
        LogOnceLogger.LOGGER.cacheSizeChanged(listCache);
        Assert.assertEquals(HANDLER.size(), 1, "Only one message should have been logged");

        final Map<String, Object> mapCache = new HashMap<>();
        for (int i = 0; i < 5; i++) {
            mapCache.put("item" + i, "value " + i);
        }
        LogOnceLogger.LOGGER.cacheSizeChanged(mapCache);
        Assert.assertEquals(HANDLER.size(), 1, "Only one message should have been logged");

        LogOnceLogger.LOGGER.cacheSizeChanged("item1", "item2", "item3", "item4");
        LogOnceLogger.LOGGER.cacheSizeChanged("item1", "item2", "item3", "item4", "item5", "item6");
        Assert.assertEquals(HANDLER.size(), 3, "The message should have been logged twice");
    }
}
