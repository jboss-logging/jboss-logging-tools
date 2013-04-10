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

package org.jboss.logging.processor.validation;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a {@link java.text.MessageFormat} string.
 * <p/>
 * <i><b>**Note:</b> Currently the format type and format style are not validated</i>
 * <p/>
 * Date: 14.06.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
class MessageFormatValidator extends AbstractFormatValidator {
    public static final Pattern PATTERN = Pattern.compile("\\{}|\\{.+?}");

    private final Set<FormatPart> formatParts = new TreeSet<FormatPart>();
    private final Set<MessageFormatPart> formats = new TreeSet<MessageFormatPart>();
    private int argumentCount;
    private boolean valid;
    private final String format;


    private MessageFormatValidator(final String format) {
        super();
        this.format = format;
        valid = true;
    }

    public static MessageFormatValidator of(final String format) {
        final MessageFormatValidator result = new MessageFormatValidator(format);
        result.init();
        result.validate();
        return result;
    }

    public static MessageFormatValidator of(final String format, final Object... parameters) {
        final MessageFormatValidator result = new MessageFormatValidator(format);
        result.init();
        result.validate();
        result.parameterCheck(parameters);
        return result;
    }

    public static MessageFormatValidator of(final String format, final int parameterCount) {
        final MessageFormatValidator result = new MessageFormatValidator(format);
        result.init();
        result.validate();
        result.parameterCheck(parameterCount);
        return result;
    }

    @Override
    public int argumentCount() {
        return argumentCount;
    }

    @Override
    public String format() {
        return format;
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    private void validate() {
        // Simple argument parser
        int start = format.indexOf("{");
        int end = format.indexOf("}");
        while (start != -1 && valid) {
            if (end < start) {
                valid = false;
                setSummaryMessage("Format %s appears to be missing an ending bracket.", format);
            }
            start = format.indexOf("{", end);
            if (start > 0) {
                end = format.indexOf("}", start);
            }
        }
    }

    private void parameterCheck(final Object... parameters) {
        if (argumentCount > 0 && parameters == null) {
            valid = false;
            setSummaryMessage("Invalid parameter count. Required %d provided null for format '%s'.", argumentCount, format);
            setDetailMessage("Required %d parameters, but none were provided for format %s.", argumentCount, format);
        } else {
            parameterCheck(parameters.length);
        }

    }

    private void parameterCheck(final int parameterCount) {
        if (argumentCount != parameterCount) {
            valid = false;
            setSummaryMessage("Invalid parameter count. Required: %d provided %d for format '%s'.", argumentCount, parameterCount, format);
            setDetailMessage("Required %d parameters, but %d were provided for format %s.", argumentCount, parameterCount, format);
        }

    }

    private void init() {
        final Matcher matcher = PATTERN.matcher(format);
        int position = 0;
        int i = 0;
        while (i < format.length()) {
            if (matcher.find(i)) {
                if (matcher.start() != i) {
                    formatParts.add(StringPart.of(position++, format.substring(i, matcher.start())));
                }
                final MessageFormatPart messageFormatPart = MessageFormatPart.of(position++, matcher.group());
                formatParts.add(messageFormatPart);
                formats.add(messageFormatPart);
                i = matcher.end();
            } else {
                formatParts.add(StringPart.of(position, format.substring(i)));
                break;
            }
        }
        final Set<Integer> counted = new HashSet<Integer>();
        // Initialize the argument count
        for (MessageFormatPart messageFormatPart : formats) {
            if (messageFormatPart.index() >= 0) {
                if (counted.add(messageFormatPart.index()))
                    argumentCount++;
            }
        }
    }

    @Override
    public String toString() {
        return new StringBuilder(getClass().getSimpleName()).append("[")
                .append("formatParts=")
                .append(formatParts)
                .append(", argumentCount=")
                .append(argumentCount)
                .append("]").toString();
    }

}
