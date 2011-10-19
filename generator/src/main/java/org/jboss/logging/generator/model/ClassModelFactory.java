package org.jboss.logging.generator.model;

import org.jboss.logging.generator.intf.model.MessageInterface;
import org.jboss.logging.generator.intf.model.MessageMethod;

import java.util.Map;

import static org.jboss.logging.generator.model.ClassModelHelper.implementationClassName;
import static org.jboss.logging.generator.util.TranslationHelper.getEnclosingTranslationClassName;

/**
 * Creates a class model for the message interface.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class ClassModelFactory {

    /**
     * Private constructor for the factory.
     */
    private ClassModelFactory() {

    }

    /**
     * Creates an implementation code model from the message interface.
     *
     * @param messageInterface the message interface to implement.
     *
     * @return the class model used to implement the interface.
     *
     * @throws IllegalArgumentException if {@link MessageInterface#isMessageBundle()} or
     *                                  {@link MessageInterface#isMessageLogger()} returns
     *                                  {@code false.}
     */
    public static ClassModel implementation(final MessageInterface messageInterface) throws IllegalArgumentException {
        if (messageInterface.isMessageBundle()) {
            return new MessageBundleImplementor(messageInterface);
        } else if (messageInterface.isMessageLogger()) {
            return new MessageLoggerImplementor(messageInterface);
        }
        throw new IllegalArgumentException(String.format("Message interface %s is not a valid message logger or message bundle.", messageInterface));
    }

    /**
     * Creates a class model for created translation implementations of the message interface.
     * <p/>
     * <b>Note:</b> The implementation class must exist before the translation implementations can be created.
     *
     * @param messageInterface  the message interface to implement.
     * @param translationSuffix the translation locale suffix.
     * @param translations      a map of the translations for the methods.
     *
     * @return the class model used to create translation implementations of the interface.
     *
     * @throws IllegalArgumentException if {@link MessageInterface#isMessageBundle()} or
     *                                  {@link MessageInterface#isMessageLogger()} returns
     *                                  {@code false.}
     */
    public static ClassModel translation(final MessageInterface messageInterface, final String translationSuffix, final Map<MessageMethod, String> translations) throws IllegalArgumentException {
        final String generatedClassName = implementationClassName(messageInterface, translationSuffix);
        final String superClassName = getEnclosingTranslationClassName(generatedClassName);
        if (messageInterface.isMessageBundle()) {
            return new MessageBundleTranslator(messageInterface, generatedClassName, superClassName, translations);
        } else if (messageInterface.isMessageLogger()) {
            return new MessageLoggerTranslator(messageInterface, generatedClassName, superClassName, translations);
        }
        throw new IllegalArgumentException(String.format("Message interface %s is not a valid message logger or message bundle.", messageInterface));
    }
}
