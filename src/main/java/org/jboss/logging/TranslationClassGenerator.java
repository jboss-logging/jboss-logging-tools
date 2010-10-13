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
package org.jboss.logging;

import org.jboss.logging.model.ClassModel;
import org.jboss.logging.model.MessageBundleClassModel;
import org.jboss.logging.model.MessageLoggerClassModel;
import org.jboss.logging.model.decorator.GeneratedAnnotation;
import org.jboss.logging.model.decorator.TranslationMethods;
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
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author Kevin Pollet
 */
public final class TranslationClassGenerator extends Generator {

    private final Filer filer;

    private final Elements elementsUtils;

    private final Messager messager;


    /**
     * The constructor.
     *
     * @param processingEnv the processing environment
     */
    public TranslationClassGenerator(ProcessingEnvironment processingEnv) {
        super(processingEnv);

        filer = processingEnv.getFiler();
        elementsUtils = processingEnv.getElementUtils();
        messager = processingEnv.getMessager();
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

                    PackageElement packageElement = elementsUtils.getPackageOf(element);
                    String packageName = packageElement.getQualifiedName().toString();
                    String interfaceName = element.getSimpleName().toString();
                    String primaryClassName = interfaceName.concat(bundleAnnotation != null ? "$bundle" : "$logger");
                    String qualifiedPrimaryClassName = packageName.concat("." + primaryClassName);

                    try {

                        FileObject fObj = filer.getResource(StandardLocation.CLASS_OUTPUT, "", packageElement.getQualifiedName());
                        String packagePath = fObj.toUri().getPath().replaceAll(Pattern.quote("."), System.getProperty("file.separator"));
                        File dir = new File(packagePath);

                        //List translations file corresponding to this MessageBundle or MessageLogger interface
                        TranslationFileFilter filter = new TranslationFileFilter(interfaceName);

                        //Last generated translation class
                        Map<String, String> lastGeneratedClass = new HashMap<String, String>();


                        File[] files = dir.listFiles(filter);
                        for (File file : files) {

                            String fileName = file.getName();
                            String locale = TranslationUtil.getTranslationFileLocale(fileName);

                            String translationClassName = primaryClassName + TranslationUtil.getTranslationClassNameSuffix(fileName);
                            String qualifiedPropertyClassName = packageName + "." + translationClassName;


                            /*
                             * Generate java code for the translation
                             * properties file.
                             */

                            messager.printMessage(Diagnostic.Kind.NOTE, String.format("Generating the %s translation file class", translationClassName));


                            String superClassName = lastGeneratedClass.get(TranslationUtil.getTranslationFileLocale(fileName));
                            if (superClassName == null) {
                                superClassName = primaryClassName;
                            }
                            

                            //Load translations
                            Properties translations = new Properties();
                            translations.load(new FileInputStream(file));

                            //Generate
                            Class<?> annotationClass = bundleAnnotation != null ? MessageBundle.class : MessageLogger.class;
                            this.generateClassFor(superClassName, qualifiedPropertyClassName, annotationClass, (Map) translations);

                            //Memorize last generated class name
                            lastGeneratedClass.put(locale, translationClassName);


                        }


                    } catch (IOException e) {
                        messager.printMessage(Diagnostic.Kind.ERROR, String.format("Cannot read %s package files", packageName));
                    }

                }

            }

        }

    }


    private void generateClassFor(final String superClass, final String clazz, final Class<?> messageAnnotationClass, final Map<String, String> translations) {

        try {

            ClassModel classModel;

            if (messageAnnotationClass.isAssignableFrom(MessageBundle.class)) {
                classModel = new MessageBundleClassModel(clazz, superClass);
                classModel = new GeneratedAnnotation(classModel, MessageBundle.class.getName());
            } else {
                classModel = new MessageLoggerClassModel(clazz, superClass);
                classModel = new GeneratedAnnotation(classModel, MessageLogger.class.getName());
            }

            classModel = new TranslationMethods(classModel, translations);
            classModel.generateModel();
            classModel.writeClass(filer.createSourceFile(classModel.getClassName()));


        } catch (Exception e) {
            this.messager.printMessage(Diagnostic.Kind.ERROR, String.format("Cannot generate %s source file", clazz));
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
