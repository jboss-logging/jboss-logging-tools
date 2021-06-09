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

package org.jboss.logging.processor.generated;

import java.util.Date;

import org.jboss.logging.Messages;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@MessageBundle(projectCode = "TEST")
public interface StringFormatMessages {

    StringFormatMessages MESSAGES = Messages.getBundle(StringFormatMessages.class);

    @Message("String %s integer %d")
    String stringInt(String s, int i);

    @Message("Duke's Birthday: %1$tm %<te,%<tY")
    String dukesBirthday(Date date);

    @Message("The error is %s, I repeat %1$s")
    String repeat(String message);

    @Message("Second %2$s first %s")
    String twoMixedIndexes(String second, String first);

    @Message("Third %3$s first %s second %s")
    String threeMixedIndexes(int third, int first, int second);

    @Message("Third %3$s first %s second %2$s repeat second %s")
    String fourMixedIndexes(int third, int first, int second);
}
