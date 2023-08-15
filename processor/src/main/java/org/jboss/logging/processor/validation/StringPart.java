/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2023 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.logging.processor.validation;

/**
 * Represents the string portions of a format string.
 * <p/>
 * Date: 13.06.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
class StringPart extends AbstractFormatPart {

    private final int position;
    private final String part;

    /**
     * Creates a new string part.
     *
     * @param position the position.
     * @param part     the string.
     */
    public StringPart(final int position, final String part) {
        this.position = position;
        this.part = part;
    }

    /**
     * Creates a new string part.
     *
     * @param position the position.
     * @param part     the string.
     *
     * @return the string part.
     */
    public static StringPart of(final int position, final String part) {
        return new StringPart(position, part);
    }

    @Override
    public int index() {
        return STRING;
    }

    @Override
    public int position() {
        return position;
    }

    @Override
    public String part() {
        return part;
    }
}
