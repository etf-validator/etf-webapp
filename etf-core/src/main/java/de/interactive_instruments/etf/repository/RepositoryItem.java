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
package de.interactive_instruments.etf.repository;

import java.net.URI;
import java.util.Date;

import de.interactive_instruments.Versionable;
import de.interactive_instruments.model.std.RetrievableItem;

/**
 * An interface for an item in a repository.
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public interface RepositoryItem extends RetrievableItem, Comparable, Versionable {

    /**
     * The items last modified data.
     *
     * @return last modification as date
     */
    Date getLastModifiedDate();

    /**
     * Hash of the item in the repository.
     *
     * NOTE: if the hash is unknown, the implementor should generate the hash on demand (and cache it).
     *
     * @return item hash as byte array
     */
    byte[] getItemHash();

    /**
     * The URI of the item in the repository.
     *
     * @return item URI
     */
    URI getUri();

    /**
     * Compares this object with the specified RepositoryItem object for order by comparing the Id, the version and the
     * label.
     *
     * Returns a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the
     * specified object.
     *
     * @param item
     *            repository item to compare with.
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the
     *         specified object.
     */
    @Override
    default int compareTo(final Object item) {
        final RepositoryItem rItem = (RepositoryItem) item;
        final int rItemId = getId().compareTo(rItem.getId());
        if (rItemId != 0) {
            return rItemId;
        }
        final int rItemVersion = getVersion().compareTo(rItem.getVersion());
        if (rItemVersion != 0) {
            return rItemVersion;
        }
        final int rItemLabel = getLabel().compareTo(rItem.getLabel());
        if (rItemLabel != 0) {
            return rItemLabel;
        }
        return 0;
    }
}
