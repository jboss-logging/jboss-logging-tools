/*
 *  JBoss, Home of Professional Open Source Copyright 2011, Red Hat, Inc., and
 *  individual contributors by the @authors tag. See the copyright.txt in the
 *  distribution for a full listing of individual contributors.
 *
 *  This is free software; you can redistribute it and/or modify it under the
 *  terms of the GNU Lesser General Public License as published by the Free
 *  Software Foundation; either version 2.1 of the License, or (at your option)
 *  any later version.
 *
 *  This software is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 *  details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this software; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 *  site: http://www.fsf.org.
 */
package org.jboss.logging.processor;

import org.jboss.logging.Cause;
import org.jboss.logging.FormatWith;
import org.jboss.logging.LogMessage;
import org.jboss.logging.Logger;
import org.jboss.logging.Message;
import org.jboss.logging.MessageBundle;
import org.jboss.logging.MessageLogger;
import org.jboss.logging.generator.Annotations;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a> - 19.Feb.2011
 */
public class BaseAnnotations implements Annotations {

    public static final Class<FormatWith> FORMAT_WITH_ANNOTATION = FormatWith.class;
    public static final Class<Cause> CAUSE_ANNOTATION = Cause.class;
    public static final Class<MessageBundle> MESSAGE_BUNDLE_ANNOTATION = MessageBundle.class;
    public static final Class<MessageLogger> MESSAGE_LOGGER_ANNOTATION = MessageLogger.class;
    public static final Class<LogMessage> LOG_MESSAGE_ANNOTATION = LogMessage.class;
    public static final Class<Message> MESSAGE_ANNOTATION = Message.class;

    @Override
    public Class<? extends Annotation> cause() {
        return CAUSE_ANNOTATION;
    }

    @Override
    public Class<? extends Annotation> formatWith() {
        return FORMAT_WITH_ANNOTATION;
    }

    @Override
    public Class<? extends Annotation> logMessage() {
        return LOG_MESSAGE_ANNOTATION;
    }

    @Override
    public Class<? extends Annotation> message() {
        return MESSAGE_ANNOTATION;
    }

    @Override
    public Class<? extends Annotation> messageBundle() {
        return MESSAGE_BUNDLE_ANNOTATION;
    }

    @Override
    public Class<? extends Annotation> messageLogger() {
        return MESSAGE_LOGGER_ANNOTATION;
    }

    @Override
    public FormatType messageFormat(final ExecutableElement method) {
        FormatType result = null;
        final Message message = method.getAnnotation(MESSAGE_ANNOTATION);
        if (message != null) {
            switch (message.format()) {
                case MESSAGE_FORMAT:
                    result = FormatType.MESSAGE_FORMAT;
                    break;
                case PRINTF:
                    result = FormatType.PRINTF;
                    break;
            }
        }
        return result;
    }

    @Override
    public String projectCode(final TypeElement intf) {
        String result = null;
        final MessageBundle bundle = intf.getAnnotation(MESSAGE_BUNDLE_ANNOTATION);
        final MessageLogger logger = intf.getAnnotation(MESSAGE_LOGGER_ANNOTATION);
        if (bundle != null) {
            result = bundle.projectCode();
        } else if (logger != null) {
            result = logger.projectCode();
        }
        return result;
    }

    @Override
    public boolean hasMessageId(final ExecutableElement method) {
        final Message message = method.getAnnotation(MESSAGE_ANNOTATION);
        return (message == null ? false : (message.id() != Message.NONE && message.id() != Message.INHERIT));
    }

    @Override
    public boolean inheritsMessageId(final ExecutableElement method) {
        final Message message = method.getAnnotation(MESSAGE_ANNOTATION);
        return (message == null ? false : (message.id() == Message.INHERIT));
    }

    @Override
    public int messageId(final ExecutableElement method) {
        final Message message = method.getAnnotation(MESSAGE_ANNOTATION);
        return (message == null ? Message.NONE : message.id());
    }

    @Override
    public String messageValue(final ExecutableElement method) {
        final Message message = method.getAnnotation(MESSAGE_ANNOTATION);
        return (message == null ? null : message.value());
    }

    @Override
    public String loggerMethod(final ExecutableElement method, final FormatType formatType) {
        return "log" + (formatType == null ? "" : formatType.logType());
    }

    @Override
    public String logLevel(final ExecutableElement method) {
        String result = null;
        final LogMessage logMessage = method.getAnnotation(LOG_MESSAGE_ANNOTATION);
        if (logMessage != null) {
            final Logger.Level logLevel = (logMessage.level() == null ? Logger.Level.INFO : logMessage.level());
            result = String.format("%s.%s.%s", Logger.class.getSimpleName(), Logger.Level.class.getSimpleName(), logLevel.name());
        }
        return result;
    }

}
