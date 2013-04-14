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

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.jboss.logging.annotations.ValidIdRange;

/**
 * Date: 28.07.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public interface MessageInterface extends Comparable<MessageInterface>, MessageObject, MessageObjectType, JavaDocComment {

    /**
     * Checks the interface to see if the {@link org.jboss.logging.processor.Loggers#loggerInterface() logger
     * interface}
     * is being extended in this interface.
     *
     * @return {@code true} if this interface extends the logger interface, otherwise {@code false}.
     */
    boolean extendsLoggerInterface();

    /**
     * A set of qualified interface names this interface extends or an empty set.
     *
     * @return a set of interface names or an empty set.
     */
    Set<MessageInterface> extendedInterfaces();

    /**
     * A collection of all the methods this interface needs to implement.
     *
     * @return a collection of methods.
     */
    Collection<MessageMethod> methods();

    /**
     * The project code for the message interface or {@code null} if {@link #isLoggerInterface()} returns {@code true}.
     *
     * @return the project code or {@code null} if {@link #isLoggerInterface()} returns {@code true}.
     */
    String projectCode();

    /**
     * The qualified name of the message interface.
     *
     * @return the qualified name.
     */
    @Override
    String name();

    /**
     * The package name of the message interface.
     *
     * @return the package name.
     */
    String packageName();

    /**
     * The name of the interface without the package.
     *
     * @return the simple interface name.
     */
    String simpleName();

    /**
     * The fully qualified class name to use for log methods. This will generally be the same result as {@link
     * #name()}.
     *
     * @return the fully qualified class name to use for logging.
     */
    String loggingFQCN();

    /**
     * Returns {@code true} if the interface is annotated as a message logger, otherwise {@code false}.
     *
     * @return {@code true} if a message logger, otherwise {@code false}.
     */
    boolean isMessageLogger();

    /**
     * Returns {@code true} if the interface is annotated as a message bundle, otherwise {@code false}.
     *
     * @return {@code true} if a message bundle, otherwise {@code false}.
     */
    boolean isMessageBundle();

    /**
     * This is a special type of {@code MessageInterface} and will only return {@code true} if this is a
     * {@link org.jboss.logging.processor.Loggers#loggerInterface() logger interface}. Otherwise {@code false} is
     * returned.
     * <p/>
     * <b>Note:</b> {@link #isMessageBundle()} and {@link #isMessageLogger()} will return {@code false} if this is
     * {@code true}.
     *
     * @return {@code true} if this is a logger interface, otherwise {@code false}.
     */
    boolean isLoggerInterface();

    /**
     * Returns a list of {@link ValidIdRange valid id ranges}.
     *
     * @return a list of valid id ranges or an empty list
     */
    List<ValidIdRange> validIdRanges();

    /**
     * The length to pad the id with. A value of less than 0 indicates no padding.
     *
     * @return the length to pad the id with
     */
    int getIdLength();
}
