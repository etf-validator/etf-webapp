/**
 * Copyright 2017 European Union, interactive instruments GmbH
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This work was supported by the EU Interoperability Solutions for
 * European Public Administrations Programme (http://ec.europa.eu/isa)
 * through Action 1.17: A Reusable INSPIRE Reference Platform (ARE3NA).
 */
package de.interactive_instruments.etf.webapp.controller;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.NoSuchFileException;

import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import org.apache.commons.fileupload.FileUploadBase;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;

import de.interactive_instruments.UriUtils;
import de.interactive_instruments.etf.LocalizableError;
import de.interactive_instruments.etf.component.ComponentLoadingException;
import de.interactive_instruments.etf.detector.IncompatibleTestObjectTypeException;
import de.interactive_instruments.etf.detector.TestObjectTypeNotDetected;
import de.interactive_instruments.etf.webapp.dto.StartTestRunRequest;
import de.interactive_instruments.exceptions.ObjectWithIdNotFoundException;
import de.interactive_instruments.exceptions.StorageException;
import de.interactive_instruments.exceptions.config.ConfigurationException;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
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

	public LocalizableApiError(final FieldError fieldError) {
		super(fieldError.getDefaultMessage());
		sensitiveInformation = false;
		sc = 400;
	}

	public LocalizableApiError(final ConstraintViolation<StartTestRunRequest> violation) {
		super(violation.getMessage());
		sensitiveInformation = false;
		sc = 400;
	}

	public LocalizableApiError(final HttpMessageNotReadableException e) {
		super(e.getMessage().contains("Required request body is missing:") ? "l.json.request.body.missing" : "", e);
		sensitiveInformation = false;
		sc = 400;
	}

	public LocalizableApiError(final FileUploadBase.SizeLimitExceededException exception) {
		super("l.max.upload.size.exceeded");
		sensitiveInformation = false;
		sc = 413;
	}

	public LocalizableApiError(final TestObjectTypeNotDetected e) {
		super("l.testObject.type.not.detected", e);
		sensitiveInformation = false;
		sc = 400;
	}

	public LocalizableApiError(final IncompatibleTestObjectTypeException e) {
		super("l.testObject.type.incomaptible", e, e.getDetectedTestObjectType().getLabel());
		sensitiveInformation = false;
		sc = 400;
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

	public LocalizableApiError(final UriUtils.UriNotAnHttpAddressException e) {
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
		super("l.json.mapping.error", e,
				e.getLocation().getLineNr(),
				e.getLocation().getColumnNr(),
				e.getPath().get(0).getFieldName(),
				e.getPath().get(0).getFrom().getClass().getSimpleName(),
				e.getMessage().indexOf("\n at [") != 0 ? e.getMessage().substring(0, e.getMessage().indexOf("\n at ["))
						: "unknown"

		);
		sensitiveInformation = false;
		sc = 404;
	}

	public LocalizableApiError(final JsonParseException e) {
		super("l.json.parse.error", e,
				e.getLocation().getLineNr(),
				e.getLocation().getColumnNr(),
				e.getOriginalMessage());
		sensitiveInformation = false;
		sc = 404;
	}

	public LocalizableApiError(final NoSuchFileException e) {
		super("l.json.parse.error", e);
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
