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

import java.util.*;
import java.util.stream.Collectors;

import de.interactive_instruments.Copyable;

/**
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public class DefaultEidHolderMap<V extends EidHolder> extends DefaultEidMap<V> implements EidHolderMap<V> {

    public DefaultEidHolderMap() {
        super();
    }

    public DefaultEidHolderMap(final Map<EID, V> map) {
        super(map);
    }

    public DefaultEidHolderMap(final Collection<V> collection) {
        super(collection.stream().collect(
                Collectors.toMap(i -> i.getId(), i -> i, (i1, i2) -> i1, LinkedHashMap::new)));

    }

    public DefaultEidHolderMap(final V[] array) {
        super(Arrays.stream(array).collect(
                Collectors.toMap(i -> i.getId(), i -> i, (i1, i2) -> i1, LinkedHashMap::new)));
    }

    public DefaultEidHolderMap(final V singleItem) {
        super(Collections.singletonMap(singleItem.getId(), singleItem));
    }

    public EidHolderMap<V> unmodifiable() {
        return new DefaultEidHolderMap<>(Collections.unmodifiableMap(this));
    }

    public EidHolderMap<V> createCopy() {
        return new DefaultEidHolderMap<>(Copyable.createCopy(this));
    }

    public EidSet<V> toSet() {
        return new DefaultEidSet<>(values());
    }

    @Override
    public EidHolderMap<V> getAll(final Collection<?> keys) {
        final EidHolderMap map = new DefaultEidHolderMap();
        for (final Object key : keys) {
            final V v = get(key);
            if (v != null) {
                map.put(key, v);
            }
        }
        return map.isEmpty() ? null : map;
    }

    public V add(V v) {
        return put(v.getId(), v);
    }

    public void addAll(Collection<V> values) {
        for (final V value : values) {
            put(value.getId(), value);
        }
    }

    /**
     * Unmodifiable
     *
     * @param singleItem
     *            single item
     * @param <V>
     *            type
     * @return an EID holding object
     */
    public static <V extends EidHolder> EidHolderMap<V> singleton(final V singleItem) {
        return new DefaultEidHolderMap<>(Collections.singleton(singleItem));
    }
}
