package org.jboss.logging.validation;

import org.jboss.logging.util.ElementHelper;
import org.jboss.logging.validation.validator.BundleReturnTypeValidator;
import org.jboss.logging.validation.validator.LoggerReturnTypeValidator;
import org.jboss.logging.validation.validator.MessageAnnotationValidator;
import org.jboss.logging.validation.validator.MessageIdValidator;
import org.jboss.logging.validation.validator.MethodParameterValidator;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jboss.logging.util.TransformationHelper;

/**
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
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

    public Collection<ValidationErrorMessage> validate(final Collection<? extends TypeElement> typeElements) {

        Collection<ValidationErrorMessage> errorMessages = new ArrayList<ValidationErrorMessage>();

        for (TypeElement element : typeElements) {
            try {

                Collection<ExecutableElement> elementMethods = ElementHelper.getInterfaceMethods(element, typeUtils);

                for (ElementValidator validator : validators) {
                    errorMessages.addAll(validator.validate(element, elementMethods));
                }
            } catch (Exception e) {
                errorMessages.add(new ValidationErrorMessage(element, TransformationHelper.stackTraceToString(e)));
            }
        }

        return errorMessages;
    }

    public void addElementValidator(final ElementValidator elementValidator) {
        this.validators.add(elementValidator);
    }
}
