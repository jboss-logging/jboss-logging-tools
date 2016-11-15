/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat, Inc., and individual contributors
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
 * Represents the string portions of a format string.
 * <p/>
 * Date: 13.06.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
class StringPart extends AbstractFormatPart {

    private final int position;
    private final String part;

    /**
     * Creates a new string part.
     *
     * @param position the position.
     * @param part     the string.
     */
    public StringPart(final int position, final String part) {
        this.position = position;
        this.part = part;
    }

    /**
     * Creates a new string part.
     *
     * @param position the position.
     * @param part     the string.
     *
     * @return the string part.
     */
    public static StringPart of(final int position, final String part) {
        return new StringPart(position, part);
    }

    @Override
    public int index() {
        return STRING;
    }

    @Override
    public int position() {
        return position;
    }

    @Override
    public String part() {
        return part;
    }
}
