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

import static de.interactive_instruments.etf.webapp.controller.WebAppUtils.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import de.interactive_instruments.etf.dal.dao.Dao;
import de.interactive_instruments.etf.dal.dto.test.ExecutableTestSuiteDto;
import de.interactive_instruments.etf.dal.dto.translation.TranslationTemplateBundleDto;
import de.interactive_instruments.etf.dal.dto.translation.TranslationTemplateDto;
import de.interactive_instruments.etf.model.OutputFormat;
import de.interactive_instruments.etf.model.ParameterSet;
import de.interactive_instruments.etf.model.Parameterizable;
import de.interactive_instruments.etf.testdriver.DependencyGraph;
import de.interactive_instruments.etf.webapp.EtfWebApi;
import de.interactive_instruments.etf.webapp.WebAppConstants;
import de.interactive_instruments.etf.webapp.helpers.View;
import de.interactive_instruments.exceptions.ObjectWithIdNotFoundException;
import de.interactive_instruments.exceptions.StorageException;
import de.interactive_instruments.exceptions.config.ConfigurationException;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * Test project controller used for accessing metadata of a test project
 */
@Controller
public class TestProjectController {

	@Autowired
	ServletContext servletContext;

	@Autowired
	private TestDriverService testDriverService;

	@Autowired
	private DataStorageService dataStorageService;

	private final Logger logger = LoggerFactory.getLogger(TestProjectController.class);
	private Dao<ExecutableTestSuiteDto> etsDao;
	private OutputFormat xmlOutputFormat;

	@PostConstruct
	private void init() throws IOException, TransformerConfigurationException {
		etsDao = dataStorageService.getDao(ExecutableTestSuiteDto.class);
		xmlOutputFormat = etsDao.getOutputFormats().values().iterator().next();
		logger.info("Executable Test Suite controller initialized!");
	}

	@RequestMapping(value = "/testprojects", method = RequestMethod.GET)
	public String overview(
			@RequestParam(value = WebAppConstants.TESTDOMAIN_PARAM, required = false) String testDomainParam,
			@CookieValue(value = WebAppConstants.TESTDOMAIN_PARAM, defaultValue = "") String testDomainCookie,
			HttpServletResponse response,
			Model model)
			throws ConfigurationException, StorageException {
		model.addAttribute("testprojects", testDriverService.getExecutableTestSuites());
		/*
		final String testDomain;
		if (testDomainParam == null) {
			testDomain = testDomainCookie;
		} else {
			testDomain = testDomainParam;
			response.addCookie(new Cookie(WebAppUtils.TESTDOMAIN_PARAM, testDomain));
		}

		if (SUtils.isNullOrEmpty(testDomain)) {
			model.addAttribute("testprojects", taskFactory.getAvailableProjects());
		} else {
			response.addCookie(new Cookie(WebAppUtils.TESTDOMAIN_PARAM, testDomain));
			model.addAttribute("testprojects",
					taskFactory.getAvailableProjects().streamAsXml2().filter(p -> Objects.equals(
							p.getProperties().getProperty(EtfConstants.ETF_TESTDOMAIN_PK),
							testDomain)).collect(Collectors.toList()));
		}
		*/
		return "testprojects/overview";
	}

	@ApiOperation(value = "Get all Executable Test Suites", tags = {"Service Capabilities"}, response = ExecutableTestSuiteDto.class, responseContainer = "List")
	@RequestMapping(value = API_BASE_URL + "/ExecutableTestSuites.json", method = RequestMethod.GET)
	public void listExecutableTestSuitesJson(HttpServletResponse response) throws StorageException, ConfigurationException, IOException, ObjectWithIdNotFoundException {
		streamAsJson2(etsDao, response, null);
	}

	@ApiOperation(value = "Get all Executable Test Suites", tags = {"Service Capabilities"})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "EtfItemCollection with multiple Executable Test Suite", reference = "www.interactive-instruments.de"),
	})
	@RequestMapping(value = {API_BASE_URL + "/ExecutableTestSuites", API_BASE_URL + "/ExecutableTestSuites.xml"}, method = RequestMethod.GET)
	public void listExecutableTestSuitesXml(HttpServletResponse response) throws IOException, StorageException, ObjectWithIdNotFoundException {
		streamAsXml2(etsDao, xmlOutputFormat, response, null);
	}

	@ApiOperation(value = "Get all Executable Test Suites", tags = {"Service Capabilities"})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "EtfItemCollection with one Executable Test Suite", reference = "www.interactive-instruments.de"),
			@ApiResponse(code = 404, message = "Executable Test Suite not found")
	})
	@RequestMapping(value = {API_BASE_URL + "/ExecutableTestSuites/{id}"}, method = RequestMethod.GET)
	public void executableTestSuiteById(@PathVariable String id, HttpServletResponse response) throws IOException, StorageException, ObjectWithIdNotFoundException {
		streamAsXml2(etsDao, xmlOutputFormat, response, id);
	}

	@ApiOperation(value = "Get all Executable Test Suites", tags = {"Service Capabilities"}, response = Parameterizable.Parameter.class, responseContainer = "List")
	@RequestMapping(value = API_BASE_URL + "/ExecutableTestSuites/{etsId}/arguments.json", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody Collection<Parameterizable.Parameter> etsParameterById(@PathVariable String etsId) throws StorageException, ConfigurationException, ObjectWithIdNotFoundException {
		// Get ETS and translation bundle to translate the description text
		final ExecutableTestSuiteDto dto = etsDao.getById(WebAppUtils.toEid(etsId)).getDto();
		if (dto.getParameters() == null) {
			return null;
		}
		final ParameterSet transferParameters = new ParameterSet();
		final TranslationTemplateBundleDto bundleDto = dto.getTranslationTemplateBundle();
		for (final Parameterizable.Parameter parameter : dto.getParameters().getParameters()) {
			final ParameterSet.MutableParameter copiedParam = new ParameterSet.MutableParameter(parameter);
			if (parameter.getDescription() != null) {
				final TranslationTemplateDto template = bundleDto.getTranslationTemplate(parameter.getDescription(), "en");
				if (template != null && template.getStrWithTokens() != null) {
					copiedParam.setDescription(template.getStrWithTokens());
				}
			}
			transferParameters.addParameter(copiedParam);
		}
		return transferParameters.getParameters();
	}

	private static class DependenciesJsonView {
		public final String id;
		public final String description;
		public final String label;

		DependenciesJsonView(ExecutableTestSuiteDto ets) {
			id = ets.getId().toString();
			description = ets.getDescription();
			label = ets.getLabel();
		}
	}

	@ApiOperation(value = "Get the dependencies of an Executable Test Suite", tags = {"Service Capabilities"}, response = DependenciesJsonView.class, responseContainer = "List")
	@RequestMapping(value = API_BASE_URL + "/ExecutableTestSuites/{etsId}/dependencies.json", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody List<DependenciesJsonView> etsDependenciesById(@PathVariable String etsId) throws StorageException, ConfigurationException, ObjectWithIdNotFoundException {
		final Collection<ExecutableTestSuiteDto> dependencies = testDriverService.getExecutableTestSuiteById(WebAppUtils.toEid(etsId)).getDependencies();
		final DependencyGraph<ExecutableTestSuiteDto> graph = new DependencyGraph(dependencies);
		final List<ExecutableTestSuiteDto> sortedDependencies = graph.sort();
		final List<DependenciesJsonView> depsJson = sortedDependencies.stream().map(DependenciesJsonView::new).collect(Collectors.toList());
		return depsJson;
	}

}
