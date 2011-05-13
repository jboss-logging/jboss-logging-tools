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
import org.jboss.logging.LoggingTools;
import org.jboss.logging.model.ImplementationClassModel;
import org.jboss.logging.model.MessageBundleImplementor;
import org.jboss.logging.model.MessageLoggerImplementor;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;

import static org.jboss.logging.LoggingTools.annotations;

/**
 * A generator for creating implementations of message bundle and logging
 * interfaces.
 *
 * @author James R. Perkins Jr. (jrp)
 */
public final class ImplementorClassGenerator extends AbstractTool {

    /**
     * @param processingEnv the processing environment.
     * @param annotations   the annotation descriptor.
     * @param loggers       the logger descriptor.
     */
    public ImplementorClassGenerator(ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    @Override
    public void processTypeElement(final TypeElement annotation, final TypeElement element, final MethodDescriptors methodDescriptors) {
        try {
            final String interfaceName = elementUtils().getBinaryName(element).toString();
            if (element.getAnnotation(LoggingTools.annotations().messageLogger()) != null) {
                createClass(new MessageLoggerImplementor(interfaceName, methodDescriptors, annotations().projectCode(element), extendsBasicLogger(element)));
            }
            if (element.getAnnotation(LoggingTools.annotations().messageBundle()) != null) {
                createClass(new MessageBundleImplementor(interfaceName, methodDescriptors, annotations().projectCode(element)));
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
     * @param classModel        the class model used to generate the source.
     * @param methodDescriptors the methods to process.
     *
     * @throws IOException           if there is an error writing the source file.
     * @throws IllegalStateException if the class has already been defined.
     */
    private void createClass(final ImplementationClassModel classModel) throws IOException,
            IllegalStateException {
        // Write the source file
        classModel.create(filer().createSourceFile(classModel.getClassName()));
    }

    private boolean extendsBasicLogger(final TypeElement element) {
        if (element.getQualifiedName().toString().equals(LoggingTools.loggers().basicLoggerClass().getName())) {
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
