/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2023 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
