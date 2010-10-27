package org.jboss.logging.util;

import org.jboss.logging.Message;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * An util class to work with element.
 * 
 * @author Kevin Pollet
 */
public final class ElementUtil {

    /**
     * Disable instantiation.
     */
    private ElementUtil() {

    }

    /**
     * Returns all methods of the given interface.
     * Include all declared and inherited methods.
     *
     * @param element the interface element
     * @param types the type util
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
            Message annotation = method.getAnnotation(Message.class);
            if (annotation != null) {
                messages.put(method.getSimpleName().toString(), annotation.value());
            }
        }

        return messages;
    }

}
