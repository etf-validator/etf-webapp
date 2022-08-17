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
package de.interactive_instruments.etf.test;

import java.io.InputStream;
import java.util.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import de.interactive_instruments.etf.dal.dao.*;
import de.interactive_instruments.etf.dal.dto.Dto;
import de.interactive_instruments.etf.dal.dto.result.TestCaseResultDto;
import de.interactive_instruments.etf.dal.dto.result.TestModuleResultDto;
import de.interactive_instruments.etf.dal.dto.result.TestStepResultDto;
import de.interactive_instruments.etf.dal.dto.result.TestTaskResultDto;
import de.interactive_instruments.etf.dal.dto.test.TestAssertionDto;
import de.interactive_instruments.etf.dal.dto.translation.TranslationTemplateBundleDto;
import de.interactive_instruments.etf.model.DefaultEidMap;
import de.interactive_instruments.etf.model.EID;
import de.interactive_instruments.etf.model.EidMap;
import de.interactive_instruments.etf.model.OutputFormat;
import de.interactive_instruments.exceptions.InitializationException;
import de.interactive_instruments.exceptions.InvalidStateTransitionException;
import de.interactive_instruments.exceptions.ObjectWithIdNotFoundException;
import de.interactive_instruments.exceptions.StorageException;
import de.interactive_instruments.exceptions.config.ConfigurationException;
import de.interactive_instruments.properties.ConfigPropertyHolder;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public class InMemoryDao<T extends Dto> implements StreamWriteDao<T> {

    private final EidMap<T> dtos = new DefaultEidMap();
    protected long lastModificationDate = System.currentTimeMillis();

    private final String id;
    private final Class<T> type;
    private final List<WriteDaoListener<T>> listeners;
    private final InMemoryDataStorage dataStorage;

    public InMemoryDao(final InMemoryDataStorage dataStorage, final Class<T> type) {
        this.dataStorage = dataStorage;
        assert type.getSimpleName().contains("Dto");
        this.type = type;
        this.id = type.getSimpleName().replace("Dto", "Dao");
        this.listeners = new ArrayList<>();
    }

    private final void updateLastModificationDate() {
        this.lastModificationDate = System.currentTimeMillis();
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public Class<T> getDtoType() {
        return type;
    }

    @Override
    public PreparedDtoCollection<T> getAll(final Filter filter) throws StorageException {
        return new ResolvedDtoCollection(dtos);
    }

    @Override
    public boolean exists(final EID id) {
        return dtos.containsKey(id);
    }

    @Override
    public boolean isDisabled(final EID id) {
        return false;
    }

    @Override
    public EidMap<OutputFormat> getOutputFormats() {
        return new DefaultEidMap<>();
    }

    @Override
    public long getLastModificationDate() {
        return lastModificationDate;
    }

    @Override
    public ConfigPropertyHolder getConfigurationProperties() {
        return dataStorage.getConfigurationProperties();
    }

    @Override
    public void init() throws ConfigurationException, InitializationException, InvalidStateTransitionException {

    }

    @Override
    public boolean isInitialized() {
        return true;
    }

    @Override
    public void release() {

    }

    @Override
    public PreparedDto<T> getById(final EID id, final Filter filter) throws StorageException, ObjectWithIdNotFoundException {
        final T dto = dtos.get(id);
        if (dto != null) {
            return new ResolvedDto<>(dto);
        } else {
            throw new ObjectWithIdNotFoundException(id.toString());
        }
    }

    @Override
    public PreparedDtoCollection<T> getByIds(final Set<EID> id, final Filter filter)
            throws StorageException, ObjectWithIdNotFoundException {
        return new ResolvedDtoCollection<>(dtos.getAll(id));
    }

    @Override
    public void add(final T dto) throws StorageException {
        this.dtos.put(dto.getId(), dto);
        fireEventAdd(dto);
        updateLastModificationDate();
    }

    @Override
    public void addAll(final Collection<T> dtoCollection) throws StorageException {
        for (final T t : dtoCollection) {
            add(t);
        }
    }

    @Override
    public T update(final T dto, final EID newId) throws StorageException, ObjectWithIdNotFoundException {
        updateLastModificationDate();
        fireEventUpdate(dto);
        this.dtos.remove(dto.getId());
        dto.setId(newId);
        return this.dtos.put(dto.getId(), dto);
    }

    @Override
    public void replace(final T dto, final EID newId) throws StorageException, ObjectWithIdNotFoundException {
        updateLastModificationDate();
        fireEventUpdate(dto);
        this.dtos.put(dto.getId(), dto);
    }

    @Override
    public Collection<T> updateAll(final Collection<T> dtoCollection) throws StorageException, ObjectWithIdNotFoundException {
        final ArrayList<T> newDtoCollection = new ArrayList<>();
        for (final T t : dtoCollection) {
            newDtoCollection.add(update(t));
        }
        return newDtoCollection;
    }

    @Override
    public void delete(final EID id) throws StorageException, ObjectWithIdNotFoundException {
        dtos.remove(id);
        fireEventDelete(id);
        updateLastModificationDate();
    }

    @Override
    public void deleteAll(final Set<EID> ids) throws StorageException, ObjectWithIdNotFoundException {
        for (final EID eid : ids) {
            delete(eid);
        }
    }

    @Override
    public void registerListener(final WriteDaoListener listener) {
        listeners.add(listener);
    }

    @Override
    public void deregisterListener(final WriteDaoListener listener) {
        listeners.remove(listener);
    }

    protected final void fireEventDelete(final EID eid) throws ObjectWithIdNotFoundException, StorageException {
        if (!listeners.isEmpty()) {
            final PreparedDto<T> dto = getById(eid);
            for (int i = 0; i < listeners.size(); i++) {
                listeners.get(i).writeOperationPerformed(WriteDaoListener.EventType.DELETE, dto);
            }
        }
    }

    protected final void fireEventUpdate(final Dto updatedDto) {
        if (!listeners.isEmpty()) {
            final ResolvedDto dto = new ResolvedDto(updatedDto);
            for (int i = 0; i < listeners.size(); i++) {
                listeners.get(i).writeOperationPerformed(WriteDaoListener.EventType.UPDATE, dto);
            }
        }
    }

    protected final void fireEventAdd(final T addedDto) {
        if (!listeners.isEmpty()) {
            final ResolvedDto dto = new ResolvedDto(addedDto);
            for (int i = 0; i < listeners.size(); i++) {
                listeners.get(i).writeOperationPerformed(WriteDaoListener.EventType.UPDATE, dto);
            }
        }
    }

    protected final void fireEventAdd(final Collection<T> addedDtos) {
        if (!listeners.isEmpty()) {
            for (final T addedDto : addedDtos) {
                final ResolvedDto dto = new ResolvedDto(addedDto);
                for (int i = 0; i < listeners.size(); i++) {
                    listeners.get(i).writeOperationPerformed(WriteDaoListener.EventType.UPDATE, dto);
                }
            }
        }
    }

    @Override
    public T add(final InputStream input, final Optional<Dto> rootType, final ChangeBeforeStoreHook<T> hook)
            throws StorageException {
        try {
            final JAXBContext jaxbContext = JAXBContext.newInstance(
                    TestTaskResultDto.class,
                    TranslationTemplateBundleDto.class,
                    TestModuleResultDto.class,
                    TestCaseResultDto.class,
                    TestStepResultDto.class,
                    TestAssertionDto.class);
            final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            return (T) jaxbUnmarshaller.unmarshal(input);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return null;
    }
}
