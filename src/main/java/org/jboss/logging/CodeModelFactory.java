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
public class CodeModelFactory {

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

    /**
     * 
     * @author James R. Perkins Jr. (jrp)
     * 
     */
    public static final class CodeModel {

        private final String className;
        private final JCodeModel codeModel;
        private JDefinedClass definedClass;
        private final String interfaceName;
        private final String packageName;
        private final Implementation type;

        private CodeModel(final Implementation type, final String interfaceName) {
            codeModel = new JCodeModel();
            this.interfaceName = interfaceName;
            this.type = type;
            className = interfaceName + type.extension;
            packageName = TransformationUtil.toPackage(interfaceName);
        }

        /**
         * Returns the code model.
         * 
         * @return
         */
        public JCodeModel codeModel() {
            return codeModel;
        }

        /**
         * Returns the main enclosing class.
         * 
         * @return the main enclosing class.
         */
        public JDefinedClass definedClass() {
            return definedClass;
        }

        /**
         * Returns the implementation type.
         * 
         * @return the implementation type.
         */
        public Implementation type() {
            return type;
        }

        public String interfaceName() {
            return interfaceName;
        }

        public String className() {
            return className;
        }

        public String packageName() {
            return packageName;
        }
    }

    /**
     * Class constructor for singleton.
     */
    private CodeModelFactory() {

    }

    public static final CodeModel createMessageLogger(
            final String qualifiedInterfaceName)
            throws JClassAlreadyExistsException {
        final CodeModelFactory factory = new CodeModelFactory();
        return factory.init(Implementation.LOGGER, qualifiedInterfaceName);
    }

    public static final CodeModel createMessageBundle(
            final String qualifiedInterfaceName)
            throws JClassAlreadyExistsException {
        final CodeModelFactory factory = new CodeModelFactory();
        return factory.init(Implementation.BUNDLE, qualifiedInterfaceName);
    }

    private CodeModel init(final Implementation impl, final String interfaceName)
            throws JClassAlreadyExistsException {
        final CodeModel result = new CodeModel(impl, interfaceName);
        final JCodeModel jCodeModel = result.codeModel();
        // Create the package
        final String packageName = result.packageName();
        if (packageName != null) {
            jCodeModel._package(packageName);
        }

        // Define the class
        final JDefinedClass definedClass = jCodeModel._class(TransformationUtil
                .toSimpleClassName(result.className()));
        definedClass.annotate(javax.annotation.Generated.class);
        definedClass._extends(Object.class);
        definedClass
                ._implements(jCodeModel.directClass(result.interfaceName()));
        definedClass._implements(java.io.Serializable.class);

        // Create the default JavaDoc
        final JDocComment docComment = definedClass.javadoc();
        docComment.add("Warning this class consists of generated code.");

        // Add the serializable UID
        final JFieldVar serialVersionUID = definedClass
                .field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL,
                        jCodeModel.LONG, "serialVersionUID");
        serialVersionUID.init(JExpr.lit(1L));
        // Add default constructor
        definedClass.constructor(JMod.PUBLIC);
        result.definedClass = definedClass;
        switch (result.type()) {
            case BUNDLE:
                initMessageBundle(result);
                break;
            case LOGGER:
                initMessageLogger(result);
                break;
        }
        return result;
    }

    private void initMessageLogger(final CodeModel codeModel)
            throws JClassAlreadyExistsException {
    }

    private void initMessageBundle(final CodeModel codeModel)
            throws JClassAlreadyExistsException {
    }
}
