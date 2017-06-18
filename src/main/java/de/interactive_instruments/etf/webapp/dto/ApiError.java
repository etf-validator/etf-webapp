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
package de.interactive_instruments.etf.webapp.dto;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.i18n.LocaleContextHolder;

import de.interactive_instruments.etf.webapp.controller.LocalizableApiError;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "ApiError")
public class ApiError {

	private static final Logger logger = LoggerFactory.getLogger(ApiError.class);

	@ApiModelProperty(value = "Error message", example = "Error message")
	private final String error;

	@ApiModelProperty(value = "Timestamp in milliseconds, measured between the time the error occurred "
			+ "and midnight, January 1, 1970 UTC(coordinated universal time).", example = "1488469744783")
	private final String timestamp = String.valueOf(System.currentTimeMillis());

	@ApiModelProperty(value = "URL that was invoked before the error occured", example = "http://localhost:8080/v2/X")
	private final String url;

	@ApiModelProperty(value = "Optional error ID which was used to translate the error message", example = "l.invalid.fooBar")
	private final String id;

	@ApiModelProperty(value = "Optional stacktrace which will only be attached in ETF development mode")
	private final String[] stacktrace;

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
