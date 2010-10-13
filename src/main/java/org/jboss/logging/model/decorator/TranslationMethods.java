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
import com.sun.codemodel.internal.JDefinedClass;
import com.sun.codemodel.internal.JExpr;
import com.sun.codemodel.internal.JMethod;
import com.sun.codemodel.internal.JMod;
import org.jboss.logging.model.ClassModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The translation methods generator.
 * 
 * @author Kevin Pollet
 */
public class TranslationMethods extends ClassModelDecorator  {

    /**
     * Translation method suffix.
     */
    private static final String METHOD_SUFFIX = "$str";

    /**
     * The translation map.
     */
    private final Map<String, String> translations;

    /**
     * Create a translation decorator who adds translation methods.
     *
     * @param model the model to decorate
     * @param translations the translations to add
     */
    public TranslationMethods(final ClassModel model, final Map<String, String> translations) {
        super(model);

        this.translations = translations != null ? translations : new HashMap<String, String>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JCodeModel generateModel() throws Exception {
        JCodeModel model = super.generateModel();
        JDefinedClass clazz = model._getClass(this.getClassName());

        Set<Map.Entry<String, String>> entries = translations.entrySet();
        for (Map.Entry<String, String> entry : entries) {

            String key = entry.getKey();
            String value = entry.getValue();

            JMethod method = clazz.method(JMod.PROTECTED, String.class, key + METHOD_SUFFIX);
            method.annotate(Override.class);
            method.body()._return(JExpr.lit(value));
        }

        return model;
    }
    
}
