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

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Locale;

/**
 * Signify that an interface is a typed logger interface.  A message logger interface may optionally extend other
 * message logger interfaces and message bundle interfaces (see {@link org.jboss.logging.annotations.MessageBundle}, as
 * well as the {@link org.jboss.logging.BasicLogger} interface.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
@Retention(CLASS)
@Target(TYPE)
@Documented
public @interface MessageLogger {

    /**
     * Get the project code for messages that have an associated code.  If no project code is associated
     * with this logger, specify {@code ""} (the empty string).
     *
     * @return the project code
     */
    String projectCode();

    /**
     * The length of the padding used for each id in the message bundle. For example given the default padding length
     * of 6 and a message with an id of 100 would result would be {@code "000100"}.
     * <p/>
     * Valid values a range of 3 to 8. Any value less than 0 turns off padding. Any other value will result in an error
     * being produced.
     *
     * @return the length the id should be padded
     */
    int length() default 6;

    /**
     * Specifies the {@linkplain Locale locale} for formatting bundle messages. This is only used in the super
     * implementation. Subclasses will define their own locale to use based on the name of the resource bundle at
     * compile time.
     * <p>
     * An empty string will default to {@link Locale#ROOT}.
     * </p>
     * <p>
     * A non-empty string will be parsed by the {@link Locale#forLanguageTag(String)}. This uses the
     * <a href="https://tools.ietf.org/html/bcp47">IETF BCP 47</a> format.
     * </p>
     *
     * @return the default locale message bundles should use for formatting messages
     */
    String rootLocale() default "";
}
