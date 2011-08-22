package org.jboss.logging.generator.validation;

import org.jboss.logging.generator.MessageInterface;
import org.jboss.logging.generator.MessageMethod;
import org.jboss.logging.generator.MessageObject;
import org.jboss.logging.generator.MessageReturnType;
import org.jboss.logging.generator.MethodParameter;
import org.jboss.logging.generator.ThrowableReturnType;
import org.jboss.logging.generator.validation.validator.FormatValidator;
import org.jboss.logging.generator.validation.validator.FormatValidatorFactory;
import org.jboss.logging.generator.validation.validator.MessageIdValidator;

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
public abstract class AbstractValidator {

    /**
     * Determines if the message object is either the same as, or is a superclass of, the class represented by the
     * specified {@code clazz} parameter. If the message object is assignable from the class, {@code true} is returned,
     * otherwise {@code false}.
     *
     * @param messageObject the message object that the class must be the same as or a superclass of.
     * @param clazz         the class class that is tested.
     *
     * @return {@code true} if the message object is the same as or a superclass of the class, otherwise {@code false}.
     */
    protected abstract boolean isAssignableFrom(MessageObject messageObject, Class<?> clazz);

    /**
     * Determines if the class is either the same as, or is a superclass of, the class represented by the specified
     * {@code messageObject} parameter. If the class is assignable from the message object, {@code true} is returned,
     * otherwise {@code false}.
     *
     * @param clazz         the class that the message object must be the same as or a superclass of.
     * @param messageObject the message object that is tested.
     *
     * @return {@code true} if the class is the same as or a superclass of the message object, otherwise {@code false}.
     */
    protected abstract boolean isAssignableFrom(Class<?> clazz, MessageObject messageObject);

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
            final Set<MessageMethod> methods = getAllMethods(messageInterface);
            messages.addAll(validateCommon(projectCode, methods));
            messages.addAll(validateBundle(methods));
        } else if (messageInterface.isMessageLogger()) {
            final String projectCode = messageInterface.projectCode();
            // Get all methods except basic logger methods
            final Set<MessageMethod> methods = getAllMethods(messageInterface);
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
    private Collection<ValidationMessage> validateCommon(final String projectCode, final Set<MessageMethod> methods) {
        final List<ValidationMessage> messages = new ArrayList<ValidationMessage>();
        final Map<String, MessageMethod> methodNames = new HashMap<String, MessageMethod>();
        for (MessageMethod method : methods) {
            final MessageMethod.Message message = method.message();
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
                    final MessageMethod previousMethod = methodNames.get(key);
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

    private Collection<ValidationMessage> validateParameters(final MessageMethod method) {
        final List<ValidationMessage> messages = new ArrayList<ValidationMessage>();
        boolean foundCause = false;
        for (MethodParameter parameter : method.allParameters()) {
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
    private Collection<ValidationMessage> validateBundle(final Set<MessageMethod> methods) {
        final List<ValidationMessage> messages = new ArrayList<ValidationMessage>();
        for (MessageMethod method : methods) {
            messages.addAll(validateBundleMethod(method));
        }
        return messages;
    }

    private Collection<ValidationMessage> validateBundleMethod(final MessageMethod method) {
        final List<ValidationMessage> messages = new ArrayList<ValidationMessage>();
        // The return type must be a Throwable or String
        final MessageReturnType returnType = method.returnType();
        if (returnType.equals(MessageReturnType.VOID) || returnType.isPrimitive()) {
            messages.add(createError(method, "Message bundle method %s has an invalid return type. Cannot be void or a primitive.", method.name()));
        } else if (returnType.isThrowable()) {
            if (!isAssignableFrom(Throwable.class, returnType)) {
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
            if (!isAssignableFrom(returnType, String.class)) {
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
    private Collection<ValidationMessage> validateLogger(final Set<MessageMethod> methods) {
        final List<ValidationMessage> messages = new ArrayList<ValidationMessage>();
        for (MessageMethod method : methods) {
            if (method.isLoggerMethod()) {
                messages.addAll(validateLoggerMethod(method));
            } else {
                messages.addAll(validateBundleMethod(method));
            }
        }
        return messages;
    }

    private Collection<ValidationMessage> validateLoggerMethod(final MessageMethod method) {
        final List<ValidationMessage> messages = new ArrayList<ValidationMessage>();
        // The return type must be void
        if (!MessageReturnType.VOID.equals(method.returnType())) {
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
    private Set<MessageMethod> getAllMethods(final MessageInterface messageInterface) {
        if (messageInterface.isBasicLogger()) {
            return Collections.emptySet();
        }
        final Set<MessageMethod> methods = new HashSet<MessageMethod>();
        for (MessageMethod method : messageInterface.methods()) {
            methods.add(method);
        }
        for (MessageInterface msgInterface : messageInterface.extendedInterfaces()) {
            methods.addAll(getAllMethods(msgInterface));
        }
        return methods;
    }
}
