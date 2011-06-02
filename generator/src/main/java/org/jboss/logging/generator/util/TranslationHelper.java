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
package org.jboss.logging.generator.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to work with translation filename.
 *
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
public final class TranslationHelper {

    private static final String TRANSLATION_FILE_NAME_EXTENSION = ".properties";

    /**
     * Private constructor to
     * disable instantiation.
     */
    private TranslationHelper() {
    }

    /**
     * Get the class name suffix to be added to the
     * generated class for the given translation file name.
     *
     * @param translationFileName the translation file name
     *
     * @return the class name suffix corresponding to the given translation filename
     * @throws NullPointerException if translationFileName is null or not valid
     */
    public static String getTranslationClassNameSuffix(final String translationFileName) {
        if (translationFileName == null) {
            throw new NullPointerException("The translationFileName parameter cannot be null");
        }

        Pattern pattern = Pattern.compile("[^_]*((_[^_.]*){1,3}).*");
        Matcher matcher = pattern.matcher(translationFileName);
        boolean found = matcher.find();

        if (!found) {
            throw new NullPointerException("The given filename is not a valid property filename");
        }

        return matcher.group(1);
    }

    /**
     * Returns the enclosing translation file name for the given
     * translation file name.
     * <p/>
     * If the given translation file name is InterfaceName.i18n_locale
     * the given translation file name is returned.
     *
     * @param translationFileName the translation file name
     *
     * @return the enclosing file name
     * @throws NullPointerException if translationFileName is null
     */
    public static String getEnclosingTranslationFileName(final String translationFileName) {
        if (translationFileName == null) {
            throw new NullPointerException("The translationClassName parameter cannot be null");
        }

        int lastUnderscore = translationFileName.lastIndexOf('_');

        if (translationFileName.indexOf('_') == lastUnderscore) {
            return translationFileName;
        } else {
            return translationFileName.substring(0, lastUnderscore).concat(TRANSLATION_FILE_NAME_EXTENSION);
        }
    }

    /**
     * Returns the enclosing translation class name for
     * the given translation class name. If the given translation
     * class name is the upper class name then the parameter class
     * name is returned.
     *
     * @param translationClassName the translation class name
     *
     * @return the enclosing class name
     * @throws NullPointerException if translationClassName is null
     */
    public static String getEnclosingTranslationClassName(final String translationClassName) {
        if (translationClassName == null) {
            throw new NullPointerException("The translationClassName parameter cannot be null");
        }

        int lastUnderScore = translationClassName.lastIndexOf("_");
        return lastUnderScore != -1 ? translationClassName.substring(0, lastUnderScore) : translationClassName;
    }
}
