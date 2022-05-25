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
import de.interactive_instruments.etf.dal.dto.RepositoryItemDto;
import de.interactive_instruments.etf.model.Disableable;
import de.interactive_instruments.etf.model.EID;
import de.interactive_instruments.exceptions.ExcUtils;
import de.interactive_instruments.exceptions.ObjectWithIdNotFoundException;
import de.interactive_instruments.exceptions.StorageException;

/**
 * Data Access Object for creating, updating and deleting Data Transfer Objects
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public interface WriteDao<T extends Dto> extends Dao<T> {

    /**
     * Add Dto to data storage
     *
     * @param dto
     *            Dto
     * @throws StorageException
     *             if the Dto can not be persisted
     */
    void add(final T dto) throws StorageException;

    /**
     * Add Dtos to data storage
     *
     * If the Dto is an instance of {@link Disableable} and is disabled, the disabled item will be overwritten.
     *
     * @param dtoCollection
     *            Collection of Dtos
     * @throws StorageException
     *             if the Dtos can not be persisted
     */
    void addAll(final Collection<T> dtoCollection) throws StorageException;

    /**
     * Update one existing Dto in the data storage
     *
     * If the Dto is an instance of {@link RepositoryItemDto}, the updated Dto with a NEW EID will be returned.
     *
     * @param dto
     *            old dto
     * @return the new dto, if the Dto is of type RepositoryItemDto its id will change!
     * @throws StorageException
     *             if the Dto can not be updated
     * @throws ObjectWithIdNotFoundException
     *             if a an item with the ID was not found
     */
    default T update(final T dto) throws StorageException, ObjectWithIdNotFoundException {
        return update(dto, null);
    }

    /**
     * Update one existing Dto in data storage with a new ID
     *
     * If the new ID parameter is null, the ID is not changed -except for a Dto that is an instance of
     * {@link RepositoryItemDto}, for which the updated Dto with a NEW EID is returned.
     *
     * @param dto
     *            old dto
     * @param newId
     *            non-existing new ID or null for no ID change/random ID (depends on type)
     * @return the new dto
     * @throws StorageException
     *             if the Dto can not be updated
     * @throws ObjectWithIdNotFoundException
     *             if a an item with the ID was not found
     */
    T update(final T dto, final EID newId) throws StorageException, ObjectWithIdNotFoundException;

    /**
     * Replace an existing Dto in data storage
     *
     * Please note: even if the Dto is an instance of {@link RepositoryItemDto}, the EID is not updated!
     *
     * @param dto
     *            old dto
     * @throws StorageException
     *             if the Dto can not be replaced
     * @throws ObjectWithIdNotFoundException
     *             if a an item with the ID was not found
     */
    default void replace(final T dto) throws StorageException, ObjectWithIdNotFoundException {
        replace(dto, null);
    }

    /**
     * Replace an existing Dto in data storage and set a new ID
     *
     * @param dto
     *            old dto
     * @param newId
     *            non-existing new ID or null for no change
     * @throws StorageException
     *             if the Dto can not be replaced
     * @throws ObjectWithIdNotFoundException
     *             if a an item with the ID was not found
     */
    void replace(final T dto, final EID newId) throws StorageException, ObjectWithIdNotFoundException;

    /**
     * Update multiple Dtos in data storage
     *
     * If non-existing Dtos are an instance of{@link RepositoryItemDto}, updated Dtos with a NEW EID will be returned.
     *
     * @param dtoCollection
     *            collection of Dtos to update
     * @return collection of new dtos, if the Dto is of type RepositoryItemDto its id will change!
     * @throws StorageException
     *             if the Dtos can not be updated
     * @throws ObjectWithIdNotFoundException
     *             if a an item with the ID was not found
     */
    Collection<T> updateAll(final Collection<T> dtoCollection) throws StorageException, ObjectWithIdNotFoundException;

    /**
     * Delete Dto by its ID
     *
     * If the Dto is an instance of {@link Disableable}, the property disabled will be set to true.
     *
     * @param id
     *            Dto ID
     * @throws StorageException
     *             if the Dto with the ID can not be deleted
     * @throws ObjectWithIdNotFoundException
     *             if a an item with the ID was not found
     */
    void delete(final EID id) throws StorageException, ObjectWithIdNotFoundException;

    /**
     * Delete Dtos by their IDs
     *
     * If the Dtos are instances of {@link Disableable}, the property disabled will be set to true.
     *
     * @param ids
     *            ID collection
     * @throws StorageException
     *             if the Dtos with the IDs can not be deleted
     * @throws ObjectWithIdNotFoundException
     *             if a an item with the ID was not found
     */
    void deleteAll(final Set<EID> ids) throws StorageException, ObjectWithIdNotFoundException;

    /**
     * Delete existing Dtos by their IDs, ignore non-existing IDs.
     *
     * @param ids
     *            ID collection
     * @throws StorageException
     *             if an internal error occurs
     */
    default void deleteAllExisting(final Set<EID> ids) throws StorageException {
        for (final EID id : ids) {
            try {
                delete(id);
            } catch (ObjectWithIdNotFoundException e) {
                ExcUtils.suppress(e);
            }
        }
    }

    /**
     * Registers a {@link WriteDaoListener} to externally listen for write events of this WriteDao
     *
     * @param listener
     *            {@link WriteDaoListener}
     */
    void registerListener(final WriteDaoListener listener);

    /**
     * Deregister a {@link WriteDaoListener} from this WriteDao
     *
     * @param listener
     *            {@link WriteDaoListener}
     */
    void deregisterListener(final WriteDaoListener listener);
}
