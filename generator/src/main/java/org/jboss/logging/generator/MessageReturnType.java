package org.jboss.logging.generator;

import javax.lang.model.element.ExecutableElement;

/**
 * Date: 29.07.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public interface MessageReturnType extends MessageObject {

    public static final MessageReturnType VOID = new VoidReturnType();

    /**
     * Checks to see if the return type is an exception, extends Throwable.
     *
     * @return {@code true} if the return type is an exception, otherwise
     *         {@code false}.
     */
    boolean isThrowable();

    /**
     * Indicates whether or not the return type is a primitive.
     *
     * @return {@code true} if a primitive, otherwise {@code false}.
     */
    boolean isPrimitive();


    /**
     * Returns a string version of the return type.
     *
     * @return a string version of the return type.
     */
    String qualifiedClassName();

    /**
     * Returns the exception return type if {@link #isThrowable()} returns {@code true}. Otherwise {@code null} is
     * returned.
     *
     * @return an exception return type, otherwise {@code null}.
     */
    ThrowableReturnType throwableReturnType();

    /**
     * Default type if the return type is void.
     */
    public final static class VoidReturnType implements MessageReturnType {

        private VoidReturnType() {
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
        public String qualifiedClassName() {
            return "void";
        }

        @Override
        public ThrowableReturnType throwableReturnType() {
            return null;
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
            if (!(obj instanceof VoidReturnType)) {
                return false;
            }
            final VoidReturnType other = (VoidReturnType) obj;
            return (qualifiedClassName() == null ? other.qualifiedClassName() == null : qualifiedClassName().equals(other.qualifiedClassName()));
        }

        @Override
        public Class<Void> reference() {
            return Void.TYPE;
        }
    }
}
