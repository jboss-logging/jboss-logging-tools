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
import org.jboss.logging.Message;
import org.jboss.logging.MessageBundle;
import org.jboss.logging.MessageLogger;
import org.jboss.logging.model.ClassModel;
import org.jboss.logging.model.ImplementationType;
import org.jboss.logging.model.MessageBundleTranslator;
import org.jboss.logging.model.MessageLoggerTranslator;
import org.jboss.logging.util.TransformationUtil;
import org.jboss.logging.util.TranslationUtil;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
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
// TODO support inner class
public final class TranslationClassGenerator extends Generator {

    /**
     * The constructor.
     *
     * @param processingEnv the processing environment
     */
    public TranslationClassGenerator(final ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return this.getClass().getSimpleName();
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

                    PackageElement packageElement = this.elementUtils().getPackageOf(element);
                    String packageName = packageElement.getQualifiedName().toString();
                    String interfaceName = element.getSimpleName().toString();
                    String primaryClassName = TransformationUtil.toQualifiedClassName(packageName, interfaceName);
                    primaryClassName = primaryClassName.concat(bundleAnnotation != null ? ImplementationType.BUNDLE.extension() : ImplementationType.LOGGER.extension());
                    Class<?> annotationClass = bundleAnnotation != null ? MessageBundle.class : MessageLogger.class;

                    try {

                        FileObject fObj = this.filer().getResource(StandardLocation.CLASS_OUTPUT, packageName, interfaceName);
                        String packagePath = fObj.toUri().getPath().replaceAll(Pattern.quote(interfaceName), "");
                        File dir = new File(packagePath);

                        File[] files = dir.listFiles(new TranslationFileFilter(interfaceName));
                        if (files != null) {
                            for (File file : files) {
                                String classNameSuffix = TranslationUtil.getTranslationClassNameSuffix(file.getName());
                                String qualifiedClassName = primaryClassName.concat(classNameSuffix);

                                //Get translations messages
                                Map<String, String> translations = this.getValidTranslationMessagesFor(element, file);
                                this.generateClassFor(primaryClassName, qualifiedClassName, annotationClass, translations);
                            }
                        }

                    } catch (IOException e) {
                        this.logger().error("Cannot read %s package files", packageName);
                    }

                }

            }

        }

    }

    /**
     * Generate a class for the given translation file.
     *
     * @param primaryClassName   the qualified super class name
     * @param generatedClassName the qualified class name
     * @param annotationClass    the annotation who trigger generation
     * @param translations    the translations message
     */
    private void generateClassFor(final String primaryClassName, final String generatedClassName, final Class<?> annotationClass, final Map<String, String> translations) {

        this.logger().note("Generating %s translation class", generatedClassName);

        try {

             //Generate super class if needed
             String superClassName = TranslationUtil.getEnclosingTranslationClassName(generatedClassName);
             String packageName = TransformationUtil.toPackage(superClassName);
             String simpleClassName = TransformationUtil.toSimpleClassName(superClassName);

             if (!superClassName.equals(primaryClassName)) {
                 FileObject fObject = this.filer().getResource(StandardLocation.SOURCE_OUTPUT, packageName, simpleClassName);
                 String pathName = fObject.toUri().getPath().replaceAll(Pattern.quote(simpleClassName), "");
                 File file = new File(pathName, simpleClassName + ".java");
                 if (!file.exists()) {
                    this.generateClassFor(primaryClassName, superClassName, annotationClass, Collections.EMPTY_MAP);
                 }
             }

            
            ClassModel classModel;

            if (annotationClass.isAssignableFrom(MessageBundle.class)) {
                classModel = new MessageBundleTranslator(generatedClassName, superClassName, translations);
            } else {
                classModel = new MessageLoggerTranslator(generatedClassName, superClassName, translations);
            }
            classModel.create(this.filer().createSourceFile(classModel.getClassName()));

        }
        catch (Exception e) {
            this.logger().error("Cannot generate %s source file", generatedClassName);
        }
    }


    /**
     * Returns only the valid translations message
     * @param element
     * @param file
     * @return
     */
    private Map<String, String> getValidTranslationMessagesFor(final TypeElement element, final File file) {

        Map<String, String> validTranslations = new HashMap<String, String>();

        try {

            Properties translations = new Properties();
            Map<String, String> elementMessages = this.getElementTranslationMessages(element);

            //Load translations
            translations.load(new FileInputStream(file));
            for (String key : translations.stringPropertyNames()) {
                if (elementMessages.containsKey(key)) {
                    validTranslations.put(key, translations.getProperty(key));
                }
            }

        } catch (IOException e) {
            this.logger().error("Cannot read the % translation file", file.getName());
        }

        return validTranslations;
    }

    /**
     * Returns all the translations messages for
     * the given type element. A translation message value is
     * defined by the {@link org.jboss.logging.Message} annotation
     * value and the annotated method is the translation key.
     *
     * @param element the element
     * @return all the translations messages
     */
    private Map<String, String> getElementTranslationMessages(final TypeElement element) {

        Map<String, String> translationsMessage = new HashMap<String, String>();
        
        if (element.getKind().isInterface()) {

            //Get super interfaces class translations messages
            List<? extends TypeMirror> superInterfaces = element.getInterfaces();
            for (TypeMirror intf : superInterfaces) {
                translationsMessage.putAll(this.getElementTranslationMessages((TypeElement) this.typeUtils().asElement(intf)));
            }

            //Get current class translation message
            List<ExecutableElement> methods = ElementFilter.methodsIn(element.getEnclosedElements());
            for (ExecutableElement method : methods) {
                Message annotation = method.getAnnotation(Message.class);
                if (annotation != null) {
                    translationsMessage.put(method.getSimpleName().toString(), annotation.value());
                }
            }

        }

        return translationsMessage;
    }

    /**
     * Translation file filter.
     */
    private class TranslationFileFilter implements FilenameFilter {

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
