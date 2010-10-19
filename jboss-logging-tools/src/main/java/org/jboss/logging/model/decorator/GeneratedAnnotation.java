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

import com.sun.codemodel.internal.JAnnotationUse;
import com.sun.codemodel.internal.JCodeModel;
import com.sun.codemodel.internal.JDefinedClass;
import org.jboss.logging.model.ClassModel;

import javax.annotation.Generated;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The generated annotation
 * decorator.
 * 
 * @author Kevin Pollet
 */
public class GeneratedAnnotation extends ClassModelDecorator {

    /**
     * The annotation value.
     */
    private final String value;

    /**
     * Add a generated annotation.
     *
     * @param model the model to decorate
     * @param value the annotation value
     */
    public GeneratedAnnotation(final ClassModel model, final String value) {
        super(model);
        
        this.value = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JCodeModel generateModel() throws Exception {
        JCodeModel model = super.generateModel();
        JDefinedClass definedClass = model._getClass(this.getClassName());

        JAnnotationUse generatedAnnotation = definedClass.annotate(Generated.class);
        generatedAnnotation.param("value", value);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        generatedAnnotation.param("date", dateFormat.format(new Date()));

        return model;
    }

    /**
     * Get the generated annotation value.
     *
     * @return the generated annotation value
     */
    public String getValue() {
        return this.value;
    }

}
