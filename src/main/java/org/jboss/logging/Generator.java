/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.logging;

import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

/**
 * @author James R. Perkins Jr. (jrp)
 *
 */
public abstract class Generator {
    protected final ProcessingEnvironment processingEnv;
    
    public Generator(final ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }
    
    public abstract String getName();
    
    public abstract void generate(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv);
    
    public final ProcessingEnvironment processingEnvironment() {
        return processingEnv;
    }
    
    public final void printInfoMessage(final String message) {
        processingEnv.getMessager().printMessage(Kind.NOTE, message);
    }
    
    public final void printErrorMessage(final String message) {
        processingEnv.getMessager().printMessage(Kind.ERROR, message);
    }
    
    public final void printWarningMessage(final String message) {
        processingEnv.getMessager().printMessage(Kind.WARNING, message);
    }

}
