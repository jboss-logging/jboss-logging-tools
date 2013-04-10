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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.AnnotationValue;
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
import org.jboss.logging.annotations.Property;
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
    static final String LEGACY_CAUSE = "org.jboss.logging.Cause";
    static final String LEGACY_FIELD = "org.jboss.logging.Field";
    static final String LEGACY_FORMAT_WITH = "org.jboss.logging.FormatWith";
    static final String LEGACY_LOGGING_CLASS = "org.jboss.logging.LoggingClass";
    static final String LEGACY_LOG_MESSAGE = "org.jboss.logging.LogMessage";
    static final String LEGACY_MESSAGE = "org.jboss.logging.Message";
    static final String LEGACY_MESSAGE_BUNDLE = "org.jboss.logging.MessageBundle";
    static final String LEGACY_MESSAGE_LOGGER = "org.jboss.logging.MessageLogger";
    static final String LEGACY_PARAM = "org.jboss.logging.Param";
    static final String LEGACY_PROPERTY = "org.jboss.logging.Property";

    private static String[] ANNOTATIONS = {
            LEGACY_MESSAGE_BUNDLE,
            LEGACY_MESSAGE_LOGGER,
            MessageBundle.class.getName(),
            MessageLogger.class.getName(),
    };

    @Override
    public Set<String> getSupportedAnnotations() {
        final Set<String> annotations = new HashSet<String>(ANNOTATIONS.length);
        Collections.addAll(annotations, ANNOTATIONS);
        return Collections.unmodifiableSet(annotations);
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
        } else if (ElementHelper.isAnnotatedWith(method, LEGACY_MESSAGE)) {
            final AnnotationValue value = ElementHelper.getAnnotationValue(method, LEGACY_MESSAGE, "format");
            if (value != null) {
                result = getEnum(FormatType.class, value.getValue().toString());
            } else {
                // This is the default if not specified
                result = FormatType.PRINTF;
            }
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
        } else {
            // Check legacy annotations
            final AnnotationValue legacyBundle = ElementHelper.getAnnotationValue(intf, LEGACY_MESSAGE_BUNDLE, "projectCode");
            final AnnotationValue legacyLogger = ElementHelper.getAnnotationValue(intf, LEGACY_MESSAGE_LOGGER, "projectCode");
            if (legacyBundle != null) {
                result = legacyBundle.getValue().toString();
            } else if (legacyLogger != null) {
                result = legacyLogger.getValue().toString();
            }
        }
        return result;
    }

    @Override
    public boolean hasCauseAnnotation(final VariableElement param) {
        return ElementHelper.isAnnotatedWith(param, Cause.class) || ElementHelper.isAnnotatedWith(param, LEGACY_CAUSE);
    }

    @Override
    public boolean hasFieldAnnotation(final VariableElement param) {
        return ElementHelper.isAnnotatedWith(param, Field.class) || ElementHelper.isAnnotatedWith(param, LEGACY_FIELD);
    }

    @Override
    public boolean hasLoggingClassAnnotation(final VariableElement param) {
        return ElementHelper.isAnnotatedWith(param, LoggingClass.class) || ElementHelper.isAnnotatedWith(param, LEGACY_LOGGING_CLASS);
    }

    @Override
    public boolean hasMessageAnnotation(final ExecutableElement method) {
        return ElementHelper.isAnnotatedWith(method, Message.class) || ElementHelper.isAnnotatedWith(method, LEGACY_MESSAGE);
    }

    @Override
    public boolean hasMessageId(final ExecutableElement method) {
        final boolean result;
        final Message message = method.getAnnotation(Message.class);
        if (message != null) {
            result = (message.id() != Message.NONE && message.id() != Message.INHERIT);
        } else {
            // Check legacy annotation
            if (ElementHelper.isAnnotatedWith(method, LEGACY_MESSAGE)) {
                final AnnotationValue value = ElementHelper.getAnnotationValue(method, LEGACY_MESSAGE, "id");
                final int id = (value != null) ? Integer.parseInt(value.getValue().toString()) : Message.INHERIT;
                result = (value != null && (id != Message.NONE && id != Message.INHERIT));
            } else {
                result = false;
            }
        }

        return result;
    }

    @Override
    public boolean hasParamAnnotation(final VariableElement param) {
        return ElementHelper.isAnnotatedWith(param, Param.class) || ElementHelper.isAnnotatedWith(param, LEGACY_PARAM);
    }

    @Override
    public boolean hasPropertyAnnotation(final VariableElement param) {
        return ElementHelper.isAnnotatedWith(param, Property.class) || ElementHelper.isAnnotatedWith(param, LEGACY_PROPERTY);
    }

    @Override
    public boolean inheritsMessageId(final ExecutableElement method) {
        final boolean result;
        final Message message = method.getAnnotation(Message.class);
        if (message != null) {
            result = message.id() == Message.INHERIT;
        } else {
            // Check legacy annotation
            if (ElementHelper.isAnnotatedWith(method, LEGACY_MESSAGE)) {
                final AnnotationValue value = ElementHelper.getAnnotationValue(method, LEGACY_MESSAGE, "id");
                final int id = (value != null) ? Integer.parseInt(value.getValue().toString()) : Message.INHERIT;
                result = (value != null && id == Message.INHERIT);
            } else {
                result = false;
            }
        }
        return result;
    }

    @Override
    public boolean isLoggerMethod(final ExecutableElement method) {
        return ElementHelper.isAnnotatedWith(method, LogMessage.class) || ElementHelper.isAnnotatedWith(method, LEGACY_LOG_MESSAGE);
    }

    @Override
    public boolean isMessageBundle(final TypeElement element) {
        return ElementHelper.isAnnotatedWith(element, MessageBundle.class) || ElementHelper.isAnnotatedWith(element, LEGACY_MESSAGE_BUNDLE);
    }

    @Override
    public boolean isMessageLogger(final TypeElement element) {
        return ElementHelper.isAnnotatedWith(element, MessageLogger.class) || ElementHelper.isAnnotatedWith(element, LEGACY_MESSAGE_LOGGER);
    }

    @Override
    public String getFormatWithAnnotationName(final VariableElement param) {
        String result = FormatWith.class.getName();
        if (ElementHelper.isAnnotatedWith(param, LEGACY_FORMAT_WITH)) {
            result = LEGACY_FORMAT_WITH;
        }
        return result;
    }

    @Override
    public String getMessageLoggerAnnotationName(final TypeElement element) {
        String result = MessageLogger.class.getName();
        if (ElementHelper.isAnnotatedWith(element, LEGACY_MESSAGE_LOGGER)) {
            result = LEGACY_MESSAGE_LOGGER;
        }
        return result;
    }

    @Override
    public int messageId(final ExecutableElement method) {
        final int result;
        final Message message = method.getAnnotation(Message.class);
        if (message != null) {
            result = message.id();
        } else {
            // Check legacy annotation
            final AnnotationValue value = ElementHelper.getAnnotationValue(method, LEGACY_MESSAGE, "id");
            result = (value == null ? Message.NONE : Integer.valueOf(value.getValue().toString()));
        }
        return result;
    }

    @Override
    public String messageValue(final ExecutableElement method) {
        final String result;
        final Message message = method.getAnnotation(Message.class);
        if (message != null) {
            result = message.value();
        } else {
            // Check legacy annotation
            final AnnotationValue value = ElementHelper.getAnnotationValue(method, LEGACY_MESSAGE, "value");
            result = (value == null ? null : value.getValue().toString());
        }
        return result;
    }

    @Override
    public String loggerMethod(final FormatType formatType) {
        return "log" + (formatType == null || formatType == FormatType.NO_FORMAT ? "" : formatType.logType());
    }

    @Override
    public String logLevel(final ExecutableElement method) {
        String result = null;
        final LogMessage logMessage = method.getAnnotation(LogMessage.class);
        if (logMessage != null) {
            final Logger.Level logLevel = (logMessage.level() == null ? Logger.Level.INFO : logMessage.level());
            result = String.format("%s.%s.%s", Logger.class.getName(), Logger.Level.class.getSimpleName(), logLevel.name());
        } else {
            // check legacy annotation
            if (ElementHelper.isAnnotatedWith(method, LEGACY_LOG_MESSAGE)) {
                final AnnotationValue value = ElementHelper.getAnnotationValue(method, LEGACY_LOG_MESSAGE, "level");
                final Logger.Level logLevel = (value == null ? Logger.Level.INFO : getEnum(Logger.Level.class, value.getValue().toString()));
                result = String.format("%s.%s.%s", Logger.class.getName(), Logger.Level.class.getSimpleName(), logLevel.name());
            }
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
        } else {
            // Check legacy annotations
            if (ElementHelper.isAnnotatedWith(param, LEGACY_FIELD)) {
                final AnnotationValue value = ElementHelper.getAnnotationValue(param, LEGACY_FIELD, "name");
                if (value == null) {
                    result = param.getSimpleName().toString();
                } else {
                    result = value.getValue().toString();
                }
            } else if (ElementHelper.isAnnotatedWith(param, LEGACY_PROPERTY)) {
                final AnnotationValue value = ElementHelper.getAnnotationValue(param, LEGACY_PROPERTY, "name");
                if (value == null) {
                    result = param.getSimpleName().toString();
                } else {
                    result = value.getValue().toString();
                }
                result = "set" + Character.toUpperCase(result.charAt(0)) + result.substring(1);
            }
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
