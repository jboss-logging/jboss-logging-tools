/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.jboss.logging.processor.apt;

import static org.jboss.logging.processor.model.Parameter.ParameterType;
import static org.jboss.logging.processor.util.Objects.HashCodeBuilder;
import static org.jboss.logging.processor.util.Objects.areEqual;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import org.jboss.logging.processor.model.MessageMethod;
import org.jboss.logging.processor.model.Parameter;
import org.jboss.logging.processor.model.ThrowableType;
import org.jboss.logging.processor.util.Objects;

/**
 * Describes information about the return type.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
final class ThrowableTypeFactory {
    private ThrowableTypeFactory() {
    }

    /**
     * Creates a new descriptor that is not primitive.
     *
     * @param elements      the element utilities from the annotation processor.
     * @param types         the type utilities from the annotation process.
     * @param type          the class name of the return type.
     * @param messageMethod the message method.
     *
     * @return the return type descriptor.
     */
    public static ThrowableType forReturnType(final Elements elements, final Types types, final TypeMirror type, final MessageMethod messageMethod) {
        final AptReturnThrowableType result = new AptReturnThrowableType(elements, types, messageMethod, type);
        result.init();
        return result;
    }

    /**
     * Creates a new descriptor that is not primitive.
     *
     * @param elements the element utilities from the annotation processor.
     * @param types    the type utilities from the annotation process.
     * @param type     the class name of the return type.
     *
     * @return the return type descriptor.
     */
    public static ThrowableType of(final Elements elements, final Types types, final TypeMirror type) {
        final AptThrowableType result = new AptThrowableType(elements, types, type);
        result.init();
        return result;
    }

    private static class AptThrowableType extends AbstractMessageObjectType implements ThrowableType {

        private final TypeMirror type;
        private final boolean isChecked;
        private boolean defaultConstructor = false;
        private boolean stringConstructor = false;
        private boolean throwableConstructor = false;
        private boolean stringAndThrowableConstructor = false;
        private boolean throwableAndStringConstructor = false;
        protected final TypeMirror stringType;
        protected final TypeMirror throwableType;

        /**
         * Creates a new descriptor that is not primitive.
         *
         * @param types    the type utilities from the annotation processor.
         * @param elements the element utilities from the annotation processor.
         * @param type     the class name of the return type.
         */
        private AptThrowableType(final Elements elements, final Types types, final TypeMirror type) {
            super(elements, types, type);
            this.type = type;
            stringType = elements.getTypeElement(String.class.getName()).asType();
            throwableType = elements.getTypeElement(Throwable.class.getName()).asType();
            final TypeMirror runtimeException = elements.getTypeElement(RuntimeException.class.getName()).asType();
            final TypeMirror error = elements.getTypeElement(Error.class.getName()).asType();
            isChecked = !(types.isAssignable(runtimeException, type) && types.isAssignable(error, type));
        }

        /**
         * Initializes the object.
         */
        protected final void init() {
            if (!type.getKind().isPrimitive() && type.getKind() != TypeKind.VOID) {
                final Element element = types.asElement(type);
                final List<ExecutableElement> constructors = ElementFilter.constructorsIn(element.getEnclosedElements());
                for (ExecutableElement constructor : constructors) {
                    // Only allow public constructors
                    if (!constructor.getModifiers().contains(Modifier.PUBLIC)) {
                        continue;
                    }
                    final List<? extends VariableElement> params = constructor.getParameters();
                    switch (params.size()) {
                        case 0:
                            defaultConstructor = true;
                            break;
                        case 1:
                            if (types.isAssignable(params.get(0).asType(), stringType)) {
                                stringConstructor = true;
                            } else if (types.isAssignable(throwableType, params.get(0).asType())) {
                                throwableConstructor = true;
                            }
                            break;
                        case 2:
                            if (types.isAssignable(params.get(0).asType(), stringType)
                                    && types.isAssignable(throwableType, params.get(1).asType())) {
                                stringAndThrowableConstructor = true;
                            } else if (types.isAssignable(throwableType, params.get(0).asType())
                                    && types.isAssignable(params.get(1).asType(), stringType)) {
                                throwableAndStringConstructor = true;
                            }
                            break;
                    }
                    init(params);
                }
            }
        }

        /**
         * Allows for additional processing of parameters.
         *
         * @param params the parameters to be processed.
         */
        protected void init(final List<? extends VariableElement> params) {

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
            return false;
        }

        @Override
        public Set<Parameter> constructionParameters() {
            return Collections.emptySet();
        }

        @Override
        public boolean isChecked() {
            return isChecked;
        }

        @Override
        public String name() {
            return type.toString();
        }

        @Override
        public int hashCode() {
            return HashCodeBuilder.builder().add(type).toHashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof AptThrowableType)) {
                return false;
            }
            final AptThrowableType other = (AptThrowableType) obj;
            return areEqual(this.type, other.type);
        }

        @Override
        public String toString() {
            return Objects.ToStringBuilder.of(this)
                    .add("type", type)
                    .add("stringConstructor", stringConstructor)
                    .add("throwableConstructor", throwableConstructor)
                    .add("stringAndThrowableConstructor", stringAndThrowableConstructor)
                    .add("throwableAndStringConstructor", throwableAndStringConstructor).toString();
        }

        @Override
        public TypeMirror reference() {
            return type;
        }

        @Override
        public int compareTo(final ThrowableType o) {
            return name().compareTo(o.name());
        }
    }

    private static class AptReturnThrowableType extends AptThrowableType {

        private final MessageMethod messageMethod;

        private final Set<Parameter> constructionParameters;

        private boolean useConstructionParameters = false;

        /**
         * Creates a new descriptor that is not primitive.
         *
         * @param types         the type utilities from the annotation processor.
         * @param elements      the element utilities from the annotation processor.
         * @param messageMethod the message method.
         * @param type          the class name of the return type.
         */
        private AptReturnThrowableType(final Elements elements, final Types types, final MessageMethod messageMethod, final TypeMirror type) {
            super(elements, types, type);
            this.messageMethod = messageMethod;
            constructionParameters = new LinkedHashSet<Parameter>();
        }

        @Override
        protected void init(final List<? extends VariableElement> params) {
            // Check to see if message has @Param annotated arguments.
            if (!messageMethod.parameters(ParameterType.CONSTRUCTION).isEmpty() && !useConstructionParameters) {
                // Checks for the first constructor that can be used. The compiler will end-up determining the constructor
                // to use, so a best guess should work.
                final Iterator<Parameter> methodParameterIterator = messageMethod.parameters(ParameterType.CONSTRUCTION).iterator();
                final Set<Parameter> matchedParams = new LinkedHashSet<Parameter>();
                boolean match = false;
                boolean causeFound = false;
                boolean messageFound = false;
                for (VariableElement param : params) {
                    if (!causeFound && messageMethod.hasCause() && types.isAssignable(throwableType, param.asType())) {
                        causeFound = true;
                        matchedParams.add(messageMethod.cause());
                        continue;
                    }
                    if (!messageFound && types.isAssignable(param.asType(), stringType)) {
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
                }
            }
        }

        @Override
        public boolean useConstructionParameters() {
            return useConstructionParameters;
        }

        @Override
        public Set<Parameter> constructionParameters() {
            return constructionParameters;
        }
    }
}
