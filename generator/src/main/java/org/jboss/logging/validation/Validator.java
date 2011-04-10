package org.jboss.logging.validation;

import org.jboss.logging.Annotations;
import org.jboss.logging.util.ElementHelper;
import org.jboss.logging.util.TransformationHelper;
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

/**
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
public final class Validator {

    private final Types typeUtils;

    private final List<ElementValidator> validators;

    private Validator(final Types typeUtils) {
        this.validators = new ArrayList<ElementValidator>();
        this.typeUtils = typeUtils;
    }

    public static Validator buildValidator(final ProcessingEnvironment pev, final Annotations annotations) {

        Validator validator = new Validator(pev.getTypeUtils());

        //Add validators
        validator.addElementValidator(new BundleReturnTypeValidator(annotations, pev.getTypeUtils()));
        validator.addElementValidator(new LoggerReturnTypeValidator(annotations, pev.getTypeUtils()));
        validator.addElementValidator(new MessageAnnotationValidator(annotations, pev.getTypeUtils()));
        validator.addElementValidator(new MethodParameterValidator(annotations, pev.getTypeUtils()));
        validator.addElementValidator(new MessageIdValidator(annotations, pev.getTypeUtils()));

        return validator;
    }

    /**
     * Validates the collection of elements and returns validation messages.
     *
     * @param typeElements the elements to validate.
     *
     * @return the collection of validator messages.
     */
    public Collection<ValidationMessage> validate(final Collection<? extends TypeElement> typeElements) {

        Collection<ValidationMessage> errorMessages = new ArrayList<ValidationMessage>();

        for (TypeElement element : typeElements) {
            try {

                Collection<ExecutableElement> elementMethods = ElementHelper.getInterfaceMethods(element, typeUtils, null);

                for (ElementValidator validator : validators) {
                    errorMessages.addAll(validator.validate(element, elementMethods));
                }
            } catch (Exception e) {
                errorMessages.add(new ValidationErrorMessage(element, TransformationHelper.stackTraceToString(e)));
            }
        }

        return errorMessages;
    }

    /**
     * Adds an element validator to validate the elements with.
     *
     * @param elementValidator the element validator.
     */
    public void addElementValidator(final ElementValidator elementValidator) {
        this.validators.add(elementValidator);
    }
}
