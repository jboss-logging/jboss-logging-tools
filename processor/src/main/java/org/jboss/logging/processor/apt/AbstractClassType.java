package org.jboss.logging.processor.apt;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import org.jboss.logging.processor.model.ClassType;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
abstract class AbstractClassType implements ClassType {
    protected final Elements elements;
    protected final Types types;
    protected final TypeMirror typeMirror;

    protected AbstractClassType(final Elements elements, final Types types, final TypeMirror typeMirror) {
        this.elements = elements;
        this.types = types;
        this.typeMirror = typeMirror;
    }

    protected AbstractClassType(final Elements elements, final Types types, final Element element) {
        this.elements = elements;
        this.types = types;
        this.typeMirror = element.asType();
    }

    @Override
    public final boolean isAssignableFrom(final Class<?> type) {
        return types.isAssignable(types.erasure(toType(type)), types.erasure(this.typeMirror));
    }

    @Override
    public final boolean isSubtypeOf(final Class<?> type) {
        return types.isSubtype(types.erasure(this.typeMirror), toType(type));
    }

    @Override
    public final boolean isSameAs(final Class<?> type) {
        return types.isSameType(types.erasure(this.typeMirror), toType(type));
    }

    private TypeMirror toType(final Class<?> type) {
        return types.erasure(elements.getTypeElement(type.getCanonicalName()).asType());
    }
}
