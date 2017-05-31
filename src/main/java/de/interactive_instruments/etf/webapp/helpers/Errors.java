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
package de.interactive_instruments.etf.webapp.helpers;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
/*
public class Errors {

	public static void error(final String id) throws LocalizableApiError {
		throw new LocalizableApiError(id, false, 400);
	}

	public static void error(final String id, final boolean sensitive, final int code) throws LocalizableApiError {
		throw new LocalizableApiError.create(id, sensitive, code);
	}

	public static void error(final String id, final Exception e) throws LocalizableApiError {
		throw new LocalizableApiError.create(id, false, 500, e);
	}

	public static void error(final String id, final Object...arguments) throws LocalizableApiError {
		throw new LocalizableApiError.create(id, false, 404, arguments);
	}


	public static void wrapNthrow(final UnknownHostException e) throws LocalizableApiError {
		throw new LocalizableApiError.create("l.unknown.host", false, 400, e, e.getMessage() );
	}

	public static void wrapNthrow(final ComponentLoadingException e) throws LocalizableApiError {
		throw new LocalizableApiError.create(false, 500, e);
	}

	public static void wrapNthrow(final URISyntaxException e) throws LocalizableApiError {
		throw LocalizableApiError.create("l.invalid.url", false, 400, e, e.getInput());
	}

	public static void wrapNthrow(final UriUtils.UriNotAbsoluteException e) throws LocalizableApiError {
		throw LocalizableApiError.create("l.uri.noSchema", false, 400, e, e.getUri().toString());
	}

	public static void wrapNthrow(final ObjectWithIdNotFoundException e) throws LocalizableApiError {
		throw LocalizableApiError.create("l.object.with.eid.not.found", false, 404, e);
	}

	public static void wrapNthrow(final NoSuchFileException e) throws LocalizableApiError {
		throw LocalizableApiError.create(true, 500, e);
	}

	public static void wrapNthrow(final IOException e) throws LocalizableApiError {
		throw LocalizableApiError.create(true, 500, e);
	}

	public static void wrapNthrow(final ConfigurationException e) throws LocalizableApiError {
		throw LocalizableApiError.create(true, 500, e);
	}

}
*/
