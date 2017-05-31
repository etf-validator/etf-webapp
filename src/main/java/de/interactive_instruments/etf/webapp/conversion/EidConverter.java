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
package de.interactive_instruments.etf.webapp.conversion;

import java.io.IOException;
import java.text.ParseException;
import java.util.Locale;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;

import org.springframework.core.convert.converter.Converter;

import de.interactive_instruments.SUtils;
import de.interactive_instruments.etf.model.EID;
import de.interactive_instruments.etf.model.EidFactory;
import de.interactive_instruments.etf.model.exceptions.IllegalEidException;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public final class EidConverter implements EtfConverter<EID> {

	// CASE_INSENSITIVE
	public static final String EID_PATTERN = "EID[0-9A-F]{8}-[0-9A-F]{4}-[1-5][0-9A-F]{3}-[89AB][0-9A-F]{3}-[0-9A-F]{12}";

	private static EidFactory eidFactory = EidFactory.getDefault();

	private static Deserializer deserializer = new Deserializer();
	private static Serializer serializer = new Serializer();

	public static EID toEid(final String eid) {
		if (SUtils.isNullOrEmpty(eid)) {
			throw new IllegalEidException("ID is null or empty");
		}
		final int length = eid.length();
		if (length == 39 && eid.startsWith("EID")) {
			return eidFactory.createAndPreserveStr(eid.substring(3));
		} else if (length == 36) {
			return eidFactory.createAndPreserveStr(eid);
		} else {
			throw new IllegalEidException(
					"Illegal identifier length (" + length + "): the ETF ID must be an 36 characters long "
							+ "hexadecimal Universally Unique Identifier "
							+ "or an UUID prefixed with 'EID'");
		}
	}

	public static String toStr(final EID eid) {
		return Objects.requireNonNull(eid, "Cannot convert empty ETF ID").getId();
	}

	@Override
	public EID parse(final String text, final Locale locale) throws ParseException {
		return toEid(text);
	}

	@Override
	public String print(final EID eid, final Locale locale) {
		return toStr(eid);
	}

	@Override
	public Class<EID> getType() {
		return EID.class;
	}

	@Override
	public JsonDeserializer<EID> jsonDeserializer() {
		return deserializer;
	}

	@Override
	public JsonSerializer<EID> jsonSerializer() {
		return serializer;
	}

	@Override
	public Converter<String, EID> typeToStrConverter() {
		return deserializer;
	}

	@Override
	public Converter<EID, String> strToTypeConverter() {
		return serializer;
	}

	private final static class Deserializer extends JsonDeserializer<EID> implements Converter<String, EID> {
		@Override
		public EID deserialize(final JsonParser jp, final DeserializationContext ctxt)
				throws IOException, JsonProcessingException {
			final JsonNode node = jp.getCodec().readTree(jp);
			return toEid(node.asText());
		}

		@Override
		public EID convert(final String source) {
			return toEid(source);
		}
	}

	private final static class Serializer extends JsonSerializer<EID> implements Converter<EID, String> {
		@Override
		public void serialize(final EID value, final JsonGenerator gen, final SerializerProvider serializers)
				throws IOException, JsonProcessingException {
			if (value != null) {
				gen.writeString(value.getId());
			}
		}

		@Override
		public String convert(final EID source) {
			return toStr(source);
		}
	}

}
