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
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerConfigurationException;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import de.interactive_instruments.IFile;
import de.interactive_instruments.SUtils;
import de.interactive_instruments.etf.EtfConstants;
import de.interactive_instruments.etf.dal.dto.plan.TestProjectDto;
import de.interactive_instruments.etf.driver.TestRunTaskFactory;
import de.interactive_instruments.etf.model.item.EID;
import de.interactive_instruments.etf.model.item.EidFactory;
import de.interactive_instruments.etf.webapp.WebAppConstants;
import de.interactive_instruments.exceptions.ExcUtils;
import de.interactive_instruments.exceptions.ObjectWithIdNotFoundException;
import de.interactive_instruments.exceptions.StoreException;
import de.interactive_instruments.exceptions.config.ConfigurationException;

/**
 * Test project controller used for accessing metadata of a test project
 */
@Controller
public class TestProjectController {

	@Autowired
	ServletContext servletContext;

	@Autowired
	private TestRunTaskFactoryService taskFactory;

	private final Logger logger = LoggerFactory.getLogger(TestProjectController.class);

	@PostConstruct
	private void init() throws IOException, TransformerConfigurationException {
		logger.info(this.getClass().getName() + " initialized!");
	}

	@RequestMapping(value = "/testproject/{componentType}/requiredproperties/{projectName}", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public Set<Map.Entry<String, String>> read(
			@PathVariable String componentType,
			@PathVariable String id)
					throws ConfigurationException, IOException, ObjectWithIdNotFoundException {
		for (TestRunTaskFactory testRunTaskFactory : taskFactory.getFactories()) {
			try {
				return testRunTaskFactory.getTestProjectStore().getById(EidFactory.getDefault().createFromStrAsStr(id)).namePropertyPairs();
			} catch (StoreException | ObjectWithIdNotFoundException e) {
				ExcUtils.supress(e);
			}
		}
		throw new ObjectWithIdNotFoundException(id);
	}

	@RequestMapping(value = "/testprojects", method = RequestMethod.GET)
	public String overview(
			@RequestParam(value = WebAppConstants.TESTDOMAIN_PARAM, required = false) String testDomainParam,
			@CookieValue(value = WebAppConstants.TESTDOMAIN_PARAM, defaultValue = "") String testDomainCookie,
			HttpServletResponse response,
			Model model)
					throws ConfigurationException, StoreException {
		final String testDomain;
		if (testDomainParam == null) {
			testDomain = testDomainCookie;
		} else {
			testDomain = testDomainParam;
			response.addCookie(new Cookie(WebAppConstants.TESTDOMAIN_PARAM, testDomain));
		}

		if (SUtils.isNullOrEmpty(testDomain)) {
			model.addAttribute("testprojects", taskFactory.getAvailableProjects());
		} else {
			response.addCookie(new Cookie(WebAppConstants.TESTDOMAIN_PARAM, testDomain));
			model.addAttribute("testprojects",
					taskFactory.getAvailableProjects().stream().filter(p -> Objects.equals(
							p.getProperties().getProperty(EtfConstants.ETF_TESTDOMAIN_PK),
							testDomain)).collect(Collectors.toList()));
		}

		return "testprojects/overview";
	}

	@RequestMapping(value = "/rest/testprojects", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody List<TestProjectDto> list() throws StoreException, ConfigurationException {
		return taskFactory.getAvailableProjects();
	}

	@RequestMapping(value = "/rest/testprojects/{id}/properties", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody Set<Map.Entry<String, String>> getProjectById(@PathVariable String id) throws StoreException, ConfigurationException, ObjectWithIdNotFoundException {
		return taskFactory.getProjectById(EidFactory.getDefault().createFromStrAsStr(id)).getProperties().namePropertyPairs().stream().filter(p -> !EtfConstants.ETF_PROPERTY_KEYS.contains(p.getKey())).collect(Collectors.toSet());
	}

}
