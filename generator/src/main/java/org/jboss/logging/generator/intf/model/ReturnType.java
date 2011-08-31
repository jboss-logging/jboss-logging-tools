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
    ThrowableReturnType throwableReturnType();

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
        public ThrowableReturnType throwableReturnType() {
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
    }

    /**
     * Date: 01.08.2011
     *
     * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
     */
    interface ThrowableReturnType extends MessageObject, MessageObjectType {

        /**
         * Checks to see the throwable has a default constructor.
         *
         * @return {@code true} if the throwable has a default constructor, otherwise {@code false}.
         */
        boolean hasDefaultConstructor();

        /**
         * Checks to see if the throwable has a string and throwable ({@code Throwable(String, Throwable)}) constructor.
         *
         * @return {@code true} if the throwable has both a string and throwable constructor, otherwise {@code false}.
         */
        boolean hasStringAndThrowableConstructor();

        /**
         * Checks to see if the throwable has a string ({@code Throwable(String)}) constructor.
         * <p/>
         * If {@code true}, {@link Throwable#initCause(Throwable)} can be used to set the throwable.
         *
         * @return {@code true} if the throwable has a string constructor, otherwise {@code false}.
         */
        boolean hasStringConstructor();

        /**
         * Checks to see if the throwable has a throwable and string ({@code Throwable(Throwable, String)}) constructor.
         *
         * @return {@code true} if the throwable has both a throwable and string constructor, otherwise {@code false}.
         */
        boolean hasThrowableAndStringConstructor();

        /**
         * Checks to see if the throwable has a string and throwable ({@code Throwable(String, Throwable)}) constructor.
         *
         * @return {@code true} if the throwable has a throwable constructor, otherwise {@code false}.
         */
        boolean hasThrowableConstructor();

        /**
         * Checks to see if the throwable has and can use a custom constructor.
         * <p/>
         * If {@code true}, the constructor parameters can be retrieved from the {@link #constructionParameters()} method.
         *
         * @return {@code true} if the throwable has a custom constructor that can be used, otherwise {@code false}.
         */
        boolean useConstructionParameters();

        /**
         * The parameters needed to construct the throwable, if not using the default constructor. If the default
         * constructor should be used an empty set should be returned.
         * <p/>
         * The order the set is returned is the order in which the parameters must be in for the constructor.
         *
         * @return a set of construction parameters or an empty set.
         */
        Set<Parameter> constructionParameters();


        /**
         * Returns the qualified class name of the return type.
         *
         * @return the qualified class name fo the return type.
         */
        @Override
        String name();

    }
}
