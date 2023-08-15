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

package org.jboss.logging.processor.apt.report;

/**
 * Defines the report type for generating reports.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public enum ReportType {
    ASCIIDOC(".adoc"),
    XML(".xml");
    private final String extension;

    ReportType(final String extension) {
        this.extension = extension;
    }

    /**
     * Returns the extension used for the file.
     *
     * @return the extension
     */
    public String getExtension() {
        return extension;
    }
}
