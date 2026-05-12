/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2026 Red Hat, Inc., and individual contributors
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
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Indicates a log message should be throttled, limiting how often it is emitted.
 * <p>
 * Three modes are supported:
 * </p>
 * <ul>
 * <li><b>Count-based:</b> log every {@code n}-th invocation. Set {@link #count()} to a value greater than 0.</li>
 * <li><b>Time-based:</b> log at most once per time period. Set {@link #period()} and optionally {@link #unit()}.</li>
 * <li><b>Combined:</b> both count and period must be satisfied before the message is emitted.</li>
 * </ul>
 * <p>
 * At least one of {@link #count()} or {@link #period()} must be greater than 0.
 * </p>
 * <p>
 * Methods that use this annotation must be {@linkplain LogMessage logger methods}. Overloaded
 * methods also annotated with {@code @Throttled} will share the same throttle state, only logging the message
 * when the throttle condition is met regardless of which overload is invoked.
 * </p>
 * <p>
 * This annotation is mutually exclusive with {@link Once}.
 * </p>
 *
 * @author Tristan Tarrant
 */
@Target(METHOD)
@Retention(CLASS)
@Documented
public @interface Throttled {

    /**
     * The number of invocations between each log emission. A value of {@code n} means every {@code n}-th call
     * will be logged. A value of 0 (default) disables count-based throttling.
     *
     * @return the count interval
     */
    long count() default 0;

    /**
     * The minimum time period between log emissions. A value of 0 (default) disables time-based throttling.
     *
     * @return the time period
     */
    long period() default 0;

    /**
     * The time unit for {@link #period()}. Defaults to {@link TimeUnit#SECONDS}.
     *
     * @return the time unit
     */
    TimeUnit unit() default TimeUnit.SECONDS;
}
