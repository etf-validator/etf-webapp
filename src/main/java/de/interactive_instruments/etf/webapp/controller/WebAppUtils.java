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
package de.interactive_instruments.etf.webapp.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import org.springframework.http.MediaType;

import de.interactive_instruments.SUtils;
import de.interactive_instruments.etf.dal.dao.Dao;
import de.interactive_instruments.etf.dal.dao.Filter;
import de.interactive_instruments.etf.dal.dto.Dto;
import de.interactive_instruments.etf.dal.dto.ModelItemDto;
import de.interactive_instruments.etf.dal.dto.ModelItemTreeNode;
import de.interactive_instruments.etf.dal.dto.test.ExecutableTestSuiteDto;
import de.interactive_instruments.etf.model.EID;
import de.interactive_instruments.etf.model.EidFactory;
import de.interactive_instruments.etf.model.OutputFormat;
import de.interactive_instruments.etf.webapp.helpers.View;
import de.interactive_instruments.exceptions.ObjectWithIdNotFoundException;
import de.interactive_instruments.exceptions.StorageException;

/**
 * @author J. Herrmann ( herrmann <aT) interactive-instruments (doT> de )
 */
public class WebAppUtils {

	private final static ObjectMapper objectMapper = initObjectMapper();

	@JsonFilter("parentModelItemTreeNodeFilter")
	private interface ModelItemTreeNodeMixin {
		@JsonIgnore
		ModelItemDto getParent();
	}

	@JsonFilter("dtoFilter")
	private static abstract class DtoMixin {
		@JsonIgnore
		EID id;

		@JsonIgnore
		public abstract String getDescriptiveLabel();

		@JsonInclude
		public String getId() {
			if (id != null) {
				return id.getId();
			}
			return null;
		}
	}

	@JsonFilter("etsDependencyFilter")
	private static class ExecutableTestSuiteDtoMixin {
		@JsonIgnore
		private List<ExecutableTestSuiteDto> dependencies;

		@JsonInclude
		public Collection<String> getDependencies() {
			if (dependencies != null) {
				return dependencies.stream().filter(dependency -> dependency.getId() != null).map(dependency -> dependency.getId().getId()).collect(Collectors.toList());
			}
			return null;
		}
	}

	private static ObjectMapper initObjectMapper() {
		final ObjectMapper mapper = new ObjectMapper();

		mapper.addMixIn(ModelItemTreeNode.class, ModelItemTreeNodeMixin.class);
		final SimpleBeanPropertyFilter parentFilter = SimpleBeanPropertyFilter.serializeAllExcept("parent");

		mapper.addMixIn(Dto.class, DtoMixin.class);
		final SimpleBeanPropertyFilter dtoFilter = SimpleBeanPropertyFilter.serializeAllExcept("id", "descriptiveLabel");

		mapper.addMixIn(ExecutableTestSuiteDto.class, ExecutableTestSuiteDtoMixin.class);
		final SimpleBeanPropertyFilter etsDepsFilter = SimpleBeanPropertyFilter.serializeAllExcept("dependencies");

		final FilterProvider filters = new SimpleFilterProvider().addFilter("etsDependencyFilter", etsDepsFilter).addFilter("dtoFilter", dtoFilter).addFilter("parentModelItemTreeNodeFilter", parentFilter);
		mapper.setFilterProvider(filters);

		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		return mapper;
	}

	public static final Filter ALL_FILTER = new Filter() {

		@Override
		public int offset() {
			return 0;
		}

		@Override
		public int limit() {
			return 2000;
		}
	};

	public static EID toEid(final String eid) {
		if (SUtils.isNullOrEmpty(eid)) {
			throw new IllegalArgumentException("ID is null or empty");
		}
		if (eid.length() == 39 && eid.startsWith("EID")) {
			return EidFactory.getDefault().createAndPreserveStr(eid.substring(3));
		} else if (eid.length() == 36) {
			return EidFactory.getDefault().createAndPreserveStr(eid);
		} else {
			throw new IllegalArgumentException(""
					+ "The ID parameter must be an 36 characters long Universally Unique Identifier or an UUID prefixed with 'EID'");
		}
	}

	public static final String API_BASE_URL = "v2";

	public static void streamAsXml2(
			final Dao dao, final OutputFormat outputFormat, final HttpServletResponse response, final String id)
			throws IOException, ObjectWithIdNotFoundException, StorageException {
		final ServletOutputStream out = response.getOutputStream();
		response.setContentType(MediaType.TEXT_XML_VALUE);
		if (id == null) {
			dao.getAll(ALL_FILTER).streamTo(outputFormat, null, out);
		} else {
			dao.getById(WebAppUtils.toEid(id)).streamTo(outputFormat, null, out);
		}
	}

	public static void streamAsJson2(
			final Dao dao, final HttpServletResponse response, final String id)
			throws IOException, ObjectWithIdNotFoundException, StorageException {
		final ServletOutputStream out = response.getOutputStream();
		response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
		if (id == null) {
			objectMapper.writeValue(out, dao.getAll(ALL_FILTER).asCollection());
		} else {
			objectMapper.writeValue(out, dao.getById(WebAppUtils.toEid(id)).getDto());
		}
	}
}
