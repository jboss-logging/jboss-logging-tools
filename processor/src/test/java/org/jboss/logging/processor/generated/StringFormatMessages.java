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

import org.jboss.logging.Messages;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@MessageBundle(projectCode = "TEST")
public interface StringFormatMessages {

    StringFormatMessages MESSAGES = Messages.getBundle(StringFormatMessages.class);

    @Message("String %s integer %d")
    String stringInt(String s, int i);

    @Message("Duke's Birthday: %1$tm %<te,%<tY")
    String dukesBirthday(Date date);

    @Message("The error is %s, I repeat %1$s")
    String repeat(String message);

    @Message("Second %2$s first %s")
    String twoMixedIndexes(String second, String first);

    @Message("Third %3$s first %s second %s")
    String threeMixedIndexes(int third, int first, int second);

    @Message("Third %3$s first %s second %2$s repeat second %s")
    String fourMixedIndexes(int third, int first, int second);
}
