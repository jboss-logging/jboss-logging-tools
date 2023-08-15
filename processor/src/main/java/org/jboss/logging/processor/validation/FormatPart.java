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
 * Date: 13.06.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
interface FormatPart extends Comparable<FormatPart> {

    /**
     * The default string index.
     */
    int STRING = -2;

    /**
     * The parameter index. For default strings (non-parameters) the value is {@code -2}.
     *
     * @return the index.
     */
    int index();

    /**
     * The position for the part.
     *
     * @return the position.
     */
    int position();

    /**
     * The part of the format.
     *
     * @return the part of the format.
     */
    String part();
}
