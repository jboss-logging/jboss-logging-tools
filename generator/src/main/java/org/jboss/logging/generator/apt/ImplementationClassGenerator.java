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
package org.jboss.logging.generator.apt;

import org.jboss.logging.generator.intf.model.MessageInterface;
import org.jboss.logging.generator.model.ClassModel;
import org.jboss.logging.generator.model.ClassModelFactory;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import java.io.IOException;

/**
 * A generator for creating implementations of message bundle and logging
 * interfaces.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
final class ImplementationClassGenerator extends AbstractGenerator {

    /**
     * @param processingEnv the processing environment.
     */
    public ImplementationClassGenerator(ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    @Override
    public void processTypeElement(final TypeElement annotation, final TypeElement element, final MessageInterface messageInterface) {
        try {
            final ClassModel classModel = ClassModelFactory.implementation(messageInterface);
            classModel.create(filer().createSourceFile(classModel.qualifiedClassName()));
        } catch (IOException e) {
            logger().error(element, e);
        } catch (IllegalStateException e) {
            logger().error(element, e);
        }
    }
}
