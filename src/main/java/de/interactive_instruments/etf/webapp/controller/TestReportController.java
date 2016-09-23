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

import static de.interactive_instruments.etf.webapp.controller.WebAppUtils.ALL_FILTER;
import static de.interactive_instruments.etf.webapp.controller.WebAppUtils.API_BASE_URL;
import static de.interactive_instruments.etf.webapp.controller.WebAppUtils.streamAsJson2;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerConfigurationException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import de.interactive_instruments.IFile;
import de.interactive_instruments.SUtils;
import de.interactive_instruments.UriUtils;
import de.interactive_instruments.etf.EtfConstants;
import de.interactive_instruments.etf.dal.dao.*;
import de.interactive_instruments.etf.dal.dto.result.AttachmentDto;
import de.interactive_instruments.etf.dal.dto.result.TestTaskResultDto;
import de.interactive_instruments.etf.dal.dto.run.TestRunDto;
import de.interactive_instruments.etf.dal.dto.run.TestTaskDto;
import de.interactive_instruments.etf.model.OutputFormat;
import de.interactive_instruments.etf.testdriver.TestRun;
import de.interactive_instruments.etf.webapp.WebAppConstants;
import de.interactive_instruments.exceptions.InitializationException;
import de.interactive_instruments.exceptions.InvalidStateTransitionException;
import de.interactive_instruments.exceptions.ObjectWithIdNotFoundException;
import de.interactive_instruments.exceptions.StorageException;
import de.interactive_instruments.exceptions.config.ConfigurationException;
import io.swagger.annotations.ApiOperation;

/**
 * Test result controller for viewing and comparing test results
 */
@Controller
public class TestReportController {

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

	@Autowired
	ServletContext servletContext;

	@Autowired
	EtfConfigController etfConfig;

	@Autowired
	DataStorageService dataStorageService;

	@Autowired
	TestRunController testRunController;

	private IFile reportDir;
	private IFile stylesheetFile;
	private Dao<TestRunDto> testRunDao;
	private Dao<TestTaskResultDto> testTaskResultDao;
	private OutputFormat testRunHtmlReportFormat;
	private OutputFormat testRunXmlOutputFormat;

	// TODO report comparison output format
	// private XslReportTransformer comparisonTransformer;
	private final Logger logger = LoggerFactory.getLogger(TestReportController.class);;

	public TestReportController() {

	}

	@PostConstruct
	public void init() throws IOException, TransformerConfigurationException, StorageException,
			ConfigurationException, InvalidStateTransitionException, InitializationException {

		final IFile etfDir = new IFile(servletContext.getRealPath(
				"/WEB-INF/etf"), "ETF");
		etfDir.expectDirIsReadable();
		reportDir = etfConfig.getPropertyAsFile(EtfConstants.ETF_DATASOURCE_DIR).expandPath("obj");

		testRunDao = dataStorageService.getDao(TestRunDto.class);

		testTaskResultDao = dataStorageService.getDao(TestTaskResultDto.class);

		for (final OutputFormat outputFormat : testRunDao.getOutputFormats().values()) {
			if ("text/html".equals(outputFormat.getMediaTypeType().getType())) {
				this.testRunHtmlReportFormat = outputFormat;
			}
			if ("text/xml".equals(outputFormat.getMediaTypeType().getType())) {
				this.testRunXmlOutputFormat = outputFormat;
			}
		}
		logger.info("Result controller initialized!");
	}

	@PreDestroy
	private void shutdown() {
		testRunDao.release();
		// testTaskResultDao.release();
	}

	/*
	public synchronized void saveReport(final TestReport report) throws StorageException, AssemblerException, ObjectWithIdNotFoundException {
		this.store.update(this.store.getDtoAssembler().assembleDto(report));
	}

	TestReportDto createReport(String label, TestObjectDto tO) throws StorageException {
		return this.store.create(label, tO);
	}

	void updateReport(TestReport report) throws AssemblerException, StorageException, ObjectWithIdNotFoundException {
		this.store.update(this.store.getDtoAssembler().assembleDto(report));
	}
	*/

