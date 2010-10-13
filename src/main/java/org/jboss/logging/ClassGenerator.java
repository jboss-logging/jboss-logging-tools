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

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import org.jboss.logging.model.ImplementationClassModel;
import org.jboss.logging.model.MessageBundleCodeModel;
import org.jboss.logging.model.MessageLoggerCodeModel;

import com.sun.codemodel.internal.JClassAlreadyExistsException;

/**
 * @author James R. Perkins Jr. (jrp)
 * 
 */
public final class ClassGenerator extends Generator {

    @Override
    public String getName() {
        return "ClassGenerator";
    }

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
    public void generate(final Set<? extends TypeElement> annotations,
            final RoundEnvironment roundEnv) {
        process(roundEnv.getRootElements());
    }

    private void process(Collection<? extends Element> elements) {
        Collection<? extends TypeElement> typeElements = ElementFilter
                .typesIn(elements);
        for (TypeElement type : typeElements) {
            process(type.getEnclosedElements());

            // Must be an interface
            if (type.getKind() != ElementKind.INTERFACE) {
                continue;
            }

            // Must be public
            if (!type.getModifiers().contains(Modifier.PUBLIC)) {
                continue;
            }

            final String interfaceName = processingEnv().getElementUtils()
                    .getBinaryName(type).toString();
            final MessageLogger logger = type
                    .getAnnotation(MessageLogger.class);
            final MessageBundle bundle = type
                    .getAnnotation(MessageBundle.class);
            try {
                if (logger != null) {
                    createClass(new MessageLoggerCodeModel(interfaceName,
                            logger.projectCode()), type);
                }
                if (bundle != null) {
                    createClass(new MessageBundleCodeModel(interfaceName,
                            bundle.projectCode()), type);
                }
            } catch (IOException e) {
                printErrorMessage(e, type);
            } catch (JClassAlreadyExistsException e) {
                printErrorMessage(e, type);
            }
        }
    }

    private void createClass(final ImplementationClassModel codeModel,
            final TypeElement type) throws IOException,
            JClassAlreadyExistsException {
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
