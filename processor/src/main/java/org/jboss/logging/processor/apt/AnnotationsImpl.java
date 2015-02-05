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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import org.jboss.logging.Logger;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.Field;
import org.jboss.logging.annotations.FormatWith;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.LoggingClass;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.logging.annotations.Param;
import org.jboss.logging.annotations.Pos;
import org.jboss.logging.annotations.Property;
import org.jboss.logging.annotations.Transform;
import org.jboss.logging.annotations.ValidIdRange;
import org.jboss.logging.annotations.ValidIdRanges;
import org.jboss.logging.processor.util.ElementHelper;

/**
 * An implementation for an annotation processor.
 * <p/>
 * This implementation handles both {@link org.jboss.logging.annotations local annotations} and deprecated annotations
 * that were part of the {@code jboss-logging} project.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@SuppressWarnings("deprecation")
public class AnnotationsImpl implements Annotations {

    private static final String[] ANNOTATIONS = {
            // Interface annotations
            MessageBundle.class.getName(),
            MessageLogger.class.getName(),
            // Other annotations
            Cause.class.getName(),
            Field.class.getName(),
            FormatWith.class.getName(),
            LoggingClass.class.getName(),
            LogMessage.class.getName(),
            Message.class.getName(),
            Param.class.getName(),
            Pos.class.getName(),
            Property.class.getName(),
            Transform.class.getName(),
            ValidIdRange.class.getName(),
            ValidIdRanges.class.getName(),
    };

    private static final List<String> INTERFACE_ANNOTATIONS = Arrays.asList(
            MessageBundle.class.getName(),
            MessageLogger.class.getName()
    );

    @Override
    public Set<String> getSupportedAnnotations() {
        final Set<String> annotations = new HashSet<String>(ANNOTATIONS.length);
        Collections.addAll(annotations, ANNOTATIONS);
        return Collections.unmodifiableSet(annotations);
    }

    @Override
    public boolean isSupportedInterfaceAnnotation(final TypeElement annotation) {
        return INTERFACE_ANNOTATIONS.contains(annotation.getQualifiedName().toString());
    }

    @Override
    public FormatType messageFormat(final ExecutableElement method) {
        FormatType result = null;
        // Check the annotation
        if (ElementHelper.isAnnotatedWith(method, Message.class)) {
            final Message message = method.getAnnotation(Message.class);
            if (message != null) {
                switch (message.format()) {
                    case MESSAGE_FORMAT:
                        result = FormatType.MESSAGE_FORMAT;
                        break;
                    case PRINTF:
                        result = FormatType.PRINTF;
                        break;
                    case NO_FORMAT:
                        result = FormatType.NO_FORMAT;
                        break;
                }
            }
            // Check the legacy annotation
        }
        return result;
    }

    @Override
    public String projectCode(final TypeElement intf) {
        String result = null;
        final MessageBundle bundle = intf.getAnnotation(MessageBundle.class);
        final MessageLogger logger = intf.getAnnotation(MessageLogger.class);
        if (bundle != null) {
            result = bundle.projectCode();
        } else if (logger != null) {
            result = logger.projectCode();
        }
        return result;
    }

    @Override
    public int idLength(final TypeElement intf) {
        int result = 6;
        final MessageBundle bundle = intf.getAnnotation(MessageBundle.class);
        final MessageLogger logger = intf.getAnnotation(MessageLogger.class);
        if (bundle != null) {
            result = bundle.length();
        } else if (logger != null) {
            result = logger.length();
        }
        return result;
    }

    @Override
    public boolean hasCauseAnnotation(final VariableElement param) {
        return ElementHelper.isAnnotatedWith(param, Cause.class);
    }

    @Override
    public boolean hasFieldAnnotation(final VariableElement param) {
        return ElementHelper.isAnnotatedWith(param, Field.class);
    }

    @Override
    public boolean hasLoggingClassAnnotation(final VariableElement param) {
        return ElementHelper.isAnnotatedWith(param, LoggingClass.class);
    }

    @Override
    public boolean hasMessageAnnotation(final ExecutableElement method) {
        return ElementHelper.isAnnotatedWith(method, Message.class);
    }

    @Override
    public boolean hasMessageId(final ExecutableElement method) {
        final Message message = method.getAnnotation(Message.class);
        return message != null && (message.id() != Message.NONE && message.id() != Message.INHERIT);
    }

    @Override
    public boolean hasParamAnnotation(final VariableElement param) {
        return ElementHelper.isAnnotatedWith(param, Param.class);
    }

    @Override
    public boolean hasPropertyAnnotation(final VariableElement param) {
        return ElementHelper.isAnnotatedWith(param, Property.class);
    }

    @Override
    public boolean inheritsMessageId(final ExecutableElement method) {
        final Message message = method.getAnnotation(Message.class);
        return message != null && message.id() == Message.INHERIT;
    }

    @Override
    public boolean isLoggerMethod(final ExecutableElement method) {
        return ElementHelper.isAnnotatedWith(method, LogMessage.class);
    }

    @Override
    public boolean isMessageBundle(final TypeElement element) {
        return ElementHelper.isAnnotatedWith(element, MessageBundle.class);
    }

    @Override
    public boolean isMessageLogger(final TypeElement element) {
        return ElementHelper.isAnnotatedWith(element, MessageLogger.class);
    }

    @Override
    public String getFormatWithAnnotationName(final VariableElement param) {
        return FormatWith.class.getName();
    }

    @Override
    public String getMessageLoggerAnnotationName(final TypeElement element) {
        return MessageLogger.class.getName();
    }

    @Override
    public int messageId(final ExecutableElement method) {
        final Message message = method.getAnnotation(Message.class);
        return message.id();
    }

    @Override
    public String messageValue(final ExecutableElement method) {
        final Message message = method.getAnnotation(Message.class);
        return message.value();
    }

    @Override
    public String loggerMethod(final FormatType formatType) {
        return "log" + (formatType == null || formatType == FormatType.NO_FORMAT ? "" : formatType.logType());
    }

    @Override
    public String logLevel(final ExecutableElement method) {
        final LogMessage logMessage = method.getAnnotation(LogMessage.class);
        final Logger.Level logLevel = (logMessage.level() == null ? Logger.Level.INFO : logMessage.level());
        return String.format("%s.%s.%s", Logger.class.getName(), Logger.Level.class.getSimpleName(), logLevel.name());
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

    static <T extends Enum<T>> T getEnum(final Class<T> type, final String name) {
        final int start = name.lastIndexOf(".");
        final String s;
        if (start > 0) {
            s = name.substring(start + 1);
        } else {
            s = name;
        }
        return T.valueOf(type, s);
    }
}
