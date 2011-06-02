/*
 *  JBoss, Home of Professional Open Source Copyright 2011, Red Hat, Inc., and
 *  individual contributors by the @authors tag. See the copyright.txt in the
 *  distribution for a full listing of individual contributors.
 *
 *  This is free software; you can redistribute it and/or modify it under the
 *  terms of the GNU Lesser General Public License as published by the Free
 *  Software Foundation; either version 2.1 of the License, or (at your option)
 *  any later version.
 *
 *  This software is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 *  details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this software; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 *  site: http://www.fsf.org.
 */
package org.jboss.logging.generator;

import org.jboss.logging.generator.util.ElementHelper;

import javax.lang.model.element.VariableElement;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a> - 20.Feb.2011
 */
public final class MethodParameter implements Comparable<MethodParameter> {

    private final VariableElement param;
    private final String fullType;
    private final String formatterClass;

    /**
     * Only allow construction from within the parent class.
     *
     * @param fullType the full type name.
     * @param param    the parameter.
     */
    MethodParameter(final String fullType, final VariableElement param) {
        this.fullType = fullType;
        this.param = param;
        formatterClass = null;
    }

    /**
     * Only allow construction from within the parent class.
     *
     * @param fullType       the full type name.
     * @param param          the parameter.
     * @param formatterClass the formatter class, or {@code null} if none
     */
    MethodParameter(final String fullType, final VariableElement param, final String formatterClass) {
        this.param = param;
        this.fullType = fullType;
        this.formatterClass = formatterClass;
    }

    /**
     * Checks the parameter and returns {@code true} if this is a cause
     * parameter, otherwise {@code false}.
     *
     * @return {@code true} if the parameter is annotated with
     *         {@link Annotations#cause()}, otherwise {@code false}.
     */
    public boolean isCause() {
        return ElementHelper.isAnnotatedWith(param, LoggingTools.annotations().cause());
    }

    /**
     * The full type name of the parameter. For example
     * {@code java.lang.String} if the parameter is a string. If the
     * parameter is a primitive, the primitive name is returned.
     *
     * @return the qualified type of the parameter.
     */
    public String fullType() {
        return fullType;
    }

    /**
     * The formatter class, or {@code null} if there is none.
     *
     * @return the formatter class
     */
    public String getFormatterClass() {
        return formatterClass;
    }

    /**
     * The variable name of the parameter.
     *
     * @return the variable name of the parameter.
     */
    public String name() {
        return param.getSimpleName().toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int hash = 1;
        hash = prime * hash + ((fullType == null) ? 0 : fullType.hashCode());
        hash = prime * hash + ((param == null) ? 0 : param.hashCode());
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof MethodParameter)) {
            return false;
        }
        final MethodParameter other = (MethodParameter) obj;
        if ((this.param == null) ? (other.param != null) : !this.param.equals(other.param)) {
            return false;
        }
        if ((this.fullType == null) ? (other.fullType != null) : !this.fullType.equals(other.fullType)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(final MethodParameter other) {
        int result = this.fullType.compareTo(other.fullType);
        result = (result != 0) ? result : this.name().compareTo(other.name());
        return result;
    }
}
