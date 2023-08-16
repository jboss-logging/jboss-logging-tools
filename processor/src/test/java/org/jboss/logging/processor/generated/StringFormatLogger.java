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

import java.util.Date;

import org.jboss.logging.Logger.Level;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@MessageLogger(projectCode = "TEST")
public interface StringFormatLogger {

    @LogMessage(level = Level.INFO)
    @Message("String %s integer %d")
    void stringInt(String s, int i);

    @LogMessage(level = Level.INFO)
    @Message("String %s integer %d")
    void stringIntFailure(String s, int i);

    @LogMessage(level = Level.INFO)
    @Message("Duke's Birthday: %1$tm %<te,%<tY")
    void dukesBirthday(Date date);

    @LogMessage(level = Level.INFO)
    @Message("Duke's Birthday: %1$tm %<te,%<tY")
    void dukesBirthdayFailure(Date date);

    @LogMessage(level = Level.INFO)
    @Message("The error is %s, I repeat %1$s")
    void repeat(String message);

    @LogMessage(level = Level.INFO)
    @Message("The error is %s, I repeat %1$s")
    void repeatFailure(String message);
}
