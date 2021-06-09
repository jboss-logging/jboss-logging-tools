/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2021, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.logging.processor.generated.tests;

import org.jboss.logging.processor.generated.SignatureMessages;
import org.jboss.logging.processor.generated.SignatureMessages.InvalidTextException;
import org.jboss.logging.processor.generated.SignatureMessages.RedirectException;
import org.jboss.logging.processor.generated.SignatureMessages.TestException;
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
        RedirectException redirectExpected = new RedirectException(formattedMessage, code, location);
        Assertions.assertEquals(redirectExpected, SignatureMessages.MESSAGES.redirect(code, location));
        redirectExpected = new RedirectException(formattedMessage, location);
        redirectExpected.initCause(cause);
        Assertions.assertEquals(redirectExpected, SignatureMessages.MESSAGES.redirect(cause, location));
        redirectExpected = new RedirectException(formattedMessage, cause, code, location);
        Assertions.assertEquals(redirectExpected, SignatureMessages.MESSAGES.redirect(cause, code, location));

        TestException testExpected = new TestException(formattedMessage);
        Assertions.assertEquals(testExpected, SignatureMessages.MESSAGES.test());
        testExpected = new TestException(formattedMessage, cause);
        Assertions.assertEquals(testExpected, SignatureMessages.MESSAGES.test(cause));

        final String invalidText = "invalid";
        InvalidTextException invalidTextExpected = new InvalidTextException(formattedMessage, invalidText);
        Assertions.assertEquals(invalidTextExpected, SignatureMessages.MESSAGES.invalidText(invalidText));
        invalidTextExpected = new InvalidTextException(formattedMessage, cause, invalidText);
        Assertions.assertEquals(invalidTextExpected, SignatureMessages.MESSAGES.invalidText(cause, invalidText));
        invalidTextExpected = new InvalidTextException(3, cause, invalidText, formattedMessage);
        Assertions.assertEquals(invalidTextExpected, SignatureMessages.MESSAGES.invalidText(3, cause, invalidText));
    }
}
