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

import static org.jboss.logging.processor.util.Objects.HashCodeBuilder;
import static org.jboss.logging.processor.util.Objects.ToStringBuilder;
import static org.jboss.logging.processor.util.Objects.areEqual;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.annotations.MessageBundle;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.logging.annotations.ValidIdRange;
import org.jboss.logging.annotations.ValidIdRanges;
import org.jboss.logging.processor.model.MessageInterface;
import org.jboss.logging.processor.model.MessageMethod;
import org.jboss.logging.processor.util.ElementHelper;

/**
 * A factory to create a {@link org.jboss.logging.processor.model.MessageInterface} for annotation processors.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public final class MessageInterfaceFactory {
    private static final Object LOCK = new Object();
    private static volatile LoggerInterface LOGGER_INTERFACE;

    /**
     * Private constructor for factory.
     */
    private MessageInterfaceFactory() {
    }

    /**
     * Creates a message interface from the {@link javax.lang.model.element.TypeElement} specified by the {@code
     * interfaceElement} parameter.
     *
     * @param processingEnvironment the annotation processing environment.
     * @param interfaceElement      the interface element to parse.
     *
     * @return a message interface for the interface element.
     */
    public static MessageInterface of(final ProcessingEnvironment processingEnvironment, final TypeElement interfaceElement) {
        final Types types = processingEnvironment.getTypeUtils();
        final Elements elements = processingEnvironment.getElementUtils();
        if (types.isSameType(interfaceElement.asType(), elements.getTypeElement(BasicLogger.class.getName()).asType())) {
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
    private static class AptMessageInterface extends AbstractMessageObjectType implements MessageInterface {
        private final TypeElement interfaceElement;
        private final Set<MessageInterface> extendedInterfaces;
        private final List<MessageMethod> messageMethods;
        private final List<ValidIdRange> validIdRanges;
        private String projectCode;
        private String packageName;
        private String simpleName;
        private String qualifiedName;
        private String fqcn;
        private int idLen;

        private AptMessageInterface(final TypeElement interfaceElement, final Types types, final Elements elements) {
            super(elements, types, interfaceElement);
            this.interfaceElement = interfaceElement;
            this.messageMethods = new LinkedList<MessageMethod>();
            this.extendedInterfaces = new LinkedHashSet<MessageInterface>();
            if (ElementHelper.isAnnotatedWith(interfaceElement, ValidIdRanges.class)) {
                validIdRanges = Arrays.asList(interfaceElement.getAnnotation(ValidIdRanges.class).value());
            } else if (ElementHelper.isAnnotatedWith(interfaceElement, ValidIdRange.class)) {
                validIdRanges = Arrays.asList(interfaceElement.getAnnotation(ValidIdRange.class));
            } else {
                validIdRanges = Collections.emptyList();
            }
        }

        @Override
        public boolean extendsLoggerInterface() {
            return LOGGER_INTERFACE != null && extendedInterfaces.contains(LOGGER_INTERFACE);
        }

        @Override
        public String name() {
            return qualifiedName;
        }

        @Override
        public Set<MessageInterface> extendedInterfaces() {
            return Collections.unmodifiableSet(extendedInterfaces);
        }

        @Override
        public int hashCode() {
            return HashCodeBuilder.builder().add(name()).toHashCode();
        }

        @Override
        public Collection<MessageMethod> methods() {
            return messageMethods;
        }

        @Override
        public int compareTo(final MessageInterface o) {
            return this.name().compareTo(o.name());
        }

        @Override
        public String projectCode() {
            return projectCode;
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
            final MessageBundle messageBundle = interfaceElement.getAnnotation(MessageBundle.class);
            final MessageLogger messageLogger = interfaceElement.getAnnotation(MessageLogger.class);
            if (messageBundle != null) {
                projectCode = messageBundle.projectCode();
                idLen = messageBundle.length();
            } else if (messageLogger != null) {
                projectCode = messageLogger.projectCode();
                idLen = messageLogger.length();
            } // TODO (jrp) this should cause an error
            qualifiedName = elements.getBinaryName(interfaceElement).toString();
            final int lastDot = qualifiedName.lastIndexOf(".");
            if (lastDot > 0) {
                packageName = qualifiedName.substring(0, lastDot);
                simpleName = qualifiedName.substring(lastDot + 1);
            } else {
                packageName = null;
                simpleName = qualifiedName;
            }
            // Get the FQCN
            final TypeElement loggingClass = ElementHelper.getClassAnnotationValue(interfaceElement, MessageLogger.class, "loggingClass");
            if (loggingClass != null) {
                final String value = loggingClass.getQualifiedName().toString();
                if (!value.equals(Void.class.getName())) {
                    fqcn = value;
                }
            }
        }

        @Override
        public String type() {
            return name();
        }

        @Override
        public String packageName() {
            return packageName;
        }

        @Override
        public String getComment() {
            return elements.getDocComment(interfaceElement);
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
        public AnnotatedType getAnnotatedType() {
            if (ElementHelper.isAnnotatedWith(interfaceElement, MessageLogger.class)) {
                return AnnotatedType.MESSAGE_LOGGER;
            } else if (ElementHelper.isAnnotatedWith(interfaceElement, MessageBundle.class)) {
                return AnnotatedType.MESSAGE_BUNDLE;
            }
            return AnnotatedType.NONE;
        }

        @Override
        public List<ValidIdRange> validIdRanges() {
            return validIdRanges;
        }

        @Override
        public int getIdLength() {
            return idLen;
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
        public String toString() {
            return ToStringBuilder.of(this).add(qualifiedName).toString();
        }


        @Override
        public TypeElement reference() {
            return interfaceElement;
        }


    }

    private static class LoggerInterface extends AbstractMessageObjectType implements MessageInterface {
        private final TypeElement loggerInterface;
        private final Set<MessageMethod> messageMethods;

        private LoggerInterface(final Elements elements, final Types types) {
            // TODO (jrp) BasicLogger type can likely be initialized once
            super(elements, types, elements.getTypeElement(BasicLogger.class.getName()));
            messageMethods = new LinkedHashSet<MessageMethod>();
            this.loggerInterface = elements.getTypeElement(BasicLogger.class.getName());
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

        static LoggerInterface of(final Elements elements, final Types types) {
            final LoggerInterface result = new LoggerInterface(elements, types);
            result.init();
            return result;
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
            return BasicLogger.class.getName();
        }

        @Override
        public String packageName() {
            return BasicLogger.class.getPackage().getName();
        }

        @Override
        public String simpleName() {
            return BasicLogger.class.getSimpleName();
        }

        @Override
        public String loggingFQCN() {
            return null;
        }

        @Override
        public AnnotatedType getAnnotatedType() {
            return AnnotatedType.NONE;
        }

        @Override
        public List<ValidIdRange> validIdRanges() {
            return Collections.emptyList();
        }

        @Override
        public int getIdLength() {
            return -1;
        }

        @Override
        public TypeElement reference() {
            return loggerInterface;
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
