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

import java.util.Date;

import org.junit.jupiter.api.Test;

/**
 * Date: 14.06.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@SuppressWarnings("MagicNumber")
public class StringFormatValidatorTest {

    @Test
    public void validFormats() {
        final StringBuilder sb = new StringBuilder();
        for (StringFormatPart.Conversion conversion : StringFormatPart.Conversion.values()) {
            sb.append("%").append(conversion.asChar());
            if (conversion.isDateTime()) {
                sb.append("m ");
            } else {
                sb.append(" ");
            }
        }
        StringFormatValidator validator = StringFormatValidator.of(sb.toString());
        assertTrue(validator.isValid(), validator.detailMessage());

        final String[] validFormats = {
                "%1$s %1$s %1$s",
                "Duke's Birthday: %1$tm %1$te,%1$tY",
                "Duke's Birthday: %1$tm %<te,%<tY",
                "Testing %s",
                "Padded id %1$05d enabled",
                "Created a %s application from %S."
        };
        for (String s : validFormats) {
            validator = StringFormatValidator.of(s);
            assertTrue(validator.isValid(), validator.detailMessage());
        }
    }

    @Test
    public void invalidFormats() {
        final StringFormatValidator validator = StringFormatValidator.of("Invalid parameter %v");
        assertFalse(validator.isValid());
    }

    @Test
    public void validateParameterCount() {
        StringFormatValidator validator = StringFormatValidator.of("%1$s %1$s %1$s", "Test");
        assertTrue(validator.isValid(), validator.detailMessage());

        validator = StringFormatValidator.of("Duke's Birthday: %1$tm %1$te,%1$tY", new Date());
        assertTrue(validator.isValid(), validator.detailMessage());

        validator = StringFormatValidator.of("Duke's Birthday: %1$tm %<te,%<tY", new Date(), new Date());
        assertFalse(validator.isValid());
    }

    @Test
    public void validateParameterTypePerPosition() {
        StringFormatValidator validator = StringFormatValidator.of("%1$s %2$d %3$s", "Test", 10, "Again");
        assertTrue(validator.isValid(), validator.detailMessage());

        validator = StringFormatValidator.of("%3$s %1$d %2$s", "Test", 42, "order");
        assertFalse(validator.isValid(), validator.detailMessage());

        validator = StringFormatValidator.of("%2$d %1$s", "Test", 42);
        assertTrue(validator.isValid(), validator.detailMessage());

        validator = StringFormatValidator.of("%3$s %1$d %3$s %2$tm", 42, new Date(), "Test");
        assertTrue(validator.isValid(), validator.detailMessage());

        validator = StringFormatValidator.of("%2$d %<d %s", "Test", 42);
        assertTrue(validator.isValid(), validator.detailMessage());

        validator = StringFormatValidator.of("The error is %s, I repeat %1$s", "invalid");
        assertTrue(validator.isValid(), validator.detailMessage());

    }

    @Test
    public void translationValidation() {
        String nonPositional = "Test param 1 %s new line %n param 2 %s%n";
        String positional = "Test param 2 %2$s new line %n param 1 %1$s%n";
        // If parameters are specified in the either one of the formats, the validation should pass
        StringFormatValidator validator = StringFormatValidator.withTranslation(nonPositional, positional);
        assertTrue(validator.isValid(), validator.detailMessage());

        validator = StringFormatValidator.withTranslation(positional, nonPositional);
        assertTrue(validator.isValid(), validator.detailMessage());

        nonPositional = "Test param 1 %s percent %% param 2 %s%%%n";
        positional = "Test param 2 %2$s percent %% param 1 %1$s%%%n";
        validator = StringFormatValidator.withTranslation(nonPositional, positional);
        assertTrue(validator.isValid(), validator.detailMessage());

        validator = StringFormatValidator.withTranslation(positional, nonPositional);
        assertTrue(validator.isValid(), validator.detailMessage());
    }
}
