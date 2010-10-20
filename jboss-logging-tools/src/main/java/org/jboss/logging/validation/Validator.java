/**
 * 
 */
package org.jboss.logging.validation;

/**
 * @author James R. Perkins Jr. (jrp)
 *
 */
public interface Validator {
    
    /**
     * 
     * @throws ValidationException
     */
    void validate() throws ValidationException;

}
