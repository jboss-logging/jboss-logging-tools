package org.jboss.logging.generator.validation;

import org.jboss.logging.generator.MessageObject;
import org.jboss.logging.generator.util.ElementHelper;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Date: 16.08.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class AptValidator extends AbstractValidator {


    @Override
    protected boolean isAssignableFrom(final MessageObject messageObject, final Class<?> clazz) {
        final Object reference = messageObject.reference();
        if (reference instanceof TypeElement) {
            final TypeElement element = TypeElement.class.cast(reference);
            return ElementHelper.isAssignableFrom(element, clazz);
        } else if (reference instanceof TypeMirror) {
            final TypeMirror typeMirror = TypeMirror.class.cast(reference);
            return ElementHelper.isAssignableFrom(typeMirror, clazz);
        } else if (reference instanceof VariableElement) {
            final TypeMirror typeMirror = VariableElement.class.cast(reference).asType();
            return ElementHelper.isAssignableFrom(typeMirror, clazz);
        }
        return false;
    }

    @Override
    protected boolean isAssignableFrom(final Class<?> clazz, final MessageObject messageObject) {
        final Object reference = messageObject.reference();
        if (reference instanceof TypeElement) {
            final TypeElement element = TypeElement.class.cast(reference);
            return ElementHelper.isAssignableFrom(clazz, element);
        } else if (reference instanceof TypeMirror) {
            final TypeMirror typeMirror = TypeMirror.class.cast(reference);
            return ElementHelper.isAssignableFrom(clazz, typeMirror);
        } else if (reference instanceof VariableElement) {
            final TypeMirror typeMirror = VariableElement.class.cast(reference).asType();
            return ElementHelper.isAssignableFrom(clazz, typeMirror);
        }
        return false;
    }
}
