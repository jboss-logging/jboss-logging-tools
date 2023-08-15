/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2023 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
