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

package org.jboss.logging.processor.model;

/**
 * A generic interface for returning basic information about parts of a message bundle or message logger interface.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public interface MessageObject {

    /**
     * Returns a name for the object.
     * <p/>
     * For an interface or class this will return the qualified class name. For a method this will return the name of
     * the method. For a parameter the name of the parameter will be returned.
     *
     * @return the name of the object.
     */
    String name();

    /**
     * The object used to extract information for the message logger or message bundle, if applicable. The reference is
     * not used for the implementation and is provided for convenience.
     * <p/>
     * For example, in an annotation processor implementation a {@link javax.lang.model.element.ExecutableElement}
     * might be returned.
     *
     * @return the reference object used to extract information.
     */
    Object reference();
}
