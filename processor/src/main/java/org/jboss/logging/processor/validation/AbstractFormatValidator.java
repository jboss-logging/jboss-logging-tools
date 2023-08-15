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
 * Date: 12.08.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
abstract class AbstractFormatValidator implements FormatValidator {
    private String summaryMessage;
    private String detailMessage;

    AbstractFormatValidator() {
        detailMessage = "";
        summaryMessage = "";
    }

    final void setDetailMessage(final String detailMessage) {
        this.detailMessage = detailMessage;
    }

    final void setDetailMessage(final String format, final Object... args) {
        this.detailMessage = String.format(format, args);
    }

    final void setSummaryMessage(final String summaryMessage) {
        this.summaryMessage = summaryMessage;
    }

    final void setSummaryMessage(final String format, final Object... args) {
        this.summaryMessage = String.format(format, args);
    }

    @Override
    public final String detailMessage() {
        return (detailMessage.isEmpty() ? summaryMessage : detailMessage);
    }

    @Override
    public final String summaryMessage() {
        return summaryMessage;
    }
}
