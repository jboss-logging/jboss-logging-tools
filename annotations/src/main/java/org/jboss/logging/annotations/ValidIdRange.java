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

/**
 * Sets a range of valid id's allowed on the {@link org.jboss.logging.annotations.Message#id() message id}. Both {@link
 * Message#INHERIT} and {@link Message#NONE} are ignored when validating.
 * <p/>
 * <b>Note:</b> Message id's from inherited interfaces are not validated within the range provided. Super interfaces
 * would need their own annotation for range validation.
 * <p/>
 * <code>
 * <pre>
 *          &#64;MessageLogger(projectCode = "EXAMPLE")
 *          &#64;ValidIdRange(min = 100, max = 200)
 *          public interface ExampleLogger {
 *
 *              &#64;LogMessage
 *              &#64;Message(id = 100, value = "Example message")
 *              void example();
 *          }
 * </pre>
 * </code>
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@Target(TYPE)
@Retention(CLASS)
@Documented
public @interface ValidIdRange {

    /**
     * The minimum id allowed in the {@link org.jboss.logging.annotations.Message#id() message id}. Both {@link
     * Message#INHERIT} and {@link Message#NONE} are ignored when validating.
     *
     * @return the minimum id allowed
     */
    int min() default 1;

    /**
     * The maximum id allowed in the {@link org.jboss.logging.annotations.Message#id() message id}. Both {@link
     * Message#INHERIT} and {@link Message#NONE} are ignored when validating.
     *
     * @return the maximum id allowed
     */
    int max() default 999999;
}
