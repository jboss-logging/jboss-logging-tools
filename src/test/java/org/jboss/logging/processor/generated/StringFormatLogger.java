/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

import org.jboss.logging.LogMessage;
import org.jboss.logging.Logger.Level;
import org.jboss.logging.Message;
import org.jboss.logging.MessageLogger;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@MessageLogger(projectCode = "TEST")
public interface StringFormatLogger {

    @LogMessage(level = Level.INFO)
    @Message("String %s integer %d")
    void stringInt(String s, int i);

    @LogMessage(level = Level.INFO)
    @Message("String %s integer %d")
    void stringIntFailure(String s, int i);

    @LogMessage(level = Level.INFO)
    @Message("Duke's Birthday: %1$tm %<te,%<tY")
    void dukesBirthday(Date date);

    @LogMessage(level = Level.INFO)
    @Message("Duke's Birthday: %1$tm %<te,%<tY")
    void dukesBirthdayFailure(Date date);
}
