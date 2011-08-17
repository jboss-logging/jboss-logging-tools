package org.jboss.logging.generator;

import org.jboss.logging.generator.util.ElementHelper;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Date: 29.07.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
final class MessageReturnTypeFactory {
    /**
     * Private constructor for factory.
     */
    private MessageReturnTypeFactory() {
    }


    public static MessageReturnType of(final Elements elements, final Types types, final TypeMirror returnType, final MessageMethod messageMethod) {
        if (returnType.getKind() == TypeKind.VOID) {
            return MessageReturnType.VOID;
        }
        final AptMessageReturnType result = new AptMessageReturnType(returnType, messageMethod);
        result.init(elements, types);
        return result;
    }

    /**
     * Implementation of return type.
     */
    private static class AptMessageReturnType implements MessageReturnType {
        private final TypeMirror returnType;
        private final MessageMethod messageMethod;
        private ThrowableReturnType throwableReturnType;

        private AptMessageReturnType(final TypeMirror returnType, final MessageMethod messageMethod) {
            this.returnType = returnType;
            this.messageMethod = messageMethod;
            throwableReturnType = null;
        }

        @Override
        public boolean isThrowable() {
            return ElementHelper.isAssignableFrom(Throwable.class, returnType);
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

        private void init(final Elements elements, final Types types) {
            if (isThrowable()) {
                throwableReturnType = ThrowableReturnTypeFactory.of(returnType, messageMethod, elements, types);
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
            if (!(obj instanceof AptMessageReturnType)) {
                return false;
            }
            final AptMessageReturnType other = (AptMessageReturnType) obj;
            return (name() == null ? other.name() == null : name().equals(other.name()));
        }

        @Override
        public TypeMirror reference() {
            return returnType;
        }
    }
}
