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
package de.interactive_instruments.etf.model;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import de.interactive_instruments.Copyable;
import de.interactive_instruments.SUtils;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public interface EidSet<V extends EidHolder> extends Set<V>, Copyable<EidSet<V>> {

    EidSet<V> unmodifiable();

    EidMap<V> toMap();

    List<V> toList();

    default boolean contains(final Object o) {
        if (o instanceof String) {
            return internalContains(new SUtils.StrEqContainer(o));
        }
        return internalContains(o);
    }

    boolean internalContains(Object o);

    default boolean remove(final Object o) {
        if (o instanceof String) {
            return internalRemove(new SUtils.StrEqContainer(o));
        }
        return internalRemove(o);
    }

    boolean internalRemove(Object o);

    default boolean containsAll(final Collection<?> c) {
        if (c != null && c.iterator().next() instanceof String) {
            return internalContainsAll(SUtils.StrEqContainer.createSet(c));
        }
        return internalContainsAll(c);
    }

    boolean internalContainsAll(Collection<?> c);

    default boolean retainAll(final Collection<?> c) {
        if (c != null && c.iterator().next() instanceof String) {
            return internalRetainAll(SUtils.StrEqContainer.createSet(c));
        }
        return internalRetainAll(c);
    }

    boolean internalRetainAll(Collection<?> c);

    default boolean removeAll(final Collection<?> c) {
        if (c != null && c.iterator().next() instanceof String) {
            return internalRemoveAll(SUtils.StrEqContainer.createSet(c));
        }
        return internalRemoveAll(c);
    }

    boolean internalRemoveAll(Collection<?> c);
}
