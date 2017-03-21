/*
 * Copyright ${year} interactive instruments GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.interactive_instruments.etf.webapp.controller;

import de.interactive_instruments.TimeUtils;
import de.interactive_instruments.etf.dal.dao.PreparedDto;
import de.interactive_instruments.etf.dal.dao.WriteDao;
import de.interactive_instruments.etf.dal.dao.WriteDaoListener;
import de.interactive_instruments.etf.dal.dto.test.ExecutableTestSuiteDto;
import de.interactive_instruments.etf.webapp.WebAppConstants;
import de.interactive_instruments.etf.webapp.helpers.View;
import de.interactive_instruments.exceptions.ObjectWithIdNotFoundException;
import de.interactive_instruments.exceptions.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

/**
 * Test project controller used for accessing metadata of a test project
 */
@RestController
public class FeedController implements WriteDaoListener {

	@Autowired
	private DataStorageService dataStorageService;

	private final Logger logger = LoggerFactory.getLogger(FeedController.class);

	private final static String feedUrl = "/updates/atom.xml";
	private final static String fullUrl = EtfConfigController.getInstance().
			getProperty(EtfConfigController.ETF_API_BASE_URL)+feedUrl;

	private final static String feed = ""
			+ "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
			+ "<feed xmlns=\"http://www.w3.org/2005/Atom\">"
			+ "<id>"+fullUrl+"</id>"
			+ "<title>"+View.getBrandingText()+" update feed</title>"
			+ "<link  rel=\"self\" href=\""+fullUrl+"\"/>"
			+"<generator>ETF-"+EtfConfigController.getInstance().getProperty(
			EtfConfigController.getInstance().getVersion())+"</generator>"
			+"<updated>"+TimeUtils.dateToIsoString(new Date())+"</updated>"
			+"<category term=\"etf_instance_updates\"/>"
			+"<category term=\"etf_ets_updates\"/>";

	@PostConstruct
	private void init() {
		final WriteDao<ExecutableTestSuiteDto> etsDao =
				((WriteDao) dataStorageService.getDao(ExecutableTestSuiteDto.class));
		etsDao.registerListener(this);
		// logger.info("FeedController controller initialized!");
		feedAddInstanceStarted();
	}

	@PreDestroy
	private void shutdown() {
		feedAddInstanceStopped();
	}


	private void feedAddInstanceStarted() {

	}

	private void feedAddInstanceStopped() {

	}

	// @RequestMapping(
	// 		value = WebAppConstants.API_BASE_URL+feedUrl,
	//		method = RequestMethod.GET, produces = "application/atom+xml")
	public void getUpdates(HttpServletResponse response) throws IOException, StorageException, ObjectWithIdNotFoundException {

	}

	@Override
	public void writeOperationPerformed(final EventType eventType, final PreparedDto preparedDto) {

	}
}
