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

package org.jboss.logging.processor.model;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a> - 20.Feb.2011
 */
public interface Parameter extends Comparable<Parameter>, ClassType, DelegatingElement {

    /**
     * The variable name of the parameter.
     *
     * @return the variable name of the parameter.
     */
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
     * Indicates whether or not the parameter is used a format parameter for the message.
     *
     * @return {@code true} if this parameter that should used as a format parameter for the message
     */
    default boolean isFormatParameter() {
        return true;
    }

    /**
     * Indicates whether or not this parameter represents the message method.
     *
     * @return {@code true} if this is the message method parameter
     */
    default boolean isMessageMethod() {
        return false;
    }

    /**
     * The formatter class, or {@code null} if there is none.
     *
     * @return the formatter class
     */
    String formatterClass();

    /**
     * Returns the name of the target field or method. For example if the parameter is annotated with
     * {@link org.jboss.logging.annotations.Field @Field} the target name is the name of the field to set on the
     * {@link org.jboss.logging.processor.model.ReturnType return type}. If no target name is defined an empty String
     * is returned.
     *
     * @return the target field name, method name or an empty string.
     */
    String targetName();
}
