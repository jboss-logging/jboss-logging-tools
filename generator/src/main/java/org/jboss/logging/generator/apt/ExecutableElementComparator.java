package org.jboss.logging.generator.apt;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import java.util.Comparator;
import java.util.List;

/**
 * Date: 29.07.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class ExecutableElementComparator implements Comparator<ExecutableElement> {

    @Override
    public int compare(final ExecutableElement o1, final ExecutableElement o2) {
        int c = o1.getSimpleName().toString().compareTo(o2.getSimpleName().toString());
        c = (c != 0) ? c : o1.getKind().compareTo(o2.getKind());
        c = (c != 0) ? c : (o1.getParameters().size() - o2.getParameters().size());
        // Compare the parameters
        if (c == 0) {
            final List<? extends VariableElement> params = o1.getParameters();
            for (int i = 0; i < params.size(); i++) {
                final VariableElement var1 = params.get(i);
                final VariableElement var2 = o2.getParameters().get(i);
                // TypeMirror.toString() should return the qualified type, example java.lang.String
                c = var1.asType().toString().compareTo(var2.asType().toString());
                if (c != 0) {
                    break;
                }
            }
        }
        return c;
    }
}
