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

package org.jboss.logging.processor.validation;

/**
 * Date: 14.06.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public interface FormatValidator {

    /**
     * The number of arguments needed for the format.
     *
     * @return the number of arguments needed.
     */
    int argumentCount();

    /**
     * Returns the format string used for validation.
     *
     * @return the format string.
     */
    String format();

    /**
     * Returns {@code true} of the format is valid, otherwise {@code false}.
     *
     * @return {@code true} of the format is valid, otherwise {@code false}.
     */
    boolean isValid();

    /**
     * A detail message if {@link #isValid()} returns {@code false}, otherwise an empty string.
     *
     * @return a detailed message.
     */
    String detailMessage();

    /**
     * A summary message if {@link #isValid()} returns {@code false}, otherwise an empty string.
     *
     * @return a summary message.
     */
    String summaryMessage();
}
