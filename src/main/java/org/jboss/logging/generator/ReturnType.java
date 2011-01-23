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
package org.jboss.logging.generator;

import java.lang.reflect.Constructor;

/**
 * Describes information about the return type.
 *
 * @author James R. Perkins (jrp)
 */
public class ReturnType {

    private final boolean primitive;

    private final Class<?> returnType;

    private final String returnTypeClassName;

    private boolean stringConsturctor = false;

    private boolean throwableConstructor = false;

    private boolean stringAndThrowableConstructor = false;

    private boolean throwableAndStringConstructor = false;

    /**
     * Creates a new descriptor that is not primitive.
     * 
     * @param returnTypeClassName the class name of the return type.
     * @throws ClassNotFoundException if the return type is not found in the classpath.
     */
    private ReturnType(final String returnTypeClassName)
            throws ClassNotFoundException {
        this.returnTypeClassName = returnTypeClassName;
        returnType = Class.forName(returnTypeClassName);
        this.primitive = false;
    }

    /**
     * Creates a new descriptor.
     * 
     * @param returnTypeName the name of the return type.
     * @param primitive      {@code true} if the return type is a primitive, 
     *                       otherwise {@code false}.
     * @throws ClassNotFoundException if the return type is not found in the classpath.
     */
    private ReturnType(final String returnTypeName, final boolean primitive)
            throws ClassNotFoundException {
        this.returnTypeClassName = returnTypeName;
        this.primitive = primitive;
        if (primitive) {
            returnType = null;
        } else {
            returnType = Class.forName(returnTypeClassName);
        }
    }

    /**
     * Creates a new descriptor that is not primitive.
     * 
     * @param returnTypeClassName the class name of the return type.
     * @throws ClassNotFoundException if the return type is not found in the classpath.
     */
    protected static ReturnType of(final String returnTypeName) throws
            ClassNotFoundException {
        final ReturnType result = new ReturnType(returnTypeName);
        result.init();
        return result;
    }

    /**
     * Creates a new descriptor.
     * 
     * @param returnTypeName the name of the return type.
     * @param primitive      {@code true} if the return type is a primitive, 
     *                       otherwise {@code false}.
     * @throws ClassNotFoundException if the return type is not found in the classpath.
     */
    protected static ReturnType of(final String returnTypeName, final boolean primitive)
            throws ClassNotFoundException {
        final ReturnType result = new ReturnType(returnTypeName, primitive);
        result.init();
        return result;
    }

    /**
     * Initializes the object.
     */
    private void init() {
        if (!primitive) {
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
    }

    /**
     * Indicates whether or not the return type is a primitive.
     * 
     * @return {@code true} if a primitive, otherwise {@code false}.
     */
    public boolean isPrimitive() {
        return primitive;
    }

    /**
     * Returns a string version of the return type.
     * 
     * @return a string version of the return type.
     */
    public String getReturnTypeAsString() {
        return returnTypeClassName;
    }

    /**
     * Returns the class created from the return type.
     * 
     * @return the return type as a class.
     */
    public Class<?> getReturnType() {
        return returnType;
    }

    /**
     * If the return type is a constructor and has a {@link java.lang.String}
     * and {@link java.lang.Throwable} constructor, {@code true} is returned. 
     * Otherwise {@code false} is returned.
     * 
     * @return {@code true} if the throwable has both a string and throwable
     *         constructor, otherwise {@code false}.
     */
    public boolean hasStringAndThrowableConstructor() {
        return stringAndThrowableConstructor;
    }

    /**
     * If the return type is a constructor and has a {@link java.lang.String}
     * constructor, {@code true} is returned. Otherwise {@code false} is 
     * returned.
     * 
     * @return {@code true} if the throwable has a string constructor, otherwise 
     *         {@code false}.
     */
    public boolean hasStringConsturctor() {
        return stringConsturctor;
    }

    /**
     * If the return type is a constructor and has a {@link java.lang.Throwable}
     * and {@link java.lang.String} constructor, {@code true} is returned. 
     * Otherwise {@code false} is returned.
     * 
     * @return {@code true} if the throwable has both a throwable and string
     *         constructor, otherwise {@code false}.
     */
    public boolean hasThrowableAndStringConstructor() {
        return throwableAndStringConstructor;
    }

    /**
     * If the return type is a constructor and has a {@link java.lang.Throwable}
     * constructor, {@code true} is returned. Otherwise {@code false} is 
     * returned.
     * 
     * @return {@code true} if the throwable has a throwable constructor, 
     *         otherwise {@code false}.
     */
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
        if (!(obj instanceof ReturnType)) {
            return false;
        }
        final ReturnType other = (ReturnType) obj;
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
