/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.logging.util;

import org.jboss.logging.Annotations;
import org.jboss.logging.Loggers;
import org.jboss.logging.model.ImplementationType;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * An utility class to work with element.
 *
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 * @author James R. Perkins
 */
public final class ElementHelper {

    /**
     * Disable instantiation.
     */
    private ElementHelper() {
    }

    /**
     * Check if an element is annotated with the given annotation.
     *
     * @param element the element to look for the annotation on.
     * @param clazz   the annotation class
     *
     * @return true if the element is annotated, false otherwise
     * @throws NullPointerException if element parameter is null
     */
    public static boolean isAnnotatedWith(final Element element, final Class<? extends Annotation> clazz) {
        if (element == null) {
            throw new NullPointerException("The element parameter is null");
        }

        Annotation annotation = element.getAnnotation(clazz);
        return (annotation != null);
    }

    /**
     * Returns the primary class simple name for an element
     * who represents a MessageBundle or MessageLogger interface.
     *
     * @param element     the element
     * @param annotations the annotation descriptor.
     *
     * @return the translation file name prefix
     * @throws NullPointerException     if element is null
     * @throws IllegalArgumentException if element is not an interface
     */
    public static String getPrimaryClassName(final TypeElement element, final Annotations annotations) {
        if (element == null) {
            throw new NullPointerException("The element parameter cannot be null");
        }
        if (!element.getKind().isInterface()) {
            throw new IllegalArgumentException("The element parameter is not an interface");
        }

        String prefix = getPrimaryClassNamePrefix(element);

        if (element.getAnnotation(annotations.messageBundle()) != null) {
            return prefix + ImplementationType.BUNDLE.toString();
        } else if (element.getAnnotation(annotations.messageLogger()) != null) {
            return prefix + ImplementationType.LOGGER.toString();
        }

        return prefix;
    }

    /**
     * Returns the primary class simple name prefix for an element
     * who represents a MessageBundle or MessageLogger interface.
     *
     * @param element the element
     *
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
     * Include all declared and inherited methods, with the exception of any
     * methods in the {@link org.jboss.logging.BasicLogger} interface.
     *
     * @param element the interface element
     * @param types   the type util
     * @param loggers the logger descriptor.
     *
     * @return the collection of all methods
     * @throws IllegalArgumentException if element is not an interface
     */
    public static Collection<ExecutableElement> getInterfaceMethods(final TypeElement element, final Types types, final Loggers loggers) {
        if (!element.getKind().isInterface()) {
            throw new IllegalArgumentException("The element parameter is not an interface");
        }

        Collection<ExecutableElement> methods = new HashSet<ExecutableElement>();

        for (TypeMirror intf : element.getInterfaces()) {
            // Ignore BasicLogger methods
            if (loggers != null && !intf.toString().equals(loggers.basicLoggerClass().getName())) {
                methods.addAll(getInterfaceMethods((TypeElement) types.asElement(intf), types, loggers));

            }
        }

        methods.addAll(ElementFilter.methodsIn(element.getEnclosedElements()));

        return methods;
    }

    /**
     * Returns a collection of all Message methods. A message
     * method is annotated with {@link org.jboss.logging.Message}.
     *
     * @param methods     the methods collection
     * @param annotations the annotation descriptor.
     *
     * @return a map containing the message where the key is the method name
     */
    public static Map<String, String> getAllMessageMethods(final Collection<ExecutableElement> methods, final Annotations annotations) {
        Map<String, String> messages = new HashMap<String, String>();

        for (ExecutableElement method : methods) {
            if (isAnnotatedWith(method, annotations.message())) {
                messages.put(method.getSimpleName().toString(), annotations.messageValue(method));
            }
        }

        return messages;
    }

    /**
     * Checks to see if the type element is assignable from the type.
     *
     * @param typeElement the type element to check.
     * @param type        the type to check.
     *
     * @return {@code true} if the type element is assignable from the type,
     *         otherwise {@code false}.
     */
    public static boolean isAssignableFrom(final TypeElement typeElement, final Class<?> type) {
        if (type.getName().equals(typeElement.getQualifiedName().toString())) {
            return true;
        }
        for (Class<?> intf : type.getInterfaces()) {
            if (isAssignableFrom(typeElement, intf)) {
                return true;
            }
        }
        return (type.getSuperclass() != null && isAssignableFrom(typeElement, type.getSuperclass()));
    }

    /**
     * Checks to see id the type mirror is assignable from the type.
     *
     * @param typeMirror the type mirror to check.
     * @param type       the type to check.
     *
     * @return {@code true} if the type mirror is assignable from the type,
     *         otherwise {@code false}.
     */
    public static boolean isAssignableFrom(final TypeMirror typeMirror, final Class<?> type) {
        if (typeMirror instanceof DeclaredType) {
            final DeclaredType dclType = (DeclaredType) typeMirror;
            final TypeElement typeElement = (TypeElement) dclType.asElement();
            return isAssignableFrom(typeElement, type);
        }
        for (Class<?> intf : type.getInterfaces()) {
            if (isAssignableFrom(typeMirror, intf)) {
                return true;
            }
        }
        return (type.getSuperclass() != null && isAssignableFrom(typeMirror, type.getSuperclass()));
    }

    /**
     * Checks to see if the type is assignable from the type element.
     *
     * @param type        the type to check.
     * @param typeElement the type element to check.
     *
     * @return {@code true} if the type is assignable from the type element,
     *         otherwise {@code false}.
     */
    public static boolean isAssignableFrom(final Class<?> type, final TypeElement typeElement) {
        if (type.getName().equals(typeElement.getQualifiedName().toString())) {
            return true;
        }
        final List<? extends TypeMirror> types = typeElement.getInterfaces();
        for (TypeMirror typeMirror : types) {
            if (isAssignableFrom(type, typeMirror)) {
                return true;
            }
        }
        return isAssignableFrom(type, typeElement.getSuperclass());
    }

    /**
     * Checks to see if the type is assignable from the type mirror.
     *
     * @param type       the type to check.
     * @param typeMirror the type mirror to check.
     *
     * @return {@code true} if the type is assignable from the type mirror,
     *         otherwise {@code false}.
     */
    public static boolean isAssignableFrom(final Class<?> type, final TypeMirror typeMirror) {
        if (typeMirror instanceof DeclaredType) {
            final DeclaredType dclType = (DeclaredType) typeMirror;
            final TypeElement typeElement = (TypeElement) dclType.asElement();
            return isAssignableFrom(type, typeElement);
        }
        return false;
    }
}
