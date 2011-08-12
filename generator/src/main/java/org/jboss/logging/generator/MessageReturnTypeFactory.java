package org.jboss.logging.generator;

import org.jboss.logging.generator.util.ElementHelper;

import javax.lang.model.element.ExecutableElement;
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
        final MessageReturnTypeImpl result = new MessageReturnTypeImpl(returnType, messageMethod);
        result.init(elements, types);
        return result;
    }

    /**
     * Implementation of return type.
     */
    private static class MessageReturnTypeImpl implements MessageReturnType {
        private final TypeMirror returnType;
        private final MessageMethod messageMethod;
        private ThrowableReturnType throwableReturnType;

        private MessageReturnTypeImpl(final TypeMirror returnType, final MessageMethod messageMethod) {
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
        public String qualifiedClassName() {
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
            return prime * result + (qualifiedClassName() == null ? 0 : qualifiedClassName().hashCode());
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof MessageReturnTypeImpl)) {
                return false;
            }
            final MessageReturnTypeImpl other = (MessageReturnTypeImpl) obj;
            return (qualifiedClassName() == null ? other.qualifiedClassName() == null : qualifiedClassName().equals(other.qualifiedClassName()));
        }

        @Override
        public TypeMirror reference() {
            return returnType;
        }
    }
}
