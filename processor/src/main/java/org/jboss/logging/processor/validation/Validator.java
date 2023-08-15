/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2023 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.logging.processor.validation;

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
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.ConstructType;
import org.jboss.logging.annotations.LoggingClass;
import org.jboss.logging.annotations.MessageBundle;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.logging.annotations.Once;
import org.jboss.logging.annotations.Param;
import org.jboss.logging.annotations.Pos;
import org.jboss.logging.annotations.Producer;
import org.jboss.logging.annotations.Signature;
import org.jboss.logging.annotations.Suppressed;
import org.jboss.logging.annotations.Transform;
import org.jboss.logging.annotations.Transform.TransformType;
import org.jboss.logging.annotations.TransformException;
import org.jboss.logging.processor.model.LoggerMessageMethod;
import org.jboss.logging.processor.model.MessageInterface;
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

    private static final Collection<String> AVAILABLE_LANGUAGES = new HashSet<>(Arrays.asList(Locale.getISOLanguages()));

    private final MessageIdValidator messageIdValidator;
    private final IdLengthValidator idLengthValidator;
    private final IdRangeValidator idRangeValidator;
    private final ProcessingEnvironment processingEnv;
    private final Elements elements;
    private final Types types;

    public Validator(final ProcessingEnvironment processingEnv) {
        messageIdValidator = new MessageIdValidator();
        idLengthValidator = new IdLengthValidator();
        idRangeValidator = new IdRangeValidator();
        this.processingEnv = processingEnv;
        this.elements = processingEnv.getElementUtils();
        this.types = processingEnv.getTypeUtils();
    }

    /**
     * Validates the message interface and returns a collection of validation messages or an empty collection.
     *
     * @param messageInterface the message interface to validate.
     *
     * @return a collection of validation messages or an empty collection.
     */
    public final Collection<ValidationMessage> validate(final MessageInterface messageInterface) {
        final List<ValidationMessage> messages = new ArrayList<>();
        String locale = null;
        if (messageInterface.isAnnotatedWith(MessageBundle.class)) {
            // Get all messageMethods except logger interface messageMethods
            final Set<MessageMethod> messageMethods = getAllMethods(messageInterface);
            messages.addAll(validateCommon(messageInterface, messageMethods));
            messages.addAll(validateBundle(messageMethods));
            locale = messageInterface.getAnnotation(MessageBundle.class).rootLocale();
        } else if (messageInterface.isAnnotatedWith(MessageLogger.class)) {
            // Get all messageMethods except logger interface messageMethods
            final Set<MessageMethod> messageMethods = getAllMethods(messageInterface);
            messages.addAll(validateCommon(messageInterface, messageMethods));
            messages.addAll(validateLogger(messageMethods));
            locale = messageInterface.getAnnotation(MessageLogger.class).rootLocale();
        } else {
            messages.add(createError(messageInterface, "Message interface %s is not a message bundle or message logger.", messageInterface.name()));
        }

        // Check the locale is in the list of available locales
        if (locale != null && !locale.isEmpty() && !AVAILABLE_LANGUAGES.contains(Locale.forLanguageTag(locale).getLanguage())) {
            // Only warn as there may be a change the locale exists on the target runtime
            messages.add(createWarning(messageInterface, "The locale '%s' may be invalid. The target runtime must " +
                    "include this locale to ensure formatting is handled correctly.", locale));
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
        final List<ValidationMessage> messages = new ArrayList<>();
        final Map<String, MessageMethod> methodNames = new HashMap<>();

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
                final Map<Integer, Parameter> positions = new TreeMap<>();
                boolean validatePositions = false;
                for (Parameter parameter : messageMethod.parameters()) {
                    // Validate the transform parameter
                    if (parameter.isAnnotatedWith(Transform.class)) {
                        validateTransform(messages, parameter, parameter.getAnnotation(Transform.class));
                    }
                    // Validate the POS annotated parameters
                    if (parameter.isAnnotatedWith(Pos.class)) {
                        validatePositions = true;
                        final Pos pos = parameter.getAnnotation(Pos.class);
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
                        final Set<Integer> usedPositions = new HashSet<>();
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

                    // Validate the @Suppressed parameter is on a message bundle, the return type is an exception and the parameter is an exception
                    if (parameter.isAnnotatedWith(Suppressed.class)) {
                        if (!messageMethod.returnType().isThrowable()) {
                            messages.add(createError(messageMethod, "The @Suppressed parameter annotation can only be used with message bundle methods that return an exception."));
                        }
                        if (!isTypeAssignableFrom(parameter, Throwable.class)) {
                            messages.add(createError(parameter, "The parameter annotated with @Suppressed must be assignable to a Throwable type."));
                        }
                    }
                }
                // Check for missing indexed parameters
                if (validatePositions) {
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
            // Validate property annotations
            messages.addAll(PropertyValidator.validate(processingEnv, messageMethod));
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
                messages.add(createError(parameter, "Invalid type (%s) for %s. Must be an array, %s, %s or %s.", parameter.asType(),
                        TransformType.SIZE, Collection.class.getName(), Map.class.getName(), CharSequence.class.getName()));
            }
        }
    }

    private Collection<ValidationMessage> validateParameters(final MessageMethod messageMethod) {
        final List<ValidationMessage> messages = new ArrayList<>();
        boolean foundCause = false;
        boolean producerFound = false;
        boolean transformExceptionFound = false;
        for (Parameter parameter : messageMethod.parameters()) {
            if (parameter.isAnnotatedWith(Cause.class)) {
                if (foundCause) {
                    messages.add(createError(messageMethod, "Only one cause parameter is allowed."));
                } else {
                    foundCause = true;
                }
            }
            if (parameter.isAnnotatedWith(LoggingClass.class)) {
                if (!parameter.isSameAs(Class.class)) {
                    messages.add(createError(parameter, "Parameter %s annotated with @LoggingClass on method %s must be of type %s.", parameter.name(), messageMethod.name(), Class.class.getName()));
                }
            }
            if (parameter.isAnnotatedWith(Producer.class)) {
                final boolean isFunction = parameter.isSubtypeOf(Function.class);
                final boolean isBiFunction = parameter.isSubtypeOf(BiFunction.class);

                // Check the type arguments
                final TypeMirror requiredReturnType = messageMethod.returnType().resolvedType();
                final List<? extends TypeMirror> typeArgs = ElementHelper.getTypeArguments(parameter);
                final int size = typeArgs.size();
                if (isFunction) {
                    if (size == 2) {
                        final TypeMirror message = typeArgs.get(0);
                        final TypeMirror returnType = typeArgs.get(1);
                        if (!isTypeAssignableFrom(message, String.class)) {
                            messages.add(createError(parameter, "The type %s must be assignable to a String.",
                                    message));
                        }
                        if (!isTypeAssignableFrom(returnType, requiredReturnType)) {
                            messages.add(createError(parameter, "The return type parameter of the function " +
                                    "must be assignable from %s", requiredReturnType));
                        }
                    } else {
                        messages.add(createError(parameter, "The type parameters could not be validated for the " +
                                "function. The first type argument of the function must be a String and the second type " +
                                "parameter must be the same as or a super type of %s.", requiredReturnType));
                    }
                } else if (isBiFunction) {
                    if (size == 3) {
                        final TypeMirror returnType = typeArgs.get(2);
                        final TypeMirror first = typeArgs.get(0);
                        final TypeMirror second = typeArgs.get(1);
                        if (!isTypeAssignableFrom(first, String.class) && !types.isSubtype(first, ElementHelper.toType(elements, Throwable.class))) {
                            messages.add(createError(parameter, "The first type type parameter for %s " +
                                    "must be assignable to a String or a super type of a Throwable.", parameter.asType()));
                        }
                        if (!isTypeAssignableFrom(second, String.class) && !types.isSubtype(second, ElementHelper.toType(elements, Throwable.class))) {
                            messages.add(createError(parameter, "The second type parameter for %s " +
                                    "must be assignable to a String or a super type of a Throwable.", parameter.asType()));
                        }
                        if (!isTypeAssignableFrom(returnType, requiredReturnType)) {
                            messages.add(createError(parameter, "The return type parameter of the function " +
                                    "must be assignable from %s", requiredReturnType));
                        }
                        if (messageMethod.hasCause()) {
                            // Make sure the cause parameter can be assigned to the cause parameter for the BiFunction
                            if (types.isSubtype(first, ElementHelper.toType(elements, Throwable.class)) && !isTypeAssignableFrom(messageMethod.cause().asType(), first)) {
                                messages.add(createError(parameter, "The first parameter type, %s, of the BiFunction must be assignable to the cause %s.",
                                        first, messageMethod.cause().asType()));
                            }
                            if (types.isSubtype(second, ElementHelper.toType(elements, Throwable.class)) && !isTypeAssignableFrom(messageMethod.cause().asType(), second)) {
                                messages.add(createError(parameter, "The second parameter type, %s, of the BiFunction must be assignable to the cause %s.",
                                        second, messageMethod.cause().asType()));
                            }
                        } else {
                            messages.add(createWarning(messageMethod, "No @Cause parameter was found on the method. " +
                                    "A null value will always be passed as the cause parameter to the BiFunction."));
                        }
                    } else {
                        messages.add(createError(parameter, "The type parameters could not be validated for the " +
                                        "function. The first and second type arguments of the function must be a String " +
                                        "or a super type of Throwable. The third type parameter must be the same as or " +
                                        "a super type of %s.",
                                requiredReturnType));
                    }
                } else {
                    messages.add(createError(parameter, "Parameter annotated with %s must be a %s or %s type",
                            Producer.class.getName(), Function.class.getName(), BiFunction.class.getName()));
                }

                if (producerFound) {
                    messages.add(createError(messageMethod,
                            "Only one parameter is allowed to be annotated with %s.", Producer.class.getName()));
                }
                producerFound = true;
            }

            if (parameter.isAnnotatedWith(TransformException.class)) {
                if (!messageMethod.returnType().isThrowable()) {
                    messages.add(createError(messageMethod, "A parameter annotated with @%s can only be on bundle methods which return a Throwable type.", TransformException.class
                            .getSimpleName()));
                } else {
                    if (transformExceptionFound) {
                        messages.add(createError(messageMethod, "Only one parameter annotated with @%s can be used", TransformException.class
                                .getSimpleName()));
                    }
                    transformExceptionFound = true;
                    if (!isTypeAssignableFrom(parameter.asType(), Throwable.class)) {
                        messages.add(createError(parameter, "The parameter annotated with @%s must be a subclass of Throwable", TransformException.class
                                .getSimpleName()));
                    }
                    // Ensure the suggested exceptions are sub-types of the return type
                    final List<TypeMirror> suggested = ElementHelper.getClassArrayAnnotationValue(parameter, TransformException.class, "value");
                    for (TypeMirror suggestion : suggested) {
                        if (!types.isAssignable(suggestion, parameter.asType())) {
                            messages.add(createError(parameter, "The suggested return type of %s is not assignable to %s", suggestion, parameter.asType()));
                        }
                    }
                }
            }
        }
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
        final List<ValidationMessage> messages = new ArrayList<>();
        for (MessageMethod messageMethod : messageMethods) {
            messages.addAll(validateBundleMethod(messageMethod));
        }
        return messages;
    }

    private Collection<ValidationMessage> validateBundleMethod(final MessageMethod messageMethod) {
        final List<ValidationMessage> messages = new ArrayList<>();
        // The return type must be a Throwable, String or a Supplier that returns a Throwable or String
        final ReturnType returnType = messageMethod.returnType();
        final TypeMirror returnTypeMirror = returnType.asType();
        final TypeMirror resolvedReturnType = returnType.resolvedType();
        if (returnTypeMirror.getKind() == TypeKind.VOID || returnTypeMirror.getKind().isPrimitive()) {
            messages.add(createError(messageMethod, "Message bundle messageMethod %s has an invalid return type. Cannot be void or a primitive.", messageMethod.name()));
        } else if (returnType.isThrowable()) {
            final ThrowableType throwableReturnType = returnType.throwableReturnType();
            if (throwableReturnType.useConstructionParameters()) {
                // Check for a matching constructor
                final Signature signature = messageMethod.getAnnotation(Signature.class);
                if (signature != null) {
                    final List<TypeMirror> args = ElementHelper.getClassArrayAnnotationValue(messageMethod, Signature.class, "value");
                    // Validate the constructor exists
                    if (!ElementHelper.hasConstructor(types, returnType, args)) {
                        messages.add(createError(messageMethod, "Could not find constructor for %s with arguments %s", messageMethod.asType(), args));
                    }
                    final int messageIndex = signature.messageIndex();
                    // Note that the messageIndex is required and must be 0 or greater
                    if (messageIndex < 0) {
                        messages.add(createError(messageMethod, "A messageIndex of 0 or greater is required. Value %d is invalid.", messageIndex));
                    }
                }
                // Validate the construct type is valid
                if (messageMethod.isAnnotatedWith(ConstructType.class)) {
                    final TypeElement constructTypeValue = ElementHelper.getClassAnnotationValue(messageMethod, ConstructType.class);
                    // Shouldn't be null
                    if (constructTypeValue == null) {
                        messages.add(createError(messageMethod, "Class not defined for the ConstructType"));
                    } else {
                        if (!types.isAssignable(constructTypeValue.asType(), returnType.asType())) {
                            messages.add(createError(messageMethod, "The requested type %s can not be assigned to %s.", constructTypeValue.asType(), returnType.asType()));
                        }
                    }
                }
            } else if (!throwableReturnType.useConstructionParameters() && !messageMethod.parametersAnnotatedWith(Param.class).isEmpty()) {
                messages.add(createError(messageMethod, "MessageMethod does not have an usable constructor for the return type %s.", returnType.name()));
            } else {
                final boolean hasMessageConstructor = (throwableReturnType.hasStringAndThrowableConstructor() || throwableReturnType.hasThrowableAndStringConstructor() ||
                        throwableReturnType.hasStringConstructor());
                final boolean usableConstructor = (throwableReturnType.hasDefaultConstructor() || throwableReturnType.hasStringAndThrowableConstructor() ||
                        throwableReturnType.hasStringConstructor() || throwableReturnType.hasThrowableAndStringConstructor() || throwableReturnType.hasThrowableConstructor());
                if (!messageMethod.parametersAnnotatedWith(Producer.class).isEmpty()) {
                    if (messageMethod.isAnnotatedWith(ConstructType.class)) {
                        messages.add(createError(messageMethod, "Method annotated with %s cannot have a parameter annotated with %s.",
                                ConstructType.class.getName(), Producer.class.getName()));
                    }
                    final TypeMirror erasure = types.erasure(resolvedReturnType);
                    if (!isTypeAssignableFrom(erasure, Throwable.class)) {
                        messages.add(createError(messageMethod, "The return type must be a super class of a java.lang.Throwable"));
                    }
                } else if (!usableConstructor) {
                    messages.add(createError(messageMethod, "MessageMethod does not have an usable constructor for the return type %s.", returnType.name()));
                } else if (!hasMessageConstructor) { // Check to see if there is no message constructor
                    messages.add(createWarning(messageMethod, "The message cannot be set via the throwable constructor and will be ignored."));
                }
            }
        } else {
            if (!isTypeAssignableFrom(resolvedReturnType, String.class) && !returnType.isThrowable()) {
                messages.add(createError(messageMethod, "Message bundle method (%s) has an invalid return type of %s. " +
                        "Return types must be a String, a subtype of Throwable or a java.util.function.Supplier which " +
                        "returns a String or a subtype of Throwable.", messageMethod.name(), returnTypeMirror));
            }
            if (messageMethod.isAnnotatedWith(ConstructType.class)) {
                messages.add(createError(messageMethod, "ConstructType annotation requires a throwable or supplier which produces a throwable return type"));
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
        final List<ValidationMessage> messages = new ArrayList<>();
        for (MessageMethod messageMethod : messageMethods) {
            if (messageMethod instanceof LoggerMessageMethod) {
                messages.addAll(validateLoggerMethod(messageMethod));
            } else {
                messages.addAll(validateBundleMethod(messageMethod));
                if (messageMethod.isAnnotatedWith(Once.class)) {
                    messages.add(createError(messageMethod, "Only @LogMessage method can be annoted with @Once"));
                }
            }
        }
        return messages;
    }

    private Collection<ValidationMessage> validateLoggerMethod(final MessageMethod messageMethod) {
        final List<ValidationMessage> messages = new ArrayList<>();
        // The return type must be void
        if (messageMethod.returnType().asType().getKind() != TypeKind.VOID) {
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
        if (messageInterface.isAnnotatedWith(MessageBundle.class) || messageInterface.isAnnotatedWith(MessageLogger.class)) {
            final Set<MessageMethod> messageMethods = new LinkedHashSet<>();
            for (MessageMethod messageMethod : messageInterface.methods()) {
                messageMethods.add(messageMethod);
            }
            for (MessageInterface msgInterface : messageInterface.extendedInterfaces()) {
                messageMethods.addAll(getAllMethods(msgInterface));
            }
            return messageMethods;
        }
        return Collections.emptySet();
    }

    /**
     * Checks the element type, if an array the type of the array is checked, against the class. If the element type is
     * assignable to the class type.
     *
     * @param element the element to test
     * @param type    the type the element needs to be assignable to
     *
     * @return {@code true} if the element type is assignable to the class type, otherwise {@code false}
     */
    private boolean isTypeAssignableFrom(final Element element, final Class<?> type) {
        return isTypeAssignableFrom(element.asType(), type);
    }

    /**
     * Checks the type, if an array the type of the array is checked, against the class. If the element type is
     * assignable to the class type.
     *
     * @param typeMirror the type to test
     * @param type       the type the element needs to be assignable to
     *
     * @return {@code true} if the element type is assignable to the class type, otherwise {@code false}
     */
    private boolean isTypeAssignableFrom(final TypeMirror typeMirror, final Class<?> type) {
        return isTypeAssignableFrom(typeMirror, ElementHelper.toType(elements, type));
    }

    /**
     * Checks the type, if an array the type of the array is checked, against the class. If the element type is
     * assignable to the class type.
     *
     * @param typeMirror the type to test
     * @param type       the type the element needs to be assignable to
     *
     * @return {@code true} if the element type is assignable to the class type, otherwise {@code false}
     */
    private boolean isTypeAssignableFrom(final TypeMirror typeMirror, final TypeMirror type) {
        TypeMirror t = typeMirror;
        if (t.getKind() == TypeKind.ARRAY) {
            t = ((ArrayType) t).getComponentType();
        }
        if (types.isAssignable(types.erasure(t), ElementHelper.toType(elements, Collection.class))) {
            // We only need the first type
            t = types.erasure(ElementHelper.getTypeArguments(t).iterator().next());
        }
        return types.isAssignable(t, type);
    }
}
