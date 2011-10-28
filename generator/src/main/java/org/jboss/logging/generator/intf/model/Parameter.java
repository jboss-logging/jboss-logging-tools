/*
 *  JBoss, Home of Professional Open Source Copyright 2011, Red Hat, Inc., and
 *  individual contributors by the @authors tag. See the copyright.txt in the
 *  distribution for a full listing of individual contributors.
 *
 *  This is free software; you can redistribute it and/or modify it under the
 *  terms of the GNU Lesser General Public License as published by the Free
 *  Software Foundation; either version 2.1 of the License, or (at your option)
 *  any later version.
 *
 *  This software is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 *  details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this software; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 *  site: http://www.fsf.org.
 */
package org.jboss.logging.generator.intf.model;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a> - 20.Feb.2011
 */
public interface Parameter extends Comparable<Parameter>, MessageObjectType {

    /**
     * The types of parameters.
     */
    public enum ParameterType {
        /**
         * Indicates the parameter can be any other type. All parameters fall under this category.
         */
        ANY,
        /**
         * Indicates the parameter is a cause parameter and needs to be set in the {@link Throwable throwable} return
         * type.
         */
        CAUSE,
        /**
         * Indicates the parameter should be used as a format parameter.
         */
        FORMAT,
        /**
         * Indicates the parameter should be used as the fully qualified class name for the logger.
         */
        FQCN,
        /**
         * Indicates the parameter is the message.
         */
        MESSAGE,
        /**
         * Indicates the parameter should be used in the construction of a {@link Throwable throwable} return type.
         */
        CONSTRUCTION,
        /**
         * Indicates the parameter is a instance field that should be set in the {@link Throwable throwable} return
         * type.
         */
        FIELD,
        /**
         * Indicates the parameter is a property and should be set via its setter in the {@link Throwable throwable}
         * return type.
         */
        PROPERTY
    }

    /**
     * The full type name of the parameter. For example
     * {@code java.lang.String} if the parameter is a string. If the
     * parameter is a primitive, the primitive name is returned.
     *
     * @return the qualified type of the parameter.
     */
    @Override
    String type();

    /**
     * The variable name of the parameter.
     *
     * @return the variable name of the parameter.
     */
    @Override
    String name();

    /**
     * Returns {@code true} if the type is an array, otherwise {@code false}.
     *
     * @return {@code true} if an array, otherwise {@code false}
     */
    boolean isArray();

    /**
     * Returns {@code true} if the type is a primitive type, otherwise {@code false}.
     *
     * @return {@code true} if primitive type, otherwise {@code false}
     */
    boolean isPrimitive();

    /**
     * Returns {@code true} if the parameter is a var args parameter, otherwise {@code false}.
     *
     * @return {@code true} if var args parameter, otherwise {@code false}.
     */
    boolean isVarArgs();

    /**
     * Returns the {@link ParameterType parameter type} of the parameter.
     *
     * @return the parameter type of the parameter.
     */
    ParameterType parameterType();

    /**
     * The formatter class, or {@code null} if there is none.
     *
     * @return the formatter class
     */
    String formatterClass();

    /**
     * Returns the class if the parameter is annotated with {@link org.jboss.logging.generator.Annotations#param()}.
     * If the annotation is not present, {@code null} is returned.
     *
     * @return the parameter class or {@code null}.
     */
    Class<?> paramClass();

    /**
     * Returns the name of the target field or method. For example if the {@link #parameterType()} returns
     * {@link ParameterType#FIELD}, the target name is the name of the field to set on the
     * {@link ReturnType return type}. If no target name is defined an empty String is returned.
     *
     * @return the target field name, method name or an empty string.
     */
    String targetName();
}
