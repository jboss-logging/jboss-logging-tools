package org.jboss.logging.generator.intf.model;

/**
 * Date: 23.08.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public interface MessageObjectType {

    /**
     * Determines if this type is either the same as, or is a supertype of, the class represented by the {@code type}
     * parameter. If this type is assignable from the class {@code true} is returned, otherwise {@code false}.
     *
     * @param type the class type to check.
     *
     * @return {@code true} if this type is the same as or a superclass of the class, otherwise {@code false}.
     */
    boolean isAssignableFrom(Class<?> type);

    /**
     * Determines if this type is a subtype of the class represented by the {@code type} parameter. If this type is a
     * subtype of the class {@code true} is returned, otherwise {@code false}.
     *
     * @param type the class type to check.
     *
     * @return {@code true} if this type is a subtype of the class, otherwise {@code false}.
     */
    boolean isSubtypeOf(Class<?> type);
}
