/**
 * Copyright 2017-2019 European Union, interactive instruments GmbH
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

import org.apache.commons.io.IOUtils;
import org.slf4j.LoggerFactory;
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
import de.interactive_instruments.properties.PropertyUtils;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
@Service
public class StreamingService {

    @Autowired
    private ObjectMapperFactory objectMapperFactory;

    private ObjectMapper mapper;

    private long initCacheSize() {
        final long defaultVal = 30;
        final long size = PropertyUtils.getenvOrProperty("ETF_STREAMING_CACHE_SIZE", defaultVal);
        if (size != defaultVal) {
            LoggerFactory.getLogger(StreamingService.class).info("Streaming cache set to: {}", size);
        }
        return size;
    }

    private final Cache<String, byte[]> bigResponseCache = Caffeine.newBuilder().maximumSize(initCacheSize()).build();

    @PostConstruct
    void init() throws Exception {
        mapper = objectMapperFactory.getObject();
    }

    void asXml2(
            final Dao<? extends Dto> dao, final HttpServletRequest request, final HttpServletResponse response,
            final Filter filter)
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
            final OutputFormat json = dao.getOutputFormats().get(
                    EidFactory.getDefault().createUUID(dao.getDtoType().getSimpleName() + "DsResult2Json"));
            dao.getAll(filter).streamTo(json, null, byteCache);
            bigResponseCache.put(keyFor(dao, filter), byteCache.toByteArray());
        } catch (IOException e) {
            ExcUtils.suppress(e);
        }
    }

    void asJson2(
            final Dao<? extends Dto> dao, final HttpServletRequest request, final HttpServletResponse response,
            final Filter filter)
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
