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

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Indicate that a method parameter value should be applied to a field on the resultant exception object.
 * <p>
 * If this annotation is placed on a method the {@linkplain #name() name} attribute becomes a required parameter and one
 * default attribute needs to be set. The value of the default attribute is used to set the filed on the resultant
 * exception object.
 * </p>
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@Retention(CLASS)
@Target({PARAMETER, METHOD})
@Repeatable(Fields.class)
@Documented
public @interface Field {

    /**
     * The field name.  If not specified, the parameter name is assumed to be the field name.
     * <p>
     * This becomes a required attrubyte if this annotation is present on a method.
     * </p>
     *
     * @return the field name
     */
    String name() default "";

    /**
     * The default {@code boolean} value if this annotation is used on a method.
     *
     * @return the default value to use
     */
    boolean booleanValue() default false;

    /**
     * The default boolean value if this annotation is used on a method.
     *
     * @return the default value to use
     */
    byte byteValue() default 0x00;

    /**
     * The default {@code byte} value if this annotation is used on a method.
     *
     * @return the default value to use
     */
    char charValue() default 0x00;

    /**
     * The default {@link Class} value if this annotation is used on a method.
     *
     * @return the default value to use
     */
    Class<?> classValue() default Object.class;

    /**
     * The default {@code double} value if this annotation is used on a method.
     *
     * @return the default value to use
     */
    double doubleValue() default 0.0d;

    /**
     * The default {@code float} value if this annotation is used on a method.
     *
     * @return the default value to use
     */
    float floatValue() default 0.0f;

    /**
     * The default {@code int} value if this annotation is used on a method.
     *
     * @return the default value to use
     */
    int intValue() default 0;

    /**
     * The default {@code long} value if this annotation is used on a method.
     *
     * @return the default value to use
     */
    long longValue() default 0L;

    /**
     * The default {@code short} value if this annotation is used on a method.
     *
     * @return the default value to use
     */
    short shortValue() default 0;

    /**
     * The default {@link String} value if this annotation is used on a method.
     *
     * @return the default value to use
     */
    String stringValue() default "";
}
