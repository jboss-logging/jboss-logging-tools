/*
 * Boss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.logging.generator;

import org.jboss.logging.generator.util.ElementHelper;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Date: 13.05.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class MethodDescriptors implements Iterable<MethodDescriptor> {

    private Set<MethodDescriptor> descriptors;

    protected final Elements elementUtil;
    protected final Types typeUtil;
    private final Collection<ExecutableElement> methods;

    private MethodDescriptors(final Elements elementUtil, final Types typeUtil, Collection<ExecutableElement> methods) {
        descriptors = new TreeSet<MethodDescriptor>();
        this.elementUtil = elementUtil;
        this.typeUtil = typeUtil;
        this.methods = methods;
    }

    public static MethodDescriptors of(final Elements elementUtil, final Types typeUtil, Collection<ExecutableElement> methods) {
        MethodDescriptors result = new MethodDescriptors(elementUtil, typeUtil, methods);
        for (ExecutableElement method : methods) {
            result.add(MethodDescriptor.of(result, method));
        }
        return result;
    }

    protected boolean isOverloaded(final ExecutableElement method) {
        return ElementHelper.isOverloadedMethod(methods, method);
    }

    protected void add(final MethodDescriptor methodDescriptor) {
        descriptors.add(methodDescriptor);
    }

    protected void update(final MethodDescriptor methodDescriptor) {
        descriptors.remove(methodDescriptor);
        descriptors.add(methodDescriptor);
    }

    /**
     * Returns a collection of method descriptors that match the method name.
     *
     * @param methodName the method name to search for.
     *
     * @return a collection of method descriptors that match the method name.
     */
    protected Collection<MethodDescriptor> find(final String methodName) {
        final Set<MethodDescriptor> result = new LinkedHashSet<MethodDescriptor>();
        for (MethodDescriptor methodDesc : descriptors) {
            if (methodName.equals(methodDesc.name())) {
                result.add(methodDesc);
            }
        }
        return result;
    }

    @Override
    public Iterator<MethodDescriptor> iterator() {
        final Collection<MethodDescriptor> result;
        if (descriptors == null) {
            result = Collections.emptyList();
        } else {
            result = Collections.unmodifiableCollection(descriptors);
        }
        return result.iterator();
    }
}
