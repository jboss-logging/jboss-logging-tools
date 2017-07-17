/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2017 Red Hat, Inc., and individual contributors
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

/**
 * Messages on reports can have a link to a {@linkplain ResolutionDoc resolution document}. This annotation can be used
 * to provide a base URL for these documents.
 * <p>
 * Expressions in the form of {@code ${property.key:default-value}} can be used for the values. If the property key is
 * prefixed with {@code sys.} a {@linkplain System#getProperty(String) system property} will be used. If the key is
 * prefixed with {@code env.} an {@linkplain System#getenv(String) environment variable} will be used. In all other cases
 * the {@code org.jboss.logging.tools.expressionProperties} processor argument is used to specify the path the properties
 * file which contains the values for the expressions.
 * </p>
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 * @since 1.2
 */
@Target(TYPE)
@Retention(CLASS)
@Documented
public @interface BaseUrl {

    /**
     * The base URL used for links to resolution documentation on reports. This can be a fully qualified URL or a
     * relative URL.
     *
     * @return the base URL
     */
    String value();
}
