package org.jboss.logging.validation;

import org.jboss.logging.util.ElementHelper;
import org.jboss.logging.validation.validators.BundleReturnTypeValidator;
import org.jboss.logging.validation.validators.LoggerReturnTypeValidator;
import org.jboss.logging.validation.validators.MessageAnnotationValidator;
import org.jboss.logging.validation.validators.MessageIdValidator;
import org.jboss.logging.validation.validators.MethodParameterValidator;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Kevin Pollet
 */
public class Validator {

    private final Types typeUtils;

    private final List<ElementValidator> validators;

    private Validator(final Types typeUtils) {
        this.validators = new ArrayList<ElementValidator>();
        this.typeUtils = typeUtils;
    }

    public static Validator buildValidator(final ProcessingEnvironment pev) {

        Validator validator = new Validator(pev.getTypeUtils());

        //Add validators
        validator.addElementValidator(new BundleReturnTypeValidator());
        validator.addElementValidator(new LoggerReturnTypeValidator());
        validator.addElementValidator(new MessageAnnotationValidator());
        validator.addElementValidator(new MethodParameterValidator());
        validator.addElementValidator(new MessageIdValidator());

        return validator; 
    }

    public Collection<ValidationErrorMessage> validate(final Collection<TypeElement> typeElements) {

        Collection<ValidationErrorMessage> errorMessages = new ArrayList<ValidationErrorMessage>();

        for (TypeElement element : typeElements) {

            Collection<ExecutableElement> elementMethods = ElementHelper.getInterfaceMethods(element, null);

            for (ElementValidator validator : validators) {
                errorMessages.addAll(validator.validate(element, elementMethods));
            }
        }

        return errorMessages;
    }

    public void addElementValidator(final ElementValidator elementValidator) {
        this.validators.add(elementValidator);
    }
}
