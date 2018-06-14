/**
 * Copyright 2017-2018 European Union, interactive instruments GmbH
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This work was supported by the EU Interoperability Solutions for
 * European Public Administrations Programme (http://ec.europa.eu/isa)
 * through Action 1.17: A Reusable INSPIRE Reference Platform (ARE3NA).
 */
package de.interactive_instruments.etf.webapp.controller;

import static de.interactive_instruments.etf.EtfConstants.ETF_DATA_STORAGE_NAME;

import java.util.Collection;
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
import de.interactive_instruments.etf.dal.dao.*;
import de.interactive_instruments.etf.dal.dto.IncompleteDtoException;
import de.interactive_instruments.etf.dal.dto.capabilities.ComponentDto;
import de.interactive_instruments.etf.dal.dto.run.TestRunDto;
import de.interactive_instruments.etf.dal.dto.run.TestTaskDto;
import de.interactive_instruments.etf.dal.dto.test.ExecutableTestSuiteDto;
import de.interactive_instruments.etf.model.EID;
import de.interactive_instruments.etf.model.EidFactory;
import de.interactive_instruments.etf.testdriver.MetadataFileTypeLoader;
import de.interactive_instruments.etf.testdriver.TestDriverManager;
import de.interactive_instruments.etf.testdriver.TestRun;
import de.interactive_instruments.etf.testdriver.TestRunInitializationException;
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
	private EtfConfigController etfConfig;

	// Wait for TestObjectTypeController to activate all standard Test Object Types
	@Autowired
	private TestObjectTypeController testObjectTypeController;

	@Autowired
	private DataStorageService dataStorageService;

	private TestDriverManager driverManager;
	private MetadataFileTypeLoader metadataTypeLoader;
	private Dao<ExecutableTestSuiteDto> etsDao;
	private final Logger logger = LoggerFactory.getLogger(TestDriverController.class);

	private static final Filter FILTER_GET_ALL = new Filter() {
		@Override
		public int offset() {
			return 0;
		}

		@Override
		public int limit() {
			return 2000;
		}
	};

	public TestDriverController() {}

	@PostConstruct
	public void init()
			throws ConfigurationException, InvalidStateTransitionException, InitializationException, StorageException {

		// Metadata need to be initialized first
		metadataTypeLoader = new MetadataFileTypeLoader(dataStorageService.getDataStorage());
		metadataTypeLoader.getConfigurationProperties().setPropertiesFrom(etfConfig, true);
		metadataTypeLoader.init();

		etsDao = dataStorageService.getDataStorage().getDao(ExecutableTestSuiteDto.class);

		// Deactivate all ETS first and ensure that drivers only reactivate existing ETS
		try {
			// keyset() does not invoke full Dto resolution, so invalid Dtos are ingored.
			((WriteDao) etsDao).deleteAll(etsDao.getAll(FILTER_GET_ALL).keySet());
		} catch (final Exception e) {
			logger.warn("Failed to clean Executable Test Suites ", e);
		}

		// Initialize test driver
		driverManager = TestDriverManager.getDefault();
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
			if (getExecutableTestSuites().size() == 0) {
				logger.warn("No Executable Test Suites loaded");
			} else {
				logger.info("{} Executable Test Suites loaded", getExecutableTestSuites().size());
			}
		}
	}

	@PreDestroy
	private void shutdown() {
		driverManager.release();
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
		return etsDao.getAll(FILTER_GET_ALL).asCollection();
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
		return driverManager.getTestDriverInfo().stream().map(ComponentDto::new).collect(Collectors.toList());
	}

	@RequestMapping(value = {MetaTypeController.COMPONENTS_URL}, params = "action=reload", method = RequestMethod.GET)
	public ResponseEntity<String> reloadAll() throws LocalizableApiError {
		if (!driverManager.getTestDriverInfo().isEmpty()) {
			return new ResponseEntity("Operation not permitted if a test driver has already been loaded!",
					HttpStatus.FORBIDDEN);
		} else {
			try {
				driverManager.loadAll();
			} catch (ConfigurationException e) {
				new LocalizableApiError(e);
			} catch (ComponentLoadingException e) {
				new LocalizableApiError(e);
			}
			return new ResponseEntity("OK", HttpStatus.NO_CONTENT);
		}
	}

}
