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

package org.jboss.logging.generator.util;

import org.jboss.logging.generator.Annotations;
import org.jboss.logging.generator.ParameterType;
import org.jboss.logging.generator.model.ImplementationType;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import static org.jboss.logging.generator.LoggingTools.annotations;
import static org.jboss.logging.generator.LoggingTools.loggers;
import static org.jboss.logging.generator.ParameterType.MatchType.SUBTYPE;
import static org.jboss.logging.generator.ParameterType.MatchType.SUPERTYPE;

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
     * @param element the element to look for the annotation on.
     * @param clazz   the annotation class
     *
     * @return true if the element is annotated, false otherwise
     *
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
     * @param element the element.
     *
     * @return the translation file name prefix
     *
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

        if (element.getAnnotation(annotations().messageBundle()) != null) {
            return prefix + ImplementationType.BUNDLE.toString();
        } else if (element.getAnnotation(annotations().messageLogger()) != null) {
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
     *
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
     * methods in the {@link org.jboss.logging.generator.Loggers#basicLoggerClass()} interface.
     *
     * @param element the interface element
     * @param types   the type util
     *
     * @return the collection of all methods
     *
     * @throws IllegalArgumentException if element is not an interface
     */
    public static Collection<ExecutableElement> getInterfaceMethods(final TypeElement element, final Types types) {
        if (!element.getKind().isInterface()) {
            throw new IllegalArgumentException("The element parameter is not an interface");
        }

        Collection<ExecutableElement> methods = new HashSet<ExecutableElement>();

        for (TypeMirror intf : element.getInterfaces()) {
            // Ignore BasicLogger methods
            if (!intf.toString().equals(loggers().basicLoggerClass().getName())) {
                methods.addAll(getInterfaceMethods((TypeElement) types.asElement(intf), types));

            }
        }

        methods.addAll(ElementFilter.methodsIn(element.getEnclosedElements()));

        return methods;
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
        final List<ExecutableElement> result = new ArrayList<ExecutableElement>();
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
            if (param.getAnnotation(annotations().cause()) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the number of parameters excluding the {@link org.jboss.logging.generator.Annotations#cause()} parameter
     * and any {@link org.jboss.logging.generator.Annotations#param()} parameters if found.
     *
     * @param params the parameters to get the count for.
     *
     * @return the number of parameters.
     */
    public static int parameterCount(final Collection<? extends VariableElement> params) {
        int result = params.size();
        for (VariableElement param : params) {
            if (isAnnotatedWith(param, annotations().param())) {
                --result;
            }
        }
        return (result - (hasCause(params) ? 1 : 0));
    }

    /**
     * Checks to see if the method has or inherits a {@link org.jboss.logging.generator.Annotations#message()}  annotation.
     *
     * @param root   the interface, root element.
     * @param method the method to check.
     * @param types  the type util
     *
     * @return {@code true} if the method has or inherits a message annotation, otherwise {@code false}.
     */
    public static boolean hasOrInheritsMessage(final TypeElement root, final ExecutableElement method, final Types types) {
        if (isAnnotatedWith(method, annotations().message())) {
            return true;
        }
        final Collection<ExecutableElement> allMethods = findByName(getInterfaceMethods(root, types), method.getSimpleName(), parameterCount(method.getParameters()));
        for (ExecutableElement m : allMethods) {
            if (isAnnotatedWith(m, annotations().message())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds the method with {@link org.jboss.logging.generator.Annotations#message()} annotation. If the {@code method}
     * parameter does not have a message, the first message that has the same name and parameter count will be returned.
     *
     * @param methods the method to search.
     * @param method  the method to check.
     *
     * @return the message for the method, {@code null} of no message is found.
     */
    public static String findMessage(final Collection<ExecutableElement> methods, final ExecutableElement method) {
        if (isAnnotatedWith(method, annotations().message())) {
            return annotations().messageValue(method);
        }
        final Collection<ExecutableElement> allMethods = findByName(methods, method.getSimpleName(), parameterCount(method.getParameters()));
        for (ExecutableElement m : allMethods) {
            if (isAnnotatedWith(m, annotations().message())) {
                return annotations().messageValue(m);
            }
        }
        return null;
    }

    /**
     * Finds the method format with {@link org.jboss.logging.generator.Annotations#message()} annotation. If the {@code method}
     * parameter does not have a message format, the first message format that has the same name and parameter count will be returned.
     *
     * @param methods the method to search.
     * @param method  the method to check.
     *
     * @return the message for the method format, {@code null} of no message format is found.
     */
    public static Annotations.FormatType findMessageFormat(final Collection<ExecutableElement> methods, final ExecutableElement method) {
        if (isAnnotatedWith(method, annotations().message())) {
            return annotations().messageFormat(method);
        }
        final Collection<ExecutableElement> allMethods = findByName(methods, method.getSimpleName(), parameterCount(method.getParameters()));
        for (ExecutableElement m : allMethods) {
            if (isAnnotatedWith(m, annotations().message())) {
                return annotations().messageFormat(m);
            }
        }
        return Annotations.FormatType.PRINTF;
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

    /**
     * Processes the constructors in the element and looks for the constructor that matches the parameter types.
     * <p/>
     * <b>Note:</b> Only public constructors are returned.
     *
     * @param element        the element to find the constructors for.
     * @param parameterTypes the parameter types to match for the constructor.
     *
     * @return the constructor found, otherwise {@code null}.
     */
    // TODO - Delete
    @Deprecated
    public static ExecutableElement getConstructor(final Element element, final Class<?>... parameterTypes) {
        final List<ExecutableElement> constructors = ElementFilter.constructorsIn(element.getEnclosedElements());
        for (ExecutableElement constructor : constructors) {
            if (!constructor.getModifiers().contains(Modifier.PUBLIC)) {
                continue;
            }
            final List<? extends VariableElement> params = constructor.getParameters();
            // If no parameters look for default constructor
            if (parameterTypes.length == 0) {
                if (params.size() == 0) {
                    return constructor;
                }
            } else {
                // If the lengths don't match, move on
                if (parameterTypes.length == params.size()) {
                    boolean match = true;
                    // Must have a 1 to 1 relationship
                    for (int i = 0; i < params.size(); i++) {
                        final TypeMirror typeMirror = params.get(i).asType();
                        if (typeMirror instanceof DeclaredType) {
                            final DeclaredType dclType = (DeclaredType) typeMirror;
                            final TypeElement typeElement = (TypeElement) dclType.asElement();
                            if (!typeElement.getQualifiedName().toString().equals(parameterTypes[i].getName())) {
                                match = false;
                            }
                        } else {
                            match = false;
                        }
                    }
                    if (match) {
                        return constructor;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Processes the constructors in the element and looks for a constructor that best suits the the parameters passed.
     * <p/>
     * The parameter types are first checked to an exact match of the constructor. If the constructor was not found,
     * the first method that can be used based on the parameter types is returned. If nothing is found {@code null} is
     * returned.
     * <p/>
     * <b>Note:</b> Only public constructors are returned.
     *
     * @param element        the element to find the constructors for.
     * @param parameterTypes the parameter types to match for the constructor.
     *
     * @return the constructor found, otherwise {@code null}.
     */
    // TODO - Delete
    @Deprecated
    public static ExecutableElement getFuzzyConstructor(final Element element, final ParameterType... parameterTypes) {
        // First check for an exact match
        final List<Class<?>> types = new LinkedList<Class<?>>();
        for (ParameterType p : parameterTypes) {
            types.add(p.type());
        }
        ExecutableElement result = getConstructor(element, types.toArray(new Class<?>[0]));
        if (result == null) {
            final List<ExecutableElement> constructors = ElementFilter.constructorsIn(element.getEnclosedElements());
            for (ExecutableElement constructor : constructors) {
                if (!constructor.getModifiers().contains(Modifier.PUBLIC)) {
                    continue;
                }
                final List<? extends VariableElement> params = constructor.getParameters();
                // If no parameters look for default constructor
                if (parameterTypes.length == 0) {
                    if (params.size() == 0) {
                        result = constructor;
                        break;
                    }
                } else {
                    // If the lengths don't match, move on
                    if (parameterTypes.length == params.size()) {
                        boolean match = true;
                        // Must have a 1 to 1 relationship
                        for (int i = 0; i < params.size(); i++) {
                            final ParameterType paramType = parameterTypes[i];
                            final TypeMirror typeMirror = params.get(i).asType();
                            switch (paramType.matchType()) {
                                case EQUALS: {
                                    if (typeMirror instanceof DeclaredType) {
                                        final DeclaredType dclType = (DeclaredType) typeMirror;
                                        final TypeElement typeElement = (TypeElement) dclType.asElement();
                                        if (!typeElement.getQualifiedName().toString().equals(paramType.type())) {
                                            match = false;
                                        }
                                    } else {
                                        match = false;
                                    }
                                    break;
                                }
                                case SUBTYPE: {
                                    if (!isAssignableFrom(typeMirror, paramType.type())) {
                                        match = false;
                                    }
                                    break;
                                }
                                case SUPERTYPE: {
                                    if (!isAssignableFrom(paramType.type(), typeMirror)) {
                                        match = false;
                                    }
                                    break;
                                }
                                default:
                                    match = false;
                                    break;
                            }
                        }
                        if (match) {
                            result = constructor;
                            break;
                        }
                    }
                }
            }
        }
        return result;
    }


    /**
     * Finds the matching constructor for the exception based on information form the method.
     * <p/>
     * Exception messages are discovered a bit differently than other constructors. First any parameters of the method
     * annotated with the {@link org.jboss.logging.generator.Annotations#cause()} are checked. If there are parameters
     * found with the annotation, the parameter annotated with {@link org.jboss.logging.generator.Annotations#cause()}
     * ,if there is one, is assumed to be the left most {@link Throwable} and the left most {@link String} parameter is
     * assumed to be the message.
     * <p/>
     * If there are no parameters annotated with {@link org.jboss.logging.generator.Annotations#cause()}, then a
     * {@link String}, {@link String} and {@link Throwable}, {@link Throwable} or {@link Throwable} and {@link String}
     * constructors searched for.
     * <p/>
     * If no methods matching any of the above criteria are found, {@code null} is returned.
     * <p/>
     * <b>Note:</b> Only public constructors are returned.
     *
     * @param element    the element to find the constructors for.
     * @param parameters the parameter types to match for the constructor.
     *
     * @return the constructor found, otherwise {@code null}.
     */
    // TODO - Delete
    @Deprecated
    public static ExecutableElement getExceptionConstructor(final Element element, final Types types, final ExecutableElement method) {
        ExecutableElement result = null;
        final List<VariableElement> params = new LinkedList<VariableElement>();
        VariableElement cause = null;
        for (VariableElement param : method.getParameters()) {
            // First extract the @Param annotated parameters
            if (isAnnotatedWith(param, annotations().param())) {
                params.add(param);
            }
            // Find the @Cause parameter
            if (isAnnotatedWith(param, annotations().cause())) {
                cause = param;
            }
        }

        // Check to see if we are @Param parameters
        if (params.isEmpty()) {
            final ExecutableElement defaultConstructor = getConstructor(element);
            final ExecutableElement stringConstructor = getFuzzyConstructor(element, ParameterType.of(String.class, SUBTYPE));
            final ExecutableElement throwableConstructor = getFuzzyConstructor(element, ParameterType.of(Throwable.class, SUPERTYPE));
            final ExecutableElement throwableStringConstructor = getFuzzyConstructor(element, ParameterType.of(Throwable.class, SUPERTYPE), ParameterType.of(String.class, SUBTYPE));
            final ExecutableElement stringThrowableConstructor = getFuzzyConstructor(element, ParameterType.of(String.class, SUBTYPE), ParameterType.of(Throwable.class, SUPERTYPE));
            if (cause == null) {
                // Prefer a string constructor
                result = stringConstructor;
            } else {
                // Prefer throwable/string combo
                if (throwableStringConstructor != null) {
                    result = throwableStringConstructor;
                } else if (stringThrowableConstructor != null) {
                    result = stringThrowableConstructor;
                } else if (stringConstructor != null) {
                    result = stringConstructor;
                } else if (throwableConstructor != null) {
                    result = throwableConstructor;
                }
            }
            // Final check if not set use the default.
            if (result == null) {
                // Use default
                result = defaultConstructor;
            }
        } else {
            // Search the constructors
            final List<ExecutableElement> constructors = ElementFilter.constructorsIn(element.getEnclosedElements());
            for (ExecutableElement constructor : constructors) {
                if (!constructor.getModifiers().contains(Modifier.PUBLIC)) {
                    continue;
                }
                final List<? extends VariableElement> ctorParams = constructor.getParameters();
                boolean causeFound = false;
                boolean messageFound = false;
                boolean match = true;
                boolean paramClassValueFound = false;
                int index = 0;

                // TODO - Incomplete. Need to check the @Param value and test exact matches first.
                for (VariableElement ctorParam : ctorParams) {
                    // Is this a throwable parameter
                    if (cause != null && !causeFound && isAssignableFrom(Throwable.class, ctorParam.asType())) {
                        causeFound = true;
                        index++;
                        continue;
                    }
                    // Is this a message parameter
                    if (!messageFound && isAssignableFrom(ctorParam.asType(), String.class)) {
                        messageFound = true;
                        index++;
                        continue;
                    }
                    if (index < params.size()) {
                        final VariableElement annoParam = params.get(index++);
                        final String className = Object.class.getName();
                        if (className == null) {
                            if (!types.isSubtype(ctorParam.asType(), annoParam.asType())) {
                                match = false;
                            }
                        } else {
                            if (!ctorParam.asType().toString().equals(className)) {
                                match = false;
                            }
                        }
                    } else {
                        // Break from parameter loop, constructor not found
                        break;
                    }
                    // Short circuit the loop.
                    if (!match) break;
                }
                if (match) {
                    result = constructor;
                    break;
                }

                // Check to see if
                causeFound = false;
                messageFound = false;
                for (VariableElement ctorParam : ctorParams) {
                    // Is this a throwable parameter
                    if (cause != null && !causeFound && isAssignableFrom(Throwable.class, ctorParam.asType())) {
                        causeFound = true;
                        index++;
                        continue;
                    }
                    // Is this a message parameter
                    if (!messageFound && isAssignableFrom(ctorParam.asType(), String.class)) {
                        messageFound = true;
                        index++;
                        continue;
                    }
                    if (index < params.size()) {
                        final VariableElement annoParam = params.get(index++);
                        if (!types.isSubtype(ctorParam.asType(), annoParam.asType())) {
                            match = false;
                        }
                    } else {
                        // Break from parameter loop, constructor not found
                        break;
                    }
                }
                if (match) {
                    result = constructor;
                    break;
                }
            }
        }
        return result;
    }
}
