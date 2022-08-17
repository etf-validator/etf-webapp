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
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public interface EidHolderWithParent<T extends EidHolderWithParent> extends ModelItemWithParent<T>, EidHolder {

    /**
     * Return the ID of this tree node item and the IDs of all parent nodes
     *
     * @return set of EIDs
     */
    default Set<EID> getIdAndParentIds() {
        final Set<EID> ids = new TreeSet<>();
        ids.add(Objects.requireNonNull(getId(), "EID is null"));
        EidHolderWithParent<T> parent = getParent();
        while (parent != null) {
            ids.add(getId());
            parent = parent.getParent();
        }
        return ids;
    }

    static <T extends EidHolderWithParent> Set<EID> getAllIdsAndParentIds(final Collection<T> eidHolderTreeNodes) {
        final Set<EID> ids = new TreeSet<>();
        eidHolderTreeNodes.forEach(holder -> ids.addAll(holder.getIdAndParentIds()));
        return ids;
    }
}
