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

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Allows a link to be created for messages on a report which contain a possible resolution to the issue reported . If
 * the method does not include an {@linkplain Message#id() id} no link will be created for the report unless a
 * {@link #path()} is defined.
 *
 * <p>
 * The rules for building the URL are as follows:
 * <ul>
 * <li>{@link #url() url}: If left empty and the type is annotated with a {@link BaseUrl} the value for the
 * {@code @BaseUrl} will be used. If defined this will override the value of the {@link BaseUrl}. If neither are
 * defined the rules for the {@link #path()} will be followed.</li>
 * <li>{@link #path() path}: If defined this will be the path appended to the {@link #url()} or the value of the
 * {@link BaseUrl}. Note that neither {@link #url()} nor {@link BaseUrl} are required. If the value is left
 * undefined the id (project code plus the {@linkplain Message#id() message id}) will be used for the path.</li>
 * <li>{@link #suffix() suffix}: The suffix to append to the {@link #path()}. This is mostly useful if the path
 * is left undefined and a suffix should be appended to the messages id. If left undefined and the {@link #path()}
 * is not defined, the suffix will be determined based on the report type.</li>
 * </ul>
 * </p>
 *
 * <p>
 * If placed on a type links will be created for all methods on the type.
 * </p>
 *
 * <p>
 * Do note that the processor does not validate the resolution document exists. It simply attempts to create links to
 * the resolution document.
 * </p>
 * <p>
 * Expressions in the form of {@code ${property.key:default-value}} can be used for the values with the exception of the
 * {@link #skip() skip} attribute. If the property key is prefixed with {@code sys.} a
 * {@linkplain System#getProperty(String) system property} will be used. If the key is prefixed with {@code env.} an
 * {@linkplain System#getenv(String) environment variable} will be used. In all other cases the
 * {@code org.jboss.logging.tools.expressionProperties} processor argument is used to specify the path the properties
 * file which contains the values for the expressions.
 * </p>
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 * @since 1.2
 */
@Target({METHOD, TYPE})
@Retention(CLASS)
@Documented
public @interface ResolutionDoc {

    /**
     * The URL, fully qualified or relative, to use for the resolution document. If defined this will override the value
     * of the {@link BaseUrl} if the annotation is used.
     *
     * @return the URL or an empty string
     */
    String url() default "";

    /**
     * The path to the resolution document. If left undefined this will default to the message id.
     *
     * @return the path to the resolution document
     */
    String path() default "";

    /**
     * The suffix to append to the path. If left undefined this will default to the extension for the report type. For
     * example if the report type is {@code xml} the default suffix would be {@code .xml}.
     *
     * @return the suffix for the resolution document
     */
    String suffix() default "";

    /**
     * Allows the creation of a link to be skipped.
     *
     * @return {@code true} if creating the link should be skipped
     */
    boolean skip() default false;
}
