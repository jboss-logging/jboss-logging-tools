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

import com.sun.codemodel.internal.JCodeModel;
import com.sun.codemodel.internal.JDefinedClass;

import javax.annotation.processing.Filer;
import javax.tools.JavaFileObject;
import java.io.IOException;

/**
 * @author Kevin Pollet
 */
public abstract class ClassModel {

    private String[] interfacesName;

    private String superClassName;

    private String className;

    private JCodeModel classModel;

    protected ClassModel(final String className, final String superClassName, final String... interfacesName) {
        this.interfacesName = interfacesName;
        this.superClassName = superClassName;
        this.className = className;
    }

    public void initModel() throws Exception {

        this.classModel = new JCodeModel();
        JDefinedClass clazz = classModel._class(this.className);

        //Add extends
        if (this.superClassName != null) {
            JCodeModel superModel = new JCodeModel();
            clazz._extends(superModel._class(this.superClassName));
        }

        //Add implements
        if (this.interfacesName != null) {
            for (String intf : this.interfacesName) {
                JCodeModel intfModel = new JCodeModel();
                clazz._implements(intfModel._class(intf));
            }
        }

    }

    /**
     * Write the class to a file.
     *
     * @param filer the annotation processor filer
     * @throws IOException if error occurs when writing class
     */
    public void writeClass(final Filer filer) throws IOException {
        JavaFileObject fileObject = filer.createSourceFile(this.className);
        this.classModel.build(new JavaFileObjectCodeWriter(fileObject));
    }

    /**
     * Get the class model.
     *
     * @return the class model
     */
    public JCodeModel getClassModel() {
        return this.classModel;
    }

    /**
     * Get the class name.
     *
     * @return the class name
     */
    public String getClassName() {
        return this.className;
    }


}
