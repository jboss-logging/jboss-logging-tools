package org.jboss.logging.generator;

/**
 * For simple usage of defining how parameters should match types for looking up information for things like
 * constructors.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@Deprecated
public class ParameterType {

    /**
     * The matching type.
     */
    public enum MatchType {
        EQUALS,
        SUBTYPE,
        SUPERTYPE;
    }

    private final Class<?> type;
    private final MatchType matchType;

    /**
     * Creates a new parameter type.
     *
     * @param type      the type to be matched.
     * @param matchType the type of the match to which {@link #type()} should be tested.
     */
    public ParameterType(final Class<?> type, final MatchType matchType) {
        this.type = type;
        this.matchType = matchType;
    }

    /**
     * Creates a new parameter type.
     *
     * @param type      the type to be matched.
     * @param matchType the type of the match to which {@link #type()} should be tested.
     *
     * @return the new parameter type.
     */
    public static ParameterType of(final Class<?> type, final MatchType matchType) {
        return new ParameterType(type, matchType);
    }

    /**
     * The type to test against another type.
     *
     * @return the type.
     */
    public Class<?> type() {
        return type;
    }

    /**
     * How the type should match the other type being tested.
     * <p/>
     * For example {@link MatchType#SUBTYPE} indicates the type returned from {@link #type()} is a subtype of the type
     * being tested.
     *
     * @return the match type.
     */
    public MatchType matchType() {
        return matchType;
    }

}
