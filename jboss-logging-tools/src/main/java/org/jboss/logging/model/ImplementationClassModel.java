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
package org.jboss.logging.model;

import com.sun.codemodel.internal.JClassAlreadyExistsException;
import com.sun.codemodel.internal.JCodeModel;
import java.io.Serializable;

import javax.lang.model.element.ExecutableElement;

import org.jboss.logging.model.validation.ValidationException;
import org.jboss.logging.util.TransformationUtil;

import com.sun.codemodel.internal.JExpr;
import com.sun.codemodel.internal.JFieldVar;
import com.sun.codemodel.internal.JMod;
import org.jboss.logging.ToolLogger;

/**
 * An abstract code model to create the source file that implements the
 * interface.
 * 
 * <p>
 * Essentially this uses a com.sun.codemodel.internal.JCodeModel to generate the
 * source files with. This class is for convenience in generating default source
 * files.
 * </p>
 * 
 * @author James R. Perkins Jr. (jrp)
 * 
 */
public abstract class ImplementationClassModel extends ClassModel {

    private final String interfaceName;
    private final String packageName;
    private final ImplementationType type;

    /**
     * Class constructor.
     * 
     * @param interfaceName
     *            the interface name to implement.
     * @param projectCode
     *            the project code to prepend messages with.
     * @param type
     *            the type of the implementation.
     */
    protected ImplementationClassModel(final ToolLogger logger, final String interfaceName,
            final String projectCode, ImplementationType type) {
        super(logger, interfaceName + type.extension(), projectCode, Object.class
                .getName(), interfaceName, Serializable.class.getName());
        this.interfaceName = interfaceName;
        this.packageName = TransformationUtil.toPackage(interfaceName());
        this.type = type;
    }

    /**
     * Returns the implementation type.
     * 
     * @return the implementation type.
     */
    public final ImplementationType type() {
        return type;
    }

    /**
     * The interface name this generated class will be implementing.
     * 
     * @return the interface name.
     */
    public final String interfaceName() {
        return interfaceName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getClassName() {
        return interfaceName() + type().extension();
    }

    /**
     * Returns the package name for the class.
     * 
     * @return the package name.
     */
    public final String packageName() {
        return packageName;
    }

    /**
     * Adds a method to the class.
     * 
     * @param method
     *            the method to add.
     */
    public abstract void addMethod(final ExecutableElement method);

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initModel() throws JClassAlreadyExistsException {
        super.initModel();
        // Add the serializable UID
        final JFieldVar serialVersionUID = definedClass().field(
                JMod.PRIVATE | JMod.STATIC | JMod.FINAL, codeModel().LONG,
                "serialVersionUID");
        serialVersionUID.init(JExpr.lit(1L));
    }

}
