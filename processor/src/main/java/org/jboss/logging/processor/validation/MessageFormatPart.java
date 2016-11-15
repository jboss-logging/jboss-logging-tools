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

package org.jboss.logging.processor.validation;

/**
 * The parameter portion of the a {@link java.text.MessageFormat}.
 * <p/>
 * <i><b>**Note:</b> Currently the format type and format style are not validated</i>
 * <p/>
 * Date: 14.06.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
class MessageFormatPart extends AbstractFormatPart {
    private final String originalFormat;
    private final int position;
    private int index;
    private String formatType;
    private String formatStyle;

    private MessageFormatPart(final int position, final String format) {
        this.position = position;
        originalFormat = format;
        index = 0;
    }

    public static MessageFormatPart of(final int position, final String format) {
        final MessageFormatPart result = new MessageFormatPart(position, format);
        // The first character and last character must be { and } respectively.
        if (format.charAt(0) != '{' || format.charAt(format.length() - 1) != '}') {
            throw new IllegalArgumentException("Format must begin with '{' and end with '}'. Format: " + format);
        }
        // Trim off the {}
        String formatText = format.substring(1, format.length() - 1);
        // Can't contain any more { or }
        if (formatText.contains("{") || formatText.contains("}")) {
            throw new IllegalArgumentException("String contains an invalid character. Cannot specify either '{' or '}'.");
        }
        result.init(formatText);
        return result;
    }

    @Override
    public int index() {
        return index;
    }

    @Override
    public int position() {
        return position;
    }

    @Override
    public String part() {
        return originalFormat;
        /** Should use something like this when
         final StringBuilder result = new StringBuilder("{");
         if (index >= 0) {
         result.append(index);
         }
         if (formatType != null) {
         result.append(",").append(formatType);
         }
         if (formatStyle != null) {
         result.append(",").append(formatStyle);
         }
         return result.append("}").toString();
         **/
    }

    private void init(final String formatText) {
        if (formatText != null && !formatText.trim().isEmpty()) {
            try {
                index = Integer.parseInt(formatText.substring(0, 1));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid index portion of format.", e);
            }
        }
    }
}
