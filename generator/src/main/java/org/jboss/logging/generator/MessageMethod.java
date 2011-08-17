package org.jboss.logging.generator;

import java.util.Set;

/**
 * Date: 29.07.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public interface MessageMethod extends Comparable<MessageMethod>, MessageObject {

    /**
     * The {@link Message} to be used for the method.
     *
     * @return the message.
     */
    Message message();

    /**
     * Indicates whether the message was inherited from another message or not. If {@code true} is returned the
     * {@link Message} was inherited from a different method, otherwise {@code false}.
     * <p/>
     * <b>Note:</b> {@code false} does not indicate the method has a {@link Annotations#message()} annotation.
     *
     * @return {@code true} if the message was inherited from a different method, otherwise {@code false}.
     */
    boolean inheritsMessage();

    /**
     * Returns the name of the method used to retrieve the message.
     *
     * @return the name of the message method.
     */
    String messageMethodName();

    /**
     * Returns the name of the key used in the translation files for the message translation.
     *
     * @return the name of the key in the translation files.
     */
    String translationKey();

    /**
     * Returns the method name.
     *
     * @return the method name.
     */
    @Override
    String name();

    /**
     * Returns {@code true} if there is a cause element, otherwise {@code false}.
     *
     * @return {@code true} if there is a cause element, otherwise {@code false}
     */
    boolean hasCause();

    /**
     * Returns {@code true} if the method is overloaded, otherwise {@code false}
     * .
     *
     * @return {@code true} if the method is overloaded, otherwise {@code false}
     */
    boolean isOverloaded();

    /**
     * Returns the cause element if {@link #hasCause()} returns {@code true}, otherwise {@code null}.
     *
     * @return the cause element, otherwise {@code null}.
     */
    MethodParameter cause();

    /**
     * Returns the return type for the method.
     *
     * @return the return type for the method.
     */
    MessageReturnType returnType();

    /**
     * Returns the LogMessage annotation associated with this methodonly if {@link #isLoggerMethod()} returns
     * {@code true}.
     *
     * @return the log message annotation
     */
    String loggerMethod();

    /**
     * Returns the log level parameter associated with the method only if {@link #isLoggerMethod()} returns
     * {@code true}.
     *
     * @return the log level annotation
     */
    String logLevelParameter();

    /**
     * Returns an unmodifiable collection of the all parameters.
     *
     * @return a collection of the all parameters.
     */
    Set<MethodParameter> allParameters();

    /**
     * Returns an unmodifiable collection of all the parameters used for the formatting of the message.
     *
     * @return a collection of the formatter parameters.
     */
    Set<MethodParameter> formatParameters();

    /**
     * Returns an unmodifiable collection of all the parameters used to construct the result.
     * <p/>
     * <b>Note:</b> this does not include the cause parameter or the message, only methods annotated with
     * {@link org.jboss.logging.generator.Annotations#param()}.
     *
     * @return a collection of parameters used to construct the result.
     */
    Set<MethodParameter> constructorParameters();

    /**
     * Returns the number of parameters minus the cause parameter count for the method.
     *
     * @return the number of parameters minus the cause parameter count for the method.
     */
    int formatParameterCount();

    /**
     * Returns {@code true} if this is a logger method, otherwise {@code false}.
     *
     * @return {@code true} if this is a logger method, otherwise {@code false}.
     */
    boolean isLoggerMethod();


    /**
     * Represents a {@link Annotations#message()} annotation on a method.
     */
    public interface Message {

        /**
         * The message id for the message to use. Any id less than 0 will be ignored.
         *
         * @return the message id.
         */
        int id();

        /**
         * Checks if the message has an id that was provided. Returns {@code true} if the message id was specified or
         * inherited, otherwise returns {@code false}.
         *
         * @return {@code true} if the message id was provided, otherwise {@code false}.
         */
        boolean hasId();

        /**
         * Checks if the message id was inherited. Returns {@code true} only if the message id is inherited, otherwise
         * {@code false} is returned.
         *
         * @return {@code true} if the message id was inherited, otherwise {@code false}.
         */
        boolean inheritsId();

        /**
         * A format string that can be used with the {@link #format()}.
         *
         * @return a format string.
         */
        String value();

        /**
         * The message format type for the message.
         *
         * @return the format type.
         */
        Annotations.FormatType format();
    }
}
