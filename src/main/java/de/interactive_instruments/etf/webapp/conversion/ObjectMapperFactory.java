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
package de.interactive_instruments.etf.webapp.conversion;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.web.util.HtmlUtils;

import de.interactive_instruments.*;
import de.interactive_instruments.etf.dal.dto.Dto;
import de.interactive_instruments.etf.dal.dto.ModelItemDto;
import de.interactive_instruments.etf.dal.dto.capabilities.TestObjectDto;
import de.interactive_instruments.etf.dal.dto.test.ExecutableTestSuiteDto;
import de.interactive_instruments.etf.dal.dto.translation.TranslationTemplateDto;
import de.interactive_instruments.etf.model.EID;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public class ObjectMapperFactory implements FactoryBean<ObjectMapper> {

	private final ObjectMapper mapper = new ObjectMapper();

	// Base Filter
	@JsonFilter("baseFilter")
	@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "t")
	@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
	@JsonIgnoreProperties(value = {"proxiedId", "cached", "descriptiveLabel", "typeName", "versionAsStr"})
	private static abstract class BaseMixin {
		@JsonManagedReference
		EID id;

		@JsonBackReference
		protected ModelItemDto parent;
	}

	private final SimpleBeanPropertyFilter baseFilter = SimpleBeanPropertyFilter.serializeAllExcept(
			"proxiedId", "cached", "descriptiveLabel", "typeName", "versionAsStr");

	// Translation Template Filter
	@JsonFilter("translationTemplateFilter")
	@JsonRootName("TranslationTemplate")
	private static abstract class TranslationTemplateMixin extends BaseMixin {}

	final SimpleBeanPropertyFilter translationTemplateFilter = SimpleBeanPropertyFilter.serializeAllExcept(
			"strWithTokens");

	// Executable Test Suite Filter
	@JsonFilter("etsFilter")
	@JsonRootName("ExecutableTestSuite")
	private static class ExecutableTestSuiteDtoMixin extends BaseMixin {
		@JsonBackReference
		private List<ExecutableTestSuiteDto> dependencies;
	}

	@JsonFilter("testObjectFilter")
	@JsonRootName("TestObject")
	private static class TestObjectDtoMixin extends BaseMixin {}

	final SimpleBeanPropertyFilter testObjectFilter = SimpleBeanPropertyFilter.serializeAllExcept(
			"resourceNames", "resourceCollection", "resourcesSize", "");

	private final SimpleBeanPropertyFilter etsFilter = SimpleBeanPropertyFilter.serializeAllExcept("assertionsSize");

	private static class HTMLCharacterEscapes extends CharacterEscapes {
		private final int[] asciiEscapes;

		public HTMLCharacterEscapes() {
			asciiEscapes = CharacterEscapes.standardAsciiEscapesForJSON();
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

		@Override
		public SerializableString getEscapeSequence(int ch) {
			return new SerializedString("\\u" + String.format("%04x", ch));
		}
	}

	public static class JsonHtmlXssDeserializer extends JsonDeserializer<String> {
		@Override
		public String deserialize(final JsonParser jp, final DeserializationContext ctxt)
				throws IOException, JsonProcessingException {
			final JsonNode node = jp.getCodec().readTree(jp);
			return HtmlUtils.htmlEscape(node.asText());
		}
	}

	public ObjectMapperFactory() {

		mapper.addMixIn(ModelItemDto.class, BaseMixin.class);
		mapper.addMixIn(Dto.class, BaseMixin.class);
		mapper.addMixIn(ExecutableTestSuiteDto.class, ExecutableTestSuiteDtoMixin.class);
		mapper.addMixIn(TranslationTemplateDto.class, TranslationTemplateMixin.class);
		mapper.addMixIn(TestObjectDto.class, TestObjectDtoMixin.class);

		// important!
		mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

		final FilterProvider filters = new SimpleFilterProvider().setDefaultFilter(baseFilter)
				.addFilter("translationTemplateFilter", translationTemplateFilter).addFilter("etsFilter", etsFilter)
				.addFilter("testObjectFilter", testObjectFilter);
		mapper.setFilterProvider(filters);
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

		final SimpleModule etfModule = new SimpleModule("EtfModule",
				new Version(1, 0, 0, null,
						"de.interactive_instruments", "etf"));

		etfModule.addSerializer(EID.class, new EidConverter().jsonSerializer());
		etfModule.addDeserializer(EID.class, new EidConverter().jsonDeserializer());

		etfModule.addSerializer(de.interactive_instruments.Version.class, new VersionConverter().jsonSerializer());
		etfModule.addDeserializer(de.interactive_instruments.Version.class, new VersionConverter().jsonDeserializer());
		// Prevent XSS
		etfModule.addDeserializer(String.class, new JsonHtmlXssDeserializer());

		mapper.registerModule(etfModule);

		mapper.getFactory().setCharacterEscapes(new HTMLCharacterEscapes());
	}

	@Override
	public ObjectMapper getObject() throws Exception {
		return mapper;
	}

	@Override
	public Class<?> getObjectType() {
		return ObjectMapper.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}
}
