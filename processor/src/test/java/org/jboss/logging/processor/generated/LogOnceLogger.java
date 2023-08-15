/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2023 Red Hat, Inc., and individual contributors
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

package org.jboss.logging.processor.generated;

import java.lang.reflect.Member;
import java.util.Collection;
import java.util.Map;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.logging.annotations.Once;
import org.jboss.logging.annotations.Transform;
import org.jboss.logging.annotations.Transform.TransformType;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@MessageLogger(projectCode = TestConstants.PROJECT_CODE)
public interface LogOnceLogger {

    LogOnceLogger LOGGER = Logger.getMessageLogger(LogOnceLogger.class, LogOnceLogger.class.getName());

    @LogMessage(level = Level.WARN)
    @Once
    @Message("'%s' has been deprecated.")
    void deprecated(String key);

    @LogMessage(level = Level.WARN, useThreadContext = true)
    @Once
    @Message("'%s' has been deprecated. Please use '%s'.")
    void deprecated(String key, String replacement);

    @LogMessage
    void deprecated(Member member);

    @LogMessage
    @Once
    @Message("Cache size changed to '%d'")
    void cacheSizeChanged(@Transform(TransformType.SIZE) Collection<String> c);

    @LogMessage
    void cacheSizeChanged(@Transform(TransformType.SIZE) String... array);

    @LogMessage
    @Once
    void cacheSizeChanged(@Transform(TransformType.SIZE) Map<String, Object> map);
}
