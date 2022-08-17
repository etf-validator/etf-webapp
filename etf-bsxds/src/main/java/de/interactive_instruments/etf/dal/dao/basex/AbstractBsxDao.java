/**
 * Copyright 2010-2022 interactive instruments GmbH
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
package de.interactive_instruments.etf.dal.dao.basex;

import static de.interactive_instruments.etf.dal.dao.basex.BsxDataStorage.ETF_NAMESPACE_DECL;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import javax.xml.transform.TransformerConfigurationException;

import org.apache.commons.io.IOUtils;
import org.basex.core.BaseXException;
import org.basex.core.cmd.XQuery;

import de.interactive_instruments.IFile;
import de.interactive_instruments.SUtils;
import de.interactive_instruments.etf.dal.dao.Dao;
import de.interactive_instruments.etf.dal.dao.Filter;
import de.interactive_instruments.etf.dal.dao.PreparedDto;
import de.interactive_instruments.etf.dal.dao.PreparedDtoCollection;
import de.interactive_instruments.etf.dal.dao.exceptions.RetrieveException;
import de.interactive_instruments.etf.dal.dto.Dto;
import de.interactive_instruments.etf.model.DefaultEidMap;
import de.interactive_instruments.etf.model.EID;
import de.interactive_instruments.etf.model.EidMap;
import de.interactive_instruments.etf.model.OutputFormat;
import de.interactive_instruments.exceptions.InitializationException;
import de.interactive_instruments.exceptions.InvalidStateTransitionException;
import de.interactive_instruments.exceptions.ObjectWithIdNotFoundException;
import de.interactive_instruments.exceptions.StorageException;
import de.interactive_instruments.exceptions.config.ConfigurationException;
import de.interactive_instruments.properties.ConfigProperties;
import de.interactive_instruments.properties.ConfigPropertyHolder;

/**
 * BaseX based Data Access Object for read only operations
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
abstract class AbstractBsxDao<T extends Dto> implements Dao<T> {

    protected final TQuery tQuery;
    protected final BsxDsCtx ctx;
    protected final String xqueryStatement;
    protected final EidMap<OutputFormat> outputFormatIdMap = new DefaultEidMap<>();
    protected final Map<String, OutputFormat> outputFormatLabelMap = new HashMap<>();
    protected ConfigProperties configProperties;
    protected boolean initialized = false;
    private final GetDtoResultCmd getDtoResultCmd;
    protected long lastModificationDate = System.currentTimeMillis();

    protected AbstractBsxDao(final TQuery tQuery,
            final BsxDsCtx ctx,
            final GetDtoResultCmd<T> getDtoResultCmd) throws StorageException {
        this.tQuery = tQuery;
        this.ctx = ctx;
        this.getDtoResultCmd = getDtoResultCmd;
        try {
            xqueryStatement = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(
                    "xquery/" + tQuery.typeName + "-xdb.xquery"), "UTF-8");
        } catch (NullPointerException | IOException e) {
            throw new StorageException("Could not load XQuery resource for " + tQuery.typeName, e);
        }
    }

    protected void ensureType(final T t) {
        if (!this.getDtoType().isAssignableFrom(t.getClass())) {
            throw new IllegalArgumentException(
                    "Item " + t.getDescriptiveLabel() + " is not of type " + this.getDtoType().getSimpleName());
        }
        if (t.getId() == null) {
            throw new IllegalArgumentException(
                    "Item " + t.getClass().getName() + t.hashCode() + " has no ID");
        }
    }

    @Override
    public String getId() {
        return this.getClass().getSimpleName();
    }

    protected final IFile getFile(final EID eid) {
        final IFile file = ctx.getStoreDir().secureExpandPathDown(
                tQuery.typeName + "-" + BsxDataStorage.ID_PREFIX + eid.getId() + ".xml");
        return file;
    }

    protected final List<IFile> getFiles(final Collection<T> dtos) {
        final String filePathPrefix = ctx.getStoreDir() + File.separator + tQuery.typeName + "-" + BsxDataStorage.ID_PREFIX;
        final List<IFile> files = new ArrayList<>(dtos.size());
        files.addAll(dtos.stream().map(dto -> new IFile(filePathPrefix + dto.getId() + ".xml")).collect(Collectors.toList()));
        return files;
    }

    @Override
    public boolean exists(final EID eid) {
        return getFile(eid).exists();
    }

    @Override
    public boolean isDisabled(final EID eid) {
        if (!exists(eid)) {
            return false;
        }
        try {
            final StringBuilder query = new StringBuilder(ETF_NAMESPACE_DECL
                    + tQuery.dataBaseQuery);
            query.append(tQuery.typeQueryPath);
            query.append("[@id = 'EID");
            query.append(eid.toString());
            query.append("']/etf:disabled='true'");
            final String result = new XQuery(query.toString()).execute(ctx.getBsxCtx());
            return "true".equals(result);
        } catch (final BaseXException e) {
            throw new IllegalStateException("Internal error in isDisabled(), ", e);
        }
    }

    @Override
    public ConfigPropertyHolder getConfigurationProperties() {
        if (configProperties == null) {
            this.configProperties = new ConfigProperties();
        }
        return configProperties;
    }

    @Override
    public void init() throws ConfigurationException, InitializationException, InvalidStateTransitionException {
        if (initialized) {
            throw new InvalidStateTransitionException(getClass().getSimpleName() + " is already initialized");
        }

        if (configProperties != null) {
            configProperties.expectAllRequiredPropertiesSet();
        }
        try {
            // XML
            final XsltOutputTransformer xmlItemCollectionTransformer = new XsltOutputTransformer(
                    this, "DsResult2Xml", "text/xml", "xslt/DsResult2Xml.xsl");
            initAndAddTransformer(xmlItemCollectionTransformer);

            // JSON
            final XsltOutputTransformer jsonItemCollectionTransformer = new XsltOutputTransformer(
                    this, "DsResult2Json", "application/json", "xslt/DsResult2Json.xsl", "xslt");
            initAndAddTransformer(jsonItemCollectionTransformer);

        } catch (IOException | TransformerConfigurationException e) {
            throw new InitializationException(e);
        }
        doInit();
        initialized = true;
    }

    private void initAndAddTransformer(final XsltOutputTransformer outputFormat)
            throws ConfigurationException, InvalidStateTransitionException, InitializationException {
        outputFormat.getConfigurationProperties().setPropertiesFrom(configProperties, true);
        outputFormat.init();
        outputFormatIdMap.put(outputFormat.getId(), outputFormat);
        outputFormatLabelMap.put(outputFormat.getLabel(), outputFormat);
    }

    protected void doInit() throws ConfigurationException, InitializationException, InvalidStateTransitionException {}

    @Override
    public final PreparedDtoCollection<T> getAll(final Filter filter) throws StorageException {
        try {
            final BsXQuery bsXQuery = createPagedQuery(filter);
            return new BsxPreparedDtoCollection(bsXQuery, getDtoResultCmd);
        } catch (BaseXException e) {
            ctx.getLogger().error(e.getMessage());
            throw new RetrieveException(e);
        }
    }

    @Override
    public PreparedDto<T> getById(final EID eid, final Filter filter) throws StorageException, ObjectWithIdNotFoundException {
        if (!exists(eid)) {
            throw new ObjectWithIdNotFoundException(this, eid.getId());
        }
        try {
            final BsXQuery bsXQuery = createIdQuery(BsxDataStorage.ID_PREFIX + eid.getId(), filter);
            return new BsxPreparedDto(eid, bsXQuery, getDtoResultCmd);
        } catch (IOException e) {
            ctx.getLogger().error(e.getMessage());
            throw new ObjectWithIdNotFoundException(this, eid.getId());
        }
    }

    @Override
    public PreparedDtoCollection<T> getByIds(final Set<EID> ids, final Filter filter)
            throws StorageException {
        try {
            for (final EID id : ids) {
                // TODO(performance): provoke cache call. Could be optimized.
                getById(id).getDto();
            }
            final BsXQuery bsXQuery = createIdsQuery(ids, filter);
            return new BsxPreparedDtoCollection(ids, bsXQuery, getDtoResultCmd);
        } catch (ObjectWithIdNotFoundException | IOException e) {
            ctx.getLogger().error(e.getMessage());
            throw new StorageException(e);
        }
    }

    private BsXQuery createPagedQuery(final Filter filter) throws BaseXException {
        return new BsXQuery(this.ctx, xqueryStatement).parameter(filter)
                .parameter("function", "paged")
                .parameter("selection",
                        tQuery.typeName);
    }

    private BsXQuery createIdQuery(final String id, final Filter filter) throws BaseXException {
        return new BsXQuery(this.ctx, xqueryStatement).parameter(filter)
                .parameter("qids", id)
                .parameter("function", "byId")
                .parameter("selection", tQuery.typeName);
    }

    private BsXQuery createIdsQuery(final Set<EID> ids, final Filter filter) throws BaseXException {
        return new BsXQuery(this.ctx, xqueryStatement).parameter(filter)
                .parameter("qids", SUtils.concatStrWithPrefixAndSuffix(
                        ",", BsxDataStorage.ID_PREFIX, "", ids),
                        "xs:string")
                .parameter("function", "byId")
                .parameter("selection", tQuery.typeName);
    }

    @Override
    public boolean isInitialized() {
        return xqueryStatement != null;
    }

    @Override
    public void release() {
        initialized = false;
    }

    @Override
    public EidMap<OutputFormat> getOutputFormats() {
        return outputFormatIdMap;
    }

    public long getLastModificationDate() {
        return lastModificationDate;
    }

}
