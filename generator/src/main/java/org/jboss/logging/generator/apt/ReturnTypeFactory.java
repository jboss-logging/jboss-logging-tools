package org.jboss.logging.generator.apt;

import org.jboss.logging.generator.intf.model.Method;
import org.jboss.logging.generator.intf.model.ReturnType;
import org.jboss.logging.generator.intf.model.ThrowableType;
import org.jboss.logging.generator.util.Objects;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static org.jboss.logging.generator.util.Objects.HashCodeBuilder;
import static org.jboss.logging.generator.util.Objects.areEqual;

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


    public static ReturnType of(final Elements elements, final Types types, final TypeMirror returnType, final Method messageMethod) {
        if (returnType.getKind() == TypeKind.VOID) {
            return ReturnType.VOID;
        }
        final AptReturnType result = new AptReturnType(elements, types, returnType, messageMethod);
        result.init();
        return result;
    }

    /**
     * Implementation of return type.
     */
    private static class AptReturnType implements ReturnType {
        private final Elements elements;
        private final Types types;
        private final TypeMirror returnType;
        private final Method messageMethod;
        private ThrowableType throwableType;

        private AptReturnType(final Elements elements, final Types types, final TypeMirror returnType, final Method messageMethod) {
            this.elements = elements;
            this.types = types;
            this.returnType = returnType;
            this.messageMethod = messageMethod;
            throwableType = null;
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
                throwableType = ThrowableTypeFactory.forReturnType(elements, types, returnType, messageMethod);
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
