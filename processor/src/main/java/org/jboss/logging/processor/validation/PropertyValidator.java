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
import java.util.List;
import java.util.Map;
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
    private final Element element;
    private final TypeMirror resultType;
    private final Collection<ValidationMessage> messages;

    private PropertyValidator(final ProcessingEnvironment processingEnv, final Element element, final TypeMirror resultType, final Collection<ValidationMessage> messages) {
        elements = processingEnv.getElementUtils();
        types = processingEnv.getTypeUtils();
        this.element = element;
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
        return validate(processingEnv, messageMethod.getDelegate(), messageMethod.getReturnType());
    }

    /**
     * Validates the element property annotations and ensures they can be set on the type.
     *
     * @param processingEnv the annotation processing environment
     * @param element       the element that may contain property annotations
     * @param type          the type the properties will be set on
     *
     * @return a collection of validation messages
     */
    static Collection<ValidationMessage> validate(final ProcessingEnvironment processingEnv, final Element element, final TypeMirror type) {
        boolean continueValidation = false;
        for (Class<? extends Annotation> annotation : VALIDATING_ANNOTATIONS) {
            if (element.getAnnotation(annotation) != null) {
                continueValidation = true;
                break;
            }
        }
        if (continueValidation) {
            final List<ValidationMessage> result = new ArrayList<>();
            if (type.getKind() == TypeKind.DECLARED) {
                final PropertyValidator validator = new PropertyValidator(processingEnv, element, type, result);
                validator.validate();
            } else {
                result.add(createError(element, "The return type is invalid for property annotations."));
            }
            return result;
        }
        return Collections.emptyList();
    }

    private void validate() {
        final Map<String, TypeMirror> fields = new HashMap<>();
        final Map<String, TypeMirror> methods = new HashMap<>();
        final Element e = types.asElement(resultType);
        for (ExecutableElement executableElement : ElementFilter.methodsIn(elements.getAllMembers((TypeElement) e))) {
            if (executableElement.getModifiers().contains(Modifier.PUBLIC) && executableElement.getParameters().size() == 1) {
                final String methodName = executableElement.getSimpleName().toString();
                // Only add setters and use a property style name
                if (methodName.startsWith("set")) {
                    final String name = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
                    methods.put(name, executableElement.getParameters().get(0).asType());
                }
            }
        }
        for (Element element : ElementFilter.fieldsIn(elements.getAllMembers((TypeElement) e))) {
            if (element.getModifiers().contains(Modifier.PUBLIC) && !element.getModifiers().contains(Modifier.FINAL)) {
                fields.put(element.getSimpleName().toString(), element.asType());
            }
        }
        ElementHelper.getAnnotations(element, Fields.class, Field.class).forEach(a -> validateAnnotation(a, fields));
        ElementHelper.getAnnotations(element, Properties.class, Property.class).forEach(a -> validateAnnotation(a, methods));
    }

    private void validateAnnotation(final AnnotationMirror annotationMirror, final Map<String, TypeMirror> properties) {
        // We should have a name value and one other value
        final Map<? extends ExecutableElement, ? extends AnnotationValue> map = annotationMirror.getElementValues();
        final int size = map.size();
        if (size < 2) {
            messages.add(createError(element, annotationMirror, "The name attribute and at least one default value are required: %s", annotationMirror));
        } else if (size > 2) {
            messages.add(createError(element, annotationMirror, "Only the name attribute and one default attribute are allowed to be defined: %s", annotationMirror));
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
                messages.add(createError(element, annotationMirror, "The name attribute is required on %s", annotationMirror));
            } else if (value == null) {
                messages.add(createError(element, annotationMirror, "No value could be determined for %s", annotationMirror));
            } else {
                final TypeMirror propertyType = properties.get(name);
                if (propertyType == null) {
                    messages.add(createError(element, annotationMirror, value, "Could not find property %s on %s.", name, resultType));
                } else {
                    final TypeMirror defaultValueType = value.accept(ValueTypeAnnotationValueVisitor.INSTANCE, elements);
                    if (!types.isAssignable(types.erasure(defaultValueType), types.erasure(propertyType))) {
                        messages.add(createError(element, annotationMirror, value, "Expected property with type %s found with type %s",
                                defaultValueType, propertyType));
                    }
                }
            }
        }
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
