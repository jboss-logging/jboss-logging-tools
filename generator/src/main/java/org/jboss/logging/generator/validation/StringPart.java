package org.jboss.logging.generator.validation;

/**
 * Represents the string portions of a format string.
 * <p/>
 * Date: 13.06.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
class StringPart extends AbstractFormatPart {

    private final int position;
    private final String part;

    /**
     * Creates a new string part.
     *
     * @param position the position.
     * @param part     the string.
     */
    public StringPart(final int position, final String part) {
        this.position = position;
        this.part = part;
    }

    /**
     * Creates a new string part.
     *
     * @param position the position.
     * @param part     the string.
     *
     * @return the string part.
     */
    public static StringPart of(final int position, final String part) {
        return new StringPart(position, part);
    }

    @Override
    public int index() {
        return STRING;
    }

    @Override
    public int position() {
        return position;
    }

    @Override
    public String part() {
        return part;
    }
}
