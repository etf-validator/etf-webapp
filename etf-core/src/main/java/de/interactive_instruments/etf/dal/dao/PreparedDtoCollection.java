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
package de.interactive_instruments.etf.dal.dao;

import java.util.Collection;

import de.interactive_instruments.Releasable;
import de.interactive_instruments.etf.dal.dto.Dto;
import de.interactive_instruments.etf.model.EidMap;

/**
 * Prepared statement for querying a collection of Data Transfer Object or for directly streaming it in the desired
 * output format
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public interface PreparedDtoCollection<T extends Dto>
        extends OutputFormatStreamable, Iterable<T>, EidMap<T>, Releasable, Comparable<PreparedDtoCollection> {

    default Collection<T> asCollection() {
        return values();
    }
}
