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

import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.FactoryBean;

/**
 * @author J. Herrmann ( herrmann <aT) interactive-instruments (doT> de )
 */
public class HtmlEscapingObjectMapperFactory implements FactoryBean<ObjectMapper> {

	private final ObjectMapper objectMapper;

	public HtmlEscapingObjectMapperFactory() {
		objectMapper = new ObjectMapper();
		objectMapper.getFactory().setCharacterEscapes(new HTMLCharacterEscapes());
	}

	@Override
	public ObjectMapper getObject() throws Exception {
		return objectMapper;
	}

	@Override
	public Class<?> getObjectType() {
		return ObjectMapper.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	public static class HTMLCharacterEscapes extends CharacterEscapes {

		private final int[] asciiEscapes;

		public HTMLCharacterEscapes() {
			// start with set of characters known to require escaping (double-quote, backslash etc)
			asciiEscapes = CharacterEscapes.standardAsciiEscapesForJSON();
			// and force escaping of a few others:
			asciiEscapes['<'] = CharacterEscapes.ESCAPE_CUSTOM;
			asciiEscapes['>'] = CharacterEscapes.ESCAPE_CUSTOM;
			asciiEscapes['&'] = CharacterEscapes.ESCAPE_CUSTOM;
			asciiEscapes['"'] = CharacterEscapes.ESCAPE_CUSTOM;
			asciiEscapes['\''] = CharacterEscapes.ESCAPE_CUSTOM;
		}

		@Override
		public int[] getEscapeCodesForAscii() {
			return asciiEscapes;
		}

		// and this for others; we don't need anything special here
		@Override
		public SerializableString getEscapeSequence(int ch) {
			return new SerializedString(StringEscapeUtils.escapeHtml4(Character.toString((char) ch)));

		}
	}
}
