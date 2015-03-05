/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2015 Red Hat, Inc., and individual contributors
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

package org.jboss.logging.annotations;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Indicate the given parameter should be transformed in each of the {@link org.jboss.logging.annotations.Transform.TransformType transform types}
 * provided. The parameter cannot be a primitive type.
 * <p/>
 * For the {@link TransformType#SIZE} type, the object must be a {@link String}, a {@link java.util.Collection}, a
 * {@link java.util.Map} or an array.
 * <p/>
 * The type {@link TransformType#GET_CLASS} can be used with {@link TransformType#HASH_CODE} or {@link
 * TransformType#IDENTITY_HASH_CODE}. The type {@link TransformType#SIZE} must be used on it's own.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 * @since 1.1.0
 */
@Retention(CLASS)
@Target(PARAMETER)
@Documented
public @interface Transform {

    /**
     * The transform type
     */
    public enum TransformType {
        /**
         * Gets the class of the object object passed, {@link Object#getClass()}.
         */
        GET_CLASS,
        /**
         * Gets the hash code of the object, {@link Object#hashCode()}.
         */
        HASH_CODE,
        /**
         * Gets the identity hash code of the object, {@link System#identityHashCode(Object)}.
         */
        IDENTITY_HASH_CODE,
        /**
         * Gets the size or length of a {@link String}, {@link java.util.Collection}, {@link java.util.Map} or array.
         */
        SIZE,
    }

    /**
     * The transform types used on the parameter.
     * <p/>
     * Valid combinations:
     * <ul>
     * <li>{@link TransformType#GET_CLASS}</li>
     * <li>{@link TransformType#GET_CLASS}, {@link TransformType#HASH_CODE}</li>
     * <li>{@link TransformType#GET_CLASS}, {@link TransformType#IDENTITY_HASH_CODE}</li>
     * <li>{@link TransformType#HASH_CODE}</li>
     * <li>{@link TransformType#IDENTITY_HASH_CODE}</li>
     * <li>{@link TransformType#SIZE}</li>
     * </ul>
     *
     * @return an array of the transform types
     */
    TransformType[] value();
}
