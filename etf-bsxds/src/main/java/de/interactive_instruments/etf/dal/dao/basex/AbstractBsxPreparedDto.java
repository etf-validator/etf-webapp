/**
 * Copyright 2010-2022 interactive instruments GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.interactive_instruments.etf.dal.dao.basex;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Objects;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.basex.core.BaseXException;

import de.interactive_instruments.SUtils;
import de.interactive_instruments.etf.dal.dao.Filter;
import de.interactive_instruments.etf.dal.dao.OutputFormatStreamable;
import de.interactive_instruments.etf.model.OutputFormat;
import de.interactive_instruments.exceptions.ExcUtils;
import de.interactive_instruments.properties.Properties;
import de.interactive_instruments.properties.PropertyHolder;

/**
 * Abstract class for a prepared XQuery statement whose result can be directly streamed.
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
abstract class AbstractBsxPreparedDto implements OutputFormatStreamable {

    protected final BsXQuery bsXquery;

    public AbstractBsxPreparedDto(final BsXQuery xquery) {
        this.bsXquery = xquery;
    }

    private static String[] optionalParameterNames = {"offset", "limit"};

    /**
     * Streams the result from the BaseX database to the Output Format directly through a piped stream
     *
     * @param outputFormat
     *            the Output Format
     * @param arguments
     *            transformation arguments
     * @param outputStream
     *            transformed output stream
     *
     */
    public void streamTo(final OutputFormat outputFormat, final PropertyHolder arguments, final OutputStream outputStream) {
        try {

            // create a copy
            final Properties properties = new Properties(arguments);

            // DETAILED_WITHOUT_HISTORY level is required
            bsXquery.parameter("levelOfDetail", String.valueOf(Filter.LevelOfDetail.DETAILED_WITHOUT_HISTORY), "xs:string");

            // Pass parameters from query to XSLT
            for (final String optionalParameterName : optionalParameterNames) {
                final String val = bsXquery.getParameter(optionalParameterName);
                if (!SUtils.isNullOrEmpty(val)) {
                    properties.setProperty(optionalParameterName, val);
                }
            }

            final String fields = bsXquery.getParameter("fields");
            if (!SUtils.isNullOrEmpty(fields) && !fields.equals("*")) {
                properties.setProperty("fields", fields);
            }

            // Required property
            properties.setProperty(
                    "selection",
                    Objects.requireNonNull(
                            bsXquery.getParameter("selection"),
                            "Invalid selection"));

            final PipedInputStream in = new PipedInputStream();
            final PipedOutputStream out = new PipedOutputStream(in);
            new Thread(() -> {
                try {
                    bsXquery.execute(out);
                } catch (final IOException e) {
                    throw new BsxPreparedDtoException(e);
                } finally {
                    try {
                        out.close();
                    } catch (IOException e) {
                        ExcUtils.suppress(e);
                    }
                }
            }).start();
            Objects.requireNonNull(outputFormat, "Output Format is null").streamTo(properties, in, outputStream);
            // statement for streaming the request without transformation - for debug purposes:
            // bsXquery.execute(outputStream);
        } catch (IOException e) {
            logError(e);
            throw new BsxPreparedDtoException(e);
        }
    }

    protected final void logError(final Throwable e) {
        bsXquery.getCtx().getLogger().error("Query Exception: {}", ExceptionUtils.getRootCauseMessage(e));
        if (bsXquery.getCtx().getLogger().isDebugEnabled()) {
            try {
                if (bsXquery.getCtx().getLogger().isTraceEnabled()) {
                    bsXquery.getCtx().getLogger().trace("Query: {}", bsXquery.toString());
                    if (ExceptionUtils.getRootCause(e) != null &&
                            ExceptionUtils.getRootCause(e) instanceof NullPointerException) {
                        bsXquery.getCtx().getLogger().trace("NullPointerException may indicate an invalid mapping!");
                    }
                }
                Thread.sleep((long) (Math.random() * 2450));
                bsXquery.getCtx().getLogger().debug("Query result: {}", bsXquery.execute());
            } catch (InterruptedException | BaseXException e2) {
                ExcUtils.suppress(e2);
            }
        }
    }
}
