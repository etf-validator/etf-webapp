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

import java.io.InputStream;
import java.util.Optional;

import de.interactive_instruments.etf.dal.dto.Dto;
import de.interactive_instruments.exceptions.StorageException;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public interface StreamWriteDao<T extends Dto> extends WriteDao<T> {

    @FunctionalInterface
    interface ChangeBeforeStoreHook<T extends Dto> {
        /**
         * Called before stored by a StreamWriteDao
         *
         * @param dto
         *            read and loaded Dto from InputStream
         * @return changed Dto
         */
        T doChangeBeforeStore(final T dto);
    }

    /**
     * Reads, validates and adds a Type from an input stream
     *
     * @return created Dto
     * @param input
     *            input stream
     * @throws StorageException
     *             if an internal error occurs
     */
    default T add(final InputStream input, final Optional<Dto> rootType) throws StorageException {
        return add(input, rootType, null);
    }

    /**
     * Reads, validates and adds a Type from an input stream
     *
     * @return created Dto
     * @param input
     *            input stream
     * @param hook
     *            a hook object that is called just before the Dto is persisted
     * @throws StorageException
     *             if an internal error occurs
     */
    T add(final InputStream input, final Optional<Dto> rootType, final ChangeBeforeStoreHook<T> hook) throws StorageException;

}
