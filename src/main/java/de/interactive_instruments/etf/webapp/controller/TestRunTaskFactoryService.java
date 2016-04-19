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

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.interactive_instruments.concurrent.TaskStateEventListener;
import de.interactive_instruments.etf.EtfConstants;
import de.interactive_instruments.etf.component.ComponentInfo;
import de.interactive_instruments.etf.component.ComponentLoadingException;
import de.interactive_instruments.etf.component.ComponentNotLoadedException;
import de.interactive_instruments.etf.dal.dto.plan.TestProjectDto;
import de.interactive_instruments.etf.dal.dto.plan.TestRunDto;
import de.interactive_instruments.etf.driver.*;
import de.interactive_instruments.etf.model.item.EID;
import de.interactive_instruments.etf.model.plan.TestObjectResourceType;
import de.interactive_instruments.exceptions.ExcUtils;
import de.interactive_instruments.exceptions.ObjectWithIdNotFoundException;
import de.interactive_instruments.exceptions.StoreException;
import de.interactive_instruments.exceptions.config.ConfigurationException;

/**
 * Service which holds the test drivers
 */
@Service
public class TestRunTaskFactoryService {

	@Autowired
	ServletContext servletContext;

	@Autowired
	private EtfConfigController etfConfig;

	private TestDriverLoader loader;
	private final Logger logger = LoggerFactory.getLogger(TestRunTaskFactoryService.class);

	public TestRunTaskFactoryService() {

	}

	@PostConstruct
	public void init() throws IOException, ParseException, ConfigurationException, ComponentLoadingException {

		loader = new TestDriverLoader(
				etfConfig.getPropertyAsFile(EtfConstants.ETF_TESTDRIVERS_DIR));
		loader.setConfig(etfConfig);
		loader.load();
	}

	@PreDestroy
	private void shutdown() {
		loader.release();
	}

	TestRunTask create(TestRunDto testRun, TaskStateEventListener listener) throws ConfigurationException, ObjectWithIdNotFoundException, StoreException, ComponentNotLoadedException {

		TestProjectDto loadedTp = null;
		TestRunTaskFactory testRunTaskFactory = null;
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
	}

	List<TestProjectDto> getAvailableProjects() throws ConfigurationException, StoreException {
		final List<TestProjectDto> testProjects = new ArrayList<>();
		for (TestRunTaskFactory testRunTaskFactory : loader.getFactories()) {
			final Collection<TestProjectDto> projs = testRunTaskFactory.getTestProjectStore().getAll();
			if (projs != null) {
				testProjects.addAll(projs);
			}
		}
		return testProjects;
	}

	TestProjectDto getProjectById(EID id) throws ConfigurationException, ObjectWithIdNotFoundException, StoreException {
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

	Collection<TestProjectDto> getAvailableProjects(String testDriverId) throws ConfigurationException, StoreException, ComponentNotLoadedException {
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

}
