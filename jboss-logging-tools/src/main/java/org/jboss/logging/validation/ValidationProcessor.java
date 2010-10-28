/*
 *  JBoss, Home of Professional Open Source Copyright 2010, Red Hat, Inc., and
 *  individual contributors by the @authors tag. See the copyright.txt in the
 *  distribution for a full listing of individual contributors.
 * 
 *  This is free software; you can redistribute it and/or modify it under the
 *  terms of the GNU Lesser General Public License as published by the Free
 *  Software Foundation; either version 2.1 of the License, or (at your option)
 *  any later version.
 * 
 *  This software is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 *  details.
 * 
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this software; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 *  site: http://www.fsf.org.
 */
package org.jboss.logging.validation;

import org.jboss.logging.AbstractTool;
import org.jboss.logging.MessageBundle;
import org.jboss.logging.MessageLogger;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Runs validation processes.
 * 
 * @author James R. Perkins (jrp)
 */
public class ValidationProcessor extends AbstractTool {

    private final List<Validator> validators;

    public ValidationProcessor(final ProcessingEnvironment processingEnv) {
        super(processingEnv);
        this.validators = new ArrayList<Validator>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processTypeElement(final TypeElement annotation, final TypeElement element,
            final Collection<ExecutableElement> methods) {
        validators.add(new MessageIdValidator(methods));
        validators.add(new MessageAnnotationValidator(methods));
        validators.add(new MethodParameterValidator(methods));
        if (element.getAnnotation(MessageLogger.class) != null) {
            validators.add(new LoggerReturnTypeValidator(methods));
        }
        if (element.getAnnotation(MessageBundle.class) != null) {
            validators.add(new BundleReturnTypeValidator(methods));
        }
        for (Validator validator : validators) {
            validator.validate();
        }
    }
}
