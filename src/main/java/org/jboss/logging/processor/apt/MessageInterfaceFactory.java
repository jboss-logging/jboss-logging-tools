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

package org.jboss.logging.processor.apt;

import static org.jboss.logging.processor.Tools.annotations;
import static org.jboss.logging.processor.Tools.aptHelper;
import static org.jboss.logging.processor.Tools.loggers;
import static org.jboss.logging.processor.util.ElementHelper.isAnnotatedWith;
import static org.jboss.logging.processor.util.Objects.HashCodeBuilder;
import static org.jboss.logging.processor.util.Objects.ToStringBuilder;
import static org.jboss.logging.processor.util.Objects.areEqual;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import org.jboss.logging.processor.intf.model.MessageInterface;
import org.jboss.logging.processor.intf.model.MessageMethod;

/**
 * A factory to create a {@link org.jboss.logging.processor.intf.model.MessageInterface} for annotation processors.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public final class MessageInterfaceFactory {
    private static volatile LoggerInterface LOGGER_INTERFACE = null;
    private static final Object LOCK = new Object();

    /**
     * Private constructor for factory.
     */
    private MessageInterfaceFactory() {
    }

    /**
     * Creates a message interface from the {@link javax.lang.model.element.TypeElement} specified by the {@code interfaceElement} parameter.
     *
     * @param processingEnvironment the annotation processing environment.
     * @param interfaceElement      the interface element to parse.
     *
     * @return a message interface for the interface element.
     */
    public static MessageInterface of(final ProcessingEnvironment processingEnvironment, final TypeElement interfaceElement) {
        final Types types = processingEnvironment.getTypeUtils();
        final Elements elements = processingEnvironment.getElementUtils();
        if (types.isSameType(interfaceElement.asType(), elements.getTypeElement(loggers().loggerInterface().getName()).asType())) {
            MessageInterface result = LOGGER_INTERFACE;
            if (result == null) {
                synchronized (LOCK) {
                    result = LOGGER_INTERFACE;
                    if (result == null) {
                        LOGGER_INTERFACE = LoggerInterface.of(elements, types);
                        result = LOGGER_INTERFACE;
                    }
                }
            }
            return result;
        }
        final AptMessageInterface result = new AptMessageInterface(interfaceElement, types, elements);
        result.init();
        for (TypeMirror typeMirror : interfaceElement.getInterfaces()) {
            final MessageInterface extended = MessageInterfaceFactory.of(processingEnvironment, (TypeElement) types.asElement(typeMirror));
            result.extendedInterfaces.add(extended);
            result.extendedInterfaces.addAll(extended.extendedInterfaces());
        }
        return result;
    }

    /**
     * Message interface implementation.
     */
    private static class AptMessageInterface implements MessageInterface {
        private final TypeElement interfaceElement;
        private final Types types;
        private final Elements elements;
        private final Set<MessageInterface> extendedInterfaces;
        private final List<MessageMethod> messageMethods;
        private String projectCode;
        private String packageName;
        private String simpleName;
        private String qualifiedName;
        private String fqcn;

        private AptMessageInterface(final TypeElement interfaceElement, final Types types, final Elements elements) {
            this.interfaceElement = interfaceElement;
            this.types = types;
            this.elements = elements;
            this.messageMethods = new LinkedList<MessageMethod>();
            this.extendedInterfaces = new LinkedHashSet<MessageInterface>();
        }

        @Override
        public boolean extendsLoggerInterface() {
            return LOGGER_INTERFACE != null && extendedInterfaces.contains(LOGGER_INTERFACE);
        }

        @Override
        public Set<MessageInterface> extendedInterfaces() {
            return Collections.unmodifiableSet(extendedInterfaces);
        }

        @Override
        public Collection<MessageMethod> methods() {
            return messageMethods;
        }

        @Override
        public String projectCode() {
            return projectCode;
        }

        @Override
        public String name() {
            return qualifiedName;
        }

        @Override
        public String packageName() {
            return packageName;
        }

        @Override
        public String simpleName() {
            return simpleName;
        }

        @Override
        public String loggingFQCN() {
            return fqcn;
        }

        @Override
        public boolean isMessageLogger() {
            return isAnnotatedWith(interfaceElement, annotations().messageLogger());
        }

        @Override
        public boolean isMessageBundle() {
            return isAnnotatedWith(interfaceElement, annotations().messageBundle());
        }

        @Override
        public boolean isLoggerInterface() {
            return false;
        }

        @Override
        public int hashCode() {
            return HashCodeBuilder.builder().add(name()).toHashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof AptMessageInterface)) {
                return false;
            }
            final AptMessageInterface other = (AptMessageInterface) obj;
            return areEqual(name(), other.name());
        }

        @Override
        public int compareTo(final MessageInterface o) {
            return this.name().compareTo(o.name());
        }

        @Override
        public String toString() {
            return ToStringBuilder.of(this).add(qualifiedName).toString();
        }


        private void init() {
            // Keeping below for now
            final Collection<ExecutableElement> methods = ElementFilter.methodsIn(interfaceElement.getEnclosedElements());
            final MessageMethodBuilder builder = MessageMethodBuilder.create(elements, types);
            for (ExecutableElement param : methods) {
                builder.add(param);
            }
            final Collection<MessageMethod> m = builder.build();
            if (m != null)
                this.messageMethods.addAll(m);
            projectCode = aptHelper().projectCode(interfaceElement);
            qualifiedName = elements.getBinaryName(interfaceElement).toString();
            final int lastDot = qualifiedName.lastIndexOf(".");
            if (lastDot > 0) {
                packageName = qualifiedName.substring(0, lastDot);
                simpleName = qualifiedName.substring(lastDot + 1);
            } else {
                packageName = null;
                simpleName = qualifiedName;
            }
            // Format class may not yet be compiled, so get it in a roundabout way
            for (AnnotationMirror mirror : interfaceElement.getAnnotationMirrors()) {
                final DeclaredType annotationType = mirror.getAnnotationType();
                if (annotationType.toString().equals(types.getDeclaredType(elements.getTypeElement(annotations().messageLogger().getName())).toString())) {
                    final Map<? extends ExecutableElement, ? extends AnnotationValue> map = mirror.getElementValues();
                    for (ExecutableElement key : map.keySet()) {
                        if (key.getSimpleName().contentEquals("loggingClass")) {
                            final String value = map.get(key).getValue().toString();
                            if (!value.equals(Void.class.getName()))
                                fqcn = value.toString();
                        }
                    }
                }
            }
        }

        @Override
        public TypeElement reference() {
            return interfaceElement;
        }

        @Override
        public String type() {
            return name();
        }

        @Override
        public boolean isAssignableFrom(final Class<?> type) {
            final TypeMirror typeMirror = elements.getTypeElement(type.getName()).asType();
            return types.isAssignable(typeMirror, interfaceElement.asType());
        }

        @Override
        public boolean isSubtypeOf(final Class<?> type) {
            final TypeMirror typeMirror = elements.getTypeElement(type.getName()).asType();
            return types.isSubtype(interfaceElement.asType(), typeMirror);
        }

        @Override
        public boolean isSameAs(final Class<?> type) {
            return qualifiedName.equals(type.getName());
        }

        @Override
        public String getComment() {
            return elements.getDocComment(interfaceElement);
        }
    }

    private static class LoggerInterface implements MessageInterface {
        private final TypeElement loggerInterface;
        private final Elements elements;
        private final Types types;
        private final Set<MessageMethod> messageMethods;

        private LoggerInterface(final Elements elements, final Types types) {
            this.elements = elements;
            this.types = types;
            messageMethods = new HashSet<MessageMethod>();
            this.loggerInterface = elements.getTypeElement(loggers().loggerInterface().getName());
        }

        static LoggerInterface of(final Elements elements, final Types types) {
            final LoggerInterface result = new LoggerInterface(elements, types);
            result.init();
            return result;
        }

        private void init() {
            final MessageMethodBuilder builder = MessageMethodBuilder.create(elements, types);
            List<ExecutableElement> methods = ElementFilter.methodsIn(loggerInterface.getEnclosedElements());
            for (ExecutableElement method : methods) {
                builder.add(method);
            }
            final Collection<MessageMethod> m = builder.build();
            this.messageMethods.addAll(m);
        }

        @Override
        public boolean extendsLoggerInterface() {
            return false;
        }

        @Override
        public Set<MessageInterface> extendedInterfaces() {
            return Collections.emptySet();
        }

        @Override
        public Collection<MessageMethod> methods() {
            return messageMethods;
        }

        @Override
        public String projectCode() {
            return null;
        }

        @Override
        public String name() {
            return loggers().loggerInterface().getName();
        }

        @Override
        public String packageName() {
            return loggers().loggerInterface().getPackage().getName();
        }

        @Override
        public String simpleName() {
            return loggers().loggerInterface().getSimpleName();
        }

        @Override
        public String loggingFQCN() {
            return null;
        }

        @Override
        public boolean isMessageLogger() {
            return false;
        }

        @Override
        public boolean isMessageBundle() {
            return false;
        }

        @Override
        public boolean isLoggerInterface() {
            return true;
        }

        @Override
        public TypeElement reference() {
            return loggerInterface;
        }

        @Override
        public String type() {
            return name();
        }

        @Override
        public boolean isAssignableFrom(final Class<?> type) {
            final TypeMirror typeMirror = elements.getTypeElement(type.getName()).asType();
            return types.isAssignable(typeMirror, loggerInterface.asType());
        }

        @Override
        public boolean isSubtypeOf(final Class<?> type) {
            final TypeMirror typeMirror = elements.getTypeElement(type.getName()).asType();
            return types.isSubtype(loggerInterface.asType(), typeMirror);
        }

        @Override
        public boolean isSameAs(final Class<?> type) {
            return name().equals(type.getName());
        }

        @Override
        public int hashCode() {
            return HashCodeBuilder.builder().add(name()).toHashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof AptMessageInterface)) {
                return false;
            }
            final AptMessageInterface other = (AptMessageInterface) obj;
            return areEqual(name(), other.name());
        }

        @Override
        public int compareTo(final MessageInterface o) {
            return this.name().compareTo(o.name());
        }

        @Override
        public String toString() {
            return ToStringBuilder.of(this).add(name()).toString();
        }

        @Override
        public String getComment() {
            return elements.getDocComment(loggerInterface);
        }
    }
}
