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

import java.util.*;
import java.util.concurrent.Callable;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import org.eclipse.persistence.jaxb.IDResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import de.interactive_instruments.etf.dal.dao.PreparedDto;
import de.interactive_instruments.etf.dal.dao.WriteDaoListener;
import de.interactive_instruments.etf.dal.dto.Dto;
import de.interactive_instruments.etf.model.EID;
import de.interactive_instruments.etf.model.EidFactory;

/**
 * Cache for queried DTO objects
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
final class DtoCache implements WriteDaoListener {

    private final Logger logger = LoggerFactory.getLogger(DtoCache.class);

    // Entries in this cache automatically evict if the cache grows beyond a certain size
    private final Cache<String, Dto> dtoCache;

    private final BsxDsCtx bsxDsCtx;

    private static class ContextIdResolver extends IDResolver implements ValidationEventHandler {

        private final Cache<String, Dto> dtoCache;
        private final BsxDsCtx bsxDsCtx;
        private final Logger logger;
        private final Set<String> lazyLookupIds;

        private ContextIdResolver(final Cache<String, Dto> dtoCache,
                final BsxDsCtx bsxDsCtx, final Logger logger) {
            this.dtoCache = dtoCache;
            this.bsxDsCtx = bsxDsCtx;
            this.logger = logger;
            this.lazyLookupIds = new LinkedHashSet<>();
        }

        private void addLookupIdref(final String lookupId) {
            lazyLookupIds.add(lookupId);
        }

        /**
         * Marks unresolved IDrefs which will be used for creating lazy Dto proxies
         *
         * @param event
         * @return
         */
        @Override
        public boolean handleEvent(final ValidationEvent event) {
            // Note: this mechanism does not work with the broken internal jre xerces as
            // only one invalid ref event will be reported so that only one
            // reference can be set!
            // Message extraction only tested with xerces.
            final String message = event.getMessage();
            if (message.startsWith("cvc-id")) {
                // Check min length for UUID
                if (message.length() > 90) {
                    final int eidLoc = message.indexOf("r IDREF 'EID");
                    if (eidLoc > 0) {
                        // add this id for creating a lazy lookup
                        addLookupIdref(message.substring(eidLoc + 12, eidLoc + 48));
                        return true;
                    }
                }
                // Check for translation lookup IDs beginning with "TR."
                final int etrLoc = message.indexOf("r IDREF 'TR.");
                if (etrLoc > 0) {
                    final int trEnd = message.indexOf("'", etrLoc + 12);
                    if (trEnd > etrLoc) {
                        addLookupIdref(message.substring(etrLoc + 12, trEnd));
                        return true;
                    }
                }
            }
            logger.debug(
                    "Validation failed\n\tSeverity: {}\n\tMessage: {}\n\tLinked exception: {}\n\tLocator: {}:{} {}\n\tObject: {}\n\tNode: {}",
                    event.getSeverity(), message, event.getLinkedException(),
                    event.getLocator().getLineNumber(), event.getLocator().getColumnNumber(), event.getLocator().getOffset(),
                    event.getLocator().getObject(), event.getLocator().getNode());
            return event.getSeverity() == ValidationEvent.WARNING;
        }

        @Override
        public Callable<?> resolve(final Object id, final Class type) throws SAXException {
            // First check if we can resolve this object. A resolve objects ID begins with EID
            if (id instanceof String && ((String) id).startsWith("EID")) {
                final String eid = ((String) id).substring(3);
                final Dto dto = dtoCache.getIfPresent(eid);
                if (dto != null) {
                    logger.debug("Returning {} ({}) from cache", dto.getDescriptiveLabel(), type.getSimpleName());
                    return (Callable<Object>) () -> dto;
                } else if (lazyLookupIds.contains(eid)) {
                    logger.debug("Creating lazy load proxy for {} ({})", eid, type.getSimpleName());
                    return (Callable<Object>) () -> bsxDsCtx.createProxy(EidFactory.getDefault().createAndPreserveStr(eid),
                            type);
                }
            }
            logger.trace("Cache miss: {} ({})", id, type);
            return null;
        }

        @Override
        public Callable<?> resolve(final Map<String, Object> id, final Class type) throws SAXException {
            logger.debug("Resolving not implemented");
            return null;
        }

        @Override
        public void bind(final Object id, final Object obj) throws SAXException {
            if (id instanceof String && ((String) id).startsWith("EID") && obj instanceof Dto) {
                final String eid = ((String) id).substring(3);
                final Dto dto = (Dto) obj;
                logger.debug("Binding {}", dto.getDescriptiveLabel());
                dtoCache.put(eid, dto);
            } else if (logger.isWarnEnabled()) {
                if (id == null || obj == null) {
                    if (id == null) {
                        logger.error("Cannot bind null ID");
                    } else {
                        logger.error("Cannot bind ID {} ", id);
                    }
                    if (obj == null) {
                        logger.error("Cannot bind null type");
                    } else {
                        logger.error("Cannot bind type {}", obj.getClass().getSimpleName());
                    }
                } else {
                    logger.warn("Cannot bind {} which has type {}", id.toString(), obj.getClass().getSimpleName());
                }
            }
        }

        @Override
        public void bind(final Map<String, Object> id, final Object obj) throws SAXException {
            logger.debug("Binding not implemented");
        }
    }

    DtoCache(final long maxCacheEntries, final BsxDsCtx bsxDsCtx) {
        this.bsxDsCtx = bsxDsCtx;
        final Caffeine evictableDtoCacheBuilder = Caffeine.newBuilder().maximumSize(maxCacheEntries);
        if (logger.isDebugEnabled()) {
            evictableDtoCacheBuilder.recordStats();
        }
        dtoCache = evictableDtoCacheBuilder.build();
    }

    ContextIdResolver newIdResolverInstance() {
        return new ContextIdResolver(dtoCache, bsxDsCtx, logger);
    }

    @Override
    public void writeOperationPerformed(final EventType eventType, final PreparedDto preparedDto) {
        if (eventType == EventType.DELETE) {
            dtoCache.invalidate(preparedDto.getDtoId());
        } else if (eventType == EventType.UPDATE) {
            // TODO
            dtoCache.invalidate(preparedDto.getDtoId());
            // dtoCache.put(preparedDto.getDtoId(), preparedDto.getDto());
        }
    }

    /**
     * Used by BsxPreparedDto
     *
     * @param eid
     *            Dto ID
     * @return cached Dto
     */
    Dto getFromCache(final EID eid) {
        final Dto dto = dtoCache.getIfPresent(eid.toString());
        if (logger.isDebugEnabled() && dto != null) {
            logger.debug("Returning {} from cache", dto.getDescriptiveLabel());
        }
        return dto;
    }

    /**
     * Delete all entries
     */
    void clear() {
        dtoCache.invalidateAll();
        dtoCache.cleanUp();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DtoCache{");
        sb.append("size=").append(dtoCache.estimatedSize());
        if (logger.isDebugEnabled()) {
            sb.append(", stats=").append(dtoCache.stats().toString()).append("");
        }
        sb.append('}');
        return sb.toString();
    }
}
