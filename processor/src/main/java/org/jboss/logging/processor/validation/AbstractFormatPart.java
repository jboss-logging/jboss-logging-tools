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

import org.jboss.logging.processor.util.Comparison;

/**
 * Abstract class that only implements Comparable for convenience. Uses the {@link org.jboss.logging.processor.validation.FormatPart#position()} for
 * the comparison.
 * <p/>
 * Date: 13.06.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
abstract class AbstractFormatPart implements FormatPart {

    @Override
    public int compareTo(final FormatPart other) {
        return Comparison.begin().
                compare(position(), other.position()).result();
    }

    @Override
    public String toString() {
        return new StringBuilder(getClass().getSimpleName()).append("[")
                .append("index=")
                .append(index())
                .append(", position=")
                .append(position())
                .append(", part=")
                .append(part())
                .toString();
    }
}
