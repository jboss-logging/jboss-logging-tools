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
