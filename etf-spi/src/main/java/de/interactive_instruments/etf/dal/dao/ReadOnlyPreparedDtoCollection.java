/**
 * Copyright 2010-2020 interactive instruments GmbH
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
package de.interactive_instruments.etf.dal.dao;

import java.util.Map;
import java.util.Set;

import de.interactive_instruments.etf.dal.dto.Dto;
import de.interactive_instruments.etf.model.DefaultEidMap;
import de.interactive_instruments.etf.model.EID;

/**
 *
 * A LazyLoad reference map
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public abstract class ReadOnlyPreparedDtoCollection<T extends Dto> extends AbstractPreparedDtoCollection<T> {

    public ReadOnlyPreparedDtoCollection(final DtoResolver<T> resolver, final Set<EID> ids) {
        super(resolver, new DefaultEidMap<>());
        ids.forEach(eid -> map.put(eid, null));
    }

    /**
     * Unsupported operation
     *
     * @throws UnsupportedOperationException
     *             always
     */
    @Override
    public Dto put(final EID key, final Dto value) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " is read only");
    }

    /**
     * Unsupported operation
     *
     * @throws UnsupportedOperationException
     *             always
     */
    @Override
    public T remove(final Object key) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " is read only");
    }

    /**
     * Unsupported operation
     *
     * @throws UnsupportedOperationException
     *             always
     */
    @Override
    public void putAll(final Map<? extends EID, ? extends T> m) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " is read only");
    }

    /**
     * Unsupported operation
     *
     * @throws UnsupportedOperationException
     *             always
     */
    @Override
    public void clear() {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " is read only");
    }

}
