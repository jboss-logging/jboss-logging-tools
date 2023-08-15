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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.IllegalFormatException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
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
public final class StringFormatValidator extends AbstractFormatValidator {
    /**
     * The Regex pattern.
     */

    public static final Pattern PATTERN = Pattern.compile("%(\\d+\\$)?([-#+ 0,(\\<]*)?(\\d+)?(\\.\\d+)?([tT])?([a-zA-Z%])");

    private final Set<FormatPart> formatParts = new TreeSet<>();
    private final Set<StringFormatPart> formats = new TreeSet<>();
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
     * <p/>
     * <b>Note:</b> The validator returned is the validator for the translation format.
     *
     * @param format            the format.
     * @param translationFormat the format of the translation
     *
     * @return the string format.
     */
    public static StringFormatValidator withTranslation(final String format, final String translationFormat) {
        final StringFormatValidator result = new StringFormatValidator(format);
        final StringFormatValidator translationResult = new StringFormatValidator(translationFormat);
        try {
            result.init();
            result.validate();
        } catch (RuntimeException e) {
            if (result.isValid()) {
                result.valid = false;
                result.setDetailMessage("Format '%s' appears to be invalid. Error: %s", format, e.getMessage());
            }
        }
        try {
            translationResult.init();
            translationResult.validate();
        } catch (RuntimeException e) {
            if (translationResult.isValid()) {
                translationResult.valid = false;
                translationResult.setDetailMessage("Format '%s' appears to be invalid. Error: %s", format, e.getMessage());
            }
        }
        // If either is invalid, return the invalid one
        if (!result.isValid())
            return result;

        if (!translationResult.isValid())
            return translationResult;

        // Sort the formats
        final List<StringFormatPart> initParts = sortParts(result.formats);
        final List<StringFormatPart> translationParts = sortParts(translationResult.formats);

        // The size should be the same as well as the position of each element
        if (initParts.size() == translationParts.size()) {
            // Parameters should be in the exact order
            final Iterator<StringFormatPart> initIter = initParts.iterator();
            final Iterator<StringFormatPart> translationIter = translationParts.iterator();
            while (initIter.hasNext()) {
                final StringFormatPart initPart = initIter.next();
                final StringFormatPart translationPart = translationIter.next();
                if (initPart.conversion() != translationPart.conversion()) {
                    translationResult.valid = false;
                    translationResult.setDetailMessage("The translated message format (%s) does not match the initial message format (%s).", translationFormat, format);
                    translationResult.setSummaryMessage("The translated message format (%s) does not match the initial message format (%s).", translationFormat, format);
                    break;
                }
            }
        } else {
            translationResult.valid = false;
            translationResult.setDetailMessage("The translated message format (%s) does not match the initial message format (%s).", translationFormat, format);
            translationResult.setSummaryMessage("The translated message format (%s) does not match the initial message format (%s).", translationFormat, format);
        }

        return translationResult;
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

    static List<StringFormatPart> sortParts(final Collection<StringFormatPart> parts) {
        final TreeMap<Integer, Set<StringFormatPart>> paramMap = new TreeMap<>();
        final int newLineIndex = Integer.MAX_VALUE - 1;
        final int percentageIndex = Integer.MAX_VALUE;
        int index = 0;
        int count = 0;
        for (StringFormatPart part : parts) {
            // Line separators and percentage constants need to be handled specially
            if (part.conversion().isLineSeparator()) {
                final Set<StringFormatPart> set = getOrAdd(paramMap, newLineIndex);
                set.add(part);
                continue;
            } else if (part.conversion().isPercent()) {
                final Set<StringFormatPart> set = getOrAdd(paramMap, percentageIndex);
                set.add(part);
                continue;
            }
            // Check the index and set appropriately
            if (part.index() > 0) {
                index = part.index();
            } else if (part.index() == 0) {
                index = ++count;
            }
            if (!paramMap.containsKey(index)) {
                paramMap.put(index, Collections.singleton(part));
            }
        }
        final List<StringFormatPart> result = new ArrayList<>();
        for (Set<StringFormatPart> p : paramMap.values()) {
            result.addAll(p);
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
            // Create a multimap to hold the parameter values for sorting
            final Map<Integer, List<Object>> paramMap = new TreeMap<>();
            int counter = 0;
            int index = 0;
            // Initialize the argument count
            for (StringFormatPart stringFormatPart : formats) {
                if (counter == argumentCount) {
                    break;
                }
                // Check the index and set appropriately
                if (stringFormatPart.index() > 0 || stringFormatPart.index() == 0) {
                    index = stringFormatPart.index();
                } else if (stringFormatPart.index() < -1) {
                    index = 0;
                }
                // Find or create the list for the multimap.
                final List<Object> params;
                if (paramMap.containsKey(index)) {
                    params = paramMap.get(index);
                    // Skip positional if already defined.
                    if (index > 0) {
                        continue;
                    }
                } else {
                    params = new ArrayList<>();
                    paramMap.put(index, params);
                }
                counter++;
                // Add the type.
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
                    // Copy the results in order to a new list.
                    final List<Object> params = new ArrayList<>();
                    for (Map.Entry<Integer, List<Object>> entry : paramMap.entrySet()) {
                        params.addAll(entry.getValue());
                    }
                    // Test the format
                    String.format(format, params.toArray());
                } catch (final IllegalFormatException e) {
                    valid = false;
                    setSummaryMessage("Invalid format for '%s' with parameters '%s'. java.util.Formatter Error: %s", format, paramMap, e.getMessage());
                    setDetailMessage("Format '%s' with parameters '%s' is invalid. StringFormatValidator: %s", format, paramMap, this);
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
                setSummaryMessage("Invalid format for '%s' with parameters '%s'. java.util.Formatter Error: %s", format, Arrays.toString(parameters), e.getMessage());
                setDetailMessage("Format '%s' with parameters '%s' is invalid. StringFormatValidator: %s", format, Arrays.toString(parameters), this);
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
        final Matcher matcher = PATTERN.matcher(format);
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
        final Set<Integer> counted = new HashSet<>();
        int count = 1;
        // Initialize the argument count
        for (StringFormatPart stringFormatPart : formats) {
            if (stringFormatPart.conversion().isLineSeparator() || stringFormatPart.conversion().isPercent())
                continue;
            if (stringFormatPart.index() > 0) {
                if (counted.add(stringFormatPart.index())) {
                    argumentCount++;
                }
            } else if (stringFormatPart.index() == 0) {
                if (!counted.contains(count)) {
                    argumentCount++;
                    counted.add(count);
                    count++;
                }
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

    private static <K, V extends Comparable<? super V>> Set<V> getOrAdd(final Map<K, Set<V>> map, final K key) {
        Set<V> set = map.get(key);
        if (set == null) {
            set = new TreeSet<>();
            map.put(key, set);
        }
        return set;
    }
}
