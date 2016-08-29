/**
 * Copyright 2010-2016 interactive instruments GmbH
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
package de.interactive_instruments.etf.webapp.converters;

import java.text.ParseException;
import java.util.Locale;

import org.springframework.format.Formatter;

import de.interactive_instruments.etf.model.EID;
import de.interactive_instruments.etf.model.EidFactory;

/**
 * @author J. Herrmann ( herrmann <aT) interactive-instruments (doT> de )
 */
public final class EidTypeFormatter implements Formatter<EID> {

	@Override
	public EID parse(final String text, final Locale locale) throws ParseException {
		return EidFactory.getDefault().createUUID(text);
	}

	@Override
	public String print(final EID eid, final Locale locale) {
		return eid.getId();
	}

}
