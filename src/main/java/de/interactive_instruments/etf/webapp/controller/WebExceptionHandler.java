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
package de.interactive_instruments.etf.webapp.controller;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import de.interactive_instruments.etf.webapp.helpers.View;

/**
 * Handles all raised, uncaught exceptions.
 */
@ControllerAdvice(annotations = Controller.class)
class WebExceptionHandler {

	@Autowired
	private StatusController statusController;

	public static final String DEFAULT_ERROR_VIEW = "error";

	private final org.slf4j.Logger logger = LoggerFactory.getLogger(WebExceptionHandler.class);

	private static byte[] reserve = new byte[30 * 1024 * 1024]; // Reserve 30 MB

	private final static String contactAdminText = " This is a critical error and the system "
			+ "will try to prevent data loss by " + " switching into maintenance mode.";

	private ModelAndView createError(final Exception e, final String hint, final String url, boolean submitReport)
			throws Exception {
		if (AnnotationUtils.findAnnotation(e.getClass(), ResponseStatus.class) != null) {
			// Not associated with a view
			throw e;
		}

		final ModelAndView mav = new ModelAndView();
		mav.addObject("ex", e);
		if (hint != null) {
			mav.addObject("hint", hint);
		}
		mav.addObject("url", url);
		final UUID exceptionId = UUID.randomUUID();
		mav.addObject("exid", "EXID-" + exceptionId.toString());
		logger.error(
				"EXID-" + exceptionId.toString() + ": An exception occurred while trying to access \"" +
						url + "\"",
				e);
		mav.addObject("submitReport", submitReport && View.getSubmitAnalysisData().equals("true"));
		mav.setViewName(DEFAULT_ERROR_VIEW);
		return mav;
	}

	@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(value = Exception.class)
	public ModelAndView defaultErrorHandler(final HttpServletRequest request, final Exception e) throws Exception {

		if (e != null && e.getCause() != null && e.getCause() instanceof OutOfMemoryError) {
			// Recover the reserved memory
			reserve = null;
			System.gc();
		}

		final String hint;
		final boolean submitReport;
		if (e.getCause() != null && e.getCause() instanceof OutOfMemoryError) {
			hint = "The Java Virtual Machine is out of memory and"
					+ " no more memory could be made available. "
					+ " Ask your System Administrator to increase the Java heap space by "
					+ " adjusting the '-Xmx' parameter!" + contactAdminText;
			submitReport = false;
			statusController.triggerMaintenance();
		} else if (Objects.equals(e.getMessage(), "No space left on device")
				|| e.getCause() != null && e.getCause() instanceof IOException &&
						Objects.equals(e.getCause().getMessage(), "No space left on device")) {
			hint = "Disk space critical. Contact your system Administrator. " + contactAdminText;
			statusController.triggerMaintenance();
			submitReport = false;
		} else if (e.getCause() != null && e.getCause() instanceof StackOverflowError || e
				.getCause() instanceof StackOverflowError) {
			hint = "This is most likely a fatal bug in the application." + " Please report it!";
			submitReport = true;
		} else {
			hint = null;
			submitReport = true;
		}
		return createError(e, hint, request.getRequestURL().toString(), submitReport);
	}
}
