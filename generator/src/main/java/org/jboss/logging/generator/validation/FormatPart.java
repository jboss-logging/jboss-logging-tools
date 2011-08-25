package org.jboss.logging.generator.validation;

/**
 * Date: 13.06.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
interface FormatPart extends Comparable<FormatPart> {

    /**
     * The default string index.
     */
    int STRING = -2;

    /**
     * The parameter index. For default strings (non-parameters) the value is {@code -2}.
     *
     * @return the index.
     */
    int index();

    /**
     * The position for the part.
     *
     * @return the position.
     */
    int position();

    /**
     * The part of the format.
     *
     * @return the part of the format.
     */
    String part();
}
