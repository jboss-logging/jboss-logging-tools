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
package org.jboss.logging.util;

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
 * @author James R. Perkins (jrp)
 */
final class ClassModelUtil {

    private static final String INSTANCE_FIELD_NAME = "INSTANCE";

    private static final String GET_INSTANCE_METHOD_NAME = "readResolve";

    /**
     * Constructor for singleton model.
     *
     */
    private ClassModelUtil() {
    }

    /**
     * Formats the message id. The message id is comprised of the project code
     * plus the id.
     *
     * @param id the id used to prepend the project code.
     * @return the formatted message id.
     */
    public static String formatMessageId(final String projectCode, final int id) {
        final StringBuilder result = new StringBuilder(projectCode);
        if (result.length() > 0) {
            result.append("-");
            result.append(padLeft("" + id, '0', 5));
            result.append(": ");
        }
        return result.toString();
    }

    /**
     * Pads the initial value with the character. If the length is greater than
     * or equal to the length of the initial value, the initial value will be
     * returned.
     *
     * @param initValue the value to pad.
     * @param padChar   the character to pad the value with.
     * @param padLen    the total length the string should be.
     * @return the padded value.
     */
    public static String padLeft(final String initValue, final char padChar,
            final int padLen) {

        final StringBuilder result = new StringBuilder();
        for (int i = initValue.length(); i < padLen; i++) {
            result.append(padChar);
        }
        result.append(initValue);
        return result.toString();
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
}
