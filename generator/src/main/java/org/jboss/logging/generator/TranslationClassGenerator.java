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

import org.jboss.logging.generator.model.ClassModel;
import org.jboss.logging.generator.model.ImplementationType;
import org.jboss.logging.generator.model.MessageBundleTranslator;
import org.jboss.logging.generator.model.MessageLoggerTranslator;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import static org.jboss.logging.generator.util.ElementHelper.getPrimaryClassName;
import static org.jboss.logging.generator.util.ElementHelper.getPrimaryClassNamePrefix;
import static org.jboss.logging.generator.util.TransformationHelper.toQualifiedClassName;
import static org.jboss.logging.generator.util.TranslationHelper.getEnclosingTranslationClassName;
import static org.jboss.logging.generator.util.TranslationHelper.getEnclosingTranslationFileName;
import static org.jboss.logging.generator.util.TranslationHelper.getTranslationClassNameSuffix;

/**
 * The translation class generator.
 * <p>
 * The aim of this generator is to generate
 * the classes corresponding to translation
 * files of a MessageLogger or MessageBundle.
 * </p>
 *
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
@SupportedOptions(TranslationClassGenerator.TRANSLATION_FILES_PATH_OPTION)
public final class TranslationClassGenerator extends AbstractTool {

    public static final String TRANSLATION_FILES_PATH_OPTION = "translationFilesPath";

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

    private final String translationFilesPath;


    /**
     * Construct an instance of the Translation
     * Class Generator.
     *
     * @param processingEnv the processing environment
     * @param annotations   the annotation descriptor.
     * @param loggers       the logger descriptor.
     */
    public TranslationClassGenerator(final ProcessingEnvironment processingEnv) {
        super(processingEnv);
        Map<String, String> options = processingEnv.getOptions();
        this.translationFilesPath = options.get(TRANSLATION_FILES_PATH_OPTION);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processTypeElement(final TypeElement annotation, final TypeElement element, final MethodDescriptors methodDescriptors) {
        String packageName = elementUtils().getPackageOf(element).getQualifiedName().toString();
        String interfaceName = element.getSimpleName().toString();
        String primaryClassName = toQualifiedClassName(packageName, getPrimaryClassName(element));
        String primaryClassNamePrefix = getPrimaryClassNamePrefix(element);
        // Map<String, String> elementTranslations = getAllMessageMethods(methods);

        try {

            String classTranslationFilesPath;

            //User defined
            if (translationFilesPath != null) {
                classTranslationFilesPath = translationFilesPath + packageName.replace('.', File.separatorChar);

                //By default use the class output folder
            } else {
                FileObject fObj = filer().getResource(StandardLocation.CLASS_OUTPUT, packageName, interfaceName);
                classTranslationFilesPath = fObj.toUri().getPath().replaceAll(Pattern.quote(interfaceName), "");
            }

            File dir = new File(classTranslationFilesPath);
            File[] files = dir.listFiles(new TranslationFileFilter(primaryClassNamePrefix));

            if (files != null) {
                for (File file : files) {
                    Map<MethodDescriptor, String> translations = validateTranslationMessages(methodDescriptors, file);
                    generateSourceFileFor(primaryClassName, classTranslationFilesPath, file.getName(), translations);
                }
            }

        } catch (IOException e) {
            logger().error(e, "Cannot read %s package files", packageName);
        }
    }

    /**
     * Returns only the valid translations message corresponding
     * to the declared {@link Message} methods in the
     * {@link MessageBundle} or {@link MessageLogger} interface.
     *
     * @param elementTranslations the declared element translations
     * @param file                the translation file
     *
     * @return the valid translations messages
     */
    private Map<MethodDescriptor, String> validateTranslationMessages(final MethodDescriptors methodDescriptors, final File file) {
        Map<MethodDescriptor, String> validTranslations = new HashMap<MethodDescriptor, String>();

        try {

            //Load translations
            Properties translations = new Properties();
            translations.load(new FileInputStream(file));
            for (MethodDescriptor methodDescriptor : methodDescriptors) {
                final String key = methodDescriptor.translationKey();
                if (translations.containsKey(key)) {
                    String message = translations.getProperty(key);
                    if (!message.trim().isEmpty()) {
                        validTranslations.put(methodDescriptor, translations.getProperty(key));
                    } else {
                        logger().warn("The translation message with key %s is ignored because value is empty or contains only whitespace", key);
                    }

                } else {
                    logger().warn("The translation message with key %s have no corresponding method.", key);
                }
            }

        } catch (IOException e) {
            logger().error(e, "Cannot read the %s translation file", file.getName());
        }

        return validTranslations;
    }

    /**
     * Generate a class for the given translation file.
     *
     * @param primaryClassName    the qualified primary class name
     * @param translationFilePath the translation file path
     * @param translationFileName the translation file name
     * @param translations        the translations message
     */
    private void generateSourceFileFor(final String primaryClassName, final String translationFilePath, final String translationFileName, final Map<MethodDescriptor, String> translations) {
        logger().note("Generating translation class for %s.", translationFileName);

        String generatedClassName = primaryClassName.concat(getTranslationClassNameSuffix(translationFileName));
        String superClassName = getEnclosingTranslationClassName(generatedClassName);

        //Generate empty translation super class if needed
        //Check if enclosing translation file exists, if not generate an empty super class
        String enclosingTranslationFileName = getEnclosingTranslationFileName(translationFileName);
        File enclosingTranslationFile = new File(translationFilePath, enclosingTranslationFileName);
        if (!enclosingTranslationFileName.equals(translationFileName) && !enclosingTranslationFile.exists()) {
            generateSourceFileFor(primaryClassName, translationFilePath, enclosingTranslationFileName, Collections.<MethodDescriptor, String>emptyMap());
        }

        //Create source file
        ClassModel classModel;

        if (primaryClassName.endsWith(ImplementationType.BUNDLE.toString())) {
            classModel = new MessageBundleTranslator(generatedClassName, superClassName, translations);
        } else {
            classModel = new MessageLoggerTranslator(generatedClassName, superClassName, translations);
        }

        try {

            classModel.create(filer().createSourceFile(classModel.getClassName()));

        } catch (IOException ex) {
            logger().error(ex, "Cannot generate %s source file", generatedClassName);

        } catch (IllegalStateException ex) {
            logger().error(ex, "Cannot generate %s source file", generatedClassName);
        }
    }

    /**
     * Translation file Filter.
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

            boolean isGenerated = name.endsWith(TranslationFileGenerator.GENERATED_FILE_EXTENSION);
            boolean isTranslationFile = name.matches(Pattern.quote(className) + TRANSLATION_FILE_EXTENSION_PATTERN);

            return !isGenerated && isTranslationFile;
        }
    }

}
