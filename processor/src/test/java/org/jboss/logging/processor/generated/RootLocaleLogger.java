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

import org.jboss.logging.Logger.Level;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@SuppressWarnings("unused")
@MessageLogger(projectCode = "LOCALE", rootLocale = "en-UK")
public interface RootLocaleLogger {

    @LogMessage(level = Level.INFO)
    @Message(id = 10, value = "Initialised %s")
    void init(Object object);

    @LogMessage(level = Level.ERROR)
    @Message(id = 20, value = "Initialisation failed, behaviour may be unpredictable.")
    void initFailed();

    @Message(id = 50, value = "Authorisation failed for %s")
    RuntimeException authFailed(String user);
}
