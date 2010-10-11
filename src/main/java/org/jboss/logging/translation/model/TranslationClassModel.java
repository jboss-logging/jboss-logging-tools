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

import com.sun.codemodel.internal.JCodeModel;
import com.sun.codemodel.internal.JDefinedClass;
import com.sun.codemodel.internal.JExpr;
import com.sun.codemodel.internal.JMethod;
import com.sun.codemodel.internal.JMod;
import org.jboss.logging.JavaFileObjectCodeWriter;

import javax.annotation.processing.Filer;
import javax.tools.JavaFileObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Basic translation model
 *
 * @author Kevin Pollet
 */
public abstract class TranslationClassModel {

    private static final String METHOD_SUFFIX = "$str";

    private String packageName;

    private String className;

    private Map<String, String> translations;

    public TranslationClassModel(final String packageName, final String className) {
        this.packageName = packageName;
        this.className = className;
        this.translations = new HashMap<String, String>();

    }

    public void addAllTranslations(final Map<String, String> properties) {
        this.translations.putAll(properties);
    }

    public void addTranslation(final String key, final String value) {
        this.translations.put(key, value);
    }

    protected JCodeModel generateCodeModel() throws Exception {

        JCodeModel codeModel = new JCodeModel();
        JDefinedClass definedClass = codeModel._class(packageName + "." + className);

        Set<Map.Entry<String, String>> entries = translations.entrySet();
        for (Map.Entry<String, String> entry : entries) {

            String key = entry.getKey();
            String value = entry.getValue();

            //Ignore Empty or whitespace only message value
            if (!value.trim().isEmpty()) {
                JMethod method = definedClass.method(JMod.PROTECTED, String.class, key + METHOD_SUFFIX);
                method.body()._return(JExpr.lit(value));
            }

        }

        return codeModel;
    }

    public void writeClass(final Filer filer) throws Exception {
        JCodeModel generatedModel = this.generateCodeModel();

        JavaFileObject fileObject = filer.createSourceFile(this.getQualifiedName());
        generatedModel.build(new JavaFileObjectCodeWriter(fileObject));
    }

    public String getQualifiedName() {
        return this.packageName + "." + this.className;
    }

    public String getPackageName() {
        return this.packageName;
    }

    /**
     * Get the class name of this
     * translation class model.
     *
     * @return the class name
     */
    public String getClassName() {
        return this.className;
    }

}
