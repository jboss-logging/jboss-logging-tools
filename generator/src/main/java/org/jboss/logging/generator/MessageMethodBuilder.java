package org.jboss.logging.generator;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;
import static org.jboss.logging.generator.LoggingTools.annotations;
import static org.jboss.logging.generator.util.ElementHelper.findByName;
import static org.jboss.logging.generator.util.ElementHelper.inheritsMessage;
import static org.jboss.logging.generator.util.ElementHelper.isAnnotatedWith;
import static org.jboss.logging.generator.util.ElementHelper.isOverloaded;
import static org.jboss.logging.generator.util.ElementHelper.parameterCount;

/**
 * Date: 29.07.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
final class MessageMethodBuilder {

    private static final String MESSAGE_METHOD_SUFFIX = "$str";
    private final List<ExecutableElement> methods;
    private final Elements elements;
    private final Types types;

    private MessageMethodBuilder(final Elements elements, final Types types) {
        this.elements = elements;
        this.types = types;
        methods = new LinkedList<ExecutableElement>();
    }

    static MessageMethodBuilder create(final Elements elements, final Types types) {
        return new MessageMethodBuilder(elements, types);
    }

    MessageMethodBuilder add(final ExecutableElement method) {
        methods.add(method);
        return this;
    }

    Set<? extends MessageMethod> build() {
        final Set<AptMessageMethod> result = new LinkedHashSet<AptMessageMethod>();
        for (ExecutableElement elementMethod : methods) {
            final AptMessageMethod resultMethod = new AptMessageMethod(elementMethod);
            resultMethod.inheritsMessage = inheritsMessage(methods, elementMethod);
            resultMethod.message = findMessage(methods, elementMethod);
            resultMethod.isOverloaded = isOverloaded(methods, elementMethod);

            // Create a list of parameters
            for (MethodParameter methodParameter : MethodParameterFactory.of(elements, types, resultMethod.method)) {
                if (methodParameter.isParam()) {
                    resultMethod.constructorParameters.add(methodParameter);
                } else if (methodParameter.isCause()) {
                    resultMethod.cause = methodParameter;
                } else {
                    resultMethod.formatParameters.add(methodParameter);
                }
                resultMethod.allParameters.add(methodParameter);
            }
            // Setup the global variables for the result

            // Check to see if the method is overloaded
            if (resultMethod.isOverloaded()) {
                resultMethod.messageMethodName = resultMethod.name() + resultMethod.formatParameterCount() + MESSAGE_METHOD_SUFFIX;
                resultMethod.translationKey = resultMethod.name() + "." + resultMethod.formatParameterCount();
            } else {
                resultMethod.messageMethodName = resultMethod.name() + MESSAGE_METHOD_SUFFIX;
                resultMethod.translationKey = resultMethod.name();
            }
            // Set the return type
            resultMethod.returnType = MessageReturnTypeFactory.of(elements, types, elementMethod.getReturnType(), resultMethod);
            result.add(resultMethod);
        }
        return Collections.unmodifiableSet(result);
    }

    private static MessageMethod.Message findMessage(final Collection<ExecutableElement> methods, final ExecutableElement method) {
        AptMessage result = null;
        if (isAnnotatedWith(method, annotations().message())) {
            result = new AptMessage();
            result.hasId = annotations().hasMessageId(method);
            result.value = annotations().messageValue(method);
            result.formatType = annotations().messageFormat(method);
            result.inheritsId = annotations().inheritsMessageId(method);
            if (result.inheritsId()) {
                result.id = findMessageId(methods, method);
            } else {
                result.id = annotations().messageId(method);
            }
        } else {
            final Collection<ExecutableElement> allMethods = findByName(methods, method.getSimpleName(), parameterCount(method.getParameters()));
            for (ExecutableElement m : allMethods) {
                if (isAnnotatedWith(m, annotations().message())) {
                    result = new AptMessage();
                    result.hasId = annotations().hasMessageId(m);
                    result.value = annotations().messageValue(m);
                    result.formatType = annotations().messageFormat(m);
                    result.inheritsId = annotations().inheritsMessageId(m);
                    if (result.inheritsId()) {
                        result.id = findMessageId(methods, m);
                    } else {
                        result.id = annotations().messageId(m);
                    }
                    break;
                }
            }
        }
        return result;
    }

    private static int findMessageId(final Collection<ExecutableElement> methods, final ExecutableElement method) {
        int result = -2;
        final Collection<ExecutableElement> allMethods = findByName(methods, method.getSimpleName(), parameterCount(method.getParameters()));
        for (ExecutableElement m : allMethods) {
            if (isAnnotatedWith(m, annotations().message())) {
                if (!annotations().inheritsMessageId(m)) {
                    result = annotations().messageId(m);
                }
            }
        }
        return result;
    }


    /**
     * An implementation for the MessageMethod interface.
     */
    private static class AptMessageMethod implements MessageMethod {

        private MethodParameter cause;
        private boolean inheritsMessage;
        private boolean isOverloaded;
        private MessageReturnType returnType;
        private Message message;
        private String messageMethodName;
        private final ExecutableElement method;
        private final Set<MethodParameter> allParameters;
        private final Set<MethodParameter> formatParameters;
        private final Set<MethodParameter> constructorParameters;
        private String translationKey;

        /**
         * Private constructor for the
         *
         * @param method the method to describe.
         */
        AptMessageMethod(final ExecutableElement method) {
            this.method = method;
            inheritsMessage = false;
            isOverloaded = false;
            allParameters = new LinkedHashSet<MethodParameter>();
            formatParameters = new LinkedHashSet<MethodParameter>();
            constructorParameters = new LinkedHashSet<MethodParameter>();
        }

        @Override
        public Message message() {
            return message;
        }

        @Override
        public boolean inheritsMessage() {
            return inheritsMessage;
        }

        @Override
        public String messageMethodName() {
            return messageMethodName;
        }

        @Override
        public String translationKey() {
            return translationKey;
        }

        @Override
        public String name() {
            return method.getSimpleName().toString();
        }

        @Override
        public boolean hasCause() {
            return cause != null;
        }

        @Override
        public boolean isOverloaded() {
            return isOverloaded;
        }

        @Override
        public MethodParameter cause() {
            return cause;
        }

        @Override
        public MessageReturnType returnType() {
            return returnType;
        }

        @Override
        public String loggerMethod() {
            return annotations().loggerMethod(method, message.format());
        }

        @Override
        public String logLevelParameter() {
            return annotations().logLevel(method);
        }

        @Override
        public Set<MethodParameter> allParameters() {
            return unmodifiableSet(allParameters);
        }

        @Override
        public Set<MethodParameter> formatParameters() {
            return unmodifiableSet(formatParameters);
        }

        @Override
        public Set<MethodParameter> constructorParameters() {
            return unmodifiableSet(constructorParameters);
        }

        @Override
        public int formatParameterCount() {
            return formatParameters.size();
        }

        @Override
        public boolean isLoggerMethod() {
            return isAnnotatedWith(method, annotations().logMessage());
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (name() == null ? 0 : name().hashCode());
            if (allParameters == null) {
                result = prime * result;
            } else {
                for (MethodParameter param : allParameters) {
                    result = prime * result + (param.hashCode());
                }
            }
            result = prime * result + (returnType == null ? 0 : returnType.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof AptMessageMethod)) {
                return false;
            }
            final AptMessageMethod other = (AptMessageMethod) obj;
            if ((name() == null) ? other.name() != null : !name().equals(other.name())) {
                return false;
            }
            if ((allParameters == null) ? other.allParameters != null : !(allParameters.equals(other.allParameters))) {
                return false;
            }
            return !((returnType == null) ? other.returnType != null : !returnType.equals(other.returnType));
        }

        @Override
        public int compareTo(final MessageMethod o) {
            int result = name().compareTo(o.name());
            result = (result != 0) ? result : returnType.name().compareTo(o.returnType().name());
            // Size does matter
            result = allParameters.size() - o.allParameters().size();
            if (result == 0) {
                // Check element by element
                final Iterator<MethodParameter> params1 = allParameters.iterator();
                final Iterator<MethodParameter> params2 = o.allParameters().iterator();
                while (params1.hasNext()) {
                    if (params2.hasNext()) {
                        final MethodParameter param1 = params1.next();
                        final MethodParameter param2 = params2.next();
                        result = param1.compareTo(param2);
                    } else {
                        result = 1;
                    }
                    // Short circuit
                    if (result != 0) break;
                }
            }
            return result;
        }

        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(getClass().getSimpleName());
            stringBuilder.append("(name=");
            stringBuilder.append(name());
            stringBuilder.append(",message=");
            stringBuilder.append(message);
            stringBuilder.append(",loggerMethod=");
            stringBuilder.append(loggerMethod());
            stringBuilder.append(")");
            return stringBuilder.toString();
        }

        @Override
        public ExecutableElement reference() {
            return method;
        }
    }

    private static class AptMessage implements MessageMethod.Message {

        private boolean hasId;
        private int id;
        private boolean inheritsId;
        private String value;
        private Annotations.FormatType formatType;

        private AptMessage() {
        }

        @Override
        public int id() {
            return id;
        }

        @Override
        public boolean hasId() {
            return hasId;
        }

        @Override
        public boolean inheritsId() {
            return inheritsId;
        }

        @Override
        public String value() {
            return value;
        }

        @Override
        public Annotations.FormatType format() {
            return formatType;
        }
    }
}
