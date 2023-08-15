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

package org.jboss.logging.processor.apt;

import static org.jboss.logging.processor.util.Objects.HashCodeBuilder;
import static org.jboss.logging.processor.util.Objects.ToStringBuilder;
import static org.jboss.logging.processor.util.Objects.areEqual;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Types;

import org.jboss.logging.annotations.Field;
import org.jboss.logging.annotations.FormatWith;
import org.jboss.logging.annotations.Pos;
import org.jboss.logging.annotations.Property;
import org.jboss.logging.annotations.Transform;
import org.jboss.logging.annotations.TransformException;
import org.jboss.logging.processor.model.MessageMethod;
import org.jboss.logging.processor.model.Parameter;
import org.jboss.logging.processor.util.Comparison;
import org.jboss.logging.processor.util.ElementHelper;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a> - 20.Feb.2011
 */
final class ParameterFactory {

    /**
     * Private constructor for factory.
     */
    private ParameterFactory() {
    }

    public static Set<Parameter> of(final ProcessingEnvironment processingEnv, final ExecutableElement method) {
        final Types types = processingEnv.getTypeUtils();
        final Set<Parameter> result = new LinkedHashSet<>();
        final List<? extends VariableElement> params = method.getParameters();
        int index = 0;
        for (VariableElement param : params) {
            final TypeElement formatClassType = ElementHelper.getClassAnnotationValue(param, FormatWith.class);
            final String formatClass = formatClassType == null ? null : formatClassType.getQualifiedName().toString();
            final String qualifiedType;
            if (param.asType().getKind().isPrimitive()) {
                qualifiedType = param.asType().toString();
            } else {
                switch ((param.asType().getKind())) {
                    case ARRAY:
                        qualifiedType = param.asType().toString().replace("[]", "");
                        break;
                    default:
                        qualifiedType = types.asElement(param.asType()).toString();
                        break;
                }
            }
            if (method.isVarArgs()) {
                result.add(new AptParameter(processingEnv, qualifiedType, param, formatClass, (++index == params.size())));
            } else {
                result.add(new AptParameter(processingEnv, qualifiedType, param, formatClass, false));
            }
        }
        return result;
    }

    public static Parameter forMessageMethod(final MessageMethod messageMethod) {
        return new MessageMethodParameter(messageMethod);
    }

    private static class AptParameter extends AbstractClassType implements Parameter {

        private final VariableElement param;
        private final String qualifiedType;
        private final String formatterClass;
        private final boolean isVarArgs;
        private final boolean isFormatArg;

        /**
         * Only allow construction from within the parent class.
         *
         * @param processingEnv  the annotation processing environment.
         * @param qualifiedType  the qualified type name of the parameter.
         * @param param          the parameter.
         * @param formatterClass the formatter class, or {@code null} if none
         * @param isVarArgs      {@code true} if this is a vararg parameter, otherwise {@code false}.
         */
        AptParameter(final ProcessingEnvironment processingEnv, final String qualifiedType, final VariableElement param,
                final String formatterClass, final boolean isVarArgs) {
            super(processingEnv, param);
            this.qualifiedType = qualifiedType;
            this.param = param;
            this.formatterClass = formatterClass;
            this.isVarArgs = isVarArgs;
            isFormatArg = param.getAnnotationMirrors().isEmpty() ||
                    ElementHelper.isAnnotatedWith(param, FormatWith.class) ||
                    ElementHelper.isAnnotatedWith(param, Transform.class) ||
                    ElementHelper.isAnnotatedWith(param, Pos.class) ||
                    ElementHelper.isAnnotatedWith(param, TransformException.class);
        }

        @Override
        public String formatterClass() {
            return formatterClass;
        }

        @Override
        public String name() {
            return param.getSimpleName().toString();
        }

        @Override
        public boolean isArray() {
            return param.asType().getKind() == TypeKind.ARRAY;
        }

        @Override
        public boolean isPrimitive() {
            return param.asType().getKind().isPrimitive();
        }

        @Override
        public boolean isVarArgs() {
            return isVarArgs;
        }

        @Override
        public boolean isFormatParameter() {
            return isFormatArg;
        }

        @Override
        public String targetName() {
            String result = "";
            final Field field = param.getAnnotation(Field.class);
            final Property property = param.getAnnotation(Property.class);
            if (field != null) {
                final String name = field.name();
                if (name.isEmpty()) {
                    result = param.getSimpleName().toString();
                } else {
                    result = name;
                }
            } else if (property != null) {
                final String name = property.name();
                if (name.isEmpty()) {
                    result = param.getSimpleName().toString();
                } else {
                    result = name;
                }
                result = "set" + Character.toUpperCase(result.charAt(0)) + result.substring(1);
            }
            return result;
        }

        @Override
        public int hashCode() {
            return HashCodeBuilder.builder()
                    .add(qualifiedType)
                    .add(param).toHashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof AptParameter)) {
                return false;
            }
            final AptParameter other = (AptParameter) obj;
            return areEqual(this.param, other.param) && areEqual(this.qualifiedType, other.qualifiedType);
        }

        @Override
        public int compareTo(final Parameter other) {
            return Comparison.begin()
                    .compare(asType().toString(), other.asType().toString())
                    .compare(this.name(), other.name()).result();
        }

        @Override
        public String toString() {
            return ToStringBuilder.of(this)
                    .add("name", name())
                    .add("type", asType()).toString();
        }

        @Override
        public Element getDelegate() {
            return param;
        }
    }

    private static class MessageMethodParameter implements Parameter {
        private final MessageMethod messageMethod;

        private MessageMethodParameter(final MessageMethod messageMethod) {
            this.messageMethod = messageMethod;
        }

        @Override
        public Element getDelegate() {
            return messageMethod;
        }

        @Override
        public String formatterClass() {
            return null;
        }

        @Override
        public String targetName() {
            return "";
        }

        @Override
        public String name() {
            return messageMethod.messageMethodName();
        }

        @Override
        public boolean isArray() {
            return false;
        }

        @Override
        public boolean isPrimitive() {
            return false;
        }

        @Override
        public boolean isVarArgs() {
            return false;
        }

        @Override
        public boolean isMessageMethod() {
            return true;
        }

        @Override
        public int hashCode() {
            return Objects.hash(messageMethod);
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof MessageMethodParameter)) {
                return false;
            }
            final MessageMethodParameter other = (MessageMethodParameter) obj;
            return Objects.equals(messageMethod, other.messageMethod);
        }

        @Override
        public int compareTo(final Parameter other) {
            if (other instanceof MessageMethodParameter) {
                final MessageMethodParameter otherParameter = (MessageMethodParameter) other;
                return messageMethod.compareTo(otherParameter.messageMethod);
            }
            // A little odd, but some kind of comparison should be done by default
            return Comparison.begin()
                    .compare(asType().toString(), other.asType().toString())
                    .compare(name(), other.name())
                    .result();
        }

        @Override
        public String toString() {
            return ToStringBuilder.of(this)
                    .add("name", name())
                    .add("type", asType()).toString();
        }

        @Override
        public boolean isAssignableFrom(final Class<?> type) {
            return false;
        }

        @Override
        public boolean isSubtypeOf(final Class<?> type) {
            return false;
        }

        @Override
        public boolean isSameAs(final Class<?> type) {
            return false;
        }
    }
}
