/**
 * Copyright 2017-2018 European Union, interactive instruments GmbH
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
package de.interactive_instruments.etf.webapp.dto;

import javax.xml.bind.annotation.*;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.i18n.LocaleContextHolder;

import de.interactive_instruments.etf.EtfConstants;
import de.interactive_instruments.etf.webapp.controller.LocalizableApiError;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "ApiError")
@XmlRootElement(name = "ApiError", namespace = EtfConstants.ETF_XMLNS)
@XmlAccessorType(XmlAccessType.FIELD)
public class ApiError {

	private static final Logger logger = LoggerFactory.getLogger(ApiError.class);

	@XmlElement(name = "error")
	@ApiModelProperty(value = "Error message", example = "Error message")
	private final String error;

	@ApiModelProperty(value = "Timestamp in milliseconds, measured between the time the error occurred "
			+ "and midnight, January 1, 1970 UTC(coordinated universal time).", example = "1488469744783")
	@XmlElement(name = "timestamp")
	private final String timestamp = String.valueOf(System.currentTimeMillis());

	@ApiModelProperty(value = "URL that was invoked before the error occured", example = "http://localhost:8080/v2/X")
	@XmlElement(name = "url")
	private final String url;

	@ApiModelProperty(value = "Optional error ID which was used to translate the error message", example = "l.invalid.fooBar")
	@XmlElement(name = "id")
	private final String id;

	@ApiModelProperty(value = "Optional stacktrace which will only be attached in ETF development mode")
	@XmlElementWrapper(name = "stacktrace")
	@XmlElement(name = "trace")
	private final String[] stacktrace;

	private ApiError() {
		// Ctor for JAXB
		error = null;
		url = null;
		id = null;
		stacktrace = null;
	}

	public ApiError(final Throwable e, final String url, final ApplicationContext applicationContext) {
		logger.error("EXID-" + timestamp + ": An exception occurred while trying to invoke \"" +
				url + "\"", e);
		final LocalizableApiError localizableApiError;
		if (e instanceof LocalizableApiError) {
			localizableApiError = (LocalizableApiError) e;
		} else if (e.getCause() instanceof LocalizableApiError) {
			localizableApiError = (LocalizableApiError) e.getCause();
		} else {
			localizableApiError = null;
		}
		if (localizableApiError != null) {
			this.id = localizableApiError.getId();
			final String err = applicationContext.getMessage(localizableApiError.getId(),
					localizableApiError.getArgumentValueArr(), null,
					// localizableApiError.getUserLocale());
					LocaleContextHolder.getLocale());
			if (err == null) {
				// Unknown
				this.error = ExceptionUtils.getRootCause(e).getMessage();
			} else {
				this.error = err;
			}
		} else {
			this.id = null;
			if (e != null) {
				final Throwable rootCause = ExceptionUtils.getRootCause(e);
				this.error = rootCause != null ? rootCause.getMessage() : e.getMessage();
			} else {
				this.error = "Internal error";
			}
		}
		stacktrace = ExceptionUtils.getRootCauseStackTrace(e);
		this.url = url;
	}

	public String getError() {
		return error;
	}

	public String getUrl() {
		return url;
	}

	public String getId() {
		return id;
	}

	public String[] getStacktrace() {
		return stacktrace;
	}
}
