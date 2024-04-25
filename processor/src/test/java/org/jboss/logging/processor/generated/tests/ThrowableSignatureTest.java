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

package org.jboss.logging.processor.generated.tests;

import org.jboss.logging.processor.generated.SignatureMessages;
import org.jboss.logging.processor.generated.SignatureMessagesInvalidTextException;
import org.jboss.logging.processor.generated.SignatureMessagesRedirectException;
import org.jboss.logging.processor.generated.SignatureMessagesTestException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class ThrowableSignatureTest {

    @Test
    public void testSignatures() {
        @SuppressWarnings("RedundantStringFormatCall")
        final String formattedMessage = String.format(SignatureMessages.TEST_MSG);
        final RuntimeException cause = new RuntimeException("This was the cause");

        final int code = 307;
        final String location = "foo";
        SignatureMessagesRedirectException redirectExpected = new SignatureMessagesRedirectException(formattedMessage, code,
                location);
        Assertions.assertEquals(redirectExpected, SignatureMessages.MESSAGES.redirect(code, location));
        redirectExpected = new SignatureMessagesRedirectException(formattedMessage, location);
        redirectExpected.initCause(cause);
        Assertions.assertEquals(redirectExpected, SignatureMessages.MESSAGES.redirect(cause, location));
        redirectExpected = new SignatureMessagesRedirectException(formattedMessage, cause, code, location);
        Assertions.assertEquals(redirectExpected, SignatureMessages.MESSAGES.redirect(cause, code, location));

        SignatureMessagesTestException testExpected = new SignatureMessagesTestException(formattedMessage);
        Assertions.assertEquals(testExpected, SignatureMessages.MESSAGES.test());
        testExpected = new SignatureMessagesTestException(formattedMessage, cause);
        Assertions.assertEquals(testExpected, SignatureMessages.MESSAGES.test(cause));

        final String invalidText = "invalid";
        SignatureMessagesInvalidTextException invalidTextExpected = new SignatureMessagesInvalidTextException(formattedMessage,
                invalidText);
        Assertions.assertEquals(invalidTextExpected, SignatureMessages.MESSAGES.invalidText(invalidText));
        invalidTextExpected = new SignatureMessagesInvalidTextException(formattedMessage, cause, invalidText);
        Assertions.assertEquals(invalidTextExpected, SignatureMessages.MESSAGES.invalidText(cause, invalidText));
        invalidTextExpected = new SignatureMessagesInvalidTextException(3, cause, invalidText, formattedMessage);
        Assertions.assertEquals(invalidTextExpected, SignatureMessages.MESSAGES.invalidText(3, cause, invalidText));
    }
}
