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
package de.interactive_instruments.etf.component.loaders;

import java.util.Collection;
import java.util.Collections;

import de.interactive_instruments.etf.dal.dto.Dto;
import de.interactive_instruments.etf.model.DefaultEidHolderMap;
import de.interactive_instruments.etf.model.EID;
import de.interactive_instruments.etf.model.EidHolder;
import de.interactive_instruments.etf.model.EidHolderMap;

/**
 * Realizes the Null Object pattern
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public final class NullItemRegistry implements ItemRegistry {

    private static NullItemRegistry instance = new NullItemRegistry();

    private NullItemRegistry() {}

    public static ItemRegistry instance() {
        return instance;
    }

    @Override
    public void register(final Collection<? extends Dto> items) {
        // null object pattern
    }

    @Override
    public void deregister(final Collection<? extends EidHolder> items) {
        // null object pattern
    }

    @Override
    public void deregisterCallback(final DependencyChangeListener listener) {
        // null object pattern
    }

    @Override
    public void update(final Collection<? extends Dto> items) {
        // null object pattern
    }

    @Override
    public EidHolderMap<? extends Dto> lookupDependency(final Collection<EID> dependencies,
            final DependencyChangeListener callbackListener) {
        return new DefaultEidHolderMap(Collections.EMPTY_MAP);
    }

    @Override
    public EidHolderMap<? extends Dto> lookup(final Collection<EID> items) {
        return new DefaultEidHolderMap(Collections.EMPTY_MAP);
    }
}
