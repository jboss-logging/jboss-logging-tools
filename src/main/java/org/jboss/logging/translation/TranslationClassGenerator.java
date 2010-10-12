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
package org.jboss.logging.translation;

import org.jboss.logging.Generator;
import org.jboss.logging.MessageBundle;
import org.jboss.logging.MessageLogger;
import org.jboss.logging.model.ClassModel;
import org.jboss.logging.model.MessageBundleClassModel;
import org.jboss.logging.model.MessageLoggerClassModel;
import org.jboss.logging.util.PropertyFileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author Kevin Pollet
 */
public final class TranslationClassGenerator extends Generator {

    /**
     * The logger.
     */
    private final Logger logger = LoggerFactory.getLogger(TranslationClassGenerator.class);

    /**
     * The constructor.
     *
     * @param processingEnv the processing environment
     */
    public TranslationClassGenerator(ProcessingEnvironment processingEnv) {
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

        //Get utils
        Filer filer = this.processingEnv().getFiler();
        Elements elementsUtils = this.processingEnv().getElementUtils();

        Set<? extends TypeElement> typesElement = ElementFilter.typesIn(roundEnv.getRootElements());

        for (TypeElement element : typesElement) {

            if (element.getKind() == ElementKind.INTERFACE && element.getModifiers().contains(Modifier.PUBLIC)) {

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

                        //List properties file
                        PropertyFileFilter filter = new TranslationClassGenerator.PropertyFileFilter(interfaceName);
                        File[] files = dir.listFiles(filter);

                        for (File file : files) {

                            String fileName = file.getName();
                            String propertyClassName = PropertyFileUtil.getClassNameFor(primaryClassName, fileName);
                            String qualifiedPropertyClassName = packageName.concat("." + propertyClassName);

                            /*
                             * Generate Java Code.
                             */

                            try {

                                this.logger.debug("Generate property classes for {} with name {}", fileName, qualifiedPropertyClassName);

                                Properties translation = new Properties();
                                translation.load(new FileReader(file));

                                ClassModel classModel;

                                if (bundleAnnotation != null) {
                                    classModel = new MessageBundleClassModel(qualifiedPropertyClassName, bundleAnnotation.projectCode(), qualifiedPrimaryClassName);
                                } else {
                                    classModel = new MessageLoggerClassModel(qualifiedPropertyClassName, loggerAnnotation.projectCode(), qualifiedPrimaryClassName);
                                }

                                classModel.initModel();
                                classModel = TranslationClassBuilder.from(classModel).withAllTranslations((Map) translation).build();
                                classModel.writeClass(filer);

                            } catch (Exception e) {
                                this.processingEnv().getMessager().printMessage(Diagnostic.Kind.ERROR, "Error during generation of class " + propertyClassName + " already exist");
                                logger.error("Error :", e);
                            }

                        }

                    } catch (IOException e1) {
                        this.processingEnv().getMessager().printMessage(Diagnostic.Kind.ERROR, "Cannot read package content", packageElement);
                    }

                }

            }

        }

    }


    /**
     * The property file filter.
     */
    private static class PropertyFileFilter implements FilenameFilter {

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
        public PropertyFileFilter(final String className) {
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
