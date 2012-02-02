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

package org.jboss.logging.processor.apt;

import java.io.PrintStream;
import java.io.PrintWriter;
import javax.lang.model.element.Element;

/**
 * Date: 26.09.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
class AtpException extends RuntimeException {
    private static final long serialVersionUID = 7219684904713743236L;
    private final Element element;
    private final Throwable cause;

    public AtpException(final Element element, final String msg) {
        super(msg);
        this.element = element;
        this.cause = null;
    }

    public AtpException(final Element element, final String msg, final Throwable cause) {
        super(msg, cause);
        this.element = element;
        this.cause = cause;
    }

    public static AtpException of(final Element element, final String msg) {
        return new AtpException(element, msg);
    }

    public static AtpException of(final Element element, final String format, final Object... args) {
        return new AtpException(element, String.format(format, args));
    }

    public static AtpException of(final Element element, final Throwable cause, final String msg) {
        return new AtpException(element, msg, cause);
    }

    public static AtpException of(final Element element, final Throwable cause, final String format, final Object... args) {
        return new AtpException(element, String.format(format, args), cause);
    }

    public Element getElement() {
        return element;
    }

    @Override
    public void printStackTrace() {
        super.printStackTrace();
        if (cause != null) {
            cause.printStackTrace();
        }
    }

    @Override
    public void printStackTrace(PrintStream ps) {
        super.printStackTrace(ps);
        if (cause != null) {
            cause.printStackTrace(ps);
        }
    }

    @Override
    public void printStackTrace(PrintWriter pw) {
        super.printStackTrace(pw);
        if (cause != null) {
            cause.printStackTrace(pw);
        }
    }
}
