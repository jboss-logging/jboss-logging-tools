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
import org.jboss.logging.Field;
import org.jboss.logging.FormatWith;
import org.jboss.logging.LogMessage;
import org.jboss.logging.LoggingClass;
import org.jboss.logging.Message;
import org.jboss.logging.MessageBundle;
import org.jboss.logging.MessageLogger;
import org.jboss.logging.Param;
import org.jboss.logging.Property;
import org.jboss.logging.generator.Annotations;

import java.lang.annotation.Annotation;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a> - 19.Feb.2011
 */
public class BaseAnnotations implements Annotations {

    public static final Class<Cause> CAUSE_ANNOTATION = Cause.class;
    public static final Class<Field> FIELD_ANNOTATION = Field.class;
    public static final Class<FormatWith> FORMAT_WITH_ANNOTATION = FormatWith.class;
    public static final Class<LoggingClass> LOGGER_CLASS_ANNOTATION = LoggingClass.class;
    public static final Class<LogMessage> LOG_MESSAGE_ANNOTATION = LogMessage.class;
    public static final Class<MessageBundle> MESSAGE_BUNDLE_ANNOTATION = MessageBundle.class;
    public static final Class<MessageLogger> MESSAGE_LOGGER_ANNOTATION = MessageLogger.class;
    public static final Class<Message> MESSAGE_ANNOTATION = Message.class;
    public static final Class<Param> PARAM_ANNOTATION = Param.class;
    public static final Class<Property> PROPERTY_ANNOTATION = Property.class;

    @Override
    public Class<? extends Annotation> cause() {
        return CAUSE_ANNOTATION;
    }

    @Override
    public Class<? extends Annotation> field() {
        return FIELD_ANNOTATION;
    }

    @Override
    public Class<? extends Annotation> formatWith() {
        return FORMAT_WITH_ANNOTATION;
    }

    @Override
    public Class<? extends Annotation> loggingClass() {
        return LOGGER_CLASS_ANNOTATION;
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
    public Class<? extends Annotation> param() {
        return PARAM_ANNOTATION;
    }

    @Override
    public Class<? extends Annotation> property() {
        return PROPERTY_ANNOTATION;
    }

}
