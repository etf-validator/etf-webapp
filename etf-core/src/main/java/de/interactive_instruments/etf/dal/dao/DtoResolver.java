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

import java.util.Collection;
import java.util.Set;

import de.interactive_instruments.etf.dal.dto.Dto;
import de.interactive_instruments.etf.model.EID;
import de.interactive_instruments.exceptions.ObjectWithIdNotFoundException;
import de.interactive_instruments.exceptions.StorageException;

/**
 * DtoResolver
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public interface DtoResolver<T extends Dto> {
    /**
     * Return a Dto
     *
     * @param id
     *            Dto ID
     * @return resolved type
     * @throws StorageException
     *             if an internal error occurs
     * @throws ObjectWithIdNotFoundException
     *             if a an item with the ID was not found
     */
    T getById(final EID id) throws StorageException, ObjectWithIdNotFoundException;

    /**
     * Return a collection of DTOs
     *
     * @param id
     *            Dto ID
     * @return collection of resolved types
     * @throws StorageException
     *             if an internal error occurs
     * @throws ObjectWithIdNotFoundException
     *             if a an item with the ID was not found
     */
    Collection<T> getByIds(final Set<EID> id) throws StorageException, ObjectWithIdNotFoundException;
}
