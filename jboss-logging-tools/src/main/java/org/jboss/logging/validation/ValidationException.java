/**
 * 
 */
package org.jboss.logging.validation;

import javax.lang.model.element.Element;

/**
 * @author James R. Perkins Jr. (jrp)
 * 
 */
public class ValidationException extends RuntimeException {
    /**
     * 
     */
    private static final long serialVersionUID = -8868861814545681091L;
    private final Element element;

    /**
     * Creates a validation exception.
     * 
     * @param element
     *            the element that caused the exception.
     */
    public ValidationException(final Element element) {
        super();
        this.element = element;
    }

    /**
     * Creates a validation exception.
     * 
     * @param message
     *            the detail message. The detail message is saved for later
     *            retrieval by the {@link Throwable.getMessage()} method.
     * @param element
     *            the element that caused the exception.
     */
    public ValidationException(final String message, final Element element) {
        super(message);
        this.element = element;
    }

    /**
     * Creates a validation exception.
     * 
     * @param cause
     *            the cause (which is saved for later retrieval by the {@link
     *            Throwable.getCause()} method). (A null value is permitted, and
     *            indicates that the cause is nonexistent or unknown.)
     * @param element
     *            the element that caused the exception.
     */
    public ValidationException(final Throwable cause, final Element element) {
        super(cause);
        this.element = element;
    }

    /**
     * Creates a validation exception.
     * 
     * @param message
     *            the detail message. The detail message is saved for later
     *            retrieval by the {@link Throwable.getMessage()} method.
     * @param cause
     *            the cause (which is saved for later retrieval by the {@link
     *            Throwable.getCause()} method). (A null value is permitted, and
     *            indicates that the cause is nonexistent or unknown.)
     * @param element
     *            the element that caused the exception.
     */
    public ValidationException(final String message, final Throwable cause,
            final Element element) {
        super(message, cause);
        this.element = element;
    }

    /**
     * Returns the element that was the cause of the exception.
     * 
     * @return the element that was the cause of the exception.
     */
    public Element getElement() {
        return element;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((element == null) ? 0 : element.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof ValidationException))
            return false;
        ValidationException other = (ValidationException) obj;
        if (element == null) {
            if (other.element != null)
                return false;
        } else if (!element.equals(other.element))
            return false;
        return true;
    }

}
