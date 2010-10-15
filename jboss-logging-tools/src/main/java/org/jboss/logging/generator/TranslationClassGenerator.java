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
import org.jboss.logging.model.MessageBundleClassModel;
import org.jboss.logging.model.MessageLoggerClassModel;
import org.jboss.logging.model.decorator.GeneratedAnnotation;
import org.jboss.logging.model.decorator.TranslationMethods;
import org.jboss.logging.util.TransformationUtil;
import org.jboss.logging.util.TranslationUtil;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.tools.FileObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;
import javax.tools.JavaFileManager.Location;
import javax.tools.StandardLocation;
import org.jboss.logging.model.ImplementationType;

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
     * {@inheritDoc }
     */
    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void generate(Set<? extends TypeElement> annotations,
            RoundEnvironment roundEnv) {

        //Do work only on @MessageLogger and @MessageBundle annotation
        generate(ElementFilter.typesIn(roundEnv.getRootElements()));

    }

    /**
     * Processes the types including inner interfaces.
     *
     * @param types the types to process.
     */
    private void generate(final Collection<? extends TypeElement> types) {
        for (TypeElement type : types) {
            // Checks for inner interfaces
            generate(ElementFilter.typesIn(type.getEnclosedElements()));
            // Process the types
            for (TypeElement element : types) {

                //Process only non-private interface
                if (element.getKind().isInterface() && !element.getModifiers().
                        contains(Modifier.PRIVATE)) {

                    MessageBundle bundleAnnotation = element.getAnnotation(
                            MessageBundle.class);
                    MessageLogger loggerAnnotation = element.getAnnotation(
                            MessageLogger.class);

                    if (bundleAnnotation != null || loggerAnnotation != null) {

                        PackageElement packageElement = elementUtils.
                                getPackageOf(element);
                        String packageName = packageElement.getQualifiedName().
                                toString();
                        String interfaceName = element.getSimpleName().toString();
                        String primaryClassName = TransformationUtil.toQualifiedClassName(packageName,
                                interfaceName.concat(bundleAnnotation != null ? ImplementationType.BUNDLE.extension() : ImplementationType.LOGGER.extension()));
                        Class<? extends Annotation> annotationClass = bundleAnnotation != null ? MessageBundle.class : MessageLogger.class;

                        try {
                            for (Location location : StandardLocation.values()) {
                                try {
                                    processResources(packageName, interfaceName, primaryClassName, annotationClass, location);
                                } catch (NullPointerException e){}
                            }
                        } catch (IOException e) {
                            logger().error("Cannot read %s package files, cause %s.", packageName, e.getMessage());
                        }

                    }

                }

            }
        }
    }

    /**
     * Process the resource files.
     *
     * @param packageName       the package name.
     * @param interfaceName     the interface name.
     * @param primaryClassName  the primary class name.
     * @param annotationClass   the annotation class.
     * @param location          the location to search for property files.
     *
     * @throws IOException
     */
    private void processResources(final String packageName,
            final String interfaceName, final String primaryClassName,
            final Class<? extends Annotation> annotationClass, Location location)
            throws
            IOException {
        FileObject fObj = filer.getResource(location, packageName, interfaceName);
        String packagePath = fObj.toUri().getPath().
                replaceAll(interfaceName, "");
        File dir = new File(packagePath);
        System.out.printf("Location: %s - %s%n", location, (dir == null ? "null" : dir.getAbsolutePath()));

        File[] files = dir.listFiles(new TranslationFileFilter(
                interfaceName));
        if (files != null && files.length > 0) {
            for (File file : files) {
                String qualifiedClassName = primaryClassName + TranslationUtil.getTranslationClassNameSuffix(file.getName());
                logger().note("Generating the %s translation file class", qualifiedClassName, packageName);
                this.generateClassFor(primaryClassName, qualifiedClassName, annotationClass, file);
            }
        } else {
            logger().note("No translations found for %s.", primaryClassName);
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
    private void generateClassFor(final String primaryClassName,
            final String generatedClassName,
            final Class<?> messageAnnotationClass, final File translationFile) {

        try {

            //TODO I think there is a best manner
            //Check if super class have been generated
            String superClassName = TranslationUtil.
                    getEnclosingTranslationClassName(generatedClassName);
            if (!superClassName.equals(primaryClassName)) {
                String name = translationFile.getName();
                int lastUnder = name.lastIndexOf("_");

                String enclosingTranslationFileName = name.substring(0,
                        lastUnder) + ".properties";
                File parent = new File(translationFile.getParent(),
                        enclosingTranslationFileName);
                if (!parent.exists()) {
                    this.generateClassFor(primaryClassName, superClassName,
                            messageAnnotationClass, parent);
                }
            }

            //Load translations
            Properties translations = new Properties();
            if (translationFile != null && translationFile.exists()) {
                translations.load(new FileInputStream(translationFile));
            }

            ClassModel classModel;

            if (messageAnnotationClass.isAssignableFrom(MessageBundle.class)) {
                classModel = new MessageBundleClassModel(logger(), generatedClassName,
                        superClassName);
                classModel = new GeneratedAnnotation(classModel, MessageBundle.class.
                        getName());
            } else {
                classModel = new MessageLoggerClassModel(logger(), generatedClassName,
                        superClassName);
                classModel = new GeneratedAnnotation(classModel, MessageLogger.class.
                        getName());
            }

            classModel = new TranslationMethods(classModel, (Map) translations);
            classModel.generateModel();
            classModel.writeClass(filer.createSourceFile(
                    classModel.getClassName()));

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
            return name.matches(
                    Pattern.quote(this.className) + PROPS_EXTENSION_PATTERN);
        }
    }
}
