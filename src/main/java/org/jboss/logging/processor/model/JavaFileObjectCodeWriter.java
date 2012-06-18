/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package org.jboss.logging.processor.model;

import java.io.IOException;
import java.io.OutputStream;
import javax.tools.JavaFileObject;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JPackage;

/**
 * <p>
 * A code writer based on the {@code OutputStream} from a {@code JavaFileObject}.
 * <p/>
 * <p>
 * The main intent is to use this class with an annotation processor. You can
 * use the {@linkplain javax.annotation.processing.Filer#createSourceFile(CharSequence,
 * javax.lang.model.element.Element...) Filer#createSourceFile} to retrieve the
 * {@linkplain javax.tools.JavaFileObject JavaFileObject}.
 * </p>
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
class JavaFileObjectCodeWriter extends CodeWriter {

    /**
     * The java file object.
     */
    private final JavaFileObject fileObject;

    /**
     * The output stream.
     */
    private OutputStream out;

    /**
     * Class constructor.
     *
     * @param fileObject the file object.
     */
    public JavaFileObjectCodeWriter(final JavaFileObject fileObject) {
        this.fileObject = fileObject;
    }

    /**
     * Note that none of the parameters are in this method are used.
     * {@inheritDoc}
     */
    @Override
    public OutputStream openBinary(final JPackage pkg, final String fileName) throws IOException {
        this.out = fileObject.openOutputStream();
        return out;
    }

    @Override
    public void close() throws IOException {
        if (out != null) {
            out.close();
        }
    }

}
