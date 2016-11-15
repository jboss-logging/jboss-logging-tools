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

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.logging.annotations.Once;
import org.jboss.logging.annotations.Transform;
import org.jboss.logging.annotations.Transform.TransformType;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@MessageLogger(projectCode = AbstractLoggerTest.PROJECT_CODE)
public interface LogOnceLogger {

    public final LogOnceLogger LOGGER = Logger.getMessageLogger(LogOnceLogger.class, LogOnceLogger.class.getName());

    @LogMessage(level = Level.WARN)
    @Once
    @Message("'%s' has been deprecated.")
    void deprecated(String key);

    @LogMessage(level = Level.WARN)
    @Once
    @Message("'%s' has been deprecated. Please use '%s'.")
    void deprecated(String key, String replacement);

    @LogMessage
    void deprecated(Member member);

    @LogMessage
    @Once
    @Message("Cache size changed to '%d'")
    void cacheSizeChanged(@Transform(TransformType.SIZE) Collection<String> c);

    @LogMessage
    void cacheSizeChanged(@Transform(TransformType.SIZE) String... array);

    @LogMessage
    @Once
    void cacheSizeChanged(@Transform(TransformType.SIZE) Map<String, Object> map);
}
