/**
 * 
 */
package org.jboss.logging.model.validation;

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
