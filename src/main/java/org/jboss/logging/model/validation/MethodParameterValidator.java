/**
 * 
 */
package org.jboss.logging.model.validation;

import java.util.Collection;

import org.jboss.logging.model.MethodDescriptor;

/**
 * @author James R. Perkins Jr. (jrp)
 * 
 */
public class MethodParameterValidator implements Validator {
    private final MethodDescriptor methodDescriptor;

    /**
     * 
     */
    private MethodParameterValidator(final MethodDescriptor methodDescriptor) {
        this.methodDescriptor = methodDescriptor;
    }
    
    public static MethodParameterValidator create(final MethodDescriptor methodDescriptor) {
        return new MethodParameterValidator(methodDescriptor);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jboss.logging.model.validation.Validator#validate()
     */
    @Override
    public void validate() throws ValidationException {
        final Collection<MethodDescriptor> methodDescriptors = methodDescriptor
                .find(methodDescriptor.name());
        final int paramCount1 = methodDescriptor.parameters().size()
                - ((methodDescriptor.hasClause()) ? 1 : 0);
        // Validate the parameters
        for (MethodDescriptor methodDesc : methodDescriptors) {
            final int paramCount2 = methodDesc.parameters().size()
                    - ((methodDesc.hasClause()) ? 1 : 0);
            if (paramCount1 != paramCount2) {
                throw new ValidationException(
                        "The number of parameters, minus the clause parameter, must match all methods with the same name.",
                        methodDescriptor.method());
            }
        }
    }

}
