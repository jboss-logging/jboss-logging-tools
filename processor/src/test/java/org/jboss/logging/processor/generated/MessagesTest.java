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

package org.jboss.logging.processor.generated;

import org.jboss.logging.processor.generated.ValidMessages.StringOnlyException;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class MessagesTest {

    @Test
    public void testFormats() {
        Assert.assertEquals(ValidMessages.MESSAGES.testWithNewLine(), String.format(ValidMessages.TEST_MSG));
        Assert.assertEquals(ValidMessages.MESSAGES.noFormat(), ValidMessages.TEST_MSG);
        Assert.assertEquals(ValidMessages.MESSAGES.noFormatException(new IllegalArgumentException()).getLocalizedMessage(), ValidMessages.TEST_MSG);

        final int value = 10;
        Assert.assertEquals(ValidMessages.MESSAGES.fieldMessage(value).value, value);
        Assert.assertEquals(ValidMessages.MESSAGES.paramMessage(value).value, value);
        Assert.assertEquals(ValidMessages.MESSAGES.propertyMessage(value).value, value);

        final StringOnlyException e = ValidMessages.MESSAGES.stringOnlyException(new RuntimeException());
        Assert.assertEquals(e.getMessage(), String.format(ValidMessages.TEST_MSG));
        Assert.assertNotNull(e.getCause());

        Assert.assertTrue(ValidMessages.MESSAGES.invalidCredentials() instanceof IllegalArgumentException, "Incorrect type constructed");
    }
}
