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

import de.interactive_instruments.etf.dal.dto.Dto;
import de.interactive_instruments.etf.model.EID;
import de.interactive_instruments.etf.model.EidMap;
import de.interactive_instruments.exceptions.ObjectWithIdNotFoundException;
import de.interactive_instruments.exceptions.StorageException;

/**
 * A LazyLoad reference map with write-back functionality
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public abstract class WritablePreparedDtoCollection<T extends Dto> extends AbstractPreparedDtoCollection<T> {

    private final WriteDao<T> writeDao;

    protected WritablePreparedDtoCollection(final DtoResolver<T> writeDao, final EidMap<Dto> map) {
        super(writeDao, map);
        this.writeDao = (WriteDao<T>) writeDao;
    }

    @Override
    public Dto put(final EID key, final Dto value) {
        final Dto oldDto = map.put(key, value);
        if (oldDto == null) {
            try {
                writeDao.add((T) value);
            } catch (StorageException e) {
                throw new IllegalArgumentException(e);
            }
        } else {
            try {
                writeDao.update((T) value);
            } catch (StorageException | ObjectWithIdNotFoundException e) {
                throw new IllegalArgumentException(e);
            }
        }
        return oldDto;
    }

    @Override
    public T remove(final Object key) {
        final Dto oldDto = map.remove(key);
        if (oldDto != null) {
            try {
                writeDao.delete(oldDto.getId());
            } catch (StorageException | ObjectWithIdNotFoundException e) {
                throw new IllegalArgumentException(e);
            }
        }
        return (T) oldDto;
    }

    /**
     * Removes all Dtos from the data storage!
     *
     * @throws StorageException
     *             if an internal error occurs
     * @throws ObjectWithIdNotFoundException
     *             if the item with the id is not found
     */
    public void removeAll() throws StorageException, ObjectWithIdNotFoundException {
        for (final EID id : map.keySet()) {
            writeDao.delete(id);
        }
        map.clear();
    }

    /**
     * To prevent unintended deletion of all Dtos this method will always throw a Unsupported operation exception. Use
     * removeAll() method instead.
     *
     * @throws UnsupportedOperationException
     *             always
     *
     */
    @Override
    public final void clear() {
        throw new UnsupportedOperationException();
    }

    /*
     * @Override public boolean add(final T dto) { return this.put(dto.getId(), dto)!=null; }
     *
     * @Override public void putAll(final Map<? extends EID, ? extends T> m) { addAll((Collection<T>) m.values()); }
     *
     * @Override public void addAll(final Collection<T> collection) { final List<T> itemsToAdd = new ArrayList<>(); final
     * List<T> itemsToUpdate = new ArrayList<>(); collection.forEach( dto -> { final Dto old = map.put(dto.getId(),dto);
     * if(old!=null) { itemsToUpdate.add(dto); }else{ itemsToUpdate.add(dto); } }); if(!itemsToAdd.isEmpty()) { try {
     * writeDao.addAll(itemsToAdd); } catch (StorageException e) { throw new IllegalArgumentException(e); } }
     * if(!itemsToUpdate.isEmpty()) { try { writeDao.updateAll(itemsToUpdate); } catch (StorageException |
     * ObjectWithIdNotFoundException e) { throw new IllegalArgumentException(e); } } }
     */
}
