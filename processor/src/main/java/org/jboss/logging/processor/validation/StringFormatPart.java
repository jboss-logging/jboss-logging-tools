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


import java.util.Collections;
import java.util.DuplicateFormatFlagsException;
import java.util.IllegalFormatPrecisionException;
import java.util.IllegalFormatWidthException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UnknownFormatConversionException;
import java.util.UnknownFormatFlagsException;

/**
 * The parameter part of a format for {@link java.util.Formatter}.
 * <p/>
 * Represents the following format {@linkplain %[argument_index$][flags][width][.precision]conversion}.
 * <p/>
 * Date: 13.06.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
class StringFormatPart extends AbstractFormatPart {
    private int index;
    private final Set<Flag> flags;
    private int width;
    private int precision;
    private Conversion conversion;
    private Character dateTimeConversion;
    private final int position;

    /**
     * Creates a parameter format part.
     *
     * @param position the position in the string format.
     */
    private StringFormatPart(final int position) {
        this.flags = new LinkedHashSet<>();
        this.position = position;
    }

    /**
     * Creates a parameter part of a format string.
     *
     * @param position the position of the part.
     * @param group    the group array of the formats (must be a length of 6).
     *
     * @return the the parameter part.
     *
     * @throws IllegalArgumentException if the length of the group array is not equal to 6 or a format was invalid.
     */
    public static StringFormatPart of(final int position, final String[] group) throws IllegalArgumentException {
        if (group.length != 6) {
            throw new IllegalArgumentException("Groups must have a length of 6.");
        }
        final StringFormatPart result = new StringFormatPart(position);
        int charIndex = 0;
        result.initIndex(group[charIndex++]);
        result.initFlags(group[charIndex++]);
        result.initWidth(group[charIndex++]);
        result.initPrecision(group[charIndex++]);
        // Handle date/time
        if (group[charIndex] != null && group[charIndex].equalsIgnoreCase(Conversion.DATE_TIME.asString())) {
            result.conversion = Conversion.DATE_TIME;
            result.dateTimeConversion = group[++charIndex].charAt(0);
        } else {
            result.conversion = Conversion.fromChar(group[++charIndex].charAt(0));
            result.dateTimeConversion = null;
        }
        return result;
    }

    /**
     * Returns the format parameter index.
     * <p/>
     * If the index is inherited, {@code -1} is returned.
     *
     * @return the format parameter index.
     */
    public int index() {
        return index;
    }

    /**
     * A collection of the flags.
     *
     * @return the flags.
     */
    public Set<Flag> flags() {
        return Collections.unmodifiableSet(flags);
    }

    /**
     * The width for the format.
     * <p/>
     * If the width was not specified, {@code -1} is returned.
     *
     * @return the width.
     */
    public int width() {
        return width;
    }

    /**
     * The precision for the format.
     * <p/>
     * If the precision was not specified, {@code -1} is returned.
     *
     * @return the precision.
     */
    public int precision() {
        return precision;
    }

    /**
     * The conversion for the string format.
     *
     * @return the conversion.
     */
    public Conversion conversion() {
        return conversion;
    }

    /**
     * The date/time conversion character.
     * <p/>
     * {@code null} if there is not date time conversion character.
     *
     * @return the date/time conversion character or {@code null}.
     */
    public char dateTimeChar() {
        return dateTimeConversion;
    }

    /**
     * Initializes the index field based on the string.
     *
     * @param s the index in string form.
     *
     * @throws IllegalArgumentException if the string is not a number.
     */
    private void initIndex(final String s) throws IllegalArgumentException {
        if (s == null) {
            index = 0;
        } else {
            try {
                index = Integer.parseInt(s.substring(0, s.length() - 1));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid index argument.", e);
            }
        }
    }

    /**
     * Initializes the flags based on the string.
     * <p/>
     * Will set the {@link org.jboss.logging.processor.validation.StringFormatPart#index} to {@code -1} if the {@link Flag#PREVIOUS} flag is found.
     *
     * @param s the flags in string form.
     *
     * @throws java.util.DuplicateFormatFlagsException
     *          if the flag is specified more than once.
     */
    private void initFlags(final String s) throws DuplicateFormatFlagsException {
        final char[] chars = s.toCharArray();
        for (char c : chars) {
            final Flag flag = Flag.parse(c);
            if (flags.contains(flag)) {
                throw new DuplicateFormatFlagsException(String.format("Duplicate %s flag found. Current flags: %s", flag, flags));
            }
            flags.add(flag);
        }
        if (flags.contains(Flag.PREVIOUS)) {
            index = -1;
        }
    }

    /**
     * Initializes the width based on the string.
     *
     * @param s the width in string form.
     *
     * @throws IllegalArgumentException the string is an invalid number.
     */
    private void initWidth(final String s) throws IllegalArgumentException {
        if (s == null) {
            width = -1;
        } else {
            try {
                width = Integer.parseInt(s);
                if (width < 0) {
                    throw new IllegalFormatWidthException(width);
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid width argument.", e);
            }
        }
    }

    /**
     * Initializes the precision based on the string.
     *
     * @param s the precision in string form.
     *
     * @throws IllegalArgumentException if the precision is less than 0 or an invalid number.
     */
    private void initPrecision(final String s) throws IllegalArgumentException {
        if (s == null) {
            precision = -1;
        } else {
            try {
                // Remove the '.' from the prevision
                precision = Integer.parseInt(s.substring(1));
                if (precision < 0)
                    throw new IllegalFormatPrecisionException(precision);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid precision argument.", e);
            }
        }
    }

    @Override
    public int position() {
        return position;
    }

    @Override
    public String part() {
        final StringBuilder result = new StringBuilder("%");
        if (index > 0) {
            result.append(index).append("$");
        }
        if (!flags.isEmpty()) {
            for (Flag flag : flags) {
                result.append(flag.flag);
            }
        }
        if (width > 0) {
            result.append(width);
        }
        if (precision > 0) {
            result.append(".").append(precision);
        }
        result.append(conversion.asChar());
        if (conversion.isDateTime() && dateTimeConversion != null) {
            result.append(dateTimeConversion);
        }
        return result.toString();
    }

    @Override
    public String toString() {
        return new StringBuilder(getClass().getSimpleName()).append("[")
                .append("index=")
                .append(index)
                .append(", flags=")
                .append(flags)
                .append(", width=")
                .append(width)
                .append(", precision=")
                .append(precision)
                .append(", conversion=")
                .append(conversion)
                .append("]")
                .toString();
    }

    /**
     * The flags for the string message format.
     *
     * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
     */
    public enum Flag {
        /**
         * The result will be left-justified.
         * </p>
         * Works on all conversions.
         */
        LEFT_JUSTIFY('-'),

        /**
         * The result should use a conversion-dependent alternate form.
         * </p>
         * Works on conversions {@link org.jboss.logging.processor.validation.StringFormatPart.Conversion#OCTAL_INTEGER}, {@link org.jboss.logging.processor.validation.StringFormatPart.Conversion#HEX_INTEGER}, all floating points
         * and most general conversions depending on the definition of {@link java.util.Formattable}.
         */
        CONVERSION_DEPENDENT_ALTERNATE('#'),

        /**
         * The result will always include a sign.
         * </p>
         * Works on all floating points, {@link org.jboss.logging.processor.validation.StringFormatPart.Conversion#DECIMAL_INTEGER}, {@link org.jboss.logging.processor.validation.StringFormatPart.Conversion#OCTAL_INTEGER},
         * {@link org.jboss.logging.processor.validation.StringFormatPart.Conversion#HEX_INTEGER} when applied to {@link java.math.BigInteger} or
         * {@link org.jboss.logging.processor.validation.StringFormatPart.Conversion#DECIMAL_INTEGER} when applied to {@code byte}, {@link Byte}, {@code short}, {@link Short},
         * {@code int}, {@link Integer}, {@code long} and {@link Long}.
         */
        INCLUDE_SIGN('+'),

        /**
         * The result will include a leading space for positive values.
         * </p>
         * Works on all floating points, {@link org.jboss.logging.processor.validation.StringFormatPart.Conversion#DECIMAL_INTEGER}, {@link org.jboss.logging.processor.validation.StringFormatPart.Conversion#OCTAL_INTEGER},
         * {@link org.jboss.logging.processor.validation.StringFormatPart.Conversion#HEX_INTEGER} when applied to {@link java.math.BigInteger} or
         * {@link org.jboss.logging.processor.validation.StringFormatPart.Conversion#DECIMAL_INTEGER} when applied to {@code byte}, {@link Byte}, {@code short}, {@link Short},
         * {@code int}, {@link Integer}, {@code long} and {@link Long}.
         */
        SPACE_FOR_POSITIVE_VALUES(' '),

        /**
         * The result will be zero-padded.
         * </p>
         * Works on all integrals and floating points.
         */
        ZERO_PADDED('0'),

        /**
         * The result will include locale-specific grouping separators.
         * </p>
         * Works only on {@link org.jboss.logging.processor.validation.StringFormatPart.Conversion#DECIMAL_INTEGER} integrals and {@link org.jboss.logging.processor.validation.StringFormatPart.Conversion#SCIENTIFIC_NOTATION},
         * {@link org.jboss.logging.processor.validation.StringFormatPart.Conversion#DECIMAL} and {@link org.jboss.logging.processor.validation.StringFormatPart.Conversion#SCIENTIFIC_NOTATION_OR_DECIMAL} floating points.
         */
        LOCALE_GROUPING_SEPARATOR(','),

        /**
         * The result will enclose negative numbers in parentheses.
         * </p>
         * Works only on {@link org.jboss.logging.processor.validation.StringFormatPart.Conversion#DECIMAL_INTEGER}, {@link org.jboss.logging.processor.validation.StringFormatPart.Conversion#OCTAL_INTEGER},
         * {@link org.jboss.logging.processor.validation.StringFormatPart.Conversion#HEX_INTEGER} when applied to {@link java.math.BigInteger} or
         * {@link org.jboss.logging.processor.validation.StringFormatPart.Conversion#DECIMAL_INTEGER} when applied to {@code byte}, {@link Byte}, {@code short}, {@link Short},
         * {@code int}, {@link Integer}, {@code long} and {@link Long} integrals and {@link org.jboss.logging.processor.validation.StringFormatPart.Conversion#SCIENTIFIC_NOTATION},
         * {@link org.jboss.logging.processor.validation.StringFormatPart.Conversion#DECIMAL} and {@link org.jboss.logging.processor.validation.StringFormatPart.Conversion#SCIENTIFIC_NOTATION_OR_DECIMAL} floating points.
         */
        PARENTHESES_FOR_NEGATIVES('('),

        /**
         * The previous position.
         */
        PREVIOUS('<');

        /**
         * The character flag.
         */
        final char flag;

        private Flag(final char flag) {
            this.flag = flag;
        }

        @Override
        public String toString() {
            return new StringBuilder(name()).append("(").append(flag).append(")").toString();
        }

        /**
         * Checks to see if the character is a valid flag.
         *
         * @param c the character to check.
         *
         * @return the corresponding flag for the character.
         *
         * @throws java.util.UnknownFormatFlagsException
         *          if the flag is invalid.
         */
        public static Flag parse(final char c) throws UnknownFormatFlagsException {
            for (Flag flag : Flag.values()) {
                if (flag.flag == c) {
                    return flag;
                }
            }
            throw new UnknownFormatFlagsException("Invalid flag: " + c);
        }
    }

    /**
     * The conversions for the string format.
     *
     * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
     */
    public enum Conversion {
        /**
         * A boolean conversion 'c' or 'C'.
         */
        BOOLEAN('b', true) {
            @Override
            public boolean isGeneral() {
                return true;
            }
        },

        /**
         * A hexadecimal conversion 'h' or 'H'.
         */
        HEX('h', true) {
            @Override
            public boolean isGeneral() {
                return true;
            }
        },

        /**
         * A string conversion 's' or 'S'.
         */
        STRING('s', true) {
            @Override
            public boolean isGeneral() {
                return true;
            }
        },

        /**
         * A unicode conversion 'c' or 'C'.
         */
        UNICODE_CHAR('c', true) {
            @Override
            public boolean isCharacter() {
                return true;
            }
        },

        /**
         * A decimal integer conversion 'd'.
         */
        DECIMAL_INTEGER('d', false) {
            @Override
            public boolean isIntegral() {
                return true;
            }
        },

        /**
         * An octal integer conversion 'o'.
         */
        OCTAL_INTEGER('o', false) {
            @Override
            public boolean isIntegral() {
                return true;
            }
        },

        /**
         * A hexadecimal integer conversion 'x' or 'X'.
         */
        HEX_INTEGER('x', true) {
            @Override
            public boolean isIntegral() {
                return true;
            }
        },

        /**
         * A scientific notation conversion 'e' or 'E'.
         */
        SCIENTIFIC_NOTATION('e', true) {
            @Override
            public boolean isFloatingPoint() {
                return true;
            }
        },

        /**
         * A decimal conversion 'f'.
         */
        DECIMAL('f', false) {
            @Override
            public boolean isFloatingPoint() {
                return true;
            }
        },

        /**
         * A scientific notation or decimal 'g' or 'G'
         */
        SCIENTIFIC_NOTATION_OR_DECIMAL('g', true) {
            @Override
            public boolean isFloatingPoint() {
                return true;
            }
        },

        /**
         * A hexadecimal floating point number 'a' or 'A'.
         */
        HEX_FLOATING_POINT('a', true) {
            @Override
            public boolean isFloatingPoint() {
                return true;
            }
        },

        /**
         * A date or time conversion 't' or 'T'.
         */
        DATE_TIME('t', true) {
            @Override
            public boolean isDateTime() {
                return true;
            }
        },

        /**
         * A percentage conversion '%'.
         */
        PERCENT('%', false) {
            @Override
            public boolean isPercent() {
                return true;
            }
        },

        /**
         * A line separator conversion 'n'.
         */
        LINE_SEPARATOR('n', false) {
            @Override
            public boolean isLineSeparator() {
                return true;
            }
        };


        private final char conversion;
        /**
         * @code true} for the case should be ignored, otherwise {@code false}
         */
        final boolean ignoreCase;

        /**
         * Private enum conversion.
         *
         * @param conversion the conversion character.
         * @param ignoreCase {@code true} for the case should be ignored, otherwise {@code false}.
         */
        private Conversion(final char conversion, final boolean ignoreCase) {
            this.conversion = conversion;
            this.ignoreCase = ignoreCase;
        }

        /**
         * If the conversion is a general conversion {@code true} is returned, otherwise {@code false}.
         *
         * @return {@code true} if a general conversion, otherwise {@code false}.
         */
        public boolean isGeneral() {
            return false;
        }

        /**
         * If the conversion is a character {@code true} is returned, otherwise {@code false}.
         *
         * @return {@code true} if c character, otherwise {@code false}.
         */
        public boolean isCharacter() {
            return false;
        }

        /**
         * If the conversion is an integral {@code true} is returned, otherwise {@code false}.
         *
         * @return {@code true} if an integral, otherwise {@code false}.
         */
        public boolean isIntegral() {
            return false;
        }

        /**
         * If the conversion is a floating point {@code true} is returned, otherwise {@code false}.
         *
         * @return {@code true} if a line separator, otherwise {@code false}.
         */
        public boolean isFloatingPoint() {
            return false;
        }

        /**
         * If the conversion is a date or time {@code true} is returned, otherwise {@code false}.
         *
         * @return {@code true} if a date or time, otherwise {@code false}.
         */
        public boolean isDateTime() {
            return false;
        }

        /**
         * If the conversion is a percent {@code true} is returned, otherwise {@code false}.
         *
         * @return {@code true} if a percent, otherwise {@code false}.
         */
        public boolean isPercent() {
            return false;
        }

        /**
         * If the conversion is a line separator {@code true} is returned, otherwise {@code false}.
         *
         * @return {@code true} if a line separator, otherwise {@code false}.
         */
        public boolean isLineSeparator() {
            return false;
        }

        /**
         * Returns the conversion character.
         *
         * @return the conversion character.
         */
        public char asChar() {
            return conversion;
        }

        /**
         * Returns the conversion character as a {@link String}
         *
         * @return the conversion character.
         */
        public String asString() {
            return String.valueOf(conversion);
        }

        @Override
        public String toString() {
            final StringBuilder result = new StringBuilder(name());
            result.append("(");
            result.append(conversion);
            if (ignoreCase) {
                result.append(", ").append(Character.toUpperCase(conversion));
            }
            result.append(")");
            return result.toString();
        }

        /**
         * Converts the character into a conversion descriptor.
         *
         * @param c the character to convert.
         *
         * @return the conversion descriptor.
         *
         * @throws java.util.UnknownFormatConversionException
         *          if the character is not a valid conversion format.
         */
        public static Conversion fromChar(final char c) throws UnknownFormatConversionException {
            for (Conversion conversion : Conversion.values()) {
                if (conversion.ignoreCase && conversion.asChar() == Character.toLowerCase(c)) {
                    return conversion;
                } else if (conversion.asChar() == c) {
                    return conversion;
                }
            }
            throw new UnknownFormatConversionException(String.valueOf(c));
        }
    }
}
