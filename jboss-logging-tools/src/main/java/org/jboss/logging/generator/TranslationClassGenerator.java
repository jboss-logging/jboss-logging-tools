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

import org.jboss.logging.Generator;
import org.jboss.logging.MessageBundle;
import org.jboss.logging.MessageLogger;
import org.jboss.logging.model.ClassModel;
import org.jboss.logging.model.MessageBundleTranslator;
import org.jboss.logging.model.MessageLoggerTranslator;
import org.jboss.logging.model.decorator.GeneratedAnnotation;
import org.jboss.logging.model.decorator.TranslationMethods;
import org.jboss.logging.util.TransformationUtil;
import org.jboss.logging.util.TranslationUtil;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;
import org.jboss.logging.ToolLogger;

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
//TODO warning if no method correspond to a translation
public final class TranslationClassGenerator extends Generator {

    /**
     * The filer.
     */
    private final Filer filer;

    /**
     * Util to work with elements.
     */
    private final Elements elementUtils;

    /**
     * The constructor.
     *
     * @param processingEnv the processing environment
     */
    public TranslationClassGenerator(ProcessingEnvironment processingEnv) {
        super(processingEnv);

        this.filer = processingEnv.getFiler();
        this.elementUtils = processingEnv.getElementUtils();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void generate(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        //Do work only on @MessageLogger and @MessageBundle annotation
        Set<? extends TypeElement> typesElement = ElementFilter.typesIn(roundEnv.getRootElements());
        for (TypeElement element : typesElement) {

            //Process only public interface
            if (element.getKind().isInterface() && element.getModifiers().contains(Modifier.PUBLIC)) {

                MessageBundle bundleAnnotation = element.getAnnotation(MessageBundle.class);
                MessageLogger loggerAnnotation = element.getAnnotation(MessageLogger.class);

                if (bundleAnnotation != null || loggerAnnotation != null) {

                    PackageElement packageElement = elementUtils.getPackageOf(element);
                    String packageName = packageElement.getQualifiedName().toString();
                    String interfaceName = element.getSimpleName().toString();
                    String primaryClassName = TransformationUtil.toQualifiedClassName(packageName, interfaceName.concat(bundleAnnotation != null ? "$bundle" : "$logger"));
                    Class<?> annotationClass = bundleAnnotation != null ? MessageBundle.class : MessageLogger.class;

                    try {

                        FileObject fObj = filer.getResource(StandardLocation.CLASS_OUTPUT, packageName, interfaceName);
                        String packagePath = fObj.toUri().getPath().replaceAll(interfaceName, "");
                        File dir = new File(packagePath);

                        File[] files = dir.listFiles(new TranslationFileFilter(interfaceName));
                        if (files != null) {
                            for (File file : files) {
                                String qualifiedClassName = primaryClassName + TranslationUtil.getTranslationClassNameSuffix(file.getName());
                                logger().note("Generating the %s translation file class", qualifiedClassName, packageName);
                                this.generateClassFor(primaryClassName, qualifiedClassName, annotationClass, file);
                            }
                        }

                    } catch (IOException e) {
                        logger().error("Cannot read %s package files", packageName);
                    }

                }

            }

        }

    }


    /**
     * Generate a class for the given translation file.
     *
     * @param primaryClassName       the qualified super class name
     * @param generatedClassName     the qualified class name
     * @param messageAnnotationClass the annotation who trigger generation
     * @param translationFile        the translation file
     */
    private void generateClassFor(final String primaryClassName, final String generatedClassName, final Class<?> messageAnnotationClass, final File translationFile) {

        try {

            //TODO I think there is a best manner
            //Check if super class have been generated
            String superClassName = TranslationUtil.getEnclosingTranslationClassName(generatedClassName);
            if (!superClassName.equals(primaryClassName)) {
                String name = translationFile.getName();
                int lastUnder = name.lastIndexOf("_");

                String enclosingTranslationFileName = name.substring(0, lastUnder) + ".properties";
                File parent = new File(translationFile.getParent(), enclosingTranslationFileName);
                if (!parent.exists()) {
                    this.generateClassFor(primaryClassName, superClassName, messageAnnotationClass, parent);
                }
            }

            //Load translations
            Properties translations = new Properties();
            if (translationFile != null && translationFile.exists()) {
                translations.load(new FileInputStream(translationFile));
            }

            ClassModel classModel;

            if (messageAnnotationClass.isAssignableFrom(MessageBundle.class)) {
                classModel = new MessageBundleTranslator(logger(), generatedClassName, superClassName);
                classModel = new GeneratedAnnotation(classModel, MessageBundle.class.getName());
            } else {
                classModel = new MessageLoggerTranslator(logger(), generatedClassName, superClassName);
                classModel = new GeneratedAnnotation(classModel, MessageLogger.class.getName());
            }

            classModel = new TranslationMethods(classModel, (Map) translations);
            classModel.generateModel();
            classModel.writeClass(filer.createSourceFile(classModel.getClassName()));

        } catch (Exception e) {
            logger().error("Cannot generate %s source file", generatedClassName);
        }


    }

    /**
     * The translation file filter.
     */
    private static class TranslationFileFilter implements FilenameFilter {

        /**
         * The properties file pattern. The property file must
         * match the given pattern <em>org.pkgname.InterfaceName.i18n_locale.properties</em> where locale is :
         * <ul>
         * <li>xx - where xx is the language like (e.g. en)</li>
         * <li>xx_YY - where xx is the language and YY is the country like (e.g. en_US)</li>
         * <li>xx_YY_ZZ - where xx is the language, YY is the country and ZZ is the variant like (e.g. en_US_POSIX)</li>
         * </ul>
         */
        private static final String PROPS_EXTENSION_PATTERN = ".i18n_[a-z]{2}(_[A-Z]{2}(_[A-Z]*)?)?\\.properties";

        /**
         * The class name.
         */
        private String className;

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
        public boolean accept(File dir, String name) {
            return name.matches(Pattern.quote(this.className) + PROPS_EXTENSION_PATTERN);
        }

    }


}
