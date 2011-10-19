package org.jboss.logging.generator.intf.model;

/**
 * Date: 23.08.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public interface MessageObjectType extends MessageObject {

    /**
     * Returns the qualified type name of the object.
     * <p/>
     * Equivalent to {@code Object.class.getName()}
     *
     * @return the qualified class name.
     */
    String type();

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

    /**
     * Determines if this type is the same type as the class represented by the {@code type} parameter. If this type is
     * the same type as the class {@code true} is returned, otherwise {@code false}.
     *
     * @param type the class type to check.
     *
     * @return {@code true} if this type is the same type as the class, otherwise {@code false}.
     */
    boolean isSameAs(Class<?> type);
}
