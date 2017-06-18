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
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import de.interactive_instruments.etf.model.exceptions.IllegalEidException;
import de.interactive_instruments.etf.webapp.dto.ApiError;
import de.interactive_instruments.exceptions.ObjectWithIdNotFoundException;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Handles exceptions thrown by RestControllers
 */
@ControllerAdvice(annotations = {RestController.class, Component.class})
class RestExceptionHandler {

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private StatusController statusController;

	private final static org.slf4j.Logger logger = LoggerFactory.getLogger(RestExceptionHandler.class);

	private static byte[] reserve = new byte[30 * 1024 * 1024]; // Reserve 30 MB

	@ExceptionHandler(value = Exception.class)
	@ResponseBody
	public ApiError defaultErrorHandler(final HttpServletRequest request, final HttpServletResponse response,
			final Exception exception) {

		if (exception != null && exception.getCause() instanceof OutOfMemoryError) {
			// Recover the reserved memory
			reserve = null;
			System.gc();
			statusController.triggerMaintenance();
			return new ApiError(exception, request.getRequestURL().toString(), applicationContext);
		} else if (Objects.equals(exception.getMessage(), "No space left on device")
				|| exception.getCause() != null && exception.getCause() instanceof IOException &&
						Objects.equals(exception.getCause().getMessage(), "No space left on device")) {
			statusController.triggerMaintenance();
			return new ApiError(exception, request.getRequestURL().toString(), applicationContext);
		} else if (exception.getCause() != null && exception.getCause() instanceof StackOverflowError
				|| exception.getCause() instanceof StackOverflowError) {
			statusController.triggerMaintenance();
			return new ApiError(exception, request.getRequestURL().toString(), applicationContext);
		}
		if (exception instanceof IllegalEidException) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		} else if (exception instanceof ObjectWithIdNotFoundException) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		} else if (exception.getCause() instanceof LocalizableApiError) {
			response.setStatus(((LocalizableApiError) exception.getCause()).getStatus());
		} else if (exception.getCause() instanceof JsonMappingException) {
			final Throwable e = new LocalizableApiError((JsonMappingException) exception.getCause());
			return new ApiError(e, request.getRequestURL().toString(), applicationContext);
		} else if (exception.getCause() instanceof JsonParseException) {
			final Throwable e = new LocalizableApiError((JsonParseException) exception.getCause());
			return new ApiError(e, request.getRequestURL().toString(), applicationContext);
		} else if (exception instanceof HttpMessageNotReadableException) {
			final Throwable e = new LocalizableApiError((HttpMessageNotReadableException) exception);
			return new ApiError(e, request.getRequestURL().toString(), applicationContext);
		} else if (exception instanceof FileUploadBase.SizeLimitExceededException) {
			final Throwable e = new LocalizableApiError((FileUploadBase.SizeLimitExceededException) exception);
			return new ApiError(e, request.getRequestURL().toString(), applicationContext);
		} else {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		return new ApiError(exception, request.getRequestURL().toString(), applicationContext);
	}
}
