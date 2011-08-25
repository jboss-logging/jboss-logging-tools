package org.jboss.logging.generator.validation;

import org.jboss.logging.generator.intf.model.MessageInterface;
import org.jboss.logging.generator.intf.model.Method;
import org.jboss.logging.generator.intf.model.Parameter;
import org.jboss.logging.generator.intf.model.ReturnType;
import org.jboss.logging.generator.intf.model.ReturnType.ThrowableReturnType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.jboss.logging.generator.validation.ValidationMessageFactory.createError;
import static org.jboss.logging.generator.validation.ValidationMessageFactory.createWarning;

/**
 * Date: 12.08.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public final class Validator {
    public static final Validator INSTANCE = new Validator();

    private Validator() {
    }

    /**
     * Validates the message interface and returns a collection of validation messages or an empty collection.
     *
     * @param messageInterface the message interface to validate.
     *
     * @return a collection of validation messages or an empty collection.
     */
    public final Collection<ValidationMessage> validate(final MessageInterface messageInterface) {
        final List<ValidationMessage> messages = new ArrayList<ValidationMessage>();
        if (messageInterface.isMessageBundle()) {
            final String projectCode = messageInterface.projectCode();
            // Get all methods except basic logger methods
            final Set<Method> methods = getAllMethods(messageInterface);
            messages.addAll(validateCommon(projectCode, methods));
            messages.addAll(validateBundle(methods));
        } else if (messageInterface.isMessageLogger()) {
            final String projectCode = messageInterface.projectCode();
            // Get all methods except basic logger methods
            final Set<Method> methods = getAllMethods(messageInterface);
            messages.addAll(validateCommon(projectCode, methods));
            messages.addAll(validateLogger(methods));
        } else {
            messages.add(createError(messageInterface, "Message interface %s is not a message bundle or message logger.", messageInterface.name()));
        }
        return messages;
    }

    /**
     * Validate common attributes to all interfaces.
     *
     * @param projectCode the project code of the interface.
     * @param methods     the methods to validate.
     *
     * @return a collection of validation messages.
     */
    private Collection<ValidationMessage> validateCommon(final String projectCode, final Set<Method> methods) {
        final List<ValidationMessage> messages = new ArrayList<ValidationMessage>();
        final Map<String, Method> methodNames = new HashMap<String, Method>();
        for (Method method : methods) {
            final Method.Message message = method.message();
            if (message == null) {
                messages.add(createError(method, "All message bundles and message logger methods must have or inherit a message."));
                continue;
            }
            // Check the message id
            if (message.hasId()) {
                // Make sure the message id is greater than 0
                if (message.id() < 0) {
                    messages.add(createError(method, "Message id %d is invalid. Must be greater than 0 or inherit another valid id.", message.id()));
                } else {
                    messages.addAll(MessageIdValidator.INSTANCE.validate(projectCode, method));
                }
            }
            final FormatValidator formatValidator = FormatValidatorFactory.create(method);
            if (formatValidator.isValid()) {
                final int paramCount = method.formatParameterCount();
                if (method.formatParameterCount() != formatValidator.argumentCount()) {
                    messages.add(createError(method, "Parameter count does not match for format '%s'. Required: %d Provided: %d", formatValidator.format(), formatValidator.argumentCount(), paramCount));
                }
            } else {
                messages.add(createError(method, formatValidator.summaryMessage()));
            }
            // Make sure there is only one @Message annotation per method name.
            if (!method.inheritsMessage()) {
                final String key = method.name() + method.formatParameterCount();
                if (methodNames.containsKey(key)) {
                    final Method previousMethod = methodNames.get(key);
                    messages.add(createError(previousMethod, "Only one message with the same format parameters is allowed."));
                    messages.add(createError(method, "Only one message with the same format parameters is allowed."));
                } else {
                    methodNames.put(key, method);
                }
            }
            // Validate the parameters
            messages.addAll(validateParameters(method));
        }
        return messages;
    }

    private Collection<ValidationMessage> validateParameters(final Method method) {
        final List<ValidationMessage> messages = new ArrayList<ValidationMessage>();
        boolean foundCause = false;
        for (Parameter parameter : method.allParameters()) {
            if (parameter.isCause()) {
                if (foundCause) {
                    messages.add(createError(method, "Only one cause parameter is allowed."));
                    break; // TODO - May need to remove if other validation is required.
                } else {
                    foundCause = true;
                }
            }
        }
        // TODO - Check all parameter counts
        return messages;
    }

    /**
     * Validate message bundle methods.
     *
     * @param methods the methods to validate.
     *
     * @return a collection of the validation messages.
     */
    private Collection<ValidationMessage> validateBundle(final Set<Method> methods) {
        final List<ValidationMessage> messages = new ArrayList<ValidationMessage>();
        for (Method method : methods) {
            messages.addAll(validateBundleMethod(method));
        }
        return messages;
    }

    private Collection<ValidationMessage> validateBundleMethod(final Method method) {
        final List<ValidationMessage> messages = new ArrayList<ValidationMessage>();
        // The return type must be a Throwable or String
        final ReturnType returnType = method.returnType();
        if (returnType.equals(ReturnType.VOID) || returnType.isPrimitive()) {
            messages.add(createError(method, "Message bundle method %s has an invalid return type. Cannot be void or a primitive.", method.name()));
        } else if (returnType.isThrowable()) {
            if (!returnType.isSubtypeOf(Throwable.class)) {
                // if (!returnType.isSubtypeOf(Throwable.class)) {
                messages.add(createError(method, "Message bundle method %s has an invalid return type of %s.", method.name(), returnType.name()));
            }
            final ThrowableReturnType throwableReturnType = returnType.throwableReturnType();
            if (throwableReturnType.useConstructionParameters()) {
                // TODO - Check the return type constructor. Currently handled via the ThrowableReturnTypeFactory.
            } else if (!throwableReturnType.useConstructionParameters() && !method.constructorParameters().isEmpty()) {
                messages.add(createError(method, "Method does not have an usable constructor for the return type %s.", returnType.name()));
            } else {
                final boolean hasMessageConstructor = (throwableReturnType.hasStringAndThrowableConstructor() || throwableReturnType.hasThrowableAndStringConstructor() ||
                        throwableReturnType.hasStringConstructor());
                final boolean usableConstructor = (throwableReturnType.hasDefaultConstructor() || throwableReturnType.hasStringAndThrowableConstructor() ||
                        throwableReturnType.hasStringConstructor() || throwableReturnType.hasThrowableAndStringConstructor() || throwableReturnType.hasThrowableConstructor());
                if (!usableConstructor) {
                    messages.add(createError(method, "Method does not have an usable constructor for the return type %s.", returnType.name()));
                } else if (!hasMessageConstructor) { // Check to see if there is no message constructor
                    messages.add(createWarning(method, "The message cannot be set via the throwable constructor and will be ignored."));
                }
            }
        } else {
            if (!returnType.isAssignableFrom(String.class)) {
                messages.add(createError(method, "Return type %s does not appear valid for a message bundle.", method.name()));
            }
        }
        return messages;
    }

    /**
     * Validate message logger methods.
     *
     * @param methods the methods to validate.
     *
     * @return a collection of the validation messages.
     */
    private Collection<ValidationMessage> validateLogger(final Set<Method> methods) {
        final List<ValidationMessage> messages = new ArrayList<ValidationMessage>();
        for (Method method : methods) {
            if (method.isLoggerMethod()) {
                messages.addAll(validateLoggerMethod(method));
            } else {
                messages.addAll(validateBundleMethod(method));
            }
        }
        return messages;
    }

    private Collection<ValidationMessage> validateLoggerMethod(final Method method) {
        final List<ValidationMessage> messages = new ArrayList<ValidationMessage>();
        // The return type must be void
        if (!ReturnType.VOID.equals(method.returnType())) {
            messages.add(createError(method, "Message logger methods can only have a void return type."));
        }
        return messages;
    }

    /**
     * Finds all methods for the given interface, but ignores basic logger methods.
     *
     * @param messageInterface the interface to find all methods for.
     *
     * @return a set of all the methods (exception basic logger methods) the interface must implement.
     */
    private Set<Method> getAllMethods(final MessageInterface messageInterface) {
        if (messageInterface.isBasicLogger()) {
            return Collections.emptySet();
        }
        final Set<Method> methods = new HashSet<Method>();
        for (Method method : messageInterface.methods()) {
            methods.add(method);
        }
        for (MessageInterface msgInterface : messageInterface.extendedInterfaces()) {
            methods.addAll(getAllMethods(msgInterface));
        }
        return methods;
    }
}
