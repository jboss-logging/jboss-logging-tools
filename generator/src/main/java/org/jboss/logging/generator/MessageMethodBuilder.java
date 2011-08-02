package org.jboss.logging.generator;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;
import static org.jboss.logging.generator.LoggingTools.annotations;
import static org.jboss.logging.generator.util.ElementHelper.isAnnotatedWith;
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
        final Set<MessageMethodImpl> result = new LinkedHashSet<MessageMethodImpl>();
        for (ExecutableElement elementMethod : methods) {
            final MessageMethodImpl resultMethod = new MessageMethodImpl(elementMethod);
            // Find the annotations
            Message message = Message.of(annotations().messageId(elementMethod), annotations().hasMessageId(elementMethod),
                    annotations().messageValue(elementMethod), annotations().messageFormat(elementMethod));

            // Find the first method with non-null @Message
            for (MessageMethodImpl method : result) {
                // Check for inherited message id's
                if (annotations().inheritsMessageId(elementMethod) && method.message.hasId()) {
                    final Message current = message;
                    message = Message.of(message.id(), message.hasId(), current.value(), current.format());
                }
                // If the message is not null, no need to process further.
                if (message.value() != null) {
                    continue;
                }

                if (method.message.value() != null && message.value() == null &&
                        parameterCount(elementMethod.getParameters()) == parameterCount(method.method.getParameters())) {
                    message = method.message;
                }
            }
            // Process through the collection and update any currently null messages
            final Set<MessageMethodImpl> toProcess = new LinkedHashSet<MessageMethodImpl>(result);
            toProcess.addAll(result);
            for (MessageMethodImpl method : toProcess) {
                // Check for inherited message id's
                if (annotations().inheritsMessageId(method.method) && message.hasId()) {
                    final Message old = method.message;
                    method.message = Message.of(message.id(), message.hasId(), old.value(), old.format());
                }
                if (method.message.value() == null) {
                    method.message = message;
                    result.remove(method);
                    result.add(method);
                }
                if (elementMethod.getSimpleName().equals(method.method.getSimpleName()) && parameterCount(method.method.getParameters()) != parameterCount(elementMethod.getParameters())) {
                    resultMethod.isOverloaded = true;
                }
            }

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
            resultMethod.message = message;
            // Check to see if the method is overloaded
            if (resultMethod.isOverloaded) {
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


    /**
     * An implementation for the MessageMethod interface.
     */
    private static class MessageMethodImpl implements MessageMethod {

        private MethodParameter cause;
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
        MessageMethodImpl(final ExecutableElement method) {
            this.method = method;
            isOverloaded = false;
            allParameters = new LinkedHashSet<MethodParameter>();
            formatParameters = new LinkedHashSet<MethodParameter>();
            constructorParameters = new LinkedHashSet<MethodParameter>();
        }

        @Override
        public boolean hasMessageId() {
            return message.hasId();
        }

        @Override
        public Annotations.FormatType messageFormat() {
            return message.format();
        }

        @Override
        public String messageValue() {
            return message.value();
        }

        @Override
        public int messageId() {
            return message.id();
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
            if (!(obj instanceof MessageMethodImpl)) {
                return false;
            }
            final MessageMethodImpl other = (MessageMethodImpl) obj;
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
            result = (result != 0) ? result : returnType.qualifiedClassName().compareTo(o.returnType().qualifiedClassName());
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
    }
}
