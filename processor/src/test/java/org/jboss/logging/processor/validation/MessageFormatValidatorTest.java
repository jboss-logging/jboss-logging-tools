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

package org.jboss.logging.processor.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Date: 14.06.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class MessageFormatValidatorTest {

    @Test
    public void validFormats() {
        MessageFormatValidator validator = MessageFormatValidator.of("Message {} is valid.");
        assertTrue(validator.isValid(), validator.detailMessage());

        validator = MessageFormatValidator.of("Parameter {1} is not compatible with {2}.");
        assertTrue(validator.isValid(), validator.detailMessage());
    }

    @Test
    public void invalidFormats() {
        MessageFormatValidator validator = MessageFormatValidator.of("Invalid parameter { is not valid.");
        assertFalse(validator.isValid());
    }

    @Test
    public void validateParameterCount() {
        MessageFormatValidator validator = MessageFormatValidator.of("{}", "Test");
        assertTrue(validator.isValid(), validator.detailMessage());

        validator = MessageFormatValidator.of("{1} {0}", "Test", "Test2");
        assertTrue(validator.isValid(), validator.detailMessage());

        validator = MessageFormatValidator.of("{0} {0}", "Test");
        assertTrue(validator.isValid(), validator.detailMessage());
    }
}
