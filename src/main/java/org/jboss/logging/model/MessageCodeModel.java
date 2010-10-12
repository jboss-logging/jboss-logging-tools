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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.lang.model.element.ExecutableElement;
import javax.tools.JavaFileObject;

import org.jboss.logging.util.TransformationUtil;

import com.sun.codemodel.internal.CodeWriter;
import com.sun.codemodel.internal.JAnnotationUse;
import com.sun.codemodel.internal.JBlock;
import com.sun.codemodel.internal.JClass;
import com.sun.codemodel.internal.JClassAlreadyExistsException;
import com.sun.codemodel.internal.JCodeModel;
import com.sun.codemodel.internal.JDefinedClass;
import com.sun.codemodel.internal.JDocComment;
import com.sun.codemodel.internal.JExpr;
import com.sun.codemodel.internal.JFieldVar;
import com.sun.codemodel.internal.JMethod;
import com.sun.codemodel.internal.JMod;
import com.sun.codemodel.internal.JType;
import com.sun.codemodel.internal.JTypeVar;
import com.sun.codemodel.internal.JVar;

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
public abstract class MessageCodeModel extends CodeModel {

    /**
     * Class constructor.
     * 
     * @param interfaceName
     *            the interface name to implement.
     * @param projectCode
     *            the project code to prepend messages with.
     */
    protected MessageCodeModel(final String interfaceName, final String projectCode) {
        super(interfaceName, projectCode);
    }

    /**
     * Adds a method to the class.
     * 
     * @param method
     *            the method to add.
     */
    public abstract void addMethod(final ExecutableElement method);

    /**
     * Initializes the class to generate with defaults.
     * 
     * @throws JClassAlreadyExistsException
     *             When the specified class/interface was already created.
     */
    @Override
    public void initModel() throws JClassAlreadyExistsException {
        super.initModel();
        // Add the serializable UID
        final JFieldVar serialVersionUID = definedClass().field(JMod.PRIVATE
                | JMod.STATIC | JMod.FINAL, codeModel().LONG, "serialVersionUID");
        serialVersionUID.init(JExpr.lit(1L));
    }

}
