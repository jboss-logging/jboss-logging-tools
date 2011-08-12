package org.jboss.logging.generator;

/**
 * A generic interface for returning basic information about parts of a message bundle or message logger interface.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public interface MessageObject {

    /**
     * The object used to extract information for the message logger or message bundle, if applicable. The reference is
     * not used for the implementation and is provided for convenience.
     * <p/>
     * For example, in an annotation processor implementation a {@link javax.lang.model.element.ExecutableElement}
     * might be returned.
     *
     * @return the reference object used to extract information.
     */
    Object reference();
}
