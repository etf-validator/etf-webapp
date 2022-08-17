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

import de.interactive_instruments.etf.dal.dao.DataStorage;
import de.interactive_instruments.etf.dal.dao.WriteDao;
import de.interactive_instruments.etf.dal.dto.capabilities.TestObjectTypeDto;
import de.interactive_instruments.etf.detector.TestObjectTypeDetectorManager;
import de.interactive_instruments.etf.model.EidMap;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public final class DataStorageTestUtils {
    private static DataStorage DATA_STORAGE = new InMemoryDataStorage();

    public static DataStorage inMemoryStorage() {
        try {
            if (!DATA_STORAGE.isInitialized()) {
                DATA_STORAGE.getConfigurationProperties().setProperty("etf.webapp.base.url", "http://localhost/etf-webapp");
                DATA_STORAGE.getConfigurationProperties().setProperty("etf.api.base.url", "http://localhost/etf-webapp/v2");
                DATA_STORAGE.init();
                final WriteDao<TestObjectTypeDto> testObjectTypeDao = ((WriteDao<TestObjectTypeDto>) (DATA_STORAGE
                        .getDao(TestObjectTypeDto.class)));
                final EidMap<TestObjectTypeDto> supportedTypes = TestObjectTypeDetectorManager.getSupportedTypes();
                if (supportedTypes != null) {
                    testObjectTypeDao.deleteAllExisting(supportedTypes.keySet());
                    for (final TestObjectTypeDto testObjectTypeDto : supportedTypes.values()) {
                        testObjectTypeDao.add(testObjectTypeDto);
                    }
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return DATA_STORAGE;
    }
}
