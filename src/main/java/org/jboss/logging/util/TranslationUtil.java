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
 * Utility class to work with
 * translation filename.
 *
 * @author Kevin Pollet
 */
public final class TranslationUtil {

    /**
     * Private constructor to
     * disable instantiation.
     */
    private TranslationUtil() {
    }
    
    public static String getTranslationFileLocale(final String translationFileName) {
        Pattern pattern = Pattern.compile("[^_]*_([^_.]*)[^.]*.properties");
        Matcher matcher = pattern.matcher(translationFileName);
        boolean found = matcher.find();

        if (found) {
            return matcher.group(1);
        }

        return null;
    }

    public static String getTranslationFileCountry(final String translationFileName) {
        Pattern pattern = Pattern.compile("[^_]*_[^_.]*_([^_.]*)[^.]*.properties");
        Matcher matcher = pattern.matcher(translationFileName);
        boolean found = matcher.find();

        if (found) {
            return matcher.group(1);
        }

        return null;
    }

    public static String getTranslationFileVariant(final String translationFileName) {
        Pattern pattern = Pattern.compile("[^_]*_[^_.]*_[^_.]*_([^_.]*)[^.]*.properties");
        Matcher matcher = pattern.matcher(translationFileName);
        boolean found = matcher.find();

        if (found) {
            return matcher.group(1);
        }

        return null;
    }


    /**
     * Get the class name suffix to be added to the
     * generated class for the given property file name.
     *
     * @param translationFileName the translation file name
     * @return the class name suffix corresponding to the given translation filename
     */
    public static String getTranslationClassNameSuffix(final String translationFileName) {
        Pattern pattern = Pattern.compile("[^_]*((_[^_.]*){1,3}).*");
        Matcher matcher = pattern.matcher(translationFileName);
        boolean found = matcher.find();

        if (!found) {
            throw new IllegalArgumentException("The given filename is not a valid property filename");
        }

        return matcher.group(1);
    }


    public static String getEnclosingTranslationClassName(final String className) {
        int lastUnderScore = className.lastIndexOf("_");
        return lastUnderScore != -1 ? className.substring(0, lastUnderScore) : className;
    }

}
