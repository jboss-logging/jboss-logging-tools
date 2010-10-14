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

import com.sun.codemodel.internal.JClassAlreadyExistsException;
import org.jboss.logging.Generator;
import org.jboss.logging.MessageBundle;
import org.jboss.logging.MessageLogger;
import org.jboss.logging.model.ImplementationClassModel;
import org.jboss.logging.model.MessageBundleImplementor;
import org.jboss.logging.model.MessageLoggerImplementor;
import org.jboss.logging.model.validation.ValidationException;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;

/**
 * @author James R. Perkins Jr. (jrp)
 */
public final class ClassGenerator extends Generator {

    /**
     * @param processingEnv
     */
    public ClassGenerator(ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.jboss.logging.Generator#generate(javax.lang.model.element.Element)
     */

    @Override
    public void generate(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {

        Collection<? extends TypeElement> typeElements = ElementFilter.typesIn(roundEnv.getRootElements());
        for (TypeElement type : typeElements) {

                try {
                   generate(type);
                   // Handle inner classes
                   final List<? extends TypeElement> e = ElementFilter.typesIn(
                           type.getEnclosedElements());
                   for (TypeElement te : e) {
                       generate(te);
                   }
                } catch (IOException e) {
                    printErrorMessage(e, type);
                    break;
                } catch (JClassAlreadyExistsException e) {
                    printErrorMessage(e, type);
                    break;
                } catch (ValidationException e) {
                    printErrorMessage(e.getMessage(), e.getElement());
                    break;
                }

        }


    }

    private void generate(TypeElement type) throws IOException, JClassAlreadyExistsException {

            //if (type.getKind().isInterface() && type.getModifiers().contains(Modifier.PUBLIC)) {

                final String interfaceName = processingEnv().getElementUtils().getBinaryName(type).toString();
                final MessageLogger logger = type.getAnnotation(MessageLogger.class);
                final MessageBundle bundle = type.getAnnotation(MessageBundle.class);
                    if (logger != null) {
                        createClass(new MessageLoggerImplementor(interfaceName,
                                logger.projectCode()), type);
                    }
                    if (bundle != null) {
                        createClass(new MessageBundleImplementor(interfaceName,
                                bundle.projectCode()), type);
                    }

            //}

    }

    private void createClass(final ImplementationClassModel codeModel,
                             final TypeElement type) throws IOException,
            JClassAlreadyExistsException, ValidationException {
        codeModel.initModel();
        // Process all extended interfaces.
        for (TypeMirror interfaceType : type.getInterfaces()) {
            for (ExecutableElement method : ElementFilter
                    .methodsIn(processingEnv().getTypeUtils()
                            .asElement(interfaceType).getEnclosedElements())) {
                codeModel.addMethod(method);
            }
        }
        // Create the methods
        for (ExecutableElement method : ElementFilter.methodsIn(type
                .getEnclosedElements())) {
            codeModel.addMethod(method);
        }

        // Write the source file
        final Filer filer = processingEnv().getFiler();
        codeModel.writeClass(filer.createSourceFile(codeModel.getClassName()));
    }
}
