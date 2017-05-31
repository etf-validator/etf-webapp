/**
 * Copyright 2010-2017 interactive instruments GmbH
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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.interactive_instruments.etf.dal.dao.Dao;
import de.interactive_instruments.etf.dal.dao.DataStorage;
import de.interactive_instruments.etf.dal.dao.DataStorageRegistry;
import de.interactive_instruments.etf.dal.dao.basex.BsxDataStorage;
import de.interactive_instruments.etf.dal.dto.Dto;
import de.interactive_instruments.exceptions.InitializationException;
import de.interactive_instruments.exceptions.InvalidStateTransitionException;
import de.interactive_instruments.exceptions.config.ConfigurationException;

/**
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
@Service
public class DataStorageService {

	@Autowired
	ServletContext servletContext;

	@Autowired
	private EtfConfigController etfConfig;

	private DataStorage dataStorage;

	DataStorageService() {}

	private final Logger logger = LoggerFactory.getLogger(DataStorageService.class);

	@PostConstruct
	void init() throws InitializationException, InvalidStateTransitionException, ConfigurationException {
		dataStorage = new BsxDataStorage();
		dataStorage.getConfigurationProperties().setPropertiesFrom(etfConfig, true);
		dataStorage.init();
		DataStorageRegistry.instance().register(dataStorage);
		logger.info("Data Storage service initialized");
	}

	@PreDestroy
	private void shutdown() {
		dataStorage.release();
	}

	<T extends Dto> Dao<T> getDao(final Class<T> dtoType) {
		return dataStorage.getDao(dtoType);
	}

	DataStorage getDataStorage() {
		return dataStorage;
	}
}
