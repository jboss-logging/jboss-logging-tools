package org.jboss.logging.generator.validation.validator;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * Date: 14.06.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class MessageFormatValidatorTest {

    @Test
    public void validFormats() {
        MessageFormatValidator validator = MessageFormatValidator.of("Message {} is valid.");
        assertTrue(validator.detailMessage(), validator.isValid());

        validator = MessageFormatValidator.of("Parameter {1} is not compatible with {2}.");
        assertTrue(validator.detailMessage(), validator.isValid());
    }

    @Test
    public void invalidFormats() {
        MessageFormatValidator validator = MessageFormatValidator.of("Invalid parameter { is not valid.");
        assertFalse(validator.isValid());
    }

    @Test
    public void validateParameterCount() {
        MessageFormatValidator validator = MessageFormatValidator.of("{}", "Test");
        assertTrue(validator.detailMessage(), validator.isValid());

        validator = MessageFormatValidator.of("{1} {0}", "Test", "Test2");
        assertTrue(validator.detailMessage(), validator.isValid());

        validator = MessageFormatValidator.of("{0} {0}", "Test");
        assertTrue(validator.detailMessage(), validator.isValid());
    }
}
