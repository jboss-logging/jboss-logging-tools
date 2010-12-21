package org.jboss.logging.util;

import org.jboss.logging.Message;
import org.jboss.logging.MessageBundle;
import org.jboss.logging.MessageLogger;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.jboss.logging.LogMessage;
import org.jboss.logging.model.ImplementationType;

/**
 * An utility class to work with element.
 *
 * @author Kevin Pollet
 */
public final class ElementHelper {

    public static final Class<MessageBundle> MESSAGE_BUNDLE_ANNOTATION = MessageBundle.class;

    public static final Class<MessageLogger> MESSAGE_LOGGER_ANNOTATION = MessageLogger.class;

    public static final Class<LogMessage> LOG_MESSAGE_ANNOTATION = LogMessage.class;

    public static final Class<Message> MESSAGE_ANNOTATION = Message.class;

    /**
     * Disable instantiation.
     */
    private ElementHelper() {
    }

    /**
     * Check if an element is annotated with the given annotation.
     *
     * @param clazz the annotation class
     * @return true if the element is annotated, false otherwise
     * @throws NullPointerException if element parameter is null
     */
    public static boolean isAnnotatedWith(final Element element, final Class<? extends Annotation> clazz) {
        if (element == null) {
            throw new NullPointerException("The element parameter is null");
        }

        Annotation annotation = element.getAnnotation(clazz);
        if (annotation != null) {
            return true;
        }

        return false;
    }

    /**
     * Check if the element is a logger method. Logger methods are annotated
     * with the {@link LogMessage} annotation.
     * 
     * @param element the element to check.
     * @return {@code true} of this is a logger method, otherwise {@code false}.
     * @throws NullPointerException if element parameter is {@code null}.
     */
    public static boolean isLoggerMethod(final Element element) {
        return isAnnotatedWith(element, LOG_MESSAGE_ANNOTATION);
    }

    /**
     * Returns the primary class simple name for an element
     * who represents a MessageBundle or MessageLogger interface.
     *
     * @param element the element
     * @return the translation file name prefix
     * @throws NullPointerException     if element is null
     * @throws IllegalArgumentException if element is not an interface
     */
    public static String getPrimaryClassName(final TypeElement element) {
        if (element == null) {
            throw new NullPointerException("The element parameter cannot be null");
        }
        if (!element.getKind().isInterface()) {
            throw new IllegalArgumentException("The element parameter is not an interface");
        }

        String prefix = getPrimaryClassNamePrefix(element);

        if (element.getAnnotation(MESSAGE_BUNDLE_ANNOTATION) != null) {
            return prefix + ImplementationType.BUNDLE.toString();
        } else if (element.getAnnotation(MESSAGE_LOGGER_ANNOTATION) != null) {
            return prefix + ImplementationType.LOGGER.toString();
        }

        return prefix;
    }

    /**
     * Returns the primary class simple name prefix for an element
     * who represents a MessageBundle or MessageLogger interface.
     *
     * @param element the element
     * @return the translation file name prefix
     * @throws NullPointerException     if element is null
     * @throws IllegalArgumentException if element is not an interface
     */
    public static String getPrimaryClassNamePrefix(final TypeElement element) {
        if (element == null) {
            throw new NullPointerException("The element parameter cannot be null");
        }
        if (!element.getKind().isInterface()) {
            throw new IllegalArgumentException("The element parameter is not an interface");
        }

        String translationFileName = element.getSimpleName().toString();

        //Check if it's an inner interface
        Element enclosingElt = element.getEnclosingElement();
        while (enclosingElt != null && enclosingElt instanceof TypeElement) {
            translationFileName = String.format("%s$%s", enclosingElt.getSimpleName().toString(), translationFileName);
            enclosingElt = enclosingElt.getEnclosingElement();
        }

        return translationFileName;
    }

    /**
     * Returns all methods of the given interface.
     * Include all declared and inherited methods.
     *
     * @param element the interface element
     * @param types   the type util
     * @return the collection of all methods
     * @throws IllegalArgumentException if element is not an interface
     */
    public static Collection<ExecutableElement> getInterfaceMethods(final TypeElement element, final Types types) {
        if (!element.getKind().isInterface()) {
            throw new IllegalArgumentException("The element parameter is not an interface");
        }

        Collection<ExecutableElement> methods = new HashSet<ExecutableElement>();

        for (TypeMirror intf : element.getInterfaces()) {
            methods.addAll(getInterfaceMethods((TypeElement) types.asElement(intf), types));
        }

        methods.addAll(ElementFilter.methodsIn(element.getEnclosedElements()));

        return methods;
    }

    /**
     * Returns a collection of all Message methods. A message
     * method is annotated with {@link org.jboss.logging.Message}.
     *
     * @param methods the methods collection
     * @return a map containing the message where the key is the method name
     */
    public static Map<String, String> getAllMessageMethods(final Collection<ExecutableElement> methods) {
        Map<String, String> messages = new HashMap<String, String>();

        for (ExecutableElement method : methods) {
            Message annotation = method.getAnnotation(MESSAGE_ANNOTATION);
            if (annotation != null) {
                messages.put(method.getSimpleName().toString(), annotation.value());
            }
        }

        return messages;
    }

    /**
     * Returns the project code from the annotation on the element.
     *
     * @param interfaceElement the interface element that contains the annotation,
     * @return the project code from the annotation or {@code null}.
     */
    public static String getProjectCode(final TypeElement interfaceElement) {
        String result = null;
        final MessageBundle messageBundle = interfaceElement.getAnnotation(MESSAGE_BUNDLE_ANNOTATION);
        final MessageLogger messageLogger = interfaceElement.getAnnotation(MESSAGE_LOGGER_ANNOTATION);
        if (messageBundle != null) {
            result = messageBundle.projectCode();
        } else if (messageLogger != null) {
            result = messageLogger.projectCode();
        }

        return result;
    }
}