	@RequestMapping(value = "/reports/{id}", method = RequestMethod.GET)
	public void getById(
			@PathVariable String id,
			@RequestParam(value = "download", required = false) String download,
			HttpServletResponse response) {

		try {
			final ServletOutputStream out = response.getOutputStream();
			if (Objects.equals(download, "true")) {
				response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
				final PreparedDto<TestRunDto> dto = testRunDao.getById(WebAppUtils.toEid(id));
				final String label = dto.getDto().getLabel();
				final String reportFileName = IFile.sanitize(label);
				response.setContentType(MediaType.TEXT_HTML_VALUE);
				response.setHeader("Content-Disposition", "attachment; filename=" + reportFileName + ".html");
				dto.streamTo(testRunHtmlReportFormat, null, out);
			} else {
				response.setContentType(MediaType.TEXT_HTML_VALUE);
				final PreparedDto<TestRunDto> dto = testRunDao.getById(WebAppUtils.toEid(id));
				dto.streamTo(testRunHtmlReportFormat, null, out);
			}
		} catch (final Exception e) {
			logger.error("Error opening report ", e);
		}
	}

	private final static String TEST_RESULTS_URL = API_BASE_URL + "/TestResults";

	@ApiOperation(value = "Get all Test Results", tags = {"Test Results"})
	@RequestMapping(value = {TEST_RESULTS_URL, TEST_RESULTS_URL + ".xml"}, method = RequestMethod.GET)
	public void testRunsXml(HttpServletResponse response) throws StorageException, IOException, ObjectWithIdNotFoundException {
		WebAppUtils.streamAsXml2(testRunDao, testRunXmlOutputFormat, response, null);
	}

	@ApiOperation(value = "Get all Test Results", tags = {"Test Results"})
	@RequestMapping(value = {TEST_RESULTS_URL + "/{id}", TEST_RESULTS_URL + "/{id}.xml"}, method = RequestMethod.GET)
	public void testRunByIdXml(@PathVariable String id, HttpServletResponse response) throws StorageException, IOException, ObjectWithIdNotFoundException {
		WebAppUtils.streamAsXml2(testRunDao, testRunXmlOutputFormat, response, id);
	}

	@ApiOperation(value = "Get all Test Results", tags = {"Test Results"})
	@RequestMapping(value = {TEST_RESULTS_URL + ".json"}, method = RequestMethod.GET, produces = "application/json")
	public Collection<TestRunDto> testRunsJson() throws IOException, StorageException, ObjectWithIdNotFoundException {
		return testRunDao.getAll(ALL_FILTER).asCollection();
	}

	@ApiOperation(value = "Get all Test Results", tags = {"Test Results"})
	@RequestMapping(value = {TEST_RESULTS_URL + "/{id}.json"}, method = RequestMethod.GET, produces = "application/json")
	public TestRunDto testRunByIdJson(@PathVariable String id) throws IOException, StorageException, ObjectWithIdNotFoundException {
		return testRunDao.getById(WebAppUtils.toEid(id)).getDto();
	}

	@ApiOperation(value = "Get a Test Run log by ID", tags = {"Test Results"})
	@RequestMapping(value = {TEST_RESULTS_URL + "/{id}/log"}, method = RequestMethod.GET)
	public void testRunLog(@PathVariable String id, HttpServletResponse response) throws StorageException, IOException, ObjectWithIdNotFoundException {
		final TestRunDto dto = testRunDao.getById(WebAppUtils.toEid(id)).getDto();
		if (dto.getLogPath() != null) {
			response.setContentType(MediaType.TEXT_PLAIN_VALUE);
			final ServletOutputStream out = response.getOutputStream();
			// FIXME log path
			IOUtils.copy(new FileInputStream(dto.getLogPath()), out);
		}
	}

	@ApiOperation(value = "Get Test Report by ID", tags = {"Test Results"})
	@RequestMapping(value = {TEST_RESULTS_URL + "/{id}.html"}, method = RequestMethod.GET)
	public void getReportById(
			@PathVariable String id,
			@RequestParam(value = "download", required = false) String download,
			HttpServletResponse response) {
		getById(id, download, response);
	}

