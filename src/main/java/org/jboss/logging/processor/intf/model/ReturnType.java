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

package org.jboss.logging.processor.intf.model;

import static org.jboss.logging.processor.util.Objects.HashCodeBuilder;
import static org.jboss.logging.processor.util.Objects.ToStringBuilder;
import static org.jboss.logging.processor.util.Objects.areEqual;

/**
 * Date: 29.07.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public interface ReturnType extends MessageObject, MessageObjectType {

    public static final ReturnType VOID = new VoidReturnType();

    /**
     * Checks to see if the return type has a field with the name with the same name and type as the
     * {@link Parameter parameter}.
     *
     * @param parameter the parameter to check.
     *
     * @return {@code true} if the field exists, is accessible,  mutable and is assignable from the type otherwise
     *         {@code false}.
     */
    boolean hasFieldFor(final Parameter parameter);

    /**
     * Checks to see if the return type has a method with the name with the same name and parameter type as the
     * {@link Parameter parameter}.
     *
     * @param parameter the parameter to check.
     *
     * @return {@code true} if the method exists, is accessible and its parameter is assignable from the type, otherwise
     *         {@code false}.
     */
    boolean hasMethodFor(final Parameter parameter);

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
        public String type() {
            return "void";
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
