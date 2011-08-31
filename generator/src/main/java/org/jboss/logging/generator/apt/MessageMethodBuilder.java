package org.jboss.logging.generator.apt;

import org.jboss.logging.generator.Annotations.FormatType;
import org.jboss.logging.generator.intf.model.Method;
import org.jboss.logging.generator.intf.model.Parameter;
import org.jboss.logging.generator.intf.model.ReturnType;
import org.jboss.logging.generator.util.Comparison;
import org.jboss.logging.generator.util.Objects;

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
import static org.jboss.logging.generator.Tools.annotations;
import static org.jboss.logging.generator.Tools.aptHelper;
import static org.jboss.logging.generator.util.ElementHelper.findByName;
import static org.jboss.logging.generator.util.ElementHelper.inheritsMessage;
import static org.jboss.logging.generator.util.ElementHelper.isAnnotatedWith;
import static org.jboss.logging.generator.util.ElementHelper.isOverloaded;
import static org.jboss.logging.generator.util.ElementHelper.parameterCount;
import static org.jboss.logging.generator.util.Objects.HashCodeBuilder;
import static org.jboss.logging.generator.util.Objects.ToStringBuilder;
import static org.jboss.logging.generator.util.Objects.areEqual;

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

    Set<? extends Method> build() {
        final Set<AptMethod> result = new LinkedHashSet<AptMethod>();
        for (ExecutableElement elementMethod : methods) {
            final AptMethod resultMethod = new AptMethod(elementMethod);
            resultMethod.inheritsMessage = inheritsMessage(methods, elementMethod);
            resultMethod.message = findMessage(methods, elementMethod);
            resultMethod.isOverloaded = isOverloaded(methods, elementMethod);

            // Create a list of parameters
            for (Parameter parameter : ParameterFactory.of(elements, types, resultMethod.method)) {
                if (parameter.isParam()) {
                    resultMethod.constructorParameters.add(parameter);
                } else if (parameter.isCause()) {
                    resultMethod.cause = parameter;
                } else {
                    resultMethod.formatParameters.add(parameter);
                }
                resultMethod.allParameters.add(parameter);
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
            resultMethod.returnType = ReturnTypeFactory.of(elements, types, elementMethod.getReturnType(), resultMethod);
            result.add(resultMethod);
        }
        return Collections.unmodifiableSet(result);
    }

    private static Method.Message findMessage(final Collection<ExecutableElement> methods, final ExecutableElement method) {
        AptMessage result = null;
        if (isAnnotatedWith(method, annotations().message())) {
            result = new AptMessage();
            result.hasId = aptHelper().hasMessageId(method);
            result.value = aptHelper().messageValue(method);
            result.formatType = aptHelper().messageFormat(method);
            result.inheritsId = aptHelper().inheritsMessageId(method);
            if (result.inheritsId()) {
                result.id = findMessageId(methods, method);
            } else {
                result.id = aptHelper().messageId(method);
            }
        } else {
            final Collection<ExecutableElement> allMethods = findByName(methods, method.getSimpleName(), parameterCount(method.getParameters()));
            for (ExecutableElement m : allMethods) {
                if (isAnnotatedWith(m, annotations().message())) {
                    result = new AptMessage();
                    result.hasId = aptHelper().hasMessageId(m);
                    result.value = aptHelper().messageValue(m);
                    result.formatType = aptHelper().messageFormat(m);
                    result.inheritsId = aptHelper().inheritsMessageId(m);
                    if (result.inheritsId()) {
                        result.id = findMessageId(methods, m);
                    } else {
                        result.id = aptHelper().messageId(m);
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
                if (!aptHelper().inheritsMessageId(m)) {
                    result = aptHelper().messageId(m);
                }
            }
        }
        return result;
    }


    /**
     * An implementation for the Method interface.
     */
    private static class AptMethod implements Method {

        private Parameter cause;
        private boolean inheritsMessage;
        private boolean isOverloaded;
        private ReturnType returnType;
        private Message message;
        private String messageMethodName;
        private final ExecutableElement method;
        private final Set<Parameter> allParameters;
        private final Set<Parameter> formatParameters;
        private final Set<Parameter> constructorParameters;
        private String translationKey;

        /**
         * Private constructor for the
         *
         * @param method the method to describe.
         */
        AptMethod(final ExecutableElement method) {
            this.method = method;
            inheritsMessage = false;
            isOverloaded = false;
            allParameters = new LinkedHashSet<Parameter>();
            formatParameters = new LinkedHashSet<Parameter>();
            constructorParameters = new LinkedHashSet<Parameter>();
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
        public Parameter cause() {
            return cause;
        }

        @Override
        public ReturnType returnType() {
            return returnType;
        }

        @Override
        public String loggerMethod() {
            return aptHelper().loggerMethod(message.format());
        }

        @Override
        public String logLevelParameter() {
            return aptHelper().logLevel(method);
        }

        @Override
        public Set<Parameter> allParameters() {
            return unmodifiableSet(allParameters);
        }

        @Override
        public Set<Parameter> formatParameters() {
            return unmodifiableSet(formatParameters);
        }

        @Override
        public Set<Parameter> constructorParameters() {
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
            return HashCodeBuilder.builder()
                    .add(name())
                    .add(allParameters)
                    .add(returnType).toHashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof AptMethod)) {
                return false;
            }
            final AptMethod other = (AptMethod) obj;
            return areEqual(name(), other.name()) &&
                    areEqual(allParameters, other.allParameters) &&
                    areEqual(returnType, other.returnType);
        }

        @Override
        public int compareTo(final Method o) {
            int result = name().compareTo(o.name());
            result = (result != Comparison.EQUAL) ? result : returnType.name().compareTo(o.returnType().name());
            // Size does matter
            result = (result != Comparison.EQUAL) ? result : allParameters.size() - o.allParameters().size();
            if (result == Comparison.EQUAL) {
                // Check element by element
                final Iterator<Parameter> params1 = allParameters.iterator();
                final Iterator<Parameter> params2 = o.allParameters().iterator();
                while (params1.hasNext()) {
                    if (params2.hasNext()) {
                        final Parameter param1 = params1.next();
                        final Parameter param2 = params2.next();
                        result = param1.compareTo(param2);
                    } else {
                        result = Comparison.GREATER;
                    }
                    // Short circuit
                    if (result != Comparison.EQUAL) break;
                }
            }
            return result;
        }

        @Override
        public String toString() {
            return ToStringBuilder.of(this)
                    .add("name", name())
                    .add("message", message)
                    .add("loggerMethod", loggerMethod()).toString();
        }

        @Override
        public ExecutableElement reference() {
            return method;
        }
    }

    private static class AptMessage implements Method.Message {

        private boolean hasId;
        private int id;
        private boolean inheritsId;
        private String value;
        private FormatType formatType;

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
        public FormatType format() {
            return formatType;
        }

        @Override
        public String toString() {
            return ToStringBuilder.of(this)
                    .add("hasId", hasId)
                    .add("id", id)
                    .add("inheritsId", inheritsId)
                    .add("value", value)
                    .add("formatType", formatType).toString();
        }
    }
}
