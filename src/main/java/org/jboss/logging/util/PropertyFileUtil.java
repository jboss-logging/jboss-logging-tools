/*
 * JBoss, Home of Professional Open Source Copyright 2010, Red Hat, Inc., and
 * individual contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.jboss.logging.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Kevin Pollet
 */
public final class PropertyFileUtil {

    /**
     * Private constructor to
     * disable instantiation.
     */
    private PropertyFileUtil() {
    }

    /**
     * File qualifier enum.
     */
    public static enum Qualifier {
        LOCALE, COUNTRY, VARIANT;
    }

    /**
     * Get the language qualifier for the given
     * property file.
     *
     * @param propertyFileName the property file name
     * @return the language qualifier or null if none
     */
    public static String getPropertyFileQualifier(final String propertyFileName, final Qualifier qualifier) {
        Pattern pattern = Pattern.compile("[^_]*_([^_.]*)(_([^_.]*)(_([^_.]*))?)?.*");
        Matcher matcher = pattern.matcher(propertyFileName);
        boolean found = matcher.find();

        String fileQualifier = null;

        if (found) {

            switch (qualifier) {

                case LOCALE: {
                    if (matcher.groupCount() >= 1) {
                        fileQualifier = matcher.group(1);
                    }

                }
                break;

                case COUNTRY: {
                    if (matcher.groupCount() >= 3) {
                        fileQualifier = matcher.group(3);
                    }

                }
                break;

                case VARIANT: {
                    if (matcher.groupCount() >= 5) {
                        fileQualifier = matcher.group(5);
                    }

                }
                break;
            }

        }

        return fileQualifier;
    }

    /**
     * Get class name to be generated for the given
     * property file name.
     *
     * @param primaryClassName the primary class name
     * @param propertyFileName the property file name
     * @return the class name corresponding to the given property filename
     */
    public static String getClassNameFor(final String primaryClassName, final String propertyFileName) {
        int firstUnderScore = propertyFileName.indexOf("_");
        int lastDot = propertyFileName.lastIndexOf(".");

        String localeQualifier = propertyFileName.substring(firstUnderScore, lastDot);

        StringBuilder builder = new StringBuilder(primaryClassName);
        builder.append(localeQualifier.replaceAll("_", "\\$"));

        return builder.toString();
    }

}
