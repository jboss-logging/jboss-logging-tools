/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.jboss.logging.processor.apt;

import static org.jboss.logging.processor.util.Objects.HashCodeBuilder;
import static org.jboss.logging.processor.util.Objects.ToStringBuilder;
import static org.jboss.logging.processor.util.Objects.areEqual;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.Field;
import org.jboss.logging.annotations.FormatWith;
import org.jboss.logging.annotations.LoggingClass;
import org.jboss.logging.annotations.Param;
import org.jboss.logging.annotations.Pos;
import org.jboss.logging.annotations.Property;
import org.jboss.logging.annotations.Transform;
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

    public static Set<Parameter> of(final Elements elements, final Types types, final ExecutableElement method) {
        final Set<Parameter> result = new LinkedHashSet<Parameter>();
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
                result.add(new AptParameter(elements, types, qualifiedType, param, formatClass, (++index == params.size())));
            } else {
                result.add(new AptParameter(elements, types, qualifiedType, param, formatClass, false));
            }
        }
        return result;
    }

    public static Parameter forMessageMethod(final MessageMethod messageMethod) {
        return new Parameter() {

            @Override
            public String formatterClass() {
                return null;
            }

            @Override
            public Class<?> paramClass() {
                return null;
            }

            @Override
            public String targetName() {
                return "";
            }

            @Override
            public Transform transform() {
                return null;
            }

            @Override
            public Pos pos() {
                return null;
            }

            @Override
            public String type() {
                return String.class.getName();
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
            public ParameterType parameterType() {
                return ParameterType.MESSAGE;
            }

            @Override
            public int hashCode() {
                return HashCodeBuilder.builder()
                        .add(type())
                        .add(name()).toHashCode();
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
                return areEqual(type(), other.type()) && areEqual(name(), other.name());
            }

            @Override
            public int compareTo(final Parameter other) {
                return Comparison.begin()
                        .compare(this.type(), other.type())
                        .compare(this.name(), other.name()).result();
            }

            @Override
            public String toString() {
                return ToStringBuilder.of(this)
                        .add("name", name())
                        .add("type", type()).toString();
            }

            @Override
            public MessageMethod reference() {
                return messageMethod;
            }

            @Override
            public boolean isAssignableFrom(final Class<?> type) {
                return String.class.isAssignableFrom(type);
            }

            @Override
            public boolean isSubtypeOf(final Class<?> type) {
                return type.isAssignableFrom(String.class);
            }

            @Override
            public boolean isSameAs(final Class<?> type) {
                return type().equals(type.getName());
            }
        };
    }

    private static class AptParameter extends AbstractMessageObjectType implements Parameter {

        private final VariableElement param;
        private final String qualifiedType;
        private final String formatterClass;
        private final Class<?> paramClass;
        private final boolean isVarArgs;
        private final ParameterType parameterType;
        private final Transform transform;
        private final Pos pos;

        /**
         * Only allow construction from within the parent class.
         *
         * @param elements       the element utilities from the annotation processor.
         * @param types          the type utilities from the annotation processor.
         * @param qualifiedType  the qualified type name of the parameter.
         * @param param          the parameter.
         * @param formatterClass the formatter class, or {@code null} if none
         * @param isVarArgs      {@code true} if this is a vararg parameter, otherwise {@code false}.
         */
        AptParameter(final Elements elements, final Types types, final String qualifiedType, final VariableElement param, final String formatterClass, final boolean isVarArgs) {
            super(elements, types, param);
            this.qualifiedType = qualifiedType;
            this.param = param;
            this.formatterClass = formatterClass;
            if (ElementHelper.isAnnotatedWith(param, Param.class)) {
                paramClass = Object.class;
                parameterType = ParameterType.CONSTRUCTION;
                transform = null;
                pos = null;
            } else if (ElementHelper.isAnnotatedWith(param, Cause.class)) {
                paramClass = null;
                parameterType = ParameterType.CAUSE;
                transform = null;
                pos = null;
            } else if (ElementHelper.isAnnotatedWith(param, Field.class)) {
                paramClass = null;
                parameterType = ParameterType.FIELD;
                transform = null;
                pos = null;
            } else if (ElementHelper.isAnnotatedWith(param, Property.class)) {
                paramClass = null;
                parameterType = ParameterType.PROPERTY;
                transform = null;
                pos = null;
            } else if (ElementHelper.isAnnotatedWith(param, LoggingClass.class)) {
                paramClass = null;
                parameterType = ParameterType.FQCN;
                transform = null;
                pos = null;
            } else if (ElementHelper.isAnnotatedWith(param, Transform.class)) {
                paramClass = null;
                parameterType = ParameterType.TRANSFORM;
                transform = param.getAnnotation(Transform.class);
                pos = null;
            } else if (ElementHelper.isAnnotatedWith(param, Pos.class)) {
                paramClass = null;
                parameterType = ParameterType.POS;
                transform = null;
                pos = param.getAnnotation(Pos.class);
            } else {
                parameterType = ParameterType.FORMAT;
                paramClass = null;
                transform = null;
                pos = null;
            }
            this.isVarArgs = isVarArgs;
        }

        @Override
        public String type() {
            return qualifiedType;
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
        public ParameterType parameterType() {
            return parameterType;
        }

        @Override
        public Class<?> paramClass() {
            return paramClass;
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
        public Transform transform() {
            return transform;
        }

        @Override
        public Pos pos() {
            return pos;
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
                    .compare(this.type(), other.type())
                    .compare(this.name(), other.name()).result();
        }

        @Override
        public String toString() {
            return ToStringBuilder.of(this)
                    .add("name", name())
                    .add("type", type()).toString();
        }

        @Override
        public VariableElement reference() {
            return param;
        }
    }
}
