package org.jboss.logging.generator.model;

import org.jboss.logging.generator.MessageInterface;
import org.jboss.logging.generator.MessageMethod;

import java.util.Map;

import static org.jboss.logging.generator.model.ImplementationType.BUNDLE;
import static org.jboss.logging.generator.model.ImplementationType.LOGGER;
import static org.jboss.logging.generator.util.TranslationHelper.getEnclosingTranslationClassName;
import static org.jboss.logging.generator.util.TranslationHelper.getTranslationClassNameSuffix;

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
     * @throws IllegalArgumentException if {@link org.jboss.logging.generator.MessageInterface#isMessageBundle()} or
     *                                  {@link org.jboss.logging.generator.MessageInterface#isMessageLogger()} returns
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
     * @param messageInterface    the message interface to implement
     * @param translationFileName the translation file name.
     * @param translations        a map of the translations for the methods.
     *
     * @return the class model used to create translation implementations of the interface.
     *
     * @throws IllegalArgumentException if {@link org.jboss.logging.generator.MessageInterface#isMessageBundle()} or
     *                                  {@link org.jboss.logging.generator.MessageInterface#isMessageLogger()} returns
     *                                  {@code false.}
     */
    public static ClassModel translation(final MessageInterface messageInterface, final String translationFileName, final Map<MessageMethod, String> translations) throws IllegalArgumentException {
        final String primaryClassName = getPrimaryClassName(messageInterface);
        final String generatedClassName = primaryClassName.concat(getTranslationClassNameSuffix(translationFileName));
        final String superClassName = getEnclosingTranslationClassName(generatedClassName);
        if (messageInterface.isMessageBundle()) {
            return new MessageBundleTranslator(messageInterface, generatedClassName, superClassName, translations);
        } else if (messageInterface.isMessageLogger()) {
            return new MessageLoggerTranslator(messageInterface, generatedClassName, superClassName, translations);
        }
        throw new IllegalArgumentException(String.format("Message interface %s is not a valid message logger or message bundle.", messageInterface));
    }


    private static String getPrimaryClassName(final MessageInterface messageInterface) {
        if (messageInterface.isMessageBundle()) {
            return messageInterface.name() + BUNDLE.toString();
        } else if (messageInterface.isMessageLogger()) {
            return messageInterface.name() + LOGGER.toString();
        }
        return messageInterface.name();
    }
}
