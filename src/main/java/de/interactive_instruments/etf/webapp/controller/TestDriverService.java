/**
 * Copyright 2010-2016 interactive instruments GmbH
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

import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.interactive_instruments.etf.component.ComponentInfo;
import de.interactive_instruments.etf.dal.dao.Dao;
import de.interactive_instruments.etf.dal.dao.Filter;
import de.interactive_instruments.etf.dal.dto.IncompleteDtoException;
import de.interactive_instruments.etf.dal.dto.capabilities.ComponentDto;
import de.interactive_instruments.etf.dal.dto.capabilities.TestObjectTypeDto;
import de.interactive_instruments.etf.dal.dto.run.TestRunDto;
import de.interactive_instruments.etf.dal.dto.test.ExecutableTestSuiteDto;
import de.interactive_instruments.etf.model.EID;
import de.interactive_instruments.etf.model.EidFactory;
import de.interactive_instruments.etf.testdriver.*;
import de.interactive_instruments.exceptions.*;
import de.interactive_instruments.exceptions.config.ConfigurationException;

/**
 * Service which holds the test drivers
 */
@Service
public class TestDriverService {

	@Autowired
	private ServletContext servletContext;

	@Autowired
	private EtfConfigController etfConfig;

	@Autowired
	private DataStorageService dataStorageService;

	private TestDriverManager driverManager;
	private Dao<ExecutableTestSuiteDto> etsDao;
	private Dao<TestObjectTypeDto> testObjectTypesDao;
	private final Logger logger = LoggerFactory.getLogger(TestDriverService.class);

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

	public TestDriverService() {}

	@PostConstruct
	public void init() throws ConfigurationException, InvalidStateTransitionException, InitializationException, StorageException {

		driverManager = TestDriverManager.getDefault();
		driverManager.getConfigurationProperties().setPropertiesFrom(etfConfig, true);
		driverManager.getConfigurationProperties().setProperty(ETF_DATA_STORAGE_NAME, "default");
		driverManager.init();
		driverManager.loadAll();

		etsDao = dataStorageService.getDataStorage().getDao(ExecutableTestSuiteDto.class);
		testObjectTypesDao = dataStorageService.getDataStorage().getDao(TestObjectTypeDto.class);
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
		testRunDto.setDefaultLang("en");
		testRunDto.ensureBasicValidity();
		testRunDto.getTestTasks().get(0).setId(EidFactory.getDefault().createRandomId());
		return driverManager.createTestRun(testRunDto);

		/*
		// Get ETS and associated test engines
		final Multimap<EID, EID> testEngineEtsMultiMap = ArrayListMultimap.create();

		for (final TestTaskDto testTaskDto : testRunDto.getTestTasks()) {
			testTaskDto.ensureBasicValidity();
			final EID id = testTaskDto.getExecutableTestSuite().getTestEngine().getId();
			testEngineEtsMultiMap.put(id, testTaskDto.getExecutableTestSuite().getId());
		}

		// get test task lists
		List<TestEngineOrderedTestTasks> testEngineTestTaskSets;
		for (final Map.Entry<EID, Collection<EID>> eidCollectionEntry : testEngineEtsMultiMap.asMap().entrySet()) {
			final TestEngine testEngine = loader.getTestEngineById(eidCollectionEntry.getKey().toString());
			testEngine.createTestTasks(eidCollectionEntry.getValue());
		}


		ExecutableTestSuiteDto loadedTp = testRunDto.get;
		TestEngine testRunTaskFactory = null;
		final String testDriverId = testRun.getTestProject().getTestDriverId();
		if (testDriverId != null && !testDriverId.isEmpty()) {
			testRunTaskFactory = loader.getFactoryById(testDriverId);
			loadedTp = testRunTaskFactory.getTestProjectStore().getDtoById(
					testRun.getTestProject().getId());
		} else {
			//probe factories...
			for (TestRunTaskFactory t : loader.getFactories()) {
				try {
					testRunTaskFactory = t;
					loadedTp = t.getTestProjectStore().getDtoById(
							testRun.getTestProject().getId());
					// break when no exception is thrown
					break;
				} catch (ObjectWithIdNotFoundException e) {
					ExcUtils.supress(e);
				}
			}
		}
		if (testRunTaskFactory == null) {
			throw new ComponentNotLoadedException("No TestDriver initialized which can handle the test project " +
					testRun.getTestProject().getId());
		}
		testRun.setTestProject(loadedTp);
		final TestRunTask task = testRunTaskFactory.createTestRunTask(testRun);
		task.getTaskProgress().addStateEventListener(listener);
		return task;
		*/
	}

	Collection<ExecutableTestSuiteDto> getExecutableTestSuites() throws ConfigurationException, StorageException {
		return etsDao.getAll(FILTER_GET_ALL).asCollection();
	}

	ExecutableTestSuiteDto getExecutableTestSuiteById(final EID id) throws StorageException, ObjectWithIdNotFoundException {
		return etsDao.getById(id).getDto();
	}

	Collection<TestObjectTypeDto> getTestObjectTypes() throws ConfigurationException, StorageException {
		return testObjectTypesDao.getAll(FILTER_GET_ALL).asCollection();
	}

	public Collection<ComponentDto> getTestDriverInfo() {
		return driverManager.getTestDriverInfo().stream().map(ComponentDto::new).collect(Collectors.toList());
	}

	/*

	TestProjectDto getProjectById(EID id) throws ConfigurationException, ObjectWithIdNotFoundException, StorageException {
		for (TestRunTaskFactory testRunTaskFactory : loader.getFactories()) {
			if (testRunTaskFactory.getTestProjectStore().exists(id)) {
				return testRunTaskFactory.getTestProjectStore().getDtoById(id);
			}
		}
		throw new ObjectWithIdNotFoundException(id.toString());
	}


	Collection<ComponentInfo> getTestDriverInfo() {
		return loader.getInfo();
	}

	ComponentInfo getTestDriverInfo(final String module) {
		return loader.getInfo(module);
	}

	void unloadAll() {
		loader.release();
	}

	void unload(String id) {
		loader.release(id);
	}

	void load(String id) throws ComponentLoadingException, ConfigurationException {
		loader.load(id);
	}

	void loadAll() throws ConfigurationException, ComponentLoadingException {
		loader.load();
	}

	void reload(String id) throws ConfigurationException, ComponentLoadingException {
		loader.reload(id);
	}

	Collection<TestProjectDto> getAvailableProjects(String testDriverId) throws ConfigurationException, StorageException, ComponentNotLoadedException {
		return getById(testDriverId).getTestProjectStore().getAll();
	}

	TestRunTaskFactory getById(String id) throws ConfigurationException, ComponentNotLoadedException {
		return loader.getFactoryById(id);
	}

	Collection<TestRunTaskFactory> getFactories() {
		return loader.getFactories();
	}

	Map<EID, TestObjectResourceType> getTestObjectResourceTypes() throws ConfigurationException {
		final Map<EID, TestObjectResourceType> resourceTypes = new TreeMap<>();
		for (TestRunTaskFactory testRunTaskFactory : loader.getFactories()) {
			resourceTypes.putAll(testRunTaskFactory.getTestProjectStore().getTestObjectResourceTypes());
		}
		return resourceTypes;
	}

	*/

}
