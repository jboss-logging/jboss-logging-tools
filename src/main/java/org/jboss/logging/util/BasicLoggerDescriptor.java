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
package org.jboss.logging.util;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.jboss.logging.BasicLogger;

/**
 * A simple descriptor for the {@link org.jboss.logging.BasicLogger}.
 * 
 * <p>
 * Reflects into the basic logger to retrieve information for implementation.
 * </p>
 * 
 * @author James R. Perkins (jrp)
 */
public final class BasicLoggerDescriptor {
    
    /**
     * The basic logger class.
     */
    public static final Class<BasicLogger> BASIC_LOGGER_CLASS = BasicLogger.class;
    
    private static final BasicLoggerDescriptor INSTANCE = new BasicLoggerDescriptor();
    
    private List<Method> methods;

    /**
     * Private constructor for singleton.
     * 
     * @param clazz the class to create the descriptor for.
     */
    private BasicLoggerDescriptor() {
        this.methods = Arrays.asList(BASIC_LOGGER_CLASS.getMethods());
    }

    /**
     * Returns the current instance of the descriptor.
     * 
     * @return the current instance of the descriptor.
     */
    public static BasicLoggerDescriptor getInstance() {
        return INSTANCE;
    }
    
    /**
     * Returns all the methods in an unmodifiable list of the basic logger.
     * 
     * @return a list of all methods.
     */
    public List<Method> getMethods() {
        return Collections.unmodifiableList(methods);
    }
    
}
