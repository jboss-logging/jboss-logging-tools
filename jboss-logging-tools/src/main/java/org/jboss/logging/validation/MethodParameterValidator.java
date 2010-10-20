/**
 * 
 */
package org.jboss.logging.validation;

import java.util.Collection;
import javax.lang.model.element.VariableElement;
import org.jboss.logging.Cause;
import org.jboss.logging.Message;

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
    public MethodParameterValidator(final MethodDescriptor methodDescriptor) {
        this.methodDescriptor = methodDescriptor;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jboss.logging.model.validation.Validator#validate()
     */
    @Override
    public void validate() throws ValidationException {
        final Collection<MethodDescriptor> methodDescriptors = methodDescriptor.
                find(methodDescriptor.name());
        final int paramCount1 = methodDescriptor.parameters().size()
                - ((methodDescriptor.hasClause()) ? 1 : 0);
        // Validate the parameters
        for (MethodDescriptor methodDesc : methodDescriptors) {
            final int paramCount2 = methodDesc.parameters().size()
                    - ((methodDesc.hasClause()) ? 1 : 0);
            if (paramCount1 != paramCount2) {
                throw new ValidationException(
                        "The number of parameters, minus the clause parameter, must match all methods with the same name.",
                        methodDesc.method());
            }
            // The method must also have a message annotation
            if (methodDesc.message() == null) {
                throw new ValidationException(
                        "All defined methods must have a @" + Message.class.
                        getName() + " annotation unless a method with the same name has the annotation present.", methodDesc.
                        method());
            }

            // Finally the method is only allowed one cause parameter
            boolean invalid = false;
            Cause ogCause = null;
            for (VariableElement varElem : methodDesc.method().getParameters()) {
                final Cause cause = varElem.getAnnotation(Cause.class);
                invalid = (ogCause != null && cause != null);
                if (invalid) {
                    throw new ValidationException(
                            "Only allowed one cause parameter per method.",
                            varElem);
                }
                ogCause = cause;
            }
        }
    }
}
