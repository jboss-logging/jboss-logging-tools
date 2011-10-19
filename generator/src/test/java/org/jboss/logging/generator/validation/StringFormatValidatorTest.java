package org.jboss.logging.generator.validation;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

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

        validator = StringFormatValidator.of("%3$s %1$d %3$s %2tm", 42, new Date(), "Test");
        assertTrue(validator.detailMessage(), validator.isValid());

    }
}
