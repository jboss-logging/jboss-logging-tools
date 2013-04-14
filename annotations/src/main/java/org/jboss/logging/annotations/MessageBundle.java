/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2010 Red Hat, Inc., and individual contributors
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
 * Signify that an interface is a message bundle interface.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
@Target(TYPE)
@Retention(CLASS)
@Documented
public @interface MessageBundle {

    /**
     * Get the project code for messages that have an associated code.  If no project code is associated
     * with this bundle, specify {@code ""} (the empty string).
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
}
