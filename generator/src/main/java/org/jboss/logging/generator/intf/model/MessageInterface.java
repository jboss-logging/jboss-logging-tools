package org.jboss.logging.generator.intf.model;

import java.util.Collection;
import java.util.Set;

/**
 * Date: 28.07.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public interface MessageInterface extends Comparable<MessageInterface>, MessageObject, MessageObjectType {

    /**
     * Checks the interface to see if the {@link org.jboss.logging.generator.Loggers#loggerInterface() logger interface}
     * is being extended in this interface.
     *
     * @return {@code true} if this interface extends the logger interface, otherwise {@code false}.
     */
    boolean extendsLoggerInterface();

    /**
     * A set of qualified interface names this interface extends or an empty set.
     *
     * @return a set of interface names or an empty set.
     */
    Set<MessageInterface> extendedInterfaces();

    /**
     * A collection of all the methods this interface needs to implement.
     *
     * @return a collection of methods.
     */
    Collection<MessageMethod> methods();

    /**
     * The project code for the message interface or {@code null} if {@link #isLoggerInterface()} returns {@code true}.
     *
     * @return the project code or {@code null} if {@link #isLoggerInterface()} returns {@code true}.
     */
    String projectCode();

    /**
     * The qualified name of the message interface.
     *
     * @return the qualified name.
     */
    @Override
    String name();

    /**
     * The package name of the message interface.
     *
     * @return the package name.
     */
    String packageName();

    /**
     * The name of the interface without the package.
     *
     * @return the simple interface name.
     */
    String simpleName();

    /**
     * The fully qualified class name to use for log methods. This will generally be the same result as {@link #name()}.
     *
     * @return the fully qualified class name to use for logging.
     */
    String loggingFQCN();

    /**
     * Returns {@code true} if the interface is annotated as a message logger, otherwise {@code false}.
     *
     * @return {@code true} if a message logger, otherwise {@code false}.
     */
    boolean isMessageLogger();

    /**
     * Returns {@code true} if the interface is annotated as a message bundle, otherwise {@code false}.
     *
     * @return {@code true} if a message bundle, otherwise {@code false}.
     */
    boolean isMessageBundle();

    /**
     * This is a special type of {@code MessageInterface} and will only return {@code true} if this is a
     * {@link org.jboss.logging.generator.Loggers#loggerInterface() logger interface}. Otherwise {@code false} is
     * returned.
     * <p/>
     * <b>Note:</b> {@link #isMessageBundle()} and {@link #isMessageLogger()} will return {@code false} if this is
     * {@code true}.
     *
     * @return {@code true} if this is a logger interface, otherwise {@code false}.
     */
    boolean isLoggerInterface();
}
