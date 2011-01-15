/*
 *  JBoss, Home of Professional Open Source Copyright 2010, Red Hat, Inc., and
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
package org.jboss.logging.model;

import java.lang.reflect.Constructor;

/**
 *
 * @author James R. Perkins (jrp)
 */
class ReturnTypeDescriptor {

    private final boolean primitive;

    private final Class<?> returnType;

    private final String returnTypeClassName;

    private boolean stringConsturctor = false;

    private boolean throwableConstructor = false;

    private boolean stringAndThrowableConstructor = false;

    private boolean throwableAndStringConstructor = false;

    public ReturnTypeDescriptor(final String returnTypeClassName)
            throws ClassNotFoundException {
        this.returnTypeClassName = returnTypeClassName;
        returnType = Class.forName(returnTypeClassName);
        this.primitive = false;
        init();
    }

    public ReturnTypeDescriptor(final String returnTypeName, final boolean primitive)
            throws ClassNotFoundException {
        this.returnTypeClassName = returnTypeName;
        this.primitive = primitive;
        if (primitive) {
            returnType = null;
        } else {
            returnType = Class.forName(returnTypeClassName);
            init();
        }
    }

    private void init() {
        final Constructor<?>[] constructors = returnType.getConstructors();
        for (Constructor<?> construct : constructors) {
            final Class<?>[] params = construct.getParameterTypes();

            switch (params.length) {
                case 1:
                    final Class<?> param = params[0];
                    if (param.isAssignableFrom(String.class)) {
                        stringConsturctor = true;
                    } else if (Throwable.class.isAssignableFrom(param)) {
                        throwableConstructor = true;
                    }
                    break;
                case 2:
                    final Class<?> param1 = params[0];
                    final Class<?> param2 = params[1];
                    if (param1.isAssignableFrom(String.class) && Throwable.class.isAssignableFrom(param2)) {
                        stringAndThrowableConstructor = true;
                    } else if (Throwable.class.isAssignableFrom(param1) && param2.isAssignableFrom(String.class)) {
                        throwableAndStringConstructor = true;
                    }
                    break;
            }
        }
    }

    public boolean isPrimitive() {
        return primitive;
    }

    public String getReturnTypeAsString() {
        return returnTypeClassName;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public String getCauseClassName() {
        return returnTypeClassName;
    }

    public boolean hasStringAndThrowableConstructor() {
        return stringAndThrowableConstructor;
    }

    public boolean hasStringConsturctor() {
        return stringConsturctor;
    }

    public boolean hasThrowableAndStringConstructor() {
        return throwableAndStringConstructor;
    }

    public boolean hasThrowableConstructor() {
        return throwableConstructor;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((returnType == null) ? 0 : returnType.hashCode());
        result = prime * result + ((stringConsturctor) ? 1 : 0);
        result = prime * result + ((throwableConstructor) ? 1 : 0);
        result = prime * result + ((stringAndThrowableConstructor) ? 1 : 0);
        result = prime * result + ((throwableAndStringConstructor) ? 1 : 0);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ReturnTypeDescriptor)) {
            return false;
        }
        final ReturnTypeDescriptor other = (ReturnTypeDescriptor) obj;
        if ((this.returnType == null) ? other.returnType != null : this.returnType.equals(other.returnType)) {
            return false;
        }
        return (this.stringConsturctor == other.stringConsturctor)
                && (this.throwableConstructor == other.throwableConstructor)
                && (this.stringAndThrowableConstructor == other.stringAndThrowableConstructor)
                && (this.throwableAndStringConstructor == other.throwableAndStringConstructor);
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        result.append(getClass().getName()).
                append("(returnType=").
                append(returnType).
                append(", stringConstructor=").
                append(stringConsturctor).
                append(", throwableConstructor=").
                append(throwableConstructor).
                append(", stringAndThrowableConstructor=").
                append(stringAndThrowableConstructor).
                append(", throwableAndStringConstructor=").
                append(throwableAndStringConstructor).
                append(")");
        return result.toString();
    }
}
