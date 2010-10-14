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
import java.io.OutputStream;

import javax.tools.JavaFileObject;

import com.sun.codemodel.internal.CodeWriter;
import com.sun.codemodel.internal.JPackage;

/**
 * A code writer based on the {@code OutputStream} from a {@code JavaFileObject}
 * .
 * 
 * <p>
 * The main intent is to use this class with an annotation processor. You can
 * use the {@code Filer#createSourceFile()} to retrieve the
 * {@code JavaFileObject}.
 * </p>
 * 
 * @author James R. Perkins Jr. (jrp)
 * 
 */
public class JavaFileObjectCodeWriter extends CodeWriter {
    private final OutputStream out;

    /**
     * Class constructor.
     * 
     * @param jFileObject
     *            the file object.
     * @throws IOException
     */
    public JavaFileObjectCodeWriter(final JavaFileObject jFileObject)
            throws IOException {
        super();
        out = jFileObject.openOutputStream();
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    /**
     * Note that none of the parameters are in this method are used.
     */
    @Override
    public OutputStream openBinary(final JPackage pkg, final String fileName)
            throws IOException {
        return out;
    }

}
