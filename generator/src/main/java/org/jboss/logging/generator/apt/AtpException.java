package org.jboss.logging.generator.apt;

import javax.lang.model.element.Element;
import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Date: 26.09.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
class AtpException extends RuntimeException {
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
