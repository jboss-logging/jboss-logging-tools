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

package org.jboss.logging.processor.generator.model;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import javax.tools.JavaFileObject;

import org.jboss.jdeparser.CodeWriter;
import org.jboss.jdeparser.JPackage;

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
        // No reason to wrap in a buffer as the FileObject returns a FilterOutputStream which writes a byte at a time
        return fileObject.openOutputStream();
    }

    @Override
    public Writer openSource(final JPackage pkg, final String fileName) throws IOException {
        // At least in OpenJDK encoding is correctly handled in the implementation
        return new BufferedWriter(fileObject.openWriter());
    }

    @Override
    public void close() throws IOException {
        // no-op
    }

}
