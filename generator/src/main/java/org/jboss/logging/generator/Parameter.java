package org.jboss.logging.generator;

/**
 * Date: 29.07.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public interface Parameter extends Comparable<Parameter> {

    /**
     * The full type name of the parameter. For example
     * {@code java.lang.String} if the parameter is a string. If the
     * parameter is a primitive, the primitive name is returned.
     *
     * @return the qualified type of the parameter.
     */
    String type();

    /**
     * The variable name of the parameter.
     *
     * @return the variable name of the parameter.
     */
    String name();

    /**
     * Returns {@code true} if the type is an array, otherwise {@code false}.
     *
     * @return {@code true} if an array, otherwise {@code false}
     */
    boolean isArray();

    /**
     * Returns {@code true} if the type is a primitive type, otherwise {@code false}.
     *
     * @return {@code true} if primitive type, otherwise {@code false}
     */
    boolean isPrimitive();

    /**
     * Returns {@code true} if the parameter is a var args parameter, otherwise {@code false}.
     *
     * @return {@code true} if var args parameter, otherwise {@code false}.
     */
    boolean isVarArgs();
}
