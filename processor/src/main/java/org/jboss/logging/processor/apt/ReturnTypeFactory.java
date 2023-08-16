/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2023 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.logging.processor.apt;

import static org.jboss.logging.processor.util.Objects.HashCodeBuilder;
import static org.jboss.logging.processor.util.Objects.areEqual;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

import org.jboss.logging.annotations.ConstructType;
import org.jboss.logging.processor.model.MessageMethod;
import org.jboss.logging.processor.model.ReturnType;
import org.jboss.logging.processor.model.ThrowableType;
import org.jboss.logging.processor.util.ElementHelper;
import org.jboss.logging.processor.util.Objects;

/**
 * Date: 29.07.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
final class ReturnTypeFactory {
    /**
     * Private constructor for factory.
     */
    private ReturnTypeFactory() {
    }

    public static ReturnType of(final ProcessingEnvironment processingEnv, final TypeMirror returnType,
            final MessageMethod method) {
        if (returnType.getKind() == TypeKind.VOID) {
            return VoidReturnType.getInstance(processingEnv.getTypeUtils());
        }
        final AptReturnType result = new AptReturnType(processingEnv, returnType, method);
        result.init();
        return result;
    }

    /**
     * Implementation of return type.
     */
    private static class AptReturnType extends AbstractClassType implements ReturnType {
        private final TypeMirror returnType;
        private final MessageMethod method;
        private final Element delegate;
        private final TypeMirror resolvedType;
        private final boolean isThrowable;
        private ThrowableType throwableType;

        AptReturnType(final ProcessingEnvironment processingEnv, final TypeMirror returnType, final MessageMethod method) {
            super(processingEnv, returnType);
            this.returnType = returnType;
            this.method = method;
            delegate = types.asElement(returnType);
            throwableType = null;
            if (types.isSubtype(types.erasure(returnType), types.erasure(ElementHelper.toType(elements, Supplier.class)))) {
                final List<? extends TypeMirror> typeArgs = ElementHelper.getTypeArguments(returnType);
                if (typeArgs.isEmpty()) {
                    resolvedType = elements.getTypeElement(Object.class.getCanonicalName()).asType();
                } else {
                    resolvedType = typeArgs.get(0);
                }
            } else {
                resolvedType = returnType;
            }
            isThrowable = types.isSubtype(types.erasure(resolvedType), ElementHelper.toType(elements, Throwable.class));
        }

        @Override
        public Element getDelegate() {
            return delegate;
        }

        @Override
        public TypeMirror asType() {
            return returnType;
        }

        @Override
        public boolean isThrowable() {
            return isThrowable;
        }

        @Override
        public String name() {
            return types.erasure(returnType).toString();
        }

        @Override
        public ThrowableType throwableReturnType() {
            return throwableType;
        }

        @Override
        public TypeMirror resolvedType() {
            return resolvedType;
        }

        private void init() {
            if (isThrowable()) {
                // The resolved type needs to be used in cases where a Supplier is being returned
                TypeMirror throwableReturnType = resolvedType;
                if (method.isAnnotatedWith(ConstructType.class)) {
                    final TypeElement constructTypeValue = ElementHelper.getClassAnnotationValue(method, ConstructType.class);
                    // Shouldn't be null
                    if (constructTypeValue == null) {
                        throw new ProcessingException(method, "Class not defined for the ConstructType");
                    }
                    throwableReturnType = constructTypeValue.asType();
                    if (!types.isAssignable(throwableReturnType, resolvedType)) {
                        throw new ProcessingException(method, "The requested type %s can not be assigned to %s.",
                                throwableReturnType, resolvedType);
                    }
                }
                throwableType = ThrowableTypeFactory.forReturnType(processingEnv, throwableReturnType, method);
            }
        }

        @Override
        public int hashCode() {
            return HashCodeBuilder.builder().add(name()).toHashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof AptReturnType)) {
                return false;
            }
            final AptReturnType other = (AptReturnType) obj;
            return areEqual(name(), other.name());
        }

        @Override
        public String toString() {
            return Objects.ToStringBuilder.of(this)
                    .add("name", name())
                    .add("throwable", isThrowable())
                    .add("throwableType", throwableType).toString();
        }
    }

    private static class VoidReturnType implements ReturnType {
        private static VoidReturnType INSTANCE = null;
        private final Element voidElement;
        private final NoType voidType;
        private final int hash;

        private VoidReturnType(final Types types) {
            voidType = types.getNoType(TypeKind.VOID);
            voidElement = types.asElement(voidType);
            hash = "void".hashCode();
        }

        private static synchronized VoidReturnType getInstance(final Types types) {
            if (INSTANCE == null) {
                INSTANCE = new VoidReturnType(types);
            }
            return INSTANCE;
        }

        @Override
        public TypeMirror asType() {
            return voidType;
        }

        @Override
        public boolean isThrowable() {
            return false;
        }

        @Override
        public String name() {
            return "void";
        }

        @Override
        public ThrowableType throwableReturnType() {
            return null;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(final Object obj) {
            return obj == this || obj instanceof VoidReturnType;
        }

        @Override
        public String toString() {
            return "void";
        }

        @Override
        public boolean isAssignableFrom(final Class<?> type) {
            return type == Void.class || type == void.class;
        }

        @Override
        public boolean isSubtypeOf(final Class<?> type) {
            return false;
        }

        @Override
        public boolean isSameAs(final Class<?> type) {
            return type == Void.class || type == void.class;
        }

        @Override
        public Element getDelegate() {
            return voidElement;
        }
    }
}
