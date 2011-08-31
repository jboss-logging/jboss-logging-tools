/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.logging.generator.apt;

import org.jboss.logging.generator.intf.model.Method;
import org.jboss.logging.generator.intf.model.Parameter;
import org.jboss.logging.generator.intf.model.ReturnType;
import org.jboss.logging.generator.util.ElementHelper;
import org.jboss.logging.generator.util.Objects;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.jboss.logging.generator.util.Objects.HashCodeBuilder;
import static org.jboss.logging.generator.util.Objects.areEqual;

/**
 * Describes information about the return type.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
final class ThrowableReturnTypeFactory {

    /**
     * Creates a new descriptor that is not primitive.
     *
     * @param elements      the element utilities from the annotation processor.
     * @param types         the type utilities from the annotation process.
     * @param returnType    the class name of the return type.
     * @param messageMethod the message method.
     *
     * @return the return type descriptor.
     */
    public static ReturnType.ThrowableReturnType of(final Elements elements, final Types types, final TypeMirror returnType, final Method messageMethod) {
        final AptThrowableReturnType result = new AptThrowableReturnType(elements, types, messageMethod, returnType);
        result.init();
        return result;
    }

    private static class AptThrowableReturnType implements ReturnType.ThrowableReturnType {

        private final Elements elements;

        private final Types types;

        private final TypeMirror returnType;

        private final Method messageMethod;

        private boolean defaultConstructor = false;

        private boolean stringConstructor = false;

        private boolean throwableConstructor = false;

        private boolean stringAndThrowableConstructor = false;

        private boolean throwableAndStringConstructor = false;

        private boolean useConstructionParameters = false;

        private final Set<Parameter> constructionParameters;

        /**
         * Creates a new descriptor that is not primitive.
         *
         * @param types         the type utilities from the annotation processor.
         * @param elements      the element utilities from the annotation processor.
         * @param messageMethod the message method.
         * @param returnType    the class name of the return type.
         */
        private AptThrowableReturnType(final Elements elements, final Types types, final Method messageMethod, final TypeMirror returnType) {
            this.elements = elements;
            this.types = types;
            this.returnType = returnType;
            this.messageMethod = messageMethod;
            constructionParameters = new LinkedHashSet<Parameter>();
        }

        /**
         * Initializes the object.
         */
        private void init() {
            if (!returnType.getKind().isPrimitive() && returnType.getKind() != TypeKind.VOID) {
                final Element element = types.asElement(returnType);
                final List<ExecutableElement> constructors = ElementFilter.constructorsIn(element.getEnclosedElements());
                for (ExecutableElement constructor : constructors) {
                    // Only allow public constructors
                    if (!constructor.getModifiers().contains(Modifier.PUBLIC)) {
                        continue;
                    }
                    final List<? extends VariableElement> params = constructor.getParameters();
                    // Check to see if message has @Param annotated arguments.
                    if (messageMethod.constructorParameters().isEmpty()) {
                        switch (params.size()) {
                            case 0:
                                defaultConstructor = true;
                                break;
                            case 1:
                                if (ElementHelper.isAssignableFrom(params.get(0).asType(), String.class)) {
                                    stringConstructor = true;
                                } else if (ElementHelper.isAssignableFrom(Throwable.class, params.get(0).asType())) {
                                    throwableConstructor = true;
                                }
                                break;
                            case 2:
                                if (ElementHelper.isAssignableFrom(params.get(0).asType(), String.class)
                                        && ElementHelper.isAssignableFrom(Throwable.class, params.get(1).asType())) {
                                    stringAndThrowableConstructor = true;
                                } else if (ElementHelper.isAssignableFrom(Throwable.class, params.get(0).asType())
                                        && ElementHelper.isAssignableFrom(params.get(1).asType(), String.class)) {
                                    throwableAndStringConstructor = true;
                                }
                                break;
                        }
                    } else {
                        // Checks for the first constructor that can be used. The compiler will end-up determining the constructor
                        // to use, so a best guess should work.
                        final Iterator<Parameter> methodParameterIterator = messageMethod.constructorParameters().iterator();
                        final Set<Parameter> matchedParams = new LinkedHashSet<Parameter>();
                        boolean match = false;
                        boolean causeFound = false;
                        boolean messageFound = false;
                        for (VariableElement param : params) {
                            if (!causeFound && messageMethod.hasCause() && ElementHelper.isAssignableFrom(Throwable.class, param.asType())) {
                                causeFound = true;
                                matchedParams.add(messageMethod.cause());
                                continue;
                            }
                            if (!messageFound && ElementHelper.isAssignableFrom(param.asType(), String.class)) {
                                messageFound = true;
                                matchedParams.add(ParameterFactory.forMessageMethod(messageMethod));
                                continue;
                            }

                            if (methodParameterIterator.hasNext()) {
                                final Parameter parameter = methodParameterIterator.next();
                                if (parameter.reference() instanceof VariableElement) {
                                    final VariableElement refType = (VariableElement) parameter.reference();
                                    match = types.isAssignable(refType.asType(), param.asType());
                                }
                                if (match) {
                                    matchedParams.add(parameter);
                                }
                            }
                            // Short circuit
                            if (!match) break;
                        }

                        if (match) {
                            useConstructionParameters = true;
                            constructionParameters.addAll(matchedParams);
                            break;
                        }
                    }
                }
            }
        }

        @Override
        public boolean hasDefaultConstructor() {
            return defaultConstructor;
        }

        @Override
        public boolean hasStringAndThrowableConstructor() {
            return stringAndThrowableConstructor;
        }

        @Override
        public boolean hasStringConstructor() {
            return stringConstructor;
        }

        @Override
        public boolean hasThrowableAndStringConstructor() {
            return throwableAndStringConstructor;
        }

        @Override
        public boolean hasThrowableConstructor() {
            return throwableConstructor;
        }

        @Override
        public boolean useConstructionParameters() {
            return useConstructionParameters;
        }

        @Override
        public Set<Parameter> constructionParameters() {
            return constructionParameters;
        }

        @Override
        public String name() {
            return returnType.toString();
        }

        @Override
        public int hashCode() {
            return HashCodeBuilder.builder().add(returnType).toHashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof AptThrowableReturnType)) {
                return false;
            }
            final AptThrowableReturnType other = (AptThrowableReturnType) obj;
            return areEqual(this.returnType, other.returnType);
        }

        @Override
        public String toString() {
            return Objects.ToStringBuilder.of(this)
                    .add("returnType", returnType)
                    .add("stringConstructor", stringConstructor)
                    .add("throwableConstructor", throwableConstructor)
                    .add("stringAndThrowableConstructor", stringAndThrowableConstructor)
                    .add("throwableAndStringConstructor", throwableAndStringConstructor).toString();
        }

        @Override
        public TypeMirror reference() {
            return returnType;
        }

        @Override
        public boolean isAssignableFrom(final Class<?> type) {
            final TypeMirror typeMirror = elements.getTypeElement(type.getName()).asType();
            return types.isAssignable(typeMirror, returnType);
        }

        @Override
        public boolean isSubtypeOf(final Class<?> type) {
            final TypeMirror typeMirror = elements.getTypeElement(type.getName()).asType();
            return types.isSubtype(returnType, typeMirror);
        }
    }
}
