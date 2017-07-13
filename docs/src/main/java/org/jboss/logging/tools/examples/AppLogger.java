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

package org.jboss.logging.tools.examples;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.logging.annotations.Once;
import org.jboss.logging.annotations.ResolutionDoc;
import org.jboss.logging.annotations.Transform;

/**
 * A common logger for the application.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@SuppressWarnings({"unused", "SameParameterValue"})
@MessageLogger(projectCode = "CW")
@ResolutionDoc(url = "errors", suffix = ".html")
public interface AppLogger extends BasicLogger {

    AppLogger LOGGER = Logger.getMessageLogger(AppLogger.class, AppLogger.class.getPackage().getName());

    /**
     * Logs an informational message, once, indicating the applications version.
     *
     * @param name  the name of the application
     * @param major the major version
     * @param minor the minor version
     * @param macro the macro version
     * @param rel   the release suffix, e.g. {@code Beta1}, {@code Final}
     */
    @LogMessage
    @Once
    @Message("%s version %d.%d.%d.%s")
    void appVersion(CharSequence name, int major, int minor, int macro, String rel);

    /**
     * Logs an error message indicating a failure to close the object.
     *
     * @param cause the cause of the error
     * @param obj   the object that failed closing
     */
    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 100, value = "Failure while closing %s")
    void closeFailure(@Cause Throwable cause, Object obj);

    /**
     * Logs a warning message indicating the encoding is invalid and a default encoding will be used.
     *
     * @param encoding the invalid encoding
     * @param dft      the default encoding
     */
    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 101, value = "Encoding %s could not be found. Defaulting to %s.")
    void encodingNotFound(String encoding, Charset dft);

    /**
     * Logs an informational message indicating the cache size has changed and the size the cache is now.
     *
     * @param c the collection holding the cache
     */
    @LogMessage
    @Message(id = 102, value = "Cache size changed to '%d'")
    @ResolutionDoc(skip = true)
    void cacheSizeChanged(@Transform(Transform.TransformType.SIZE) Collection<String> c);

    /**
     * Logs an informational message indicating the cache size has changed and the size the cache is now.
     *
     * @param array the cache arracy
     */
    @LogMessage
    @ResolutionDoc(skip = true)
    void cacheSizeChanged(@Transform(Transform.TransformType.SIZE) String... array);

    /**
     * Logs an informational message indicating the cache size has changed and the size the cache is now.
     *
     * @param map the map holding the cache
     */
    @LogMessage
    @ResolutionDoc(skip = true)
    void cacheSizeChanged(@Transform(Transform.TransformType.SIZE) Map<String, Object> map);
}
