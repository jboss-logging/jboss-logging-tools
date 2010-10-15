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
package org.jboss.logging.model.decorator;

import com.sun.codemodel.internal.JCodeModel;
import org.jboss.logging.model.ClassModel;
import org.jboss.logging.model.JavaFileObjectCodeWriter;

import javax.tools.JavaFileObject;
import java.io.IOException;
import org.jboss.logging.ToolLogger;

/**
 * @author Kevin Pollet
 */
public abstract class ClassModelDecorator extends ClassModel {

    /**
     * The model to decorate.
     */
    private ClassModel model;

    /**
     * Create a decorator to add
     * generated code.
     *
     * @param model the model to decorate
     */
    public ClassModelDecorator(final ClassModel model) {
        super(null, null, null);
        this.model = model;
    }

    /**
     * {@inheritDoc}
     */
    public JCodeModel generateModel() throws Exception {
        return this.model.generateModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeClass(final JavaFileObject fileObject) throws IOException {
        this.getClassModel().build(new JavaFileObjectCodeWriter(fileObject));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JCodeModel getClassModel() {
        return this.model.getClassModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getClassName() {
        return this.model.getClassName();
    }

    @Override
    protected void beforeWrite() {

    }
}

