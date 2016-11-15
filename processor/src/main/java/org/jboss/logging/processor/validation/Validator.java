/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat, Inc., and individual contributors
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

package org.jboss.logging.processor.validation;

import static org.jboss.logging.processor.model.Parameter.ParameterType;
import static org.jboss.logging.processor.validation.ValidationMessageFactory.createError;
import static org.jboss.logging.processor.validation.ValidationMessageFactory.createWarning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.jboss.logging.annotations.ConstructType;
import org.jboss.logging.annotations.Once;
import org.jboss.logging.annotations.Pos;
import org.jboss.logging.annotations.Transform;
import org.jboss.logging.annotations.Transform.TransformType;
import org.jboss.logging.processor.model.MessageInterface;
import org.jboss.logging.processor.model.MessageInterface.AnnotatedType;
import org.jboss.logging.processor.model.MessageMethod;
import org.jboss.logging.processor.model.Parameter;
import org.jboss.logging.processor.model.ReturnType;
import org.jboss.logging.processor.model.ThrowableType;
import org.jboss.logging.processor.util.ElementHelper;

/**
 * Date: 12.08.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public final class Validator {

    private final MessageIdValidator messageIdValidator;
    private final IdLengthValidator idLengthValidator;
    private final IdRangeValidator idRangeValidator;

    public Validator() {
        messageIdValidator = new MessageIdValidator();
        idLengthValidator = new IdLengthValidator();
        idRangeValidator = new IdRangeValidator();
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
        switch (messageInterface.getAnnotatedType()) {
            case MESSAGE_BUNDLE: {
                // Get all messageMethods except logger interface messageMethods
                final Set<MessageMethod> messageMethods = getAllMethods(messageInterface);
                messages.addAll(validateCommon(messageInterface, messageMethods));
                messages.addAll(validateBundle(messageMethods));
                break;
            }
            case MESSAGE_LOGGER: {
                // Get all messageMethods except logger interface messageMethods
                final Set<MessageMethod> messageMethods = getAllMethods(messageInterface);
                messages.addAll(validateCommon(messageInterface, messageMethods));
                messages.addAll(validateLogger(messageMethods));
                break;
            }
            default:
                messages.add(createError(messageInterface, "Message interface %s is not a message bundle or message logger.", messageInterface.name()));
        }
        return messages;
    }

    /**
     * Validate common attributes to all interfaces.
     *
     * @param messageInterface the interface.
     * @param messageMethods   the messageMethods to validate.
     *
     * @return a collection of validation messages.
     */
    private Collection<ValidationMessage> validateCommon(final MessageInterface messageInterface, final Set<MessageMethod> messageMethods) {
        final List<ValidationMessage> messages = new ArrayList<ValidationMessage>();
        final Map<String, MessageMethod> methodNames = new HashMap<String, MessageMethod>();

        messages.addAll(idLengthValidator.validate(messageInterface));
        messages.addAll(idRangeValidator.validate(messageInterface));

        for (MessageMethod messageMethod : messageMethods) {
            // Check for checked exceptions thrown on the interface messageMethod
            for (ThrowableType throwableType : messageMethod.thrownTypes()) {
                if (throwableType.isChecked()) {
                    messages.add(createError(messageMethod, "Interface message methods cannot throw checked exceptions."));
                }
            }
            final MessageMethod.Message message = messageMethod.message();
            if (message == null) {
                messages.add(createError(messageMethod, "All message bundles and message logger message methods must have or inherit a message."));
                continue;
            }
            // Check the message id
            if (message.hasId()) {
                // Make sure the message id is greater than 0
                if (message.id() < 0) {
                    messages.add(createError(messageMethod, "Message id %d is invalid. Must be greater than 0 or inherit another valid id.", message.id()));
                } else {
                    messages.addAll(messageIdValidator.validate(messageInterface, messageMethod));
                }
            }
            final FormatValidator formatValidator = FormatValidatorFactory.create(messageMethod);
            if (formatValidator.isValid()) {
                final int paramCount = messageMethod.formatParameterCount();
                if (messageMethod.formatParameterCount() != formatValidator.argumentCount()) {
                    messages.add(createError(messageMethod, "Parameter count does not match for format '%s'. Required: %d Provided: %d", formatValidator.format(), formatValidator.argumentCount(), paramCount));
                }
                // Validate the transform parameter
                if (!messageMethod.parameters(ParameterType.TRANSFORM).isEmpty()) {
                    final Set<Parameter> parameters = messageMethod.parameters(ParameterType.TRANSFORM);
                    // Validate each parameter
                    for (Parameter parameter : parameters) {
                        validateTransform(messages, parameter, parameter.transform());
                    }
                }
                // Validate the POS annotated parameters
                if (!messageMethod.parameters(ParameterType.POS).isEmpty()) {
                    final Map<Integer, Parameter> positions = new TreeMap<Integer, Parameter>();
                    final Set<Parameter> parameters = messageMethod.parameters(ParameterType.POS);
                    // Validate each parameter
                    for (Parameter parameter : parameters) {
                        final Pos pos = parameter.pos();
                        final Transform[] transforms = pos.transform();
                        if (transforms != null && transforms.length > 0) {
                            if (pos.value().length != transforms.length) {
                                messages.add(createError(parameter, "Positional parameters with transforms must have an equal number of positions and transforms."));
                            } else {
                                for (Transform transform : transforms) {
                                    validateTransform(messages, parameter, transform);
                                }
                            }
                        }
                        // Validate the positions
                        final Set<Integer> usedPositions = new HashSet<Integer>();
                        for (int position : pos.value()) {
                            if (usedPositions.contains(position)) {
                                messages.add(createError(parameter, "Position '%d' already used for this parameter.", position));
                            } else {
                                usedPositions.add(position);
                            }
                            if (positions.containsKey(position)) {
                                messages.add(createError(parameter, "Position '%d' already defined on parameter '%s'", position, positions.get(position).name()));
                            } else {
                                positions.put(position, parameter);
                            }
                        }
                    }
                    // Check for missing indexed parameters
                    for (int i = 0; i < messageMethod.formatParameterCount(); i++) {
                        final int positionIndex = i + 1;
                        if (!positions.containsKey(positionIndex)) {
                            messages.add(createError(messageMethod, "Missing parameter with position '%d' defined.", positionIndex));
                        }
                    }
                }
            } else {
                messages.add(createError(messageMethod, formatValidator.summaryMessage()));
            }
            // Make sure there is only one @Message annotation per messageMethod name.
            if (!messageMethod.inheritsMessage()) {
                final String key = messageMethod.name() + messageMethod.formatParameterCount();
                if (methodNames.containsKey(key)) {
                    final MessageMethod previousMethod = methodNames.get(key);
                    messages.add(createError(previousMethod, "Only one message with the same format parameters is allowed."));
                    messages.add(createError(messageMethod, "Only one message with the same format parameters is allowed."));
                } else {
                    methodNames.put(key, messageMethod);
                }
            }
            // Validate the parameters
            messages.addAll(validateParameters(messageMethod));
        }
        return messages;
    }

    private void validateTransform(final List<ValidationMessage> messages, final Parameter parameter, final Transform transform) {
        final List<TransformType> transformTypes = Arrays.asList(transform.value());
        // If annotated with @Transform, must be an Object, primitives are not allowed
        if (parameter.isPrimitive()) {
            messages.add(createError(parameter, "Parameters annotated with @Transform cannot be primitives."));
        } else if (transformTypes.contains(TransformType.GET_CLASS) && transformTypes.contains(TransformType.SIZE)) {
            messages.add(createError(parameter, "Transform type '%s' not allowed with type '%s'", TransformType.GET_CLASS, TransformType.SIZE));
        } else if (transformTypes.contains(TransformType.HASH_CODE) && transformTypes.contains(TransformType.SIZE)) {
            messages.add(createError(parameter, "Transform type '%s' not allowed with type '%s'", TransformType.HASH_CODE, TransformType.SIZE));
        } else if (transformTypes.contains(TransformType.IDENTITY_HASH_CODE) && transformTypes.contains(TransformType.SIZE)) {
            messages.add(createError(parameter, "Transform type '%s' not allowed with type '%s'", TransformType.IDENTITY_HASH_CODE, TransformType.SIZE));
        } else if (transformTypes.contains(TransformType.IDENTITY_HASH_CODE) && transformTypes.contains(TransformType.HASH_CODE)) {
            messages.add(createError(parameter, "Transform type '%s' not allowed with type '%s'", TransformType.IDENTITY_HASH_CODE, TransformType.HASH_CODE));
        } else if (transformTypes.contains(TransformType.SIZE)) {
            if (!(parameter.isArray() || parameter.isVarArgs() || parameter.isSubtypeOf(Map.class) ||
                    parameter.isSubtypeOf(Collection.class) || parameter.isSubtypeOf(CharSequence.class))) {
                messages.add(createError(parameter, "Invalid type (%s) for %s. Must be an array, %s, %s or %s.", parameter.type(),
                        TransformType.SIZE, Collection.class.getName(), Map.class.getName(), CharSequence.class.getName()));
            }
        }
    }

    private Collection<ValidationMessage> validateParameters(final MessageMethod messageMethod) {
        final List<ValidationMessage> messages = new ArrayList<ValidationMessage>();
        boolean foundCause = false;
        final ReturnType returnType = messageMethod.returnType();
        for (Parameter parameter : messageMethod.parameters(ParameterType.ANY)) {
            switch (parameter.parameterType()) {
                case CAUSE: {
                    if (foundCause) {
                        messages.add(createError(messageMethod, "Only one cause parameter is allowed."));
                    } else {
                        foundCause = true;
                    }
                    break;
                }
                case FQCN:
                    if (!parameter.type().equals(Class.class.getName())) {
                        messages.add(createError(parameter, "Parameter %s annotated with @LoggingClass on method %s must be of type %s.", parameter.name(), messageMethod.name(), Class.class.getName()));
                    }
                    break;
                case FIELD: {
                    if (!returnType.hasFieldFor(parameter)) {
                        messages.add(createError(parameter, "No target field found in %s with name %s with type %s.", returnType.type(), parameter.targetName(), parameter.type()));
                    }
                    break;
                }
                case PROPERTY: {
                    if (!returnType.hasMethodFor(parameter)) {
                        messages.add(createError(parameter, "No method found in %s with signature %s(%s).", returnType.type(), parameter.targetName(), parameter.type()));
                    }
                    break;
                }
            }
        }
        // TODO - Check all parameter counts
        return messages;
    }

    /**
     * Validate message bundle messageMethods.
     *
     * @param messageMethods the messageMethods to validate.
     *
     * @return a collection of the validation messages.
     */
    private Collection<ValidationMessage> validateBundle(final Set<MessageMethod> messageMethods) {
        final List<ValidationMessage> messages = new ArrayList<ValidationMessage>();
        for (MessageMethod messageMethod : messageMethods) {
            messages.addAll(validateBundleMethod(messageMethod));
        }
        return messages;
    }

    private Collection<ValidationMessage> validateBundleMethod(final MessageMethod messageMethod) {
        final List<ValidationMessage> messages = new ArrayList<ValidationMessage>();
        // The return type must be a Throwable or String
        final ReturnType returnType = messageMethod.returnType();
        if (returnType.equals(ReturnType.VOID) || returnType.isPrimitive()) {
            messages.add(createError(messageMethod, "Message bundle messageMethod %s has an invalid return type. Cannot be void or a primitive.", messageMethod.name()));
        } else if (returnType.isThrowable()) {
            final ThrowableType throwableReturnType = returnType.throwableReturnType();
            if (throwableReturnType.useConstructionParameters()) {
                // TODO - Check the return type constructor. Currently handled via the ThrowableReturnTypeFactory.
            } else if (!throwableReturnType.useConstructionParameters() && !messageMethod.parameters(ParameterType.CONSTRUCTION).isEmpty()) {
                messages.add(createError(messageMethod, "MessageMethod does not have an usable constructor for the return type %s.", returnType.name()));
            } else {
                final boolean hasMessageConstructor = (throwableReturnType.hasStringAndThrowableConstructor() || throwableReturnType.hasThrowableAndStringConstructor() ||
                        throwableReturnType.hasStringConstructor());
                final boolean usableConstructor = (throwableReturnType.hasDefaultConstructor() || throwableReturnType.hasStringAndThrowableConstructor() ||
                        throwableReturnType.hasStringConstructor() || throwableReturnType.hasThrowableAndStringConstructor() || throwableReturnType.hasThrowableConstructor());
                if (!usableConstructor) {
                    messages.add(createError(messageMethod, "MessageMethod does not have an usable constructor for the return type %s.", returnType.name()));
                } else if (!hasMessageConstructor) { // Check to see if there is no message constructor
                    messages.add(createWarning(messageMethod, "The message cannot be set via the throwable constructor and will be ignored."));
                }
            }
        } else {
            if (!returnType.isAssignableFrom(String.class)) {
                messages.add(createError(messageMethod, "Message bundle method (%s) has an invalid return type of %s.", messageMethod.name(), returnType.name()));
            }
            if (ElementHelper.isAnnotatedWith(messageMethod.reference(), ConstructType.class)) {
                messages.add(createError(messageMethod, "ConstructType annotation requires a throwable return type"));
            }
        }
        return messages;
    }

    /**
     * Validate message logger messageMethods.
     *
     * @param messageMethods the messageMethods to validate.
     *
     * @return a collection of the validation messages.
     */
    private Collection<ValidationMessage> validateLogger(final Set<MessageMethod> messageMethods) {
        final List<ValidationMessage> messages = new ArrayList<ValidationMessage>();
        for (MessageMethod messageMethod : messageMethods) {
            if (messageMethod.isLoggerMethod()) {
                messages.addAll(validateLoggerMethod(messageMethod));
            } else {
                messages.addAll(validateBundleMethod(messageMethod));
                if (ElementHelper.isAnnotatedWith(messageMethod.reference(), Once.class)) {
                    messages.add(createError(messageMethod, "Only @LogMessage method can be annoted with @Once"));
                }
            }
        }
        return messages;
    }

    private Collection<ValidationMessage> validateLoggerMethod(final MessageMethod messageMethod) {
        final List<ValidationMessage> messages = new ArrayList<ValidationMessage>();
        // The return type must be void
        if (!ReturnType.VOID.equals(messageMethod.returnType())) {
            messages.add(createError(messageMethod, "Message logger methods can only have a void return type."));
        }
        return messages;
    }

    /**
     * Finds all methods for the given interface, but ignores logger interface methods.
     *
     * @param messageInterface the interface to find all methods for.
     *
     * @return a set of all the methods (exception logger interface methods) the interface must implement.
     */
    private Set<MessageMethod> getAllMethods(final MessageInterface messageInterface) {
        if (messageInterface.getAnnotatedType() == AnnotatedType.NONE) {
            return Collections.emptySet();
        }
        final Set<MessageMethod> messageMethods = new LinkedHashSet<MessageMethod>();
        for (MessageMethod messageMethod : messageInterface.methods()) {
            messageMethods.add(messageMethod);
        }
        for (MessageInterface msgInterface : messageInterface.extendedInterfaces()) {
            messageMethods.addAll(getAllMethods(msgInterface));
        }
        return messageMethods;
    }
}
