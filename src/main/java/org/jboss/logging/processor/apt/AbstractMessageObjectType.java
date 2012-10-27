package org.jboss.logging.processor.apt;

import static org.jboss.logging.processor.util.ElementHelper.typeToString;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import org.jboss.logging.processor.model.MessageObjectType;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
abstract class AbstractMessageObjectType implements MessageObjectType {
    protected final Elements elements;
    protected final Types types;
    protected final TypeMirror typeMirror;

    protected AbstractMessageObjectType(final Elements elements, final Types types, final TypeMirror typeMirror) {
        this.elements = elements;
        this.types = types;
        this.typeMirror = typeMirror;
    }

    protected AbstractMessageObjectType(final Elements elements, final Types types, final Element element) {
        this.elements = elements;
        this.types = types;
        this.typeMirror = element.asType();
    }

    @Override
    public String type() {
        return name();
    }

    @Override
    public final boolean isAssignableFrom(final Class<?> type) {
        final TypeMirror typeMirror = elements.getTypeElement(typeToString(type)).asType();
        return types.isAssignable(this.typeMirror, typeMirror);
    }

    @Override
    public final boolean isSubtypeOf(final Class<?> type) {
        final TypeMirror typeMirror = elements.getTypeElement(type.getName()).asType();
        return types.isSubtype(this.typeMirror, typeMirror);
    }

    @Override
    public final boolean isSameAs(final Class<?> type) {
        return type().equals(type.getName().replace("$", "."));
    }
}
