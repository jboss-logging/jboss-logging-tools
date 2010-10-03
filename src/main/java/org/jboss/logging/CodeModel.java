/*
 * JBoss, Home of Professional Open Source Copyright 2010, Red Hat, Inc., and
 * individual contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.jboss.logging;

import javax.lang.model.element.ExecutableElement;

import com.sun.codemodel.internal.JClassAlreadyExistsException;
import com.sun.codemodel.internal.JCodeModel;
import com.sun.codemodel.internal.JDefinedClass;
import com.sun.codemodel.internal.JDocComment;
import com.sun.codemodel.internal.JExpr;
import com.sun.codemodel.internal.JFieldVar;
import com.sun.codemodel.internal.JMod;

/**
 * @author James R. Perkins Jr. (jrp)
 * 
 */
public abstract class CodeModel {

    /**
     * 
     * @author James R. Perkins Jr. (jrp)
     * 
     */
    public static enum Implementation {
        BUNDLE("$bundle"),
        LOGGER("$logger");
        protected final String extension;

        private Implementation(final String extension) {
            this.extension = extension;
        }
    }

    private final JCodeModel codeModel;
    private JDefinedClass definedClass;
    private final String interfaceName;

    protected CodeModel(final String interfaceName)
            throws JClassAlreadyExistsException {
        codeModel = new JCodeModel();
        this.interfaceName = interfaceName;
        init();
    }

    /**
     * Returns the code model.
     * 
     * @return
     */
    public final JCodeModel codeModel() {
        return codeModel;
    }

    /**
     * Returns the main enclosing class.
     * 
     * @return the main enclosing class.
     */
    public final JDefinedClass definedClass() {
        return definedClass;
    }

    /**
     * Returns the implementation type.
     * 
     * @return the implementation type.
     */
    public abstract Implementation type();

    public abstract void addMethod(final ExecutableElement method);

    public final String interfaceName() {
        return interfaceName;
    }

    public final String className() {
        return interfaceName() + type().extension;
    }

    public final String packageName() {
        return TransformationUtil.toPackage(interfaceName());
    }

    private void init() throws JClassAlreadyExistsException {
        // Create the package
        final String packageName = packageName();
        if (packageName != null) {
            codeModel._package(packageName);
        }

        // Define the class
        definedClass = codeModel._class(TransformationUtil
                .toSimpleClassName(className()));
        definedClass.annotate(javax.annotation.Generated.class);
        definedClass._extends(Object.class);
        definedClass._implements(codeModel.directClass(interfaceName()));
        definedClass._implements(java.io.Serializable.class);

        // Create the default JavaDoc
        final JDocComment docComment = definedClass.javadoc();
        docComment.add("Warning this class consists of generated code.");

        // Add the serializable UID
        final JFieldVar serialVersionUID = definedClass.field(JMod.PRIVATE
                | JMod.STATIC | JMod.FINAL, codeModel.LONG, "serialVersionUID");
        serialVersionUID.init(JExpr.lit(1L));
    }

}
