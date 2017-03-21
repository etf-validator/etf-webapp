/**
 * Copyright 2010-2017 interactive instruments GmbH
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
package de.interactive_instruments.etf.webapp.controller;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.file.NoSuchFileException;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.JsonMappingException;
import de.interactive_instruments.UriUtils;
import de.interactive_instruments.etf.LocalizableError;
import de.interactive_instruments.etf.component.ComponentLoadingException;
import de.interactive_instruments.exceptions.ObjectWithIdNotFoundException;
import de.interactive_instruments.exceptions.StorageException;
import de.interactive_instruments.exceptions.config.ConfigurationException;

/**
 * @author J. Herrmann ( herrmann <aT) interactive-instruments (doT> de )
 */
public class LocalizableApiError extends LocalizableError {
	protected final boolean sensitiveInformation;
	protected final int sc;

	public LocalizableApiError(final String id, final boolean sensitive, final int code) {
		super(id);
		sensitiveInformation = sensitive;
		sc = code;
	}

	public LocalizableApiError(final String id, final boolean sensitive, final int code, final Exception e) {
		super(id, e);
		sensitiveInformation = sensitive;
		sc = code;
	}

	public LocalizableApiError(final String id, final Exception e) {
		super(id, e);
		sensitiveInformation = false;
		sc = 400;
	}

	public LocalizableApiError(final String id, final boolean sensitive, final int code, final Object... arguments) {
		super(id, arguments);
		sensitiveInformation = sensitive;
		sc = code;
	}

	public LocalizableApiError(final String id, final boolean sensitive, final int code, final Exception e,
			final Object... arguments) {
		super(id, e, arguments);
		sensitiveInformation = sensitive;
		sc = code;
	}

	public LocalizableApiError(final boolean sensitive, final int code, final Exception e) {
		super(e);
		sensitiveInformation = sensitive;
		sc = code;
	}

	public boolean isSensitiveInformation() {
		return sensitiveInformation;
	}

	public int getStatus() {
		return sc;
	}

	void setError(final HttpServletResponse response) {
		if (sc != 0) {
			response.setStatus(sc);
		}
	}

	public LocalizableApiError(final ComponentLoadingException e) {
		super(e);
		sensitiveInformation = false;
		sc = 500;
	}

	public LocalizableApiError(final URISyntaxException e) {
		super("l.invalid.url", e, e.getInput());
		sensitiveInformation = false;
		sc = 400;
	}

	public LocalizableApiError(final UriUtils.UriNotAbsoluteException e) {
		super("l.uri.noSchema", e, e.getUri().toString());
		sensitiveInformation = false;
		sc = 400;
	}

	public LocalizableApiError(final ObjectWithIdNotFoundException e) {
		super("l.object.with.eid.not.found", e);
		sensitiveInformation = false;
		sc = 404;
	}



	public LocalizableApiError(final JsonMappingException e) {
		super("l.json.parse.error", e,
				e.getLocation().getLineNr(),
				e.getLocation().getColumnNr(),
				e.getPath().get(0).getFieldName(),
				e.getPath().get(0).getFrom().getClass().getSimpleName(),
				e.getMessage().indexOf("\n at [")!=0 ?
						e.getMessage().substring(0, e.getMessage().indexOf("\n at [")) : "unknown"

		);
		sensitiveInformation = false;
		sc = 404;
	}



	public LocalizableApiError(final NoSuchFileException e) {
		super(e);
		sensitiveInformation = true;
		sc = 500;
	}

	public LocalizableApiError(final IOException e) {
		super(e);
		sensitiveInformation = true;
		sc = 500;
	}

	public LocalizableApiError(final ConfigurationException e) {
		super(e);
		sensitiveInformation = true;
		sc = 500;
	}

	public LocalizableApiError(final StorageException e) {
		super(e);
		sensitiveInformation = true;
		sc = 500;
	}
}
