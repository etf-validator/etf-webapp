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
package de.interactive_instruments.etf.webapp.controller;

import static de.interactive_instruments.etf.EtfConstants.ETF_DATA_STORAGE_NAME;

import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import de.interactive_instruments.etf.component.ComponentInfo;
import de.interactive_instruments.etf.component.ComponentLoadingException;
import de.interactive_instruments.etf.component.loaders.LoadingContext;
import de.interactive_instruments.etf.component.loaders.MetadataFilesLoader;
import de.interactive_instruments.etf.dal.dao.*;
import de.interactive_instruments.etf.dal.dto.IncompleteDtoException;
import de.interactive_instruments.etf.dal.dto.capabilities.ComponentDto;
import de.interactive_instruments.etf.dal.dto.run.TestRunDto;
import de.interactive_instruments.etf.dal.dto.run.TestTaskDto;
import de.interactive_instruments.etf.dal.dto.test.ExecutableTestSuiteDto;
import de.interactive_instruments.etf.model.EID;
import de.interactive_instruments.etf.model.EidFactory;
import de.interactive_instruments.etf.testdriver.TestDriverManager;
import de.interactive_instruments.etf.testdriver.TestRun;
import de.interactive_instruments.etf.testdriver.TestRunInitializationException;
import de.interactive_instruments.etf.webapp.helpers.SimpleFilter;
import de.interactive_instruments.exceptions.InitializationException;
import de.interactive_instruments.exceptions.InvalidStateTransitionException;
import de.interactive_instruments.exceptions.ObjectWithIdNotFoundException;
import de.interactive_instruments.exceptions.StorageException;
import de.interactive_instruments.exceptions.config.ConfigurationException;

/**
 * Controller for the test drivers
 */
@RestController
public class TestDriverController implements PreparedDtoResolver<ExecutableTestSuiteDto> {

    @Autowired
    private EtfConfig etfConfig;

    @Autowired
    private DataStorageService dataStorageService;

    @Autowired
    private LoadingContext loadingContext;

    private TestDriverManager driverManager;
    private MetadataFilesLoader metadataFilesLoader;
    private Dao<ExecutableTestSuiteDto> etsDao;
    private final Logger logger = LoggerFactory.getLogger(TestDriverController.class);

    public TestDriverController() {}

    @PostConstruct
    public void init()
            throws ConfigurationException, InvalidStateTransitionException, InitializationException, StorageException {

        // Metadata need to be initialized first
        metadataFilesLoader = new MetadataFilesLoader(dataStorageService.getDataStorage(), loadingContext);
        metadataFilesLoader.getConfigurationProperties().setPropertiesFrom(etfConfig, true);
        metadataFilesLoader.init();

        etsDao = dataStorageService.getDataStorage().getDao(ExecutableTestSuiteDto.class);

        // Deactivate all ETS first and ensure that drivers only reactivate existing ETS
        try {
            // keyset() does not invoke full Dto resolution, so invalid Dtos are ingored.
            ((WriteDao) etsDao).deleteAll(etsDao.getAll(SimpleFilter.allItems()).keySet());
        } catch (final Exception e) {
            logger.warn("Failed to clean Executable Test Suites ", e);
        }

        // Initialize test driver
        driverManager = TestDriverManager.getDefault();
        driverManager.setLoadingContext(loadingContext);
        driverManager.getConfigurationProperties().setPropertiesFrom(etfConfig, true);
        driverManager.getConfigurationProperties().setProperty(ETF_DATA_STORAGE_NAME, "default");
        driverManager.init();
        driverManager.loadAll();

        logger.info("Test Driver service initialized");
        if (driverManager.getTestDriverInfo().isEmpty()) {
            logger.warn("No Test Driver loaded");
        } else {
            for (final ComponentInfo componentInfo : driverManager.getTestDriverInfo()) {
                logger.info("Loaded Test Driver {} - {} ({})", componentInfo.getName(),
                        componentInfo.getVersion(), componentInfo.getId());
            }
        }
    }

    @PreDestroy
    private void shutdown() {
        driverManager.release();
        metadataFilesLoader.release();
    }

    TestRun create(TestRunDto testRunDto) throws IncompleteDtoException, TestRunInitializationException {
        testRunDto.setStartTimestamp(new Date());
        testRunDto.ensureBasicValidity();
        for (final TestTaskDto testTaskDto : testRunDto.getTestTasks()) {
            testTaskDto.setId(EidFactory.getDefault().createRandomId());
        }
        return driverManager.createTestRun(testRunDto);
    }

    Collection<ExecutableTestSuiteDto> getExecutableTestSuites() throws ConfigurationException, StorageException {
        return etsDao.getAll(SimpleFilter.allItems()).asCollection();
    }

    ExecutableTestSuiteDto getExecutableTestSuiteById(final EID id) throws StorageException, ObjectWithIdNotFoundException {
        return etsDao.getById(id).getDto();
    }

    @Override
    public PreparedDto<ExecutableTestSuiteDto> getById(final EID eid, final Filter filter)
            throws StorageException, ObjectWithIdNotFoundException {
        return this.etsDao.getById(eid, filter);
    }

    @Override
    public PreparedDtoCollection<ExecutableTestSuiteDto> getByIds(final Set<EID> eids, final Filter filter)
            throws StorageException, ObjectWithIdNotFoundException {
        return this.etsDao.getByIds(eids, filter);
    }

    public Collection<ComponentDto> getTestDriverInfo() {
        final Comparator<ComponentDto> comparator = (o1, o2) -> o2.getLabel().compareTo(o1.getLabel());
        return driverManager.getTestDriverInfo().stream().map(ComponentDto::new).sorted(comparator)
                .collect(Collectors.toList());
    }

    @RequestMapping(value = {MetaTypeController.COMPONENTS_URL}, params = "action=reload", method = RequestMethod.GET)
    public ResponseEntity<String> reloadAll() throws LocalizableApiError {
        if (!driverManager.getTestDriverInfo().isEmpty()) {
            return new ResponseEntity<>("Operation not permitted if a test driver has already been loaded!",
                    HttpStatus.FORBIDDEN);
        } else {
            try {
                driverManager.loadAll();
            } catch (ConfigurationException e) {
                throw new LocalizableApiError(e);
            } catch (ComponentLoadingException e) {
                throw new LocalizableApiError(e);
            }
            return new ResponseEntity<>("OK", HttpStatus.NO_CONTENT);
        }
    }

}
