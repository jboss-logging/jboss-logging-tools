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

import java.util.Date;

import javax.annotation.Generated;

import org.jboss.logging.MessageBundle;

import com.sun.codemodel.internal.JAnnotationUse;
import com.sun.codemodel.internal.JClassAlreadyExistsException;
import com.sun.codemodel.internal.JCodeModel;
import com.sun.codemodel.internal.JDefinedClass;
import com.sun.codemodel.internal.JExpr;
import com.sun.codemodel.internal.JFieldVar;
import com.sun.codemodel.internal.JMethod;
import com.sun.codemodel.internal.JMod;

/**
 * @author Kevin Pollet
 */
public class MessageBundleClassModel extends ClassModel {

    private static final String INSTANCE_FIELD_NAME = "INSTANCE";

    private static final String GET_INSTANCE_METHOD_NAME = "readResolve";

    public MessageBundleClassModel(final String className, final String projectCode, final String superClassName, final String... interfacesName) {
        super(className, projectCode, superClassName, interfacesName);
    }

    @Override
    public void initModel() throws JClassAlreadyExistsException {
        super.initModel();

        JCodeModel model = this.codeModel();

        /*
         * Add MessageBundle specific code
         */

        JDefinedClass definedClass = this.definedClass();

        //Add generated annotation
        JAnnotationUse generatedAnnotation = definedClass.annotate(Generated.class);
        generatedAnnotation.param("value", MessageBundle.class.getName());
        generatedAnnotation.param("date", new Date().toString());

        JMethod constructor = definedClass.constructor(JMod.PROTECTED);
        constructor.body().invoke("super");

        JFieldVar field = definedClass.field(JMod.PUBLIC + JMod.STATIC + JMod.FINAL, definedClass, INSTANCE_FIELD_NAME);
        field.init(JExpr._new(definedClass));

        JMethod readResolve = definedClass.method(JMod.PROTECTED, definedClass, GET_INSTANCE_METHOD_NAME);
        readResolve.annotate(Override.class);
        readResolve.body()._return(JExpr.ref(INSTANCE_FIELD_NAME));
    }

    @Override
    protected void beforeWrite() {
        // TODO Auto-generated method stub
        
    }


}
