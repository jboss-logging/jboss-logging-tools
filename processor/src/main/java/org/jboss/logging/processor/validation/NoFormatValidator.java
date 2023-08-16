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
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
final class NoFormatValidator extends AbstractFormatValidator {
    static final NoFormatValidator INSTANCE = new NoFormatValidator();

    @Override
    public int argumentCount() {
        return 0;
    }

    @Override
    public String format() {
        return "none";
    }

    @Override
    public boolean isValid() {
        return true;
    }
}
