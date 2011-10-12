package org.jboss.logging.generator.apt;

import org.jboss.logging.generator.intf.model.MessageInterface;
import org.jboss.logging.generator.intf.model.Method;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.jboss.logging.generator.Tools.annotations;
import static org.jboss.logging.generator.Tools.aptHelper;
import static org.jboss.logging.generator.Tools.loggers;
import static org.jboss.logging.generator.util.ElementHelper.isAnnotatedWith;
import static org.jboss.logging.generator.util.Objects.HashCodeBuilder;
import static org.jboss.logging.generator.util.Objects.ToStringBuilder;
import static org.jboss.logging.generator.util.Objects.areEqual;

/**
 * A factory to create a {@link org.jboss.logging.generator.intf.model.MessageInterface} for annotation processors.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public final class MessageInterfaceFactory {
    private static volatile BasicLoggerInterface BASIC_LOGGER_INTERFACE = null;
    private static final Object LOCK = new Object();

    /**
     * Private constructor for factory.
     */
    private MessageInterfaceFactory() {
    }

    /**
     * Creates a message interface from the {@link TypeElement} specified by the {@code interfaceElement} parameter.
     *
     * @param processingEnvironment the annotation processing environment.
     * @param interfaceElement      the interface element to parse.
     *
     * @return a message interface for the interface element.
     */
    public static MessageInterface of(final ProcessingEnvironment processingEnvironment, final TypeElement interfaceElement) {
        final Types types = processingEnvironment.getTypeUtils();
        final Elements elements = processingEnvironment.getElementUtils();
        if (types.isSameType(interfaceElement.asType(), elements.getTypeElement(loggers().basicLoggerClass().getName()).asType())) {
            MessageInterface result = BASIC_LOGGER_INTERFACE;
            if (result == null) {
                synchronized (LOCK) {
                    result = BASIC_LOGGER_INTERFACE;
                    if (result == null) {
                        BASIC_LOGGER_INTERFACE = BasicLoggerInterface.of(elements, types);
                        result = BASIC_LOGGER_INTERFACE;
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
        private final List<Method> methods;
        private String projectCode;
        private String packageName;
        private String simpleName;
        private String qualifiedName;

        private AptMessageInterface(final TypeElement interfaceElement, final Types types, final Elements elements) {
            this.interfaceElement = interfaceElement;
            this.types = types;
            this.elements = elements;
            this.methods = new LinkedList<Method>();
            this.extendedInterfaces = new LinkedHashSet<MessageInterface>();
        }

        @Override
        public Set<MessageInterface> extendedInterfaces() {
            return Collections.unmodifiableSet(extendedInterfaces);
        }

        @Override
        public Collection<Method> methods() {
            return methods;
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
        public boolean isMessageLogger() {
            return isAnnotatedWith(interfaceElement, annotations().messageLogger());
        }

        @Override
        public boolean isMessageBundle() {
            return isAnnotatedWith(interfaceElement, annotations().messageBundle());
        }

        @Override
        public boolean isBasicLogger() {
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
            final Collection<? extends Method> m = builder.build();
            if (m != null)
                this.methods.addAll(m);
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
        }

        @Override
        public TypeElement reference() {
            return interfaceElement;
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
    }

    private static class BasicLoggerInterface implements MessageInterface {
        private final TypeElement basicLogger;
        private final Elements elements;
        private final Types types;
        private final Set<Method> methods;

        private BasicLoggerInterface(final Elements elements, final Types types) {
            this.elements = elements;
            this.types = types;
            methods = new HashSet<Method>();
            this.basicLogger = elements.getTypeElement(loggers().basicLoggerClass().getName());
        }

        static BasicLoggerInterface of(final Elements elements, final Types types) {
            final BasicLoggerInterface result = new BasicLoggerInterface(elements, types);
            result.init();
            return result;
        }

        private void init() {
            final MessageMethodBuilder builder = MessageMethodBuilder.create(elements, types);
            List<ExecutableElement> methods = ElementFilter.methodsIn(basicLogger.getEnclosedElements());
            for (ExecutableElement method : methods) {
                builder.add(method);
            }
            final Collection<? extends Method> m = builder.build();
            this.methods.addAll(m);
        }

        @Override
        public Set<MessageInterface> extendedInterfaces() {
            return Collections.emptySet();
        }

        @Override
        public Collection<Method> methods() {
            return methods;
        }

        @Override
        public String projectCode() {
            return null;
        }

        @Override
        public String name() {
            return loggers().basicLoggerClass().getName();
        }

        @Override
        public String packageName() {
            return loggers().basicLoggerClass().getPackage().getName();
        }

        @Override
        public String simpleName() {
            return loggers().basicLoggerClass().getSimpleName();
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
        public boolean isBasicLogger() {
            return true;
        }

        @Override
        public TypeElement reference() {
            return basicLogger;
        }

        @Override
        public boolean isAssignableFrom(final Class<?> type) {
            final TypeMirror typeMirror = elements.getTypeElement(type.getName()).asType();
            return types.isAssignable(typeMirror, basicLogger.asType());
        }

        @Override
        public boolean isSubtypeOf(final Class<?> type) {
            final TypeMirror typeMirror = elements.getTypeElement(type.getName()).asType();
            return types.isSubtype(basicLogger.asType(), typeMirror);
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
    }
}
