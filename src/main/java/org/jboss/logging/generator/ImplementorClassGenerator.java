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

import org.jboss.logging.AbstractTool;
import org.jboss.logging.model.ImplementationClassModel;
import org.jboss.logging.model.MessageBundleImplementor;
import org.jboss.logging.model.MessageLoggerImplementor;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.Collection;
import javax.lang.model.type.TypeMirror;
import org.jboss.logging.Annotations;
import org.jboss.logging.Loggers;

/**
 * A generator for creating implementations of message bundle and logging
 * interfaces.
 *
 * @author James R. Perkins Jr. (jrp)
 */
public final class ImplementorClassGenerator extends AbstractTool {

    /**
     * @param processingEnv
     */
    public ImplementorClassGenerator(ProcessingEnvironment processingEnv, final Annotations annotations, final Loggers loggers) {
        super(processingEnv, annotations, loggers);
    }

    @Override
    public void processTypeElement(final TypeElement annotation, final TypeElement element,
            final Collection<ExecutableElement> methods) {
        try {
            final String interfaceName = elementUtils().getBinaryName(element).toString();
            if (element.getAnnotation(annotations().messageLogger()) != null) {
                createClass(new MessageLoggerImplementor(interfaceName, annotations().projectCode(element),extendsBasicLogger(element)), methods);
            }
            if (element.getAnnotation(annotations().messageBundle()) != null) {
                createClass(new MessageBundleImplementor(interfaceName, annotations().projectCode(element)), methods);
            }
        } catch (IOException e) {
            logger().error(element, e);
        } catch (IllegalStateException e) {
            logger().error(element, e);
        }
    }

    /**
     * Creates the actual implementation.
     *
     * @param classModel the class model used to generate the source.
     * @param methods   the methods to process.
     *
     * @throws IOException           if there is an error writing the source file.
     * @throws IllegalStateException if the class has already been defined.
     */
    private void createClass(final ImplementationClassModel classModel,
            final Collection<ExecutableElement> methods) throws IOException,
                                                                IllegalStateException {
        // Create the methods
        //for (ExecutableElement method : methods) {
        //    classModel.addMethod(method);
       // }

        classModel.setMethodDescriptor(MethodDescriptor.create(elementUtils(), typeUtils(), methods, annotations()));
        // Write the source file
        classModel.create(filer().createSourceFile(classModel.getClassName()));
    }

    private boolean extendsBasicLogger(final TypeElement element) {
        if (element.getQualifiedName().toString().equals(loggers().basicLoggerClass().getName())) {
            return true;
        }
        for (TypeMirror type : element.getInterfaces()) {
           if (extendsBasicLogger((TypeElement) super.typeUtils().asElement(type))) {
               return true;
           }
        }
        return false;
    }
}
