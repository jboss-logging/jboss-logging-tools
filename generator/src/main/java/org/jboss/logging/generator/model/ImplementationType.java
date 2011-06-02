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
package org.jboss.logging.generator.model;

/**
 * The implementation types.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public enum ImplementationType {

    /**
     * Represents the {@code org.jboss.logging.MessageBundle}.
     */
    BUNDLE("_$bundle"),
    /**
     * Represents the {@code org.jboss.logging.MessageLogger}.
     */
    LOGGER("_$logger");

    /**
     * The extension to append the implementation with.
     */
    private final String extension;

    /**
     * Enum constructor.
     *
     * @param extension the extension to append the implementation with.
     */
    private ImplementationType(final String extension) {
        this.extension = extension;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return extension;
    }

}
