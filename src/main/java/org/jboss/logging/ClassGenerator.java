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

import java.io.File;
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
import javax.tools.JavaFileObject;

import com.sun.codemodel.internal.JClassAlreadyExistsException;
import com.sun.codemodel.internal.writer.SingleStreamCodeWriter;

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

            final String interfaceName = processingEnv.getElementUtils()
                    .getBinaryName(type).toString();
            final MessageLogger logger = type
                    .getAnnotation(MessageLogger.class);
            final MessageBundle bundle = type
                    .getAnnotation(MessageBundle.class);
            try {
                if (logger != null) {
                    createClass(
                            CodeModelFactory.createMessageLogger(interfaceName),
                            type);
                }
                if (bundle != null) {
                    createClass(
                            CodeModelFactory.createMessageBundle(interfaceName),
                            type);
                }
            } catch (IOException e) {
                printErrorMessage(e);
            } catch (JClassAlreadyExistsException e) {
                printErrorMessage(e);
            }
        }
    }

    private void createClass(final CodeModel codeModel, final TypeElement type)
            throws IOException {
        for (TypeMirror interfaceType : type.getInterfaces()) {
            for (ExecutableElement method : ElementFilter
                    .methodsIn(processingEnv.getTypeUtils()
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
        final Filer filer = processingEnv.getFiler();
        final JavaFileObject fileObject = filer.createSourceFile(
                codeModel.className(), type);
        // final File file = new File(fileObject.toUri());
        // if (!file.exists()) {
        //    file.createNewFile();
        // }
        // codeModel.codeModel().build(file);
        final SingleStreamCodeWriter codeWriter = new SingleStreamCodeWriter(fileObject.openOutputStream());
        codeModel.codeModel().build(codeWriter);
    }
}
