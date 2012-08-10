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
        } else if (ElementHelper.isAnnotatedWith(method, org.jboss.logging.Message.class)) {
            final org.jboss.logging.Message message = method.getAnnotation(org.jboss.logging.Message.class);
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
            final org.jboss.logging.MessageBundle legacyBundle = intf.getAnnotation(org.jboss.logging.MessageBundle.class);
            final org.jboss.logging.MessageLogger legacyLogger = intf.getAnnotation(org.jboss.logging.MessageLogger.class);
            if (legacyBundle != null) {
                result = legacyBundle.projectCode();
            } else if (legacyLogger != null) {
                result = legacyLogger.projectCode();
            }
        }
        return result;
    }

    @Override
    public boolean hasCauseAnnotation(final VariableElement param) {
        return ElementHelper.isAnnotatedWith(param, Cause.class, org.jboss.logging.Cause.class);
    }

    @Override
    public boolean hasFieldAnnotation(final VariableElement param) {
        return ElementHelper.isAnnotatedWith(param, Field.class, org.jboss.logging.Field.class);
    }

    @Override
    public boolean hasLoggingClassAnnotation(final VariableElement param) {
        return ElementHelper.isAnnotatedWith(param, LoggingClass.class, org.jboss.logging.LoggingClass.class);
    }

    @Override
    public boolean hasMessageAnnotation(final ExecutableElement method) {
        return ElementHelper.isAnnotatedWith(method, Message.class, org.jboss.logging.Message.class);
    }

    @Override
    public boolean hasMessageId(final ExecutableElement method) {
        final boolean result;
        final Message message = method.getAnnotation(Message.class);
        if (message != null) {
            result = (message.id() != Message.NONE && message.id() != Message.INHERIT);
        } else {
            // Check legacy annotation
            final org.jboss.logging.Message legacyMessage = method.getAnnotation(org.jboss.logging.Message.class);
            result = (legacyMessage != null && (legacyMessage.id() != org.jboss.logging.Message.NONE &&
                    legacyMessage.id() != org.jboss.logging.Message.INHERIT));
        }

        return result;
    }

    @Override
    public boolean hasParamAnnotation(final VariableElement param) {
        return ElementHelper.isAnnotatedWith(param, Param.class, org.jboss.logging.Param.class);
    }

    @Override
    public boolean hasPropertyAnnotation(final VariableElement param) {
        return ElementHelper.isAnnotatedWith(param, Property.class, org.jboss.logging.Property.class);
    }

    @Override
    public boolean inheritsMessageId(final ExecutableElement method) {
        final boolean result;
        final Message message = method.getAnnotation(Message.class);
        if (message != null) {
            result = message.id() == Message.INHERIT;
        } else {
            // Check legacy annotation
            final org.jboss.logging.Message legacyMessage = method.getAnnotation(org.jboss.logging.Message.class);
            result = (legacyMessage != null && legacyMessage.id() == org.jboss.logging.Message.INHERIT);
        }
        return result;
    }

    @Override
    public boolean isLoggerMethod(final ExecutableElement method) {
        return ElementHelper.isAnnotatedWith(method, org.jboss.logging.LogMessage.class, org.jboss.logging.annotations.LogMessage.class);
    }

    @Override
    public boolean isMessageBundle(final TypeElement element) {
        return ElementHelper.isAnnotatedWith(element, org.jboss.logging.MessageBundle.class, org.jboss.logging.annotations.MessageBundle.class);
    }

    @Override
    public boolean isMessageLogger(final TypeElement element) {
        return ElementHelper.isAnnotatedWith(element, org.jboss.logging.MessageLogger.class, org.jboss.logging.annotations.MessageLogger.class);
    }

    @Override
    public boolean isValidInterfaceAnnotation(final TypeElement annotation) {
        final String name = annotation.getQualifiedName().toString();
        return name != null && (name.equals(MessageBundle.class.getName()) || name.equals(MessageLogger.class.getName()) ||
                name.equals(org.jboss.logging.MessageBundle.class.getName()) ||
                name.equals(org.jboss.logging.MessageLogger.class.getName()));
    }

    @Override
    public String getFormatWithAnnotationName(final VariableElement param) {
        String result = FormatWith.class.getName();
        if (param.getAnnotation(org.jboss.logging.FormatWith.class) != null) {
            result = org.jboss.logging.FormatWith.class.getName();
        }
        return result;
    }

    @Override
    public String getMessageLoggerAnnotationName(final TypeElement element) {
        String result = MessageLogger.class.getName();
        if (element.getAnnotation(org.jboss.logging.MessageLogger.class) != null) {
            result = org.jboss.logging.MessageLogger.class.getName();
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
            final org.jboss.logging.Message legacyMessage = method.getAnnotation(org.jboss.logging.Message.class);
            result = (legacyMessage == null ? Message.NONE : legacyMessage.id());
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
            final org.jboss.logging.Message legacyMessage = method.getAnnotation(org.jboss.logging.Message.class);
            result = (legacyMessage == null ? null : legacyMessage.value());
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
            result = String.format("%s.%s.%s", Logger.class.getSimpleName(), Logger.Level.class.getSimpleName(), logLevel.name());
        } else {
            // check legacy annotation
            final org.jboss.logging.LogMessage legacyLogMessage = method.getAnnotation(org.jboss.logging.LogMessage.class);
            if (legacyLogMessage != null) {
                final Logger.Level logLevel = (legacyLogMessage.level() == null ? Logger.Level.INFO : legacyLogMessage.level());
                result = String.format("%s.%s.%s", Logger.class.getSimpleName(), Logger.Level.class.getSimpleName(), logLevel.name());
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
            final org.jboss.logging.Field legacyField = param.getAnnotation(org.jboss.logging.Field.class);
            final org.jboss.logging.Property legacyProperty = param.getAnnotation(org.jboss.logging.Property.class);
            if (legacyField != null) {
                final String name = legacyField.name();
                if (name.isEmpty()) {
                    result = param.getSimpleName().toString();
                } else {
                    result = name;
                }
            } else if (legacyProperty != null) {
                final String name = legacyProperty.name();
                if (name.isEmpty()) {
                    result = param.getSimpleName().toString();
                } else {
                    result = name;
                }
                result = "set" + Character.toUpperCase(result.charAt(0)) + result.substring(1);
            }
        }
        return result;
    }
}
