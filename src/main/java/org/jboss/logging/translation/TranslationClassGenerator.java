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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Writer;
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
        Types typesUtils = this.processingEnv().getTypeUtils();
        Elements elementsUtils = this.processingEnv().getElementUtils();

        Set<? extends TypeElement> typesElement = ElementFilter.typesIn(roundEnv.getRootElements());

        for (TypeElement element : typesElement) {

            MessageBundle bundleAnnotation = element.getAnnotation(MessageBundle.class);
            MessageLogger loggerAnnotation = element.getAnnotation(MessageLogger.class);

            if (bundleAnnotation != null || loggerAnnotation != null) {

                //Must be an interface and public
                if (element.getKind() == ElementKind.INTERFACE && element.getModifiers().contains(Modifier.PUBLIC)) {

                    Name className = element.getSimpleName();
                    PackageElement packageElement = elementsUtils.getPackageOf(element);

                    try {
                        // Get package

                        FileObject fObj = filer.getResource(StandardLocation.CLASS_OUTPUT, "", packageElement.getQualifiedName());

                        //Get properties file
                        String packagePath = fObj.toUri().getPath().replaceAll(Pattern.quote("."), System.getProperty("file.separator"));
                        File dir = new File(packagePath);
                        String[] filesName = dir.list(new TranslationClassGenerator.PropertyFileFilter(className.toString()));

                        for (String fileName : filesName) {
                            this.logger.debug("Generate property classes for {}.", fileName);

                            //Generate Java Code
                            JavaFileObject object = filer.createSourceFile(packageElement.getQualifiedName() + "." + this.getClassNameForPropertyFile(fileName), packageElement);
                            Writer writer = object.openWriter();
                            writer.write("public static Toto() { }");
                            writer.close();

                        }

                    } catch (IOException e1) {
                        this.processingEnv().getMessager().printMessage(Diagnostic.Kind.ERROR, "Cannot read the package content.", packageElement);
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


    private String getClassNameForPropertyFile(String name) {
        int first = name.indexOf(".");
        int last = name.lastIndexOf(".");
        int firstUnder = name.indexOf("_");

        String clazz = name.substring(0, first);
        String qualifier = name.substring(firstUnder, last);

        return clazz.concat(qualifier.replaceAll("_", "\\$"));
    }

}
