package org.jboss.logging.generator;

import java.util.Set;

/**
 * Date: 29.07.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public interface MessageMethod extends Comparable<MessageMethod>, MessageObject {


    /**
     * Returns {@code true} if the method has a message id, otherwise {@code false},
     *
     * @return {@code true} if the method has a message id, otherwise {@code false},
     */
    boolean hasMessageId();

    /**
     * Returns the message format.
     *
     * @return the message format.
     */
    Annotations.FormatType messageFormat();

    /**
     * Returns the Message annotation associated with this method.
     *
     * @return the message annotation.
     */
    String messageValue();

    /**
     * Returns the id of the message.
     *
     * @return the id of the message.
     */
    int messageId();

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
}
