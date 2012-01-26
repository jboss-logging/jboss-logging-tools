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

package org.jboss.logging.processor.apt;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import org.jboss.logging.Field;
import org.jboss.logging.LogMessage;
import org.jboss.logging.Logger;
import org.jboss.logging.Message;
import org.jboss.logging.MessageBundle;
import org.jboss.logging.MessageLogger;
import org.jboss.logging.Property;
import org.jboss.logging.processor.Annotations.FormatType;
import org.jboss.logging.processor.BaseAnnotations;

/**
 * Date: 24.08.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class AptHelperImpl implements AptHelper {

    @Override
    public FormatType messageFormat(final ExecutableElement method) {
        FormatType result = null;
        final Message message = method.getAnnotation(BaseAnnotations.MESSAGE_ANNOTATION);
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
        final MessageBundle bundle = intf.getAnnotation(BaseAnnotations.MESSAGE_BUNDLE_ANNOTATION);
        final MessageLogger logger = intf.getAnnotation(BaseAnnotations.MESSAGE_LOGGER_ANNOTATION);
        if (bundle != null) {
            result = bundle.projectCode();
        } else if (logger != null) {
            result = logger.projectCode();
        }
        return result;
    }

    @Override
    public boolean hasMessageId(final ExecutableElement method) {
        final Message message = method.getAnnotation(BaseAnnotations.MESSAGE_ANNOTATION);
        return (message != null && (message.id() != Message.NONE && message.id() != Message.INHERIT));
    }

    @Override
    public boolean inheritsMessageId(final ExecutableElement method) {
        final Message message = method.getAnnotation(BaseAnnotations.MESSAGE_ANNOTATION);
        return (message != null && (message.id() == Message.INHERIT));
    }

    @Override
    public int messageId(final ExecutableElement method) {
        final Message message = method.getAnnotation(BaseAnnotations.MESSAGE_ANNOTATION);
        return (message == null ? Message.NONE : message.id());
    }

    @Override
    public String messageValue(final ExecutableElement method) {
        final Message message = method.getAnnotation(BaseAnnotations.MESSAGE_ANNOTATION);
        return (message == null ? null : message.value());
    }

    @Override
    public String loggerMethod(final FormatType formatType) {
        return "log" + (formatType == null ? "" : formatType.logType());
    }

    @Override
    public String logLevel(final ExecutableElement method) {
        String result = null;
        final LogMessage logMessage = method.getAnnotation(BaseAnnotations.LOG_MESSAGE_ANNOTATION);
        if (logMessage != null) {
            final Logger.Level logLevel = (logMessage.level() == null ? Logger.Level.INFO : logMessage.level());
            result = String.format("%s.%s.%s", Logger.class.getSimpleName(), Logger.Level.class.getSimpleName(), logLevel.name());
        }
        return result;
    }

    @Override
    public String targetName(final VariableElement param) {
        String result = "";
        final Field field = param.getAnnotation(Field.class);
        final Property property = param.getAnnotation(Property.class);
        if (field != null) {
            final String name = field.name();
            if (name.isEmpty()) {
                result = param.getSimpleName().toString();
            } else {
                result = name;
            }
        } else if (property != null) {
            final String name = property.name();
            if (name.isEmpty()) {
                result = param.getSimpleName().toString();
            } else {
                result = name;
            }
            result = "set" + Character.toUpperCase(result.charAt(0)) + result.substring(1);
        }
        return result;
    }
}
