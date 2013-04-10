package org.jboss.logging.processor.apt;

import static org.jboss.logging.processor.util.ElementHelper.typeToString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.ElementFilter;
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
        return types.isAssignable(types.erasure(typeMirror), types.erasure(this.typeMirror));
    }

    @Override
    public final boolean isSubtypeOf(final Class<?> type) {
        final TypeMirror typeMirror = elements.getTypeElement(typeToString(type)).asType();
        return types.isSubtype(types.erasure(this.typeMirror), types.erasure(typeMirror));
    }

    @Override
    public final boolean isSameAs(final Class<?> type) {
        return type().equals(typeToString(type));
    }
}
