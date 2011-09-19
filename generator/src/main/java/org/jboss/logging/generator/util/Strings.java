package org.jboss.logging.generator.util;

/**
 * Date: 19.09.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class Strings {
    private Strings() {
    }

    /**
     * Creates a string filled with the with the value of the {@code filler} parameter with a length defined by the
     * {@code len} parameter.
     *
     * @param filler the filler character.
     * @param len    the length to fill.
     *
     * @return the generated string.
     */
    public static String fill(final char filler, final int len) {
        final StringBuilder result = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            result.append(filler);
        }
        return result.toString();
    }


    /**
     * Creates a string filled with the with the value of the {@code filler} parameter with a length defined by the
     * {@code len} parameter.
     *
     * @param filler the filler sequence.
     * @param len    the length to fill.
     *
     * @return the generated string.
     */
    public static String fill(final CharSequence filler, final int len) {
        final StringBuilder result = new StringBuilder((filler.length() * len));
        for (int i = 0; i < len; i++) {
            result.append(filler);
        }
        return result.toString();
    }
}
