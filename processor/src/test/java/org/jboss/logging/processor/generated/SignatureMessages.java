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

import org.jboss.logging.Messages;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;
import org.jboss.logging.annotations.Param;
import org.jboss.logging.annotations.Signature;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@MessageBundle(projectCode = "SIG")
public interface SignatureMessages {
    String TEST_MSG = "Test signature message";

    SignatureMessages MESSAGES = Messages.getBundle(SignatureMessages.class);

    @Message(TEST_MSG)
    SignatureMessagesRedirectException redirect(@Param int responseCode, @Param String location);

    SignatureMessagesRedirectException redirect(@Cause Throwable cause, @Param int responseCode, @Param String location);

    @Signature({ String.class, String.class })
    SignatureMessagesRedirectException redirect(@Cause Throwable cause, @Param String location);

    @Message(TEST_MSG)
    SignatureMessagesTestException test();

    SignatureMessagesTestException test(@Cause Throwable cause);

    @Message(TEST_MSG)
    SignatureMessagesInvalidTextException invalidText(@Param String text);

    SignatureMessagesInvalidTextException invalidText(@Cause Throwable cause, @Param String text);

    @Signature(causeIndex = 1, messageIndex = 3, value = { int.class, Throwable.class, String.class, String.class })
    SignatureMessagesInvalidTextException invalidText(@Param int position, @Cause Throwable cause, @Param String text);

}
