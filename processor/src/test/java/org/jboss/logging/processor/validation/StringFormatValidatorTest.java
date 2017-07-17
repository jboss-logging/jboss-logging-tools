/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat, Inc., and individual contributors
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

package org.jboss.logging.processor.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;

/**
 * Date: 14.06.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
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
        assertTrue(validator.detailMessage(), validator.isValid());

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
            assertTrue(validator.detailMessage(), validator.isValid());
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
        assertTrue(validator.detailMessage(), validator.isValid());

        validator = StringFormatValidator.of("Duke's Birthday: %1$tm %1$te,%1$tY", new Date());
        assertTrue(validator.detailMessage(), validator.isValid());

        validator = StringFormatValidator.of("Duke's Birthday: %1$tm %<te,%<tY", new Date(), new Date());
        assertFalse(validator.isValid());
    }

    @Test
    public void validateParameterTypePerPosition() {
        StringFormatValidator validator = StringFormatValidator.of("%1$s %2$d %3$s", "Test", 10, "Again");
        assertTrue(validator.detailMessage(), validator.isValid());

        validator = StringFormatValidator.of("%3$s %1$d %2$s", "Test", 42, "order");
        assertFalse(validator.detailMessage(), validator.isValid());

        validator = StringFormatValidator.of("%2$d %1$s", "Test", 42);
        assertTrue(validator.detailMessage(), validator.isValid());

        validator = StringFormatValidator.of("%3$s %1$d %3$s %2$tm", 42, new Date(), "Test");
        assertTrue(validator.detailMessage(), validator.isValid());

        validator = StringFormatValidator.of("%2$d %<d %s", "Test", 42);
        assertTrue(validator.detailMessage(), validator.isValid());

        validator = StringFormatValidator.of("The error is %s, I repeat %1$s", "invalid");
        assertTrue(validator.detailMessage(), validator.isValid());

    }

    @Test
    public void translationValidation() {
        String nonPositional = "Test param 1 %s new line %n param 2 %s%n";
        String positional = "Test param 2 %2$s new line %n param 1 %1$s%n";
        // If parameters are specified in the either one of the formats, the validation should pass
        StringFormatValidator validator = StringFormatValidator.withTranslation(nonPositional, positional);
        assertTrue(validator.detailMessage(), validator.isValid());

        validator = StringFormatValidator.withTranslation(positional, nonPositional);
        assertTrue(validator.detailMessage(), validator.isValid());

        nonPositional = "Test param 1 %s percent %% param 2 %s%%%n";
        positional = "Test param 2 %2$s percent %% param 1 %1$s%%%n";
        validator = StringFormatValidator.withTranslation(nonPositional, positional);
        assertTrue(validator.detailMessage(), validator.isValid());

        validator = StringFormatValidator.withTranslation(positional, nonPositional);
        assertTrue(validator.detailMessage(), validator.isValid());
    }
}
