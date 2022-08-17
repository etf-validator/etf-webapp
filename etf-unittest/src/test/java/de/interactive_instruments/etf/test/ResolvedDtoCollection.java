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
package de.interactive_instruments.etf.test;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import de.interactive_instruments.etf.dal.dao.PreparedDtoCollection;
import de.interactive_instruments.etf.dal.dto.Dto;
import de.interactive_instruments.etf.model.DefaultEidMap;
import de.interactive_instruments.etf.model.EID;
import de.interactive_instruments.etf.model.EidMap;
import de.interactive_instruments.etf.model.OutputFormat;
import de.interactive_instruments.properties.PropertyHolder;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
final class ResolvedDtoCollection<T extends Dto> implements PreparedDtoCollection<T> {

    private final EidMap<T> dtos;

    ResolvedDtoCollection(final EidMap<T> dtos) {
        this.dtos = new DefaultEidMap<>(dtos);
    }

    @Override
    public void release() {

    }

    @Override
    public void streamTo(final OutputFormat outputFormat, final PropertyHolder arguments, final OutputStream outputStream)
            throws IOException {

    }

    @Override
    public EidMap<T> unmodifiable() {
        return dtos.unmodifiable();
    }

    @Override
    public EidMap<T> getAll(final Collection<?> keys) {
        return dtos.getAll(keys);
    }

    @Override
    public void removeAll(final Collection<?> keys) {
        dtos.removeAll(keys);
    }

    @Override
    public T _internalGet(final Object key) {
        return dtos._internalGet(key);
    }

    @Override
    public T _internalRemove(final Object key) {
        return dtos._internalRemove(key);
    }

    @Override
    public boolean _internalContainsKey(final Object key) {
        return dtos._internalContainsKey(key);
    }

    @Override
    public EidMap<T> createCopy() {
        return dtos.createCopy();
    }

    @Override
    public int compareTo(final PreparedDtoCollection o) {
        return Integer.compare(dtos.size(), o.size());
    }

    @Override
    public Iterator<T> iterator() {
        return dtos.asCollection().iterator();
    }

    @Override
    public int size() {
        return dtos.size();
    }

    @Override
    public boolean isEmpty() {
        return dtos.isEmpty();
    }

    @Override
    public boolean containsValue(final Object value) {
        return dtos.containsKey(value);
    }

    @Override
    public T put(final EID key, final T value) {
        return dtos.put(key, value);
    }

    @Override
    public void putAll(final Map<? extends EID, ? extends T> m) {
        dtos.putAll(m);
    }

    @Override
    public void clear() {
        dtos.clear();
    }

    @Override
    public Set<EID> keySet() {
        return dtos.keySet();
    }

    @Override
    public Collection<T> values() {
        return dtos.values();
    }

    @Override
    public Set<Entry<EID, T>> entrySet() {
        return dtos.entrySet();
    }
}
