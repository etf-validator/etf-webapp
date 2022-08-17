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
package de.interactive_instruments.etf.model;

import java.util.Collection;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public interface EidHolderMap<V extends EidHolder> extends EidMap<V> {

    EidHolderMap<V> unmodifiable();

    EidHolderMap<V> createCopy();

    EidSet<V> toSet();

    V add(V v);

    void addAll(Collection<V> values);

    EidHolderMap<V> getAll(final Collection<?> keys);
}
