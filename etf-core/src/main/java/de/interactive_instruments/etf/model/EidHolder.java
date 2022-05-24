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
import java.util.Set;
import java.util.TreeSet;

/**
 * Interface for objects that possess an ETF ID
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public interface EidHolder extends Comparable {
    EID getId();

    @Override
    default int compareTo(final Object o) {
        if (o instanceof EidHolder) {
            return ((EidHolder) o).getId().compareTo(this.getId());
        } else if (o instanceof String) {
            return ((String) o).compareTo(this.getId().getId());
        }
        throw new IllegalArgumentException("Invalid object type comparison: " +
                o.getClass().getName() + " can not be compared with an EidHolder.");
    }

    static Set<EID> getAllIds(final Collection<? extends EidHolder> holders) {
        final Set<EID> eids = new TreeSet<>();
        holders.forEach(e -> eids.add(e.getId()));
        return eids;
    }
}
