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

package org.jboss.logging.processor.util;

import java.io.File;
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
     *
     * @throws IllegalArgumentException if translationFileName is null or not valid
     */
    public static String getTranslationClassNameSuffix(final String translationFileName) {
        if (translationFileName == null) {
            throw new IllegalArgumentException("The translationFileName parameter cannot be null");
        }

        Pattern pattern = Pattern.compile("[^_]*((_[^_.]*){1,3}).*");
        Matcher matcher = pattern.matcher(translationFileName);
        boolean found = matcher.find();

        if (!found) {
            throw new IllegalArgumentException("The given filename is not a valid property filename");
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
     * @param translationFile the translation file
     *
     * @return the enclosing file name
     *
     * @throws IllegalArgumentException if translationFileName is null
     */
    public static String getEnclosingTranslationFileName(final File translationFile) {
        if (translationFile == null) {
            throw new IllegalArgumentException("The translationClassName parameter cannot be null");
        }
        final String translationFileName = translationFile.getName();
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
     *
     * @throws IllegalArgumentException if translationClassName is null
     */
    public static String getEnclosingTranslationClassName(final String translationClassName) {
        if (translationClassName == null) {
            throw new IllegalArgumentException("The translationClassName parameter cannot be null");
        }

        int lastUnderScore = translationClassName.lastIndexOf("_");
        return lastUnderScore != -1 ? translationClassName.substring(0, lastUnderScore) : translationClassName;
    }
}
