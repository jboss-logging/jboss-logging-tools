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
package org.jboss.logging.traduction;

import org.jboss.logging.Generator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author Kevin Pollet
 */
public final class PropertyClassGenerator extends Generator {

    /**
     * The logger.
     */
    private final Logger logger = LoggerFactory.getLogger(PropertyClassGenerator.class);

    public PropertyClassGenerator(ProcessingEnvironment processingEnv) {
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

        Filer filer = this.processingEnv().getFiler();
        Types typesUtils = this.processingEnv().getTypeUtils();
        Elements elementsUtils = this.processingEnv().getElementUtils();

        if (!annotations.isEmpty()) {

            logger.debug("Start traduction annotation processor");

            for (TypeElement element : annotations) {

                for (final Element e : roundEnv.getElementsAnnotatedWith(element)) {

                    //Must be an interface and public
                    if (e.getKind() == ElementKind.INTERFACE) {

                        Name className = e.getSimpleName();
                        PackageElement packageElement = elementsUtils.getPackageOf(e);

                        try {
                            // Get package

                            FileObject fObj = filer.getResource(StandardLocation.CLASS_OUTPUT, "", packageElement.getQualifiedName());

                            //Get properties file
                            String packagePath = fObj.toUri().getPath().replaceAll(Pattern.quote("."), System.getProperty("file.separator"));
                            File dir = new File(packagePath);
                            File[] files = dir.listFiles(new PropertyClassGenerator.PropertyFileFilter(className.toString()));

                            for (File f : files) {
                                logger.debug("Property file found for {} : {}", className, f.getName());
                            }

                        } catch (IOException e1) {
                            this.processingEnv().getMessager().printMessage(Diagnostic.Kind.ERROR, "Cannot read the package content.", packageElement);
                        }

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
