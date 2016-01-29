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

import static org.jboss.logging.processor.util.Objects.HashCodeBuilder;
import static org.jboss.logging.processor.util.Objects.areEqual;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.NoType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import org.jboss.logging.annotations.ConstructType;
import org.jboss.logging.processor.model.MessageMethod;
import org.jboss.logging.processor.model.Parameter;
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


    public static ReturnType of(final Elements elements, final Types types, final TypeMirror returnType, final MessageMethod method) {
        if (returnType.getKind() == TypeKind.VOID) {
            return VoidReturnType.getInstance(types);
        }
        final AptReturnType result = new AptReturnType(elements, types, returnType, method);
        result.init();
        return result;
    }

    /**
     * Implementation of return type.
     */
    private static class AptReturnType extends AbstractClassType implements ReturnType {
        private final Map<String, TypeMirror> fields;
        private final Map<String, TypeMirror> methods;
        private final TypeMirror returnType;
        private final MessageMethod method;
        private ThrowableType throwableType;

        AptReturnType(final Elements elements, final Types types, final TypeMirror returnType, final MessageMethod method) {
            super(elements, types, returnType);
            this.returnType = returnType;
            this.method = method;
            throwableType = null;
            fields = new LinkedHashMap<>();
            methods = new LinkedHashMap<>();
        }

        @Override
        public Element getDelegate() {
            return types.asElement(returnType);
        }

        @Override
        public boolean hasFieldFor(final Parameter parameter) {
            return fields.containsKey(parameter.targetName()) && checkType(parameter, fields.get(parameter.targetName()));
        }

        @Override
        public boolean hasMethodFor(final Parameter parameter) {
            return methods.containsKey(parameter.targetName()) && checkType(parameter, methods.get(parameter.targetName()));
        }

        @Override
        public boolean isThrowable() {
            return isSubtypeOf(Throwable.class);
        }

        @Override
        public boolean isPrimitive() {
            return returnType.getKind().isPrimitive();
        }

        @Override
        public String name() {
            return returnType.toString();
        }

        @Override
        public ThrowableType throwableReturnType() {
            return throwableType;
        }

        private void init() {
            if (isThrowable()) {
                TypeMirror returnType = this.returnType;
                if (ElementHelper.isAnnotatedWith(method, ConstructType.class)) {
                    final TypeElement constructTypeValue = ElementHelper.getClassAnnotationValue(method, ConstructType.class);
                    // Shouldn't be null
                    if (constructTypeValue == null) {
                        throw new ProcessingException(method, "Class not defined for the ConstructType");
                    }
                    returnType = constructTypeValue.asType();
                    if (!types.isAssignable(returnType, this.returnType)) {
                        throw new ProcessingException(method, "The requested type %s can not be assigned to %s.", returnType, this.returnType);
                    }
                }
                throwableType = ThrowableTypeFactory.forReturnType(elements, types, returnType, method);
            }
            final Element e = types.asElement(returnType);
            if (e instanceof TypeElement) {
                final List<ExecutableElement> returnTypeMethods = ElementFilter.methodsIn(elements.getAllMembers((TypeElement) e));
                for (ExecutableElement executableElement : returnTypeMethods) {
                    if (executableElement.getModifiers().contains(Modifier.PUBLIC) && executableElement.getParameters().size() == 1) {
                        methods.put(executableElement.getSimpleName().toString(), executableElement.getParameters().get(0).asType());
                    }
                }
                for (Element element : ElementFilter.fieldsIn(elements.getAllMembers((TypeElement) e))) {
                    if (element.getModifiers().contains(Modifier.PUBLIC) && !element.getModifiers().contains(Modifier.FINAL)) {
                        fields.put(element.getSimpleName().toString(), element.asType());
                    }
                }
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
                    .add("primitive", isPrimitive())
                    .add("throwable", isThrowable())
                    .add("throwableType", throwableType).toString();
        }

        private boolean checkType(final Parameter parameter, final TypeMirror type) {
            if (parameter.isPrimitive()) {
                if (type.getKind().isPrimitive()) {
                    return types.isSameType(parameter.asType(), type);
                }
                return types.isAssignable(types.unboxedType(parameter.asType()), type);
            }
            if (type.getKind().isPrimitive()) {
                final TypeElement primitiveType = types.boxedClass((PrimitiveType) type);
                return types.isAssignable(parameter.asType(), primitiveType.asType());
            }
            return types.isAssignable(parameter.asType(), type);
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
        public boolean hasFieldFor(final Parameter parameter) {
            return false;
        }

        @Override
        public boolean hasMethodFor(final Parameter parameter) {
            return false;
        }

        @Override
        public boolean isThrowable() {
            return false;
        }

        @Override
        public boolean isPrimitive() {
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
