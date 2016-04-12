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

import static org.jboss.logging.processor.validation.ValidationMessageFactory.createError;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.SimpleAnnotationValueVisitor8;
import javax.lang.model.util.Types;

import org.jboss.logging.annotations.Field;
import org.jboss.logging.annotations.Fields;
import org.jboss.logging.annotations.Properties;
import org.jboss.logging.annotations.Property;
import org.jboss.logging.processor.model.MessageMethod;
import org.jboss.logging.processor.model.Parameter;
import org.jboss.logging.processor.util.ElementHelper;

/**
 * Validates property annotations on methods.
 * <p>
 * Valid property annotations are:
 * <ul>
 * <li>{@link Properties}</li>
 * <li>{@link Property}</li>
 * <li>{@link Fields}</li>
 * <li>{@link Field}</li>
 * </ul>
 * </p>
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
class PropertyValidator {
    private static final List<Class<? extends Annotation>> VALIDATING_ANNOTATIONS = Arrays.asList(Properties.class, Property.class, Fields.class, Field.class);
    private final Elements elements;
    private final Types types;
    private final MessageMethod method;
    private final TypeMirror resultType;
    private final Collection<ValidationMessage> messages;

    private PropertyValidator(final ProcessingEnvironment processingEnv, final MessageMethod method, final TypeMirror resultType, final Collection<ValidationMessage> messages) {
        elements = processingEnv.getElementUtils();
        types = processingEnv.getTypeUtils();
        this.method = method;
        this.resultType = resultType;
        this.messages = messages;
    }

    /**
     * Validates the message method property annotations.
     *
     * @param processingEnv the annotation processing environment
     * @param messageMethod the method to validate
     *
     * @return a collection of validation messages
     */
    static Collection<ValidationMessage> validate(final ProcessingEnvironment processingEnv, final MessageMethod messageMethod) {
        boolean continueValidation = !(messageMethod.parametersAnnotatedWith(Field.class).isEmpty() && messageMethod.parametersAnnotatedWith(Property.class).isEmpty());
        for (Class<? extends Annotation> annotation : VALIDATING_ANNOTATIONS) {
            if (messageMethod.isAnnotatedWith(annotation)) {
                continueValidation = true;
                break;
            }
        }
        if (continueValidation) {
            final TypeMirror returnType = messageMethod.getReturnType();
            final List<ValidationMessage> result = new ArrayList<>();
            if (returnType.getKind() == TypeKind.DECLARED) {
                final PropertyValidator validator = new PropertyValidator(processingEnv, messageMethod, returnType, result);
                validator.validate();
            } else {
                result.add(createError(messageMethod, "The return type is invalid for property annotations."));
            }
            return result;
        }
        return Collections.emptyList();
    }

    private void validate() {
        final Map<String, Set<TypeMirror>> fields = new HashMap<>();
        final Map<String, Set<TypeMirror>> methods = new HashMap<>();
        final TypeElement e = (TypeElement) types.asElement(resultType);
        for (ExecutableElement executableElement : ElementFilter.methodsIn(elements.getAllMembers(e))) {
            if (executableElement.getModifiers().contains(Modifier.PUBLIC) && executableElement.getParameters().size() == 1) {
                final String methodName = executableElement.getSimpleName().toString();
                // Only add setters and use a property style name
                if (methodName.startsWith("set")) {
                    final String name = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
                    final Set<TypeMirror> types = methods.computeIfAbsent(name, (key -> new HashSet<>()));
                    types.add(executableElement.getParameters().get(0).asType());
                }
            }
        }
        for (Element element : ElementFilter.fieldsIn(elements.getAllMembers(e))) {
            if (element.getModifiers().contains(Modifier.PUBLIC) && !element.getModifiers().contains(Modifier.FINAL)) {
                final Set<TypeMirror> types = fields.computeIfAbsent(element.getSimpleName().toString(), (key -> new HashSet<>()));
                types.add(element.asType());
            }
        }
        // Validate default properties
        ElementHelper.getAnnotations(method, Fields.class, Field.class).forEach(a -> validateAnnotation(a, fields));
        ElementHelper.getAnnotations(method, Properties.class, Property.class).forEach(a -> validateAnnotation(a, methods));

        // Validate fields
        for (Parameter parameter : method.parametersAnnotatedWith(Field.class)) {
            final Set<TypeMirror> propertyTypes = fields.get(resolveFieldName(parameter));
            final TypeMirror valueType = parameter.asType();
            if (!assignablePropertyFound(valueType, propertyTypes)) {
                messages.add(createError(parameter, "No target field found in %s with name %s with type %s.", resultType, parameter.targetName(), valueType));
            }
            validateCommonAnnotation(parameter, Field.class);
        }
        // Validate properties
        for (Parameter parameter : method.parametersAnnotatedWith(Property.class)) {
            final Set<TypeMirror> propertyTypes = methods.get(resolveSetterName(parameter));
            final TypeMirror valueType = parameter.asType();
            if (!assignablePropertyFound(valueType, propertyTypes)) {
                messages.add(createError(parameter, "No method found in %s with signature %s(%s).", resultType, parameter.targetName(), valueType));
            }
            validateCommonAnnotation(parameter, Property.class);
        }
    }

    private void validateCommonAnnotation(final Parameter parameter, final Class<? extends Annotation> annotation) {
        final Collection<AnnotationMirror> annotations = ElementHelper.getAnnotations(parameter, null, annotation);
        // There should only be one annotation
        if (annotations.size() != 1) {
            messages.add(createError(parameter, "Parameters can contain only a single @%s annotation.", annotation.getName()));
        } else {
            // We should have a name value and one other value
            final AnnotationMirror annotationMirror = annotations.iterator().next();
            final Map<? extends ExecutableElement, ? extends AnnotationValue> map = annotationMirror.getElementValues();
            if (!map.isEmpty()) {
                // Look for the name attribute and a single value
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : map.entrySet()) {
                    final ExecutableElement attribute = entry.getKey();
                    final AnnotationValue attributeValue = entry.getValue();
                    if (!"name".contentEquals(attribute.getSimpleName())) {
                        messages.add(createError(parameter, annotationMirror, attributeValue,
                                "Default values are not allowed for parameters annotated with @%s. %s", annotation.getName(), annotationMirror));
                    }
                }
            }
        }
    }

    private void validateAnnotation(final AnnotationMirror annotationMirror, final Map<String, Set<TypeMirror>> properties) {
        // We should have a name value and one other value
        final Map<? extends ExecutableElement, ? extends AnnotationValue> map = annotationMirror.getElementValues();
        final int size = map.size();
        if (size < 2) {
            messages.add(createError(method, annotationMirror, "The name attribute and at least one default value are required: %s", annotationMirror));
        } else if (size > 2) {
            messages.add(createError(method, annotationMirror, "Only the name attribute and one default attribute are allowed to be defined: %s", annotationMirror));
        } else {
            // Look for the name attribute and a single value
            String name = null;
            AnnotationValue value = null;
            for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : map.entrySet()) {
                final ExecutableElement attribute = entry.getKey();
                final AnnotationValue attributeValue = entry.getValue();
                if ("name".contentEquals(attribute.getSimpleName())) {
                    name = String.valueOf(attributeValue.getValue());
                } else {
                    value = attributeValue;
                }
            }
            if (name == null) {
                messages.add(createError(method, annotationMirror, "The name attribute is required on %s", annotationMirror));
            } else if (value == null) {
                messages.add(createError(method, annotationMirror, "No value could be determined for %s", annotationMirror));
            } else {
                final Set<TypeMirror> propertyTypes = properties.get(name);
                if (propertyTypes == null) {
                    messages.add(createError(method, annotationMirror, value, "Could not find property %s on %s.", name, resultType));
                } else {
                    final TypeMirror defaultValueType = value.accept(ValueTypeAnnotationValueVisitor.INSTANCE, elements);
                    if (!assignablePropertyFound(defaultValueType, propertyTypes)) {
                        messages.add(createError(method, annotationMirror, value, "Expected property with type %s found with type %s",
                                defaultValueType, propertyTypes));
                    }
                }
            }
        }
    }

    private boolean assignablePropertyFound(final TypeMirror valueType, final Set<TypeMirror> propertyTypes) {
        if (propertyTypes != null) {
            for (TypeMirror propertyType : propertyTypes) {
                if (types.isAssignable(types.erasure(valueType), types.erasure(propertyType))) {
                    return true;
                }
            }
        }
        return false;
    }

    private String resolveFieldName(final Parameter parameter) {
        String result = "";
        final Field field = parameter.getAnnotation(Field.class);
        if (field != null) {
            final String name = field.name();
            if (name.isEmpty()) {
                result = parameter.getSimpleName().toString();
            } else {
                result = name;
            }
        }
        return result;
    }

    private String resolveSetterName(final Parameter parameter) {
        String result = "";
        final Property property = parameter.getAnnotation(Property.class);
        if (property != null) {
            final String name = property.name();
            if (name.isEmpty()) {
                result = parameter.getSimpleName().toString();
            } else {
                result = name;
            }
        }
        return result;
    }

    private static class ValueTypeAnnotationValueVisitor extends SimpleAnnotationValueVisitor8<TypeMirror, Elements> {
        private static final ValueTypeAnnotationValueVisitor INSTANCE = new ValueTypeAnnotationValueVisitor();

        @Override
        protected TypeMirror defaultAction(final Object o, final Elements elements) {
            return elements.getTypeElement(o.getClass().getName()).asType();
        }

        @Override
        public TypeMirror visitType(final TypeMirror t, final Elements elements) {
            return elements.getTypeElement(Class.class.getName()).asType();
        }
    }
}
