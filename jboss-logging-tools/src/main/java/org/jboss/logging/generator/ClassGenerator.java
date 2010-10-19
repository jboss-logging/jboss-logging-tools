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

import org.jboss.logging.Generator;
import org.jboss.logging.MessageBundle;
import org.jboss.logging.MessageLogger;
import org.jboss.logging.model.ImplementationClassModel;
import org.jboss.logging.model.MessageBundleImplementor;
import org.jboss.logging.model.MessageLoggerImplementor;
import org.jboss.logging.model.validation.ValidationException;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import javax.lang.model.element.Modifier;

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

    /**
     * {@inheritDoc }
     */
    @Override
    public String getName() {
        return getClass().getSimpleName();
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

        Collection<? extends TypeElement> typeElements = ElementFilter.typesIn(roundEnv.
                getRootElements());
        generate(typeElements);
    }

    /**
     * Processes the types including inner interfaces.
     *
     * @param types
     *              the types to process.
     */
    private void generate(final Collection<? extends TypeElement> types) {
        for (TypeElement type : types) {
            try {
                generate(ElementFilter.typesIn(type.getEnclosedElements()));
                generate(type);
            } catch (IOException e) {
                logger().error(e, type);
                break;
            } catch (ValidationException e) {
                logger().error(e.getMessage(), e.getElement());
                break;
            } catch (Exception e) {
                logger().error(e, type);
                break;
            }
        }
    }

    private void generate(TypeElement type) throws IOException, Exception {
        final String interfaceName = processingEnv().getElementUtils().
                getBinaryName(type).toString();
        final MessageLogger messageLogger = type.getAnnotation(
                MessageLogger.class);
        final MessageBundle messageBundle = type.getAnnotation(
                MessageBundle.class);
        if (messageLogger != null) {
            if (type.getKind().isInterface() && !type.getModifiers().contains(
                    Modifier.PRIVATE)) {
                createClass(new MessageLoggerImplementor(logger(), interfaceName,
                        messageLogger.projectCode()), type);
            } else {
                logger().warn(
                        "Type %s must be an interface with at least package-private access. Skipping processing.",
                        interfaceName);
            }
        }
        if (messageBundle != null) {
            if (type.getKind().isInterface() && !type.getModifiers().contains(
                    Modifier.PRIVATE)) {
                createClass(new MessageBundleImplementor(logger(), interfaceName,
                        messageBundle.projectCode()), type);
            } else {
                logger().warn(
                        "Type %s must be an interface with at least package-private access. Skipping processing.",
                        interfaceName);
            }
        }

    }

    private void createClass(final ImplementationClassModel codeModel,
            final TypeElement type) throws IOException, Exception,
                                           ValidationException {
        // Process all extended interfaces.
        for (TypeMirror interfaceType : type.getInterfaces()) {
            for (ExecutableElement method : ElementFilter.methodsIn(
                    processingEnv().getTypeUtils().asElement(interfaceType).
                    getEnclosedElements())) {
                codeModel.addMethod(method);
            }
        }
        // Create the methods
        for (ExecutableElement method : ElementFilter.methodsIn(type.
                getEnclosedElements())) {
            codeModel.addMethod(method);
        }

        // Write the source file
        codeModel.create(filer().createSourceFile(codeModel.getClassName()));
    }
}
