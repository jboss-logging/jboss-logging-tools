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
package org.jboss.logging.generator;

import org.jboss.logging.AbstractToolProcessor;
import org.jboss.logging.Message;
import org.jboss.logging.MessageBundle;
import org.jboss.logging.MessageLogger;
import org.jboss.logging.model.ClassModel;
import org.jboss.logging.model.ImplementationType;
import org.jboss.logging.model.MessageBundleTranslator;
import org.jboss.logging.model.MessageLoggerTranslator;
import org.jboss.logging.util.ElementUtil;
import org.jboss.logging.util.TransformationUtil;
import org.jboss.logging.util.TranslationUtil;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * The translation class generator.
 * <p>
 * The aim of this generator is to generate
 * the classes corresponding to translation
 * files of a MessageLogger or MessageBundle.
 * </p>
 *
 * @author Kevin Pollet
 */
//TODO generate empty translation
//TODO support inner class
@SupportedOptions("translation.files.path")
public final class TranslationClassGenerator extends AbstractToolProcessor {

    private static final String SOURCE_FILE_EXTENSION = ".java";

    private static final String TRANSLATION_FILES_PATH = "translation.files.path";

    /**
     * The properties file pattern. The property file must
     * match the given pattern <em>org.pkgname.InterfaceName.i18n_locale.properties</em> where locale is :
     * <ul>
     * <li>xx - where xx is the language like (e.g. en)</li>
     * <li>xx_YY - where xx is the language and YY is the country like (e.g. en_US)</li>
     * <li>xx_YY_ZZ - where xx is the language, YY is the country and ZZ is the variant like (e.g. en_US_POSIX)</li>
     * </ul>
     */
    private static final String TRANSLATION_FILE_EXTENSION_PATTERN = ".i18n_[a-z]*(_[A-Z]*){0,2}\\.properties";

    private final String translationFilesFolder;

    /**
     * Construct an instance of the Translation
     * Class Generator.
     * 
     * @param processingEnv the processing environment
     */
    public TranslationClassGenerator(final ProcessingEnvironment processingEnv) {
        super(processingEnv);

        Map<String, String> options = processingEnv.getOptions();
        this.translationFilesFolder = options.get(TRANSLATION_FILES_PATH);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processTypeElement(final TypeElement annotation, final TypeElement element,
            final Collection<ExecutableElement> methods) {
        ImplementationType type;

        if (element.getAnnotation(MessageBundle.class) != null) {
            type = ImplementationType.BUNDLE;
        } else {
            type = ImplementationType.LOGGER;
        }

        PackageElement packageElement = elementUtils().getPackageOf(element);
        String packageName = packageElement.getQualifiedName().toString();
        String interfaceName = element.getSimpleName().toString();
        String primaryClassName = TransformationUtil.toQualifiedClassName(
                packageName, interfaceName);
        primaryClassName = primaryClassName.concat(type.toString());

        Map<String, String> elementTranslations = ElementUtil.getAllMessageMethods(methods);

        try {

            String packagePath;

            //User defined
            if (translationFilesFolder != null) {

                packagePath = translationFilesFolder + packageName.replaceAll(
                        "\\.", System.getProperty("file.separator"));

                //By default use the class output folder
            } else {
                FileObject fObj = this.filer().getResource(
                        StandardLocation.CLASS_OUTPUT, packageName,
                        interfaceName);
                packagePath = fObj.toUri().getPath().replaceAll(Pattern.quote(
                        interfaceName), "");
            }

            File dir = new File(packagePath);
            File[] files = dir.listFiles(
                    new TranslationFileFilter(interfaceName));

            if (files != null) {
                for (File file : files) {
                    String classNameSuffix = TranslationUtil.
                            getTranslationClassNameSuffix(file.getName());
                    String qualifiedClassName = primaryClassName.concat(
                            classNameSuffix);

                    //Generate source file
                    Map<String, String> translations = this.
                            validateTranslationMessages(elementTranslations,
                            file);
                    this.generateSourceFile(primaryClassName, qualifiedClassName,
                            translations);
                }
            }

        } catch (IOException e) {
            this.logger().error("Cannot read %s package files", packageName);
        }

    }

    /**
     * Generate a class for the given translation file.
     *
     * @param primaryClassName   the qualified super class name
     * @param generatedClassName the qualified class name
     * @param translations    the translations message
     */
    private void generateSourceFile(final String primaryClassName,
            final String generatedClassName,
            final Map<String, String> translations) {

        this.logger().note("Generating %s translation class", generatedClassName);

        try {

            //Generate super class if needed
            String superClassName = TranslationUtil.
                    getEnclosingTranslationClassName(generatedClassName);
            String packageName = TransformationUtil.toPackage(superClassName);
            String simpleClassName = TransformationUtil.toSimpleClassName(
                    superClassName);

            if (!superClassName.equals(primaryClassName)) {
                FileObject fObject = this.filer().getResource(
                        StandardLocation.SOURCE_OUTPUT, packageName,
                        simpleClassName);
                String pathName = fObject.toUri().getPath().replaceAll(Pattern.
                        quote(simpleClassName), "");
                File file = new File(pathName, simpleClassName.concat(
                        SOURCE_FILE_EXTENSION));
                if (!file.exists()) {
                    this.generateSourceFile(primaryClassName, superClassName,
                            Collections.EMPTY_MAP);
                }
            }

            //Create source file
            ClassModel classModel;

            if (primaryClassName.contains(ImplementationType.BUNDLE.toString())) {
                classModel = new MessageBundleTranslator(generatedClassName,
                        superClassName, translations);
            } else {
                classModel = new MessageLoggerTranslator(generatedClassName,
                        superClassName, translations);
            }

            classModel.create(this.filer().createSourceFile(classModel.
                    getClassName()));

        } catch (Exception e) {
            this.logger().error("Cannot generate %s source file",
                    generatedClassName);
        }
    }

    /**
     * Returns only the valid translations message corresponding
     * to the declared {@link Message} methods in the
     * {@link MessageBundle} or {@link MessageLogger} interface.
     *
     * @param elementTranslations the declared element translations
     * @param file the translation file
     * @return the valid translations messages
     */
    private Map<String, String> validateTranslationMessages(
            final Map<String, String> elementTranslations, final File file) {
        Map<String, String> validTranslations = new HashMap<String, String>();

        try {

            //Load translations
            Properties translations = new Properties();
            translations.load(new FileInputStream(file));

            for (String key : translations.stringPropertyNames()) {
                if (elementTranslations.containsKey(key)) {
                    validTranslations.put(key, translations.getProperty(key));
                } else {
                    this.logger().warn(
                            "The translation message with key %s have no corresponding method.",
                            key);
                }
            }

        } catch (IOException e) {
            this.logger().error("Cannot read the % translation file", file.
                    getName());
        }

        return validTranslations;
    }

    /**
     * Translation file
     * Filter.
     */
    private class TranslationFileFilter implements FilenameFilter {

        private final String className;

        /**
         * The property file filter.
         *
         * @param className the class that have i18n property file
         */
        public TranslationFileFilter(final String className) {
            this.className = className;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean accept(final File dir, final String name) {
            return name.matches(
                    Pattern.quote(className) + TRANSLATION_FILE_EXTENSION_PATTERN);
        }
    }
}
