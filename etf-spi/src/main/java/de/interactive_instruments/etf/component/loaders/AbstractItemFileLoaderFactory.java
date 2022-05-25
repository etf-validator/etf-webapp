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
package de.interactive_instruments.etf.component.loaders;

import java.util.concurrent.ConcurrentHashMap;

import de.interactive_instruments.etf.dal.dto.Dto;
import de.interactive_instruments.etf.model.DefaultEidMap;
import de.interactive_instruments.etf.model.EID;
import de.interactive_instruments.etf.model.EidMap;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public abstract class AbstractItemFileLoaderFactory<T extends Dto>
        implements ItemFileLoaderFactory, ItemFileLoaderResultListener<T> {

    private final EidMap<T> items = new DefaultEidMap<>(new ConcurrentHashMap<>());
    protected LoadingContext loadingContext = NullLoadingContext.instance();

    public void setContextLoader(final LoadingContext loadingContext) {
        this.loadingContext = loadingContext;
    }

    protected ItemRegistry getItemRegistry() {
        return loadingContext.getItemRegistry();
    }

    @Override
    public final void eventItemBuilt(final T dto) {
        items.put(dto.getId(), dto);
    }

    @Override
    public final void eventItemDestroyed(final EID id) {
        items.remove(id);
    }

    @Override
    public final void eventItemUpdated(final T dto) {
        items.put(dto.getId(), dto);
    }

    protected final EidMap<T> getItems() {
        return items.unmodifiable();
    }
}
