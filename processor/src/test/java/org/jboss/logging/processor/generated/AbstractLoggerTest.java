/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public abstract class AbstractLoggerTest {

    static final String PROJECT_CODE = "LOGL";
    static final MessageListHandler HANDLER = new MessageListHandler();
    static final String CATEGORY = AbstractLoggerTest.class.getPackage().getName();
    static final String LOGGER_ID_PATTERN = "LOG.*[0-9]:\\s";

    @BeforeClass
    public static void installHandler() {
        org.jboss.logmanager.Logger.getLogger(CATEGORY).addHandler(HANDLER);
    }

    @AfterClass
    public static void uninstallHandler() {
        HANDLER.close();
        org.jboss.logmanager.Logger.getLogger(CATEGORY).removeHandler(HANDLER);
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
