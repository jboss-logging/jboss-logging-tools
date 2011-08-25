package org.jboss.logging.generator.apt;

import org.jboss.logging.generator.intf.model.Method;
import org.jboss.logging.generator.intf.model.ReturnType;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

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
        private ThrowableReturnType throwableReturnType;

        private AptReturnType(final Elements elements, final Types types, final TypeMirror returnType, final Method messageMethod) {
            this.elements = elements;
            this.types = types;
            this.returnType = returnType;
            this.messageMethod = messageMethod;
            throwableReturnType = null;
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
        public ThrowableReturnType throwableReturnType() {
            return throwableReturnType;
        }

        private void init() {
            if (isThrowable()) {
                throwableReturnType = ThrowableReturnTypeFactory.of(elements, types, returnType, messageMethod);
            }
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            return prime * result + (name() == null ? 0 : name().hashCode());
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
            return (name() == null ? other.name() == null : name().equals(other.name()));
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
