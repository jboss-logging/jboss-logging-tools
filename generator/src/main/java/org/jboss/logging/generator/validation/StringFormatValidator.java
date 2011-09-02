/*
 * Boss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

package org.jboss.logging.generator.validation;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.IllegalFormatException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UnknownFormatConversionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A string format representation.
 * <p/>
 * Date: 13.06.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
class StringFormatValidator extends AbstractFormatValidator {
    /**
     * The Regex pattern.
     */
    public static final String PATTERN = "%(\\d+\\$)?([-#+ 0,(\\<]*)?(\\d+)?(\\.\\d+)?([tT])?([a-zA-Z%])";

    private final Set<FormatPart> formatParts = new TreeSet<FormatPart>();
    private final Set<StringFormatPart> formats = new TreeSet<StringFormatPart>();
    private int argumentCount;
    private boolean valid;
    private final String format;

    /**
     * Private constructor for the singleton pattern.
     *
     * @param format the format.
     */
    private StringFormatValidator(final String format) {
        super();
        this.format = format;
        this.valid = true;
    }

    /**
     * Creates a string format.
     *
     * @param format the format.
     *
     * @return the string format.
     */
    public static StringFormatValidator of(final String format) {
        final StringFormatValidator result = new StringFormatValidator(format);
        try {
            result.init();
            result.validate();
        } catch (RuntimeException e) {
            if (result.isValid()) {
                result.valid = false;
                result.setDetailMessage("Format '%s' appears to be invalid. Error: %s", format, e.getMessage());
            }
        }
        return result;
    }

    /**
     * Creates a string format.
     *
     * @param format     the format.
     * @param parameters the parameters to validate against.
     *
     * @return the string format.
     */
    public static StringFormatValidator of(final String format, final Object... parameters) {
        final StringFormatValidator result = new StringFormatValidator(format);
        try {
            result.init();
            result.validate(parameters);
        } catch (RuntimeException e) {
            if (result.isValid()) {
                result.valid = false;
                result.setSummaryMessage("Format '%s' appears to be invalid. Error: %s", format, e.getMessage());
            }
        }
        return result;
    }

    /**
     * Validates
     */
    private void validate() {
        if (!format.equalsIgnoreCase(asFormat())) {
            valid = false;
            setSummaryMessage("Formats don't match. Internal error: %s Reconstructed: %s", format, asFormat());
            setDetailMessage("The original is '%s' and the reconstructed format is '%s'. This is likely an internal error and should be reported.", format, asFormat());
        } else {
            // Attempt to use String.format() with default values
            final List<Object> params = new LinkedList<Object>();
            int counter = 0;
            // Initialize the argument count
            for (StringFormatPart stringFormatPart : formats) {
                if (counter == argumentCount) {
                    break;
                }
                counter++;
                switch (stringFormatPart.conversion()) {
                    case BOOLEAN:
                        params.add(true);
                        break;
                    case DATE_TIME:
                        params.add(new Date());
                        break;
                    case DECIMAL:
                    case HEX_FLOATING_POINT:
                        params.add(1.5f);
                        break;
                    case DECIMAL_INTEGER:
                    case HEX_INTEGER:
                    case OCTAL_INTEGER:
                        params.add(33);
                        break;
                    case HEX:
                    case STRING:
                        params.add("JBoss");
                        break;
                    case SCIENTIFIC_NOTATION:
                    case SCIENTIFIC_NOTATION_OR_DECIMAL:
                        params.add(10000.55050d);
                        break;
                    case UNICODE_CHAR:
                        params.add('c');
                        break;
                    case LINE_SEPARATOR:
                    case PERCENT:
                        counter--;
                        break;
                    default:
                        valid = false;
                        setSummaryMessage("Format not found: %s", stringFormatPart);
                }
            }
            if (valid) {
                try {
                    String.format(format, params.toArray());
                } catch (final IllegalFormatException e) {
                    valid = false;
                    setSummaryMessage("Invalid format for '%s' with parameters '%s'. java.util.Formatter Error: %s", format, params, e.getMessage());
                    setDetailMessage("Format '%s' with parameters '%s' is invalid. StringFormatValidator: %s", format, params, this);
                }
            }
        }
    }

    private void validate(final Object... parameters) {
        // First perform general validation
        validate();
        // Now test parameters counts
        final int paramCount = (parameters == null ? 0 : parameters.length);
        if (argumentCount != paramCount) {
            valid = false;
            setSummaryMessage("Parameter lengths do not match. Format (%s) requires %d arguments, supplied %d.", format, argumentCount, paramCount);
        }
        // Create a parameter list based on the parameters passed
        if (valid) {
            try {
                String.format(format, parameters);
            } catch (final IllegalFormatException e) {
                valid = false;
                setSummaryMessage("Invalid format for '%s' with parameters '%s'. java.util.Formatter Error: %s", format, Arrays.asList(parameters), e.getMessage());
                setDetailMessage("Format '%s' with parameters '%s' is invalid. StringFormatValidator: %s", format, Arrays.asList(parameters), this);
            }
        }
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

    /**
     * Recreates the format using the internal formatting descriptors.
     *
     * @return the format.
     */
    public String asFormat() {
        final StringBuilder result = new StringBuilder();
        for (FormatPart formatPart : formatParts) {
            result.append(formatPart.part());
        }
        return result.toString();
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

    /**
     * Initialize the string format.
     */
    private void init() {
        final Pattern pattern = Pattern.compile(PATTERN);
        final Matcher matcher = pattern.matcher(format);
        int position = 0;
        int i = 0;
        while (i < format.length()) {
            if (matcher.find(i)) {
                if (matcher.start() != i) {
                    formatParts.add(StringPart.of(position++, format.substring(i, matcher.start())));
                }

                // Pattern should produce 6 groups.
                final String[] formatGroup = new String[6];
                for (int groupIndex = 0; groupIndex < matcher.groupCount(); groupIndex++) {
                    formatGroup[groupIndex] = matcher.group(groupIndex + 1);
                }
                final StringFormatPart stringFormatPart = StringFormatPart.of(position++, formatGroup);
                formatParts.add(stringFormatPart);
                formats.add(stringFormatPart);
                i = matcher.end();
            } else {
                // No more formats found, but validate for invalid remaining characters.
                checkText(format.substring(i));
                formatParts.add(StringPart.of(position, format.substring(i)));
                break;
            }
        }
        final Set<Integer> counted = new HashSet<Integer>();
        // Initialize the argument count
        for (StringFormatPart stringFormatPart : formats) {
            if (stringFormatPart.conversion().isLineSeparator() || stringFormatPart.conversion().isPercent())
                continue;
            if (stringFormatPart.index() > 0) {
                if (counted.add(stringFormatPart.index()))
                    argumentCount++;
            } else if (stringFormatPart.index() == 0) {
                argumentCount++;
            }
        }
    }


    /**
     * Checks text to make sure we don't have extra garbage.
     *
     * @param text the text to check.
     */
    private static void checkText(final String text) {
        final int index;
        // If there are any '%' in the given string, we got a bad format
        // specifier.
        if ((index = text.indexOf('%')) != -1) {
            final char c = (index > text.length() - 2 ? '%' : text.charAt(index + 1));
            throw new UnknownFormatConversionException(String.valueOf(c));
        }
    }
}
