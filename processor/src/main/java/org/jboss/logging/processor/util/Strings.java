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

package org.jboss.logging.processor.util;

/**
 * Date: 19.09.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class Strings {
    private Strings() {
    }

    /**
     * Creates a string filled with the with the value of the {@code filler} parameter with a length defined by the
     * {@code len} parameter.
     *
     * @param filler the filler character.
     * @param len    the length to fill.
     *
     * @return the generated string.
     */
    public static String fill(final char filler, final int len) {
        final StringBuilder result = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            result.append(filler);
        }
        return result.toString();
    }


    /**
     * Creates a string filled with the with the value of the {@code filler} parameter with a length defined by the
     * {@code len} parameter.
     *
     * @param filler the filler sequence.
     * @param len    the length to fill.
     *
     * @return the generated string.
     */
    public static String fill(final CharSequence filler, final int len) {
        final StringBuilder result = new StringBuilder((filler.length() * len));
        for (int i = 0; i < len; i++) {
            result.append(filler);
        }
        return result.toString();
    }
}
