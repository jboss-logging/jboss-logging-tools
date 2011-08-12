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
package org.jboss.logging.generator;

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

import static org.jboss.logging.generator.util.ElementHelper.isAssignableFrom;

/**
 * Describes information about the return type.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
final class ThrowableReturnTypeFactory {

    /**
     * Creates a new descriptor that is not primitive.
     *
     * @param returnType    the class name of the return type.
     * @param messageMethod the message method.
     * @param typeUtil      the type utilities.
     *
     * @return the return type descriptor.
     */
    public static ThrowableReturnType of(final TypeMirror returnType, final MessageMethod messageMethod, final Elements elementUtil, final Types typeUtil) {
        final ThrowableReturnTypeImpl result = new ThrowableReturnTypeImpl(messageMethod, returnType);
        result.init(typeUtil, elementUtil);
        return result;
    }

    private static class ThrowableReturnTypeImpl implements ThrowableReturnType {

        private final TypeMirror returnType;

        private final MessageMethod messageMethod;

        private boolean defaultConstructor = false;

        private boolean stringConstructor = false;

        private boolean throwableConstructor = false;

        private boolean stringAndThrowableConstructor = false;

        private boolean throwableAndStringConstructor = false;

        private boolean useConstructionParameters = false;

        private Set<MethodParameter> constructionParameters;

        /**
         * Creates a new descriptor that is not primitive.
         *
         * @param messageMethod the message method.
         * @param returnType    the class name of the return type.
         */
        private ThrowableReturnTypeImpl(final MessageMethod messageMethod, final TypeMirror returnType) {
            this.returnType = returnType;
            this.messageMethod = messageMethod;
            constructionParameters = new LinkedHashSet<MethodParameter>();
        }

        /**
         * Initializes the object.
         *
         * @param typeUtil the type utilities.
         */
        private void init(final Types typeUtil, final Elements elementUtil) {
            if (!returnType.getKind().isPrimitive() && returnType.getKind() != TypeKind.VOID) {
                final Element element = typeUtil.asElement(returnType);
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
                                if (isAssignableFrom(params.get(0).asType(), String.class)) {
                                    stringConstructor = true;
                                } else if (isAssignableFrom(Throwable.class, params.get(0).asType())) {
                                    throwableConstructor = true;
                                }
                                break;
                            case 2:
                                if (isAssignableFrom(params.get(0).asType(), String.class)
                                        && isAssignableFrom(Throwable.class, params.get(1).asType())) {
                                    stringAndThrowableConstructor = true;
                                } else if (isAssignableFrom(Throwable.class, params.get(0).asType())
                                        && isAssignableFrom(params.get(1).asType(), String.class)) {
                                    throwableAndStringConstructor = true;
                                }
                                break;
                        }
                    } else {
                        // Checks for the first constructor that can be used. The compiler will end-up determining the constructor
                        // to use, so a best guess should work.
                        final Iterator<MethodParameter> methodParameterIterator = messageMethod.constructorParameters().iterator();
                        final Set<MethodParameter> matchedParams = new LinkedHashSet<MethodParameter>();
                        boolean match = false;
                        boolean causeFound = false;
                        boolean messageFound = false;
                        for (VariableElement param : params) {
                            if (!causeFound && messageMethod.hasCause() && isAssignableFrom(Throwable.class, param.asType())) {
                                causeFound = true;
                                matchedParams.add(messageMethod.cause());
                                continue;
                            }
                            if (!messageFound && isAssignableFrom(param.asType(), String.class)) {
                                messageFound = true;
                                matchedParams.add(MethodParameterFactory.forMessageMethod(messageMethod));
                                continue;
                            }

                            if (methodParameterIterator.hasNext()) {
                                final MethodParameter methodParameter = methodParameterIterator.next();
                                if (methodParameter.reference() instanceof VariableElement) {
                                    final VariableElement refType = (VariableElement) methodParameter.reference();
                                    match = typeUtil.isAssignable(refType.asType(), param.asType());
                                }
                                if (match) {
                                    matchedParams.add(methodParameter);
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
        public Set<MethodParameter> constructionParameters() {
            return constructionParameters;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((returnType == null) ? 0 : returnType.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof ThrowableReturnTypeImpl)) {
                return false;
            }
            final ThrowableReturnTypeImpl other = (ThrowableReturnTypeImpl) obj;
            if ((this.returnType == null) ? other.returnType != null : this.returnType.equals(other.returnType)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            final StringBuilder result = new StringBuilder();
            result.append(getClass().getName()).
                    append("(returnType=").
                    append(returnType).
                    append(", stringConstructor=").
                    append(stringConstructor).
                    append(", throwableConstructor=").
                    append(throwableConstructor).
                    append(", stringAndThrowableConstructor=").
                    append(stringAndThrowableConstructor).
                    append(", throwableAndStringConstructor=").
                    append(throwableAndStringConstructor).
                    append(")");
            return result.toString();
        }

        @Override
        public TypeMirror reference() {
            return returnType;
        }
    }
}
