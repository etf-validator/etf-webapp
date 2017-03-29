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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import de.interactive_instruments.etf.webapp.helpers.SimpleFilter;
import de.interactive_instruments.properties.PropertyUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import de.interactive_instruments.etf.dal.dao.Dao;
import de.interactive_instruments.etf.dal.dao.Filter;
import de.interactive_instruments.etf.dal.dto.Dto;
import de.interactive_instruments.etf.model.EidFactory;
import de.interactive_instruments.etf.model.OutputFormat;
import de.interactive_instruments.etf.webapp.conversion.EidConverter;
import de.interactive_instruments.etf.webapp.conversion.ObjectMapperFactory;
import de.interactive_instruments.etf.webapp.helpers.CacheControl;
import de.interactive_instruments.exceptions.ExcUtils;
import de.interactive_instruments.exceptions.ObjectWithIdNotFoundException;
import de.interactive_instruments.exceptions.StorageException;

/**
 * @author J. Herrmann ( herrmann <aT) interactive-instruments (doT> de )
 */
@Service
public class StreamingService {

	@Autowired
	private ObjectMapperFactory objectMapperFactory;

	private ObjectMapper mapper;

	private Cache<String, byte[]> bigResponseCache = Caffeine.newBuilder().maximumSize(
			PropertyUtils.getenvOrProperty("ETF_STREAMING_CACHE_SIZE",30)).build();

	@PostConstruct
	void init() throws Exception {
		mapper = objectMapperFactory.getObject();
	}

	void asXml2(
			final Dao<? extends Dto> dao, final HttpServletRequest request, final HttpServletResponse response, final Filter filter)
			throws IOException, ObjectWithIdNotFoundException, StorageException {
		if (CacheControl.clientNeedsUpdate(dao, request, response)) {
			final ServletOutputStream out = response.getOutputStream();
			response.setContentType(MediaType.TEXT_XML_VALUE);
			final OutputFormat xml = dao.getOutputFormats()
					.get(EidFactory.getDefault().createUUID(dao.getDtoType().getSimpleName() + "DsResult2Xml"));
			dao.getAll(filter).streamTo(xml, null, out);
		}
	}

	void asXml2(
			final Dao<? extends Dto> dao, final HttpServletRequest request, final HttpServletResponse response, final String id)
			throws IOException, ObjectWithIdNotFoundException, StorageException {
		if (CacheControl.clientNeedsUpdate(dao, request, response)) {
			final ServletOutputStream out = response.getOutputStream();
			response.setContentType(MediaType.TEXT_XML_VALUE);
			final OutputFormat xml = dao.getOutputFormats()
					.get(EidFactory.getDefault().createUUID(dao.getDtoType().getSimpleName() + "DsResult2Xml"));
			dao.getById(EidConverter.toEid(id)).streamTo(xml, null, out);
		}
	}

	private static String keyFor(final Dao<? extends Dto> dao, final Filter filter) {
		final StringBuilder k = new StringBuilder(dao.getId());
		k.append(".").append(dao.getLastModificationDate());
		k.append(".").append(filter.offset());
		k.append(".").append(filter.limit());
		k.append(".").append(filter.fields());
		return k.toString();
	}

	public void prepareCache(final Dao<? extends Dto> dao, final Filter filter) {
		try (ByteArrayOutputStream byteCache = new ByteArrayOutputStream()) {
			try {
				final OutputFormat json = dao.getOutputFormats().get(
						EidFactory.getDefault().createUUID(dao.getDtoType().getSimpleName() + "DsResult2Json"));
				dao.getAll(new SimpleFilter(0, 0)).streamTo(json, null, byteCache);
				bigResponseCache.put(keyFor(dao, filter), byteCache.toByteArray());
			} catch (StorageException | IOException e) {
				ExcUtils.suppress(e);
			}
		} catch (IOException e) {
			ExcUtils.suppress(e);
		}
	}

	void asJson2(
			final Dao<? extends Dto> dao, final HttpServletRequest request, final HttpServletResponse response, final Filter filter)
			throws IOException, ObjectWithIdNotFoundException, StorageException {
		if (CacheControl.clientNeedsUpdate(dao, request, response)) {
			final ServletOutputStream out = response.getOutputStream();
			response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);

			// Check if response is in cache
			final String k = keyFor(dao, filter);
			byte[] preparedResponse = bigResponseCache.getIfPresent(k);
			if (preparedResponse == null) {
				// save in cache
				try (ByteArrayOutputStream byteCache = new ByteArrayOutputStream()) {
					final OutputFormat json = dao.getOutputFormats()
							.get(EidFactory.getDefault().createUUID(dao.getDtoType().getSimpleName() + "DsResult2Json"));
					dao.getAll(filter).streamTo(json, null, byteCache);
					preparedResponse = byteCache.toByteArray();
					bigResponseCache.put(k, preparedResponse);
				}
			}
			try (ByteArrayInputStream byteStream = new ByteArrayInputStream(preparedResponse)) {
				IOUtils.copy(byteStream, out);
			}
		}
	}

	void asJson2(
			final Dao<? extends Dto> dao, final HttpServletRequest request, final HttpServletResponse response, final String id)
			throws IOException, ObjectWithIdNotFoundException, StorageException {
		if (CacheControl.clientNeedsUpdate(dao, request, response)) {
			final ServletOutputStream out = response.getOutputStream();
			response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
			final OutputFormat json = dao.getOutputFormats()
					.get(EidFactory.getDefault().createUUID(dao.getDtoType().getSimpleName() + "DsResult2Json"));
			dao.getById(EidConverter.toEid(id)).streamTo(json, null, out);
		}
	}

	void asJson2(
			final Dto dto, final HttpServletResponse response)
			throws IOException, ObjectWithIdNotFoundException, StorageException {
		final ServletOutputStream out = response.getOutputStream();
		response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
		mapper.writeValue(out, dto);
	}
}
