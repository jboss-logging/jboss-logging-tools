/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.jboss.logging.processor.util;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.Field;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.Param;
import org.jboss.logging.annotations.Property;

/**
 * An utility class to work with element.
 *
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
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
     * @param annotatedConstruct the object to look for the annotation on.
     * @param clazz              the annotation class
     *
     * @return {@code true} if the element is annotated, otherwise {@code false}
     *
     * @throws IllegalArgumentException if element parameter is null
     */
    public static boolean isAnnotatedWith(final AnnotatedConstruct annotatedConstruct, final Class<? extends Annotation> clazz) {
        if (annotatedConstruct == null) {
            throw new IllegalArgumentException("The element parameter is null");
        }

        Annotation annotation = annotatedConstruct.getAnnotation(clazz);
        return (annotation != null);
    }

    /**
     * Returns the primary class simple name prefix for an element
     * who represents a MessageBundle or MessageLogger interface.
     *
     * @param element the element
     *
     * @return the translation file name prefix
     *
     * @throws IllegalArgumentException if element is null or the element is not an interface
     */
    public static String getPrimaryClassNamePrefix(final TypeElement element) {
        if (element == null) {
            throw new IllegalArgumentException("The element parameter cannot be null");
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
     * Returns a collection of methods with the same name.
     *
     * @param methods    the methods to process.
     * @param methodName the method name to find.
     *
     * @return a collection of methods with the same name.
     */
    public static Collection<ExecutableElement> findByName(final Collection<ExecutableElement> methods, final Name methodName) {
        final List<ExecutableElement> result = new ArrayList<>();
        for (ExecutableElement method : methods) {
            if (methodName.equals(method.getSimpleName())) {
                result.add(method);
            }
        }
        return result;
    }


    /**
     * Returns a collection of methods with the same name.
     *
     * @param methods    the methods to process.
     * @param methodName the method name to find.
     * @param paramCount the number of parameters the method must have.
     *
     * @return a collection of methods with the same name.
     */
    public static Collection<ExecutableElement> findByName(final Collection<ExecutableElement> methods, final Name methodName, final int paramCount) {
        final List<ExecutableElement> result = new ArrayList<>();
        for (ExecutableElement method : methods) {
            if (methodName.equals(method.getSimpleName()) && parameterCount(method.getParameters()) == paramCount) {
                result.add(method);
            }
        }
        return result;
    }

    /**
     * Checks to see if there is a cause parameter.
     *
     * @param params the parameters to check.
     *
     * @return {@code true} if there is a cause, otherwise {@code false}.
     */
    public static boolean hasCause(final Collection<? extends VariableElement> params) {
        // Look for cause
        for (VariableElement param : params) {
            if (isAnnotatedWith(param, Cause.class)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the number of parameters excluding the {@link org.jboss.logging.annotations.Cause} parameter
     * and any {@link org.jboss.logging.annotations.Param} parameters if found.
     *
     * @param params the parameters to get the count for.
     *
     * @return the number of parameters.
     */
    public static int parameterCount(final Collection<? extends VariableElement> params) {
        int result = params.size();
        for (VariableElement param : params) {
            if (isAnnotatedWith(param, Param.class) || isAnnotatedWith(param, Field.class) ||
                    isAnnotatedWith(param, Property.class)) {
                --result;
            }
        }
        return (result - (hasCause(params) ? 1 : 0));
    }

    /**
     * Checks to see if the method has or inherits a {@link org.jboss.logging.annotations.Message}
     * annotation.
     *
     * @param methods the method to search.
     * @param method  the method to check.
     *
     * @return {@code true} if the method has or inherits a message annotation, otherwise {@code false}.
     */
    public static boolean inheritsMessage(final Collection<ExecutableElement> methods, final ExecutableElement method) {
        if (isAnnotatedWith(method, Message.class)) {
            return false;
        }
        final Collection<ExecutableElement> allMethods = findByName(methods, method.getSimpleName());
        for (ExecutableElement m : allMethods) {
            if (isAnnotatedWith(m, Message.class)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks to see if the method is overloaded. An overloaded method has a different parameter count based on the
     * format parameters only. Parameters annotated with {@link org.jboss.logging.annotations.Cause} or
     * {@link org.jboss.logging.annotations.Param}
     * are not counted.
     *
     * @param methods the method to search.
     * @param method  the method to check.
     *
     * @return {@code true} if the method is overloaded, otherwise {@code false}.
     */
    public static boolean isOverloaded(final Collection<ExecutableElement> methods, final ExecutableElement method) {
        final Collection<ExecutableElement> allMethods = findByName(methods, method.getSimpleName());
        for (ExecutableElement m : allMethods) {
            if (method.getSimpleName().equals(m.getSimpleName()) && parameterCount(method.getParameters()) != parameterCount(m.getParameters())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieves the first attribute value from the annotation and assumes it's a {@link Class class} type.
     *
     * @param element    the element the annotation is on
     * @param annotation the annotation to get the value from
     *
     * @return a {@link TypeElement} representing the value for the first annotation attribute or {@code null} if no
     * attributes were found
     */
    public static TypeElement getClassAnnotationValue(final Element element, final Class<? extends Annotation> annotation) {
        for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
            final DeclaredType annotationType = mirror.getAnnotationType();
            if (annotationType.toString().equals(annotation.getName())) {
                final AnnotationValue value = mirror.getElementValues().values().iterator().next();
                return ((TypeElement) (((DeclaredType) value.getValue()).asElement()));
            }
        }
        return null;
    }

    /**
     * Retrieves the attribute value from the annotation and assumes it's a {@link Class class} type.
     *
     * @param element       the element the annotation is on
     * @param annotation    the annotation to get the value from
     * @param attributeName the name of the attribute to retrieve the class value for
     *
     * @return a {@link TypeElement} representing the value for the annotation attribute or {@code null} if the
     * attribute was not found
     */
    public static TypeElement getClassAnnotationValue(final Element element, final Class<? extends Annotation> annotation, final String attributeName) {
        for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
            final DeclaredType annotationType = mirror.getAnnotationType();
            if (annotationType.toString().equals(annotation.getName())) {
                final Map<? extends ExecutableElement, ? extends AnnotationValue> map = mirror.getElementValues();
                for (ExecutableElement key : map.keySet()) {
                    if (key.getSimpleName().contentEquals(attributeName)) {
                        return ((TypeElement) (((DeclaredType) map.get(key).getValue()).asElement()));
                    }
                }
            }
        }
        return null;
    }

    /**
     * Retrieves the attribute value from the annotation and assumes it's an array {@link Class classes}.
     *
     * @param element       the element the annotation is on
     * @param annotation    the annotation to get the value from
     * @param attributeName the name of the attribute to retrieve the class value array for
     *
     * @return a list of {@link TypeMirror} representing the value for the annotation attribute or an empty list
     */
    public static List<TypeMirror> getClassArrayAnnotationValue(final Element element, final Class<? extends Annotation> annotation, final String attributeName) {
        for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
            final DeclaredType annotationType = mirror.getAnnotationType();
            if (annotationType.toString().equals(annotation.getName())) {
                final Map<? extends ExecutableElement, ? extends AnnotationValue> map = mirror.getElementValues();
                for (ExecutableElement key : map.keySet()) {
                    if (key.getSimpleName().contentEquals(attributeName)) {
                        @SuppressWarnings("unchecked")
                        final List<AnnotationValue> annotationValues = (List<AnnotationValue>) map.get(key).getValue();
                        final List<TypeMirror> result = new ArrayList<>(annotationValues.size());
                        for (AnnotationValue value : annotationValues) {
                            result.add((TypeMirror) value.getValue());
                        }
                        return result;
                    }
                }
            }
        }
        return Collections.emptyList();
    }

    /**
     * Checks whether or not a constructor matching the parameters exists.
     *
     * @param types   the type utility used to compare the type arguments
     * @param element the element that contains the constructors
     * @param args    the arguments the constructor should match
     *
     * @return {@code true} if a matching constructor was found otherwise {@code false}
     */
    public static boolean hasConstructor(final Types types, final Element element, final List<TypeMirror> args) {
        final int len = args.size();
        final List<ExecutableElement> constructors = ElementFilter.constructorsIn(element.getEnclosedElements());
        for (ExecutableElement constructor : constructors) {
            final List<? extends VariableElement> parameters = constructor.getParameters();
            if (len == parameters.size()) {
                boolean match = false;
                for (int i = 0; i < len; i++) {
                    final TypeMirror type = args.get(i);
                    final VariableElement parameter = parameters.get(i);
                    if (types.isSameType(type, parameter.asType())) {
                        match = true;
                    } else {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the type as a {@link TypeMirror}.
     *
     * @param elements the element utility used to generate the tye type
     * @param type     the type to create the {@link TypeMirror} for
     *
     * @return the type
     */
    public static TypeMirror toType(final Elements elements, final Class<?> type) {
        return elements.getTypeElement(type.getCanonicalName()).asType();
    }
}