	@ApiOperation(value = "Get all Test Result attachments", tags = {"Test Results"})
	@RequestMapping(value = {API_BASE_URL + "/TestTaskResults/{resultId}/Attachments"}, method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody Collection<AttachmentDto> getAttachmentsAsJson(
			@PathVariable String resultId) throws ObjectWithIdNotFoundException, StorageException, IOException {

		final TestTaskResultDto testTaskResultDto = testTaskResultDao.getById(WebAppUtils.toEid(resultId)).getDto();
		return testTaskResultDto.getAttachments();
	}

	@ApiOperation(value = "Get a Test Result's attachment by ID", tags = {"Test Results"})
	@RequestMapping(value = {API_BASE_URL + "/TestTaskResults/{resultId}/Attachments/{attachmentId}"}, method = RequestMethod.GET)
	public void getAttachmentById(
			@PathVariable String resultId,
			@PathVariable String attachmentId,
			HttpServletResponse response) throws ObjectWithIdNotFoundException, StorageException, IOException {

		final TestTaskResultDto testTaskResultDto = testTaskResultDao.getById(WebAppUtils.toEid(resultId)).getDto();
		final AttachmentDto attachmentDto = testTaskResultDto.getAttachmentById(WebAppUtils.toEid(attachmentId));
		if (attachmentDto == null) {
			throw new ObjectWithIdNotFoundException(attachmentId);
		}

		if (SUtils.isNullOrEmpty(attachmentDto.getMimeType())) {
			response.setContentType(MediaType.TEXT_PLAIN_VALUE);
		} else {
			response.setContentType(attachmentDto.getMimeType());
		}
		UriUtils.stream(attachmentDto.getReferencedData(), response.getOutputStream());
	}

	@RequestMapping(value = "/reports/{id}/testtaskresult/{resultTaskId}/attachments/{attachmentId}", method = RequestMethod.GET)
	public synchronized void getAppendixItemById(
			@PathVariable String id,
			@PathVariable String resultTaskId,
			@PathVariable String attachmentId,
			HttpServletResponse response) {
		try {
			final ServletOutputStream out = response.getOutputStream();
			final PreparedDto<TestRunDto> dto = testRunDao.getById(WebAppUtils.toEid(id));
			// TODO
			/*
			final AttachmentDto attachment = dto.getDto().getAttachmentById(attachmentId);
			if (attachment == null) {
				throw new ObjectWithIdNotFoundException(attachmentId);
			}
			UriUtils.streamAsXml2(attachment.getReferencedData(), out);
			response.setContentType(attachment.getMimeType());
			*/
		} catch (final Exception e) {
			logger.error("Error opening attachment ", e);
		}
	}

	@RequestMapping(value = "/reports", method = RequestMethod.GET)
	public String overview(
			@CookieValue(value = WebAppConstants.TESTDOMAIN_PARAM, defaultValue = "") String testDomain,
			Model model)
			throws ConfigurationException, StorageException {
		// TODO tag filter

		model.addAttribute("testRuns", this.testRunDao.getAll(FILTER_GET_ALL).asCollection());
		model.addAttribute("runningTestRuns", testRunController.getTestRunIds());

		return "reports/overview";
	}

	@RequestMapping(value = "/reports/{id}/delete", method = RequestMethod.GET)
	public synchronized String delete(@PathVariable String id) throws StorageException, ObjectWithIdNotFoundException {
		((WriteDao) this.testRunDao).delete(WebAppUtils.toEid(id));
		return "redirect:/reports";
	}

	public void storeTestRun(final TestRunDto testRunDto) throws StorageException {
		// create copy and remove test task result ids
		final TestRunDto dto = testRunDto.createCopy();
		if (dto.getTestTasks() != null) {
			for (final TestTaskDto testTaskDto : dto.getTestTasks()) {
				testTaskDto.setTestTaskResult(null);
			}
		}
		((WriteDao<TestRunDto>) testRunDao).add(dto);
	}

	public void updateTestRun(final TestRun testRunDto) throws ObjectWithIdNotFoundException, StorageException {
		((WriteDao<TestRunDto>) testRunDao).replace(testRunDto.getResult());
	}

	/*
	// TODO report comparison output format
	@RequestMapping(value = "/reportcomparison", method = RequestMethod.GET)
	public String compare(Model model) throws ConfigurationException, StorageException {
		model.addAttribute(new ReportSelections());
		model.addAttribute("reports", this.store.getAll());
		return "reports/compare";
	}

	@RequestMapping(value = "/reportcomparison/result", method = RequestMethod.POST)
	public void showDiffs(@ModelAttribute("testObject") ReportSelections reportSelections, HttpServletResponse response) throws IOException, TransformationException {
		final ServletOutputStream out = response.getOutputStream();
		response.setContentType(MediaType.TEXT_HTML_VALUE);
		store.diffTo(reportSelections.getReport1(), reportSelections.getReport2(), "html_diff", out);
	}
	*/
}
