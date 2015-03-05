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
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 * @since 1.1.0
 */
@Retention(CLASS)
@Target(PARAMETER)
@Documented
public @interface Pos {

    /**
     * The positions the value should be used at.
     *
     * @return an array of the positions for the parameter
     */
    int[] value();

    /**
     * The transform types used on the parameter.
     *
     * @return an array of the transformer types
     *
     * @see Transform
     */
    Transform[] transform() default {};
}
