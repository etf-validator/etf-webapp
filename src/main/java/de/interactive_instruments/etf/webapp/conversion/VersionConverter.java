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

import de.interactive_instruments.Version;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public class VersionConverter implements EtfConverter<Version> {

	private static Deserializer deserializer = new Deserializer();
	private static Serializer serializer = new Serializer();

	public static Version toVersion(final String version) {
		return Version.parse(version);
	}

	public static String toStr(final Version version) {
		return Objects.requireNonNull(version, "Cannot convert empty Version").getAsString();
	}

	@Override
	public Version parse(final String text, final Locale locale) throws ParseException {
		return toVersion(text);
	}

	@Override
	public String print(final Version version, final Locale locale) {
		return toStr(version);
	}

	@Override
	public Class<Version> getType() {
		return Version.class;
	}

	@Override
	public JsonDeserializer<Version> jsonDeserializer() {
		return deserializer;
	}

	@Override
	public JsonSerializer<Version> jsonSerializer() {
		return serializer;
	}

	@Override
	public Converter<String, Version> typeToStrConverter() {
		return deserializer;
	}

	@Override
	public Converter<Version, String> strToTypeConverter() {
		return serializer;
	}

	private final static class Deserializer extends JsonDeserializer<Version> implements Converter<String, Version> {
		@Override
		public Version deserialize(final JsonParser jp, final DeserializationContext ctxt)
				throws IOException, JsonProcessingException {
			final JsonNode node = jp.getCodec().readTree(jp);
			return new Version(node.asText());
		}

		@Override
		public Version convert(final String source) {
			return toVersion(source);
		}
	}

	private final static class Serializer extends JsonSerializer<Version> implements Converter<Version, String> {
		@Override
		public void serialize(final Version value, final JsonGenerator gen, final SerializerProvider serializers)
				throws IOException, JsonProcessingException {
			if (value != null) {
				gen.writeString(value.getAsString());
			}
		}

		@Override
		public String convert(final Version source) {
			return toStr(source);
		}
	}
}
