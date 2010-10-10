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
package org.jboss.logging.translation.model;

import com.sun.codemodel.internal.CodeWriter;
import com.sun.codemodel.internal.JBlock;
import com.sun.codemodel.internal.JClassAlreadyExistsException;
import com.sun.codemodel.internal.JCodeModel;
import com.sun.codemodel.internal.JDefinedClass;
import com.sun.codemodel.internal.JExpr;
import com.sun.codemodel.internal.JMethod;
import com.sun.codemodel.internal.JPackage;

import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Modifier;
import java.util.Enumeration;

/**
 * @author Kevin Pollet
 */
public class TranslationClassModel {

    private String packageName;
    private String className;

    public TranslationClassModel(final String packageName, final String className) {
        this.packageName = packageName;
        this.className = className;

    }

    public JCodeModel build() throws JClassAlreadyExistsException {

        JCodeModel codeModel = new JCodeModel();
        JDefinedClass definedClass = codeModel._class(packageName + "." + className);

        return codeModel;
    }

}
