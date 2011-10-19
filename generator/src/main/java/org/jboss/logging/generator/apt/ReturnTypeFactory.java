package org.jboss.logging.generator.apt;

import org.jboss.logging.generator.intf.model.MessageMethod;
import org.jboss.logging.generator.intf.model.Parameter;
import org.jboss.logging.generator.intf.model.ReturnType;
import org.jboss.logging.generator.intf.model.ThrowableType;
import org.jboss.logging.generator.util.Objects;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jboss.logging.generator.util.Objects.HashCodeBuilder;
import static org.jboss.logging.generator.util.Objects.areEqual;

/**
 * Date: 29.07.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
final class ReturnTypeFactory {
    /**
     * Private constructor for factory.
     */
    private ReturnTypeFactory() {
    }


    public static ReturnType of(final Elements elements, final Types types, final TypeMirror returnType, final MessageMethod method) {
        if (returnType.getKind() == TypeKind.VOID) {
            return ReturnType.VOID;
        }
        final AptReturnType result = new AptReturnType(elements, types, returnType, method);
        result.init();
        return result;
    }

    /**
     * Implementation of return type.
     */
    private static class AptReturnType implements ReturnType {
        private final Elements elements;
        private final Map<String, TypeMirror> fields;
        private final Map<String, TypeMirror> methods;
        private final Types types;
        private final TypeMirror returnType;
        private final MessageMethod method;
        private ThrowableType throwableType;

        AptReturnType(final Elements elements, final Types types, final TypeMirror returnType, final MessageMethod method) {
            this.elements = elements;
            this.types = types;
            this.returnType = returnType;
            this.method = method;
            throwableType = null;
            fields = new HashMap<String, TypeMirror>();
            methods = new HashMap<String, TypeMirror>();
        }

        @Override
        public boolean hasFieldFor(final Parameter parameter) {
            return fields.containsKey(parameter.targetName()) && checkType(parameter, fields.get(parameter.targetName()));
        }

        @Override
        public boolean hasMethodFor(final Parameter parameter) {
            return methods.containsKey(parameter.targetName()) && checkType(parameter, methods.get(parameter.targetName()));
        }

        @Override
        public boolean isThrowable() {
            return isSubtypeOf(Throwable.class);
        }

        @Override
        public boolean isPrimitive() {
            return returnType.getKind().isPrimitive();
        }

        @Override
        public String name() {
            return returnType.toString();
        }

        @Override
        public ThrowableType throwableReturnType() {
            return throwableType;
        }

        private void init() {
            if (isThrowable()) {
                throwableType = ThrowableTypeFactory.forReturnType(elements, types, returnType, method);
            }
            final Element e = types.asElement(returnType);
            if (e instanceof TypeElement) {
                final List<ExecutableElement> returnTypeMethods = ElementFilter.methodsIn(elements.getAllMembers((TypeElement) e));
                for (ExecutableElement executableElement : returnTypeMethods) {
                    if (executableElement.getModifiers().contains(Modifier.PUBLIC) && executableElement.getParameters().size() == 1) {
                        methods.put(executableElement.getSimpleName().toString(), executableElement.getParameters().get(0).asType());
                    }
                }
                for (Element element : ElementFilter.fieldsIn(elements.getAllMembers((TypeElement) e))) {
                    if (element.getModifiers().contains(Modifier.PUBLIC) && !element.getModifiers().contains(Modifier.FINAL)) {
                        fields.put(element.getSimpleName().toString(), element.asType());
                    }
                }
            }
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
            if (!(obj instanceof AptReturnType)) {
                return false;
            }
            final AptReturnType other = (AptReturnType) obj;
            return areEqual(name(), other.name());
        }

        @Override
        public String toString() {
            return Objects.ToStringBuilder.of(this)
                    .add("name", name())
                    .add("primitive", isPrimitive())
                    .add("throwable", isThrowable())
                    .add("throwableType", throwableType).toString();
        }

        @Override
        public TypeMirror reference() {
            return returnType;
        }

        @Override
        public String type() {
            return name();
        }

        @Override
        public boolean isAssignableFrom(final Class<?> type) {
            final TypeMirror typeMirror = elements.getTypeElement(type.getName()).asType();
            return types.isAssignable(typeMirror, returnType);
        }

        @Override
        public boolean isSubtypeOf(final Class<?> type) {
            final TypeMirror typeMirror = elements.getTypeElement(type.getName()).asType();
            return types.isSubtype(returnType, typeMirror);
        }

        @Override
        public boolean isSameAs(final Class<?> type) {
            return type().equals(type.getName());
        }

        private boolean checkType(final Parameter parameter, final TypeMirror type) {
            if (parameter.isPrimitive()) {
                if (type.getKind().isPrimitive()) {
                    return parameter.type().equalsIgnoreCase(type.getKind().name());
                }
                return types.isAssignable(elements.getTypeElement(unbox(parameter)).asType(), type);
            }
            if (type.getKind().isPrimitive()) {
                final TypeElement primitiveType = types.boxedClass((PrimitiveType) type);
                return types.isAssignable(elements.getTypeElement(parameter.type()).asType(), primitiveType.asType());
            }
            return types.isAssignable(elements.getTypeElement(parameter.type()).asType(), type);
        }

        private String unbox(final Parameter parameter) {
            String result = parameter.type();
            if (parameter.isPrimitive()) {
                if ("int".equals(result)) {
                    result = Integer.class.getName();
                } else if ("char".equals(result)) {
                    result = Character.class.getName();
                } else {
                    result = "java.lang." + Character.toUpperCase(result.charAt(0)) + result.substring(1);
                }
            }
            return result;
        }
    }
}
