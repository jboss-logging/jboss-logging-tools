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
