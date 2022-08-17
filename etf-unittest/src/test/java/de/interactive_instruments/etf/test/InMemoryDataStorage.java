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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.interactive_instruments.etf.dal.dao.Dao;
import de.interactive_instruments.etf.dal.dao.DataStorage;
import de.interactive_instruments.etf.dal.dto.Dto;
import de.interactive_instruments.etf.dal.dto.capabilities.ComponentDto;
import de.interactive_instruments.etf.dal.dto.capabilities.TagDto;
import de.interactive_instruments.etf.dal.dto.capabilities.TestObjectDto;
import de.interactive_instruments.etf.dal.dto.capabilities.TestObjectTypeDto;
import de.interactive_instruments.etf.dal.dto.result.TestTaskResultDto;
import de.interactive_instruments.etf.dal.dto.run.TestRunDto;
import de.interactive_instruments.etf.dal.dto.test.ExecutableTestSuiteDto;
import de.interactive_instruments.etf.dal.dto.test.TestItemTypeDto;
import de.interactive_instruments.etf.dal.dto.translation.TranslationTemplateBundleDto;
import de.interactive_instruments.exceptions.StorageException;
import de.interactive_instruments.properties.ConfigProperties;
import de.interactive_instruments.properties.ConfigPropertyHolder;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public class InMemoryDataStorage implements DataStorage {

    private final Map daos = new HashMap<>();
    private final ConfigPropertyHolder configPropertyHolder = new ConfigProperties();

    public InMemoryDataStorage() {

    }

    @Override
    public void reset() {
        release();
        init();
    }

    @Override
    public String createBackup() throws StorageException {
        return null;
    }

    @Override
    public List<String> getBackupList() {
        return null;
    }

    @Override
    public void restoreBackup(final String backupName) throws StorageException {

    }

    @Override
    public <T extends Dto> Map<Class<T>, Dao<T>> getDaoMappings() {
        return daos;
    }

    @Override
    public void cleanAndOptimize() throws StorageException {

    }

    @Override
    public ConfigPropertyHolder getConfigurationProperties() {
        return configPropertyHolder;
    }

    @Override
    public void init() {
        daos.put(TestObjectDto.class, new InMemoryDao<>(this, TestObjectDto.class));
        daos.put(TestObjectTypeDto.class, new InMemoryDao<>(this, TestObjectTypeDto.class));
        daos.put(TestRunDto.class, new InMemoryDao<>(this, TestRunDto.class));
        daos.put(TranslationTemplateBundleDto.class, new InMemoryDao<>(this, TranslationTemplateBundleDto.class));
        daos.put(TagDto.class, new InMemoryDao<>(this, TagDto.class));
        daos.put(ExecutableTestSuiteDto.class, new InMemoryDao<>(this, ExecutableTestSuiteDto.class));
        daos.put(ComponentDto.class, new InMemoryDao<>(this, ComponentDto.class));
        daos.put(TestTaskResultDto.class, new InMemoryDao<>(this, TestTaskResultDto.class));
        daos.put(TestItemTypeDto.class, new InMemoryDao<>(this, TestItemTypeDto.class));
        // daos.put(TestTaskDto.class, new InMemoryDao<>(this, TestTaskDto.class));
    }

    @Override
    public boolean isInitialized() {
        return !daos.isEmpty();
    }

    @Override
    public void release() {
        daos.clear();
    }
}
