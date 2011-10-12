package org.jboss.logging.generator.intf.model;

import org.jboss.logging.generator.util.Objects;

import java.util.Set;

import static org.jboss.logging.generator.util.Objects.HashCodeBuilder;
import static org.jboss.logging.generator.util.Objects.ToStringBuilder;
import static org.jboss.logging.generator.util.Objects.areEqual;

/**
 * Date: 29.07.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public interface ReturnType extends MessageObject, MessageObjectType {

    public static final ReturnType VOID = new VoidReturnType();

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
     * Returns the qualified class name of the return type.
     *
     * @return the qualified class name fo the return type.
     */
    @Override
    String name();

    /**
     * Returns the exception return type if {@link #isThrowable()} returns {@code true}. Otherwise {@code null} is
     * returned.
     *
     * @return an exception return type, otherwise {@code null}.
     */
    ThrowableType throwableReturnType();

    /**
     * Default type if the return type is void.
     */
    final static class VoidReturnType implements ReturnType {

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
        public String name() {
            return "void";
        }

        @Override
        public ThrowableType throwableReturnType() {
            return null;
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
            if (!(obj instanceof VoidReturnType)) {
                return false;
            }
            final VoidReturnType other = (VoidReturnType) obj;
            return areEqual(this.name(), other.name());
        }

        @Override
        public String toString() {
            return ToStringBuilder.of(this).add(name()).toString();
        }

        @Override
        public Class<Void> reference() {
            return Void.TYPE;
        }

        @Override
        public boolean isAssignableFrom(final Class<?> type) {
            return type.equals(Void.TYPE);
        }

        @Override
        public boolean isSubtypeOf(final Class<?> type) {
            return false;
        }

        @Override
        public boolean isSameAs(final Class<?> type) {
            return type.equals(Void.TYPE);
        }
    }
}
