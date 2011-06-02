/*
 *  JBoss, Home of Professional Open Source Copyright 2010, Red Hat, Inc., and
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
package org.jboss.logging.generator.model;

import com.sun.codemodel.internal.JDefinedClass;
import com.sun.codemodel.internal.JExpr;
import com.sun.codemodel.internal.JFieldVar;
import com.sun.codemodel.internal.JMethod;
import com.sun.codemodel.internal.JMod;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utilities for the code model.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
final class ClassModelUtil {

    private static final String STRING_ID_FORMAT = "%06d: ";

    private static final String INSTANCE_FIELD_NAME = "INSTANCE";

    private static final String GET_INSTANCE_METHOD_NAME = "readResolve";

    /**
     * Constructor for singleton model.
     */
    private ClassModelUtil() {
    }

    /**
     * Returns the current date formatted in the ISO 8601 format.
     *
     * @return the current date formatted in ISO 8601.
     */
    protected static String generatedDateValue() {
        final SimpleDateFormat sdf = new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ssZ");
        return sdf.format(new Date());
    }

    /**
     * Creates the read resolve method and instance field.
     *
     * @param definedClass the class to create the methods for.
     *
     * @return the read resolve method.
     */
    public static JMethod createReadResolveMethod(
            final JDefinedClass definedClass) {
        final JFieldVar instance = definedClass.field(
                JMod.PUBLIC | JMod.STATIC | JMod.FINAL, definedClass,
                INSTANCE_FIELD_NAME);
        instance.init(JExpr._new(definedClass));
        final JMethod readResolveMethod = definedClass.method(JMod.PROTECTED,
                definedClass, GET_INSTANCE_METHOD_NAME);
        readResolveMethod.body()._return(instance);
        return readResolveMethod;
    }

    /**
     * Formats message id.
     *
     * @param messageId the message id to format.
     * @return the formatted message id.
     */
    public static String formatMessageId(final int messageId) {
        return String.format(STRING_ID_FORMAT, messageId);
    }
}
