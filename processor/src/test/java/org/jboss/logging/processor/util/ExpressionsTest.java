/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat, Inc., and individual contributors
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

package org.jboss.logging.processor.util;

import java.io.IOException;
import java.util.Properties;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class ExpressionsTest {

    private static final Properties PROPERTIES = new Properties();

    @BeforeClass
    public static void configureProperties() throws IOException {
        PROPERTIES.load(ExpressionsTest.class.getResourceAsStream("/expression.properties"));
    }

    @Test
    public void testEnvironmentVariables() {
        Assert.assertEquals("envValue", Expressions.resolve(PROPERTIES, "${env.JBOSS_LOGGING_TEST_VAR}"));
        Assert.assertEquals("defaultValue", Expressions.resolve(PROPERTIES, "${env.JBOSS_LOGGING_TEST_INVALID:defaultValue}"));
    }

    @Test
    public void testSystemProperties() {
        Assert.assertEquals(System.getProperty("user.home"), Expressions.resolve(PROPERTIES, "${sys.user.home}"));
        Assert.assertEquals("sysValue", Expressions.resolve(PROPERTIES, "${sys.test.property}"));
        Assert.assertEquals("defaultValue", Expressions.resolve(PROPERTIES, "${sys.invalid.property:defaultValue}"));
    }

    @Test
    public void testProperties() {
        Assert.assertEquals("test property value", Expressions.resolve(PROPERTIES, "${test.property}"));
        Assert.assertEquals("defaultValue", Expressions.resolve(PROPERTIES, "${invalid.property:defaultValue}"));
    }
}
