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
package de.interactive_instruments.etf.dal.dao;

import java.util.Set;

import de.interactive_instruments.etf.dal.dto.Dto;
import de.interactive_instruments.etf.model.EID;
import de.interactive_instruments.exceptions.ObjectWithIdNotFoundException;
import de.interactive_instruments.exceptions.StorageException;

/**
 * PreparedDtoResolver
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public interface PreparedDtoResolver<T extends Dto> {

    /**
     * Return a PreparedDto to query a DTO
     *
     * @param id
     *            Dto ID
     *
     * @return prepared Dto
     * @throws StorageException
     *             internal Store exception
     * @throws ObjectWithIdNotFoundException
     *             Invalid ID provided
     */
    default PreparedDto<T> getById(final EID id) throws StorageException, ObjectWithIdNotFoundException {
        return getById(id, null);
    }

    /**
     * Return a PreparedDto to query a filtered DTO
     *
     * @param id
     *            Dto ID
     * @param filter
     *            Filter
     *
     * @return prepared Dto
     * @throws StorageException
     *             internal Store exception
     * @throws ObjectWithIdNotFoundException
     *             Invalid ID provided
     */
    PreparedDto<T> getById(final EID id, final Filter filter) throws StorageException, ObjectWithIdNotFoundException;

    /**
     * Return a PreparedDtoCollection to query a collection of DTOs
     *
     * @param id
     *            Dto ID
     *
     * @return prepared Dto collection
     * @throws StorageException
     *             internal Store exception
     * @throws ObjectWithIdNotFoundException
     *             Invalid ID provided
     */
    default PreparedDtoCollection<T> getByIds(final Set<EID> id) throws StorageException, ObjectWithIdNotFoundException {
        return getByIds(id, null);
    }

    /**
     * Return a PreparedDtoCollection to query a filtered collection of DTOs
     *
     * @param id
     *            Dto ID
     * @param filter
     *            Filter
     *
     * @return prepared Dto collection
     * @throws StorageException
     *             internal Store exception
     * @throws ObjectWithIdNotFoundException
     *             Invalid ID provided
     */
    PreparedDtoCollection<T> getByIds(final Set<EID> id, final Filter filter)
            throws StorageException, ObjectWithIdNotFoundException;
}
