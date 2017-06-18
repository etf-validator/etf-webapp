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

import static de.interactive_instruments.etf.webapp.SwaggerConfig.TEST_RESULTS_TAG_NAME;
import static de.interactive_instruments.etf.webapp.WebAppConstants.API_BASE_URL;
import static de.interactive_instruments.etf.webapp.dto.DocumentationConstants.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerConfigurationException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import de.interactive_instruments.IFile;
import de.interactive_instruments.SUtils;
import de.interactive_instruments.UriUtils;
import de.interactive_instruments.etf.EtfConstants;
import de.interactive_instruments.etf.dal.dao.Dao;
import de.interactive_instruments.etf.dal.dao.PreparedDto;
import de.interactive_instruments.etf.dal.dao.WriteDao;
import de.interactive_instruments.etf.dal.dto.Dto;
import de.interactive_instruments.etf.dal.dto.result.AttachmentDto;
import de.interactive_instruments.etf.dal.dto.result.TestResultStatus;
import de.interactive_instruments.etf.dal.dto.result.TestTaskResultDto;
import de.interactive_instruments.etf.dal.dto.run.TestRunDto;
import de.interactive_instruments.etf.dal.dto.run.TestTaskDto;
import de.interactive_instruments.etf.model.EID;
import de.interactive_instruments.etf.model.OutputFormat;
import de.interactive_instruments.etf.testdriver.TestRun;
import de.interactive_instruments.etf.webapp.conversion.EidConverter;
import de.interactive_instruments.etf.webapp.dto.AttachmentCollection;
import de.interactive_instruments.etf.webapp.helpers.CacheControl;
import de.interactive_instruments.etf.webapp.helpers.SimpleFilter;
import de.interactive_instruments.exceptions.InitializationException;
import de.interactive_instruments.exceptions.InvalidStateTransitionException;
import de.interactive_instruments.exceptions.ObjectWithIdNotFoundException;
import de.interactive_instruments.exceptions.StorageException;
import de.interactive_instruments.exceptions.config.ConfigurationException;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * Test result controller for viewing and comparing test results
 */
@RestController
public class TestResultController {

	@Autowired
	ServletContext servletContext;

	@Autowired
	EtfConfigController etfConfig;

	@Autowired
	DataStorageService dataStorageService;

	@Autowired
	TestRunController testRunController;

	@Autowired
	private StreamingService streaming;

	private IFile reportDir;
	private IFile stylesheetFile;
	private Dao<TestRunDto> testRunDao;
	private Dao<TestTaskResultDto> testTaskResultDao;
	private OutputFormat testRunHtmlReportFormat;
	private final static String TEST_RUNS_URL = API_BASE_URL + "/TestRuns";
	private final static String TEST_TASKS_URL = API_BASE_URL + "/TestTaskResults";

	// TODO report comparison output format
	// private XslReportTransformer comparisonTransformer;
	private final Logger logger = LoggerFactory.getLogger(TestResultController.class);;

	private final static String TEST_RUN_DESCRIPTION = "The Test Run model is described in the "
			+ "[XML schema documentation](https://services.interactive-instruments.de/etf/schemadoc/run_xsd.html#TestRun). "
			+ ETF_ITEM_COLLECTION_DESCRIPTION;

	private final static String TEST_TASK_RESULT_NOTE = " Note: a Test Run consists of one or multiple Test Task Results. "
			+ "A Test Task Result represents the result of the execution of one single Test Suite. "
			+ "Use the Test Run interface to get all results of a Test Run and the Test Task Result interfaces to get only one single result. ";

	private final static String TEST_TASK_RESULT_DESCRIPTION = "The Test Task model is described in the "
			+ "[XML schema documentation](https://services.interactive-instruments.de/etf/schemadoc/result_xsd.html#TestTaskResult). "
			+ TEST_TASK_RESULT_NOTE
			+ ETF_ITEM_COLLECTION_DESCRIPTION;

	public TestResultController() {

	}

	@PostConstruct
	public void init() throws IOException, TransformerConfigurationException, StorageException,
			ConfigurationException, InvalidStateTransitionException, InitializationException {

		reportDir = etfConfig.getPropertyAsFile(EtfConstants.ETF_DATASOURCE_DIR).expandPath("obj");

		testRunDao = dataStorageService.getDao(TestRunDto.class);

		testTaskResultDao = dataStorageService.getDao(TestTaskResultDto.class);

		for (final OutputFormat outputFormat : testRunDao.getOutputFormats().values()) {
			if ("text/html".equals(outputFormat.getMediaTypeType().getType())) {
				this.testRunHtmlReportFormat = outputFormat;
			}
		}

		streaming.prepareCache(testRunDao, new SimpleFilter());

		logger.info("Result controller initialized!");
	}

	@PreDestroy
	private void shutdown() {
		testRunDao.release();
		// testTaskResultDao.release();
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

	private void getByIdHtml(
			final Dao<? extends Dto> dao,
			final String id,
			final String download,
			final HttpServletRequest request,
			final HttpServletResponse response) throws LocalizableApiError {
		if (CacheControl.clientNeedsUpdate(dao, request, response, TimeUnit.SECONDS.toDays(31)))
			try {
				final ServletOutputStream out = response.getOutputStream();
				final PreparedDto preparedDto = dao.getById(EidConverter.toEid(id));

				if (Objects.equals(download, "true")) {
					final String reportFileName;
					if (preparedDto.getDto() instanceof TestRun) {
						final TestRunDto testRunDto = (TestRunDto) preparedDto.getDto();
						if (TestResultStatus.valueOf(testRunDto.getTestResultStatus()) == TestResultStatus.UNDEFINED) {
							response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE);
							return;
						}
						reportFileName = testRunDto.getLabel();
					} else if (preparedDto.getDto() instanceof TestTaskResultDto) {
						final TestTaskResultDto testTaskResultDto = (TestTaskResultDto) preparedDto.getDto();
						if (testTaskResultDto.getResultStatus() == TestResultStatus.UNDEFINED) {
							response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE);
							return;
						}
						reportFileName = testTaskResultDto.getId().getId();
					} else {
						reportFileName = "Out";
					}
					response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
					response.setContentType(MediaType.TEXT_HTML_VALUE);
					response.setHeader("Content-Disposition",
							"attachment; filename=" + IFile.sanitize(reportFileName) + ".html");
					preparedDto.streamTo(testRunHtmlReportFormat, null, out);
				} else {
					response.setContentType(MediaType.TEXT_HTML_VALUE);
					preparedDto.streamTo(testRunHtmlReportFormat, null, out);
				}
			} catch (final ObjectWithIdNotFoundException e) {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				logger.error("Report not found: ", e);
			} catch (final StorageException e) {
				throw new LocalizableApiError(e);
			} catch (final IOException e) {
				throw new LocalizableApiError(e);
			}
	}

	//
	// Rest interfaces
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@ApiOperation(value = "Get multiple Test Results as XML", notes = TEST_RUN_DESCRIPTION, tags = {TEST_RESULTS_TAG_NAME})
	@RequestMapping(value = {TEST_RUNS_URL + ".xml"}, method = RequestMethod.GET)
	public void testRunsXml(
			@ApiParam(value = OFFSET_DESCRIPTION) @RequestParam(required = false, defaultValue = "0") int offset,
			@ApiParam(value = LIMIT_DESCRIPTION) @RequestParam(required = false, defaultValue = "0") int limit,
			HttpServletRequest request,
			HttpServletResponse response) throws StorageException, IOException, ObjectWithIdNotFoundException {
		streaming.asXml2(testRunDao, request, response, new SimpleFilter(offset, limit));
	}

	@ApiOperation(value = "Get a single Test Result as XML", notes = TEST_RUN_DESCRIPTION, tags = {TEST_RESULTS_TAG_NAME})
	@RequestMapping(value = {TEST_RUNS_URL + "/{id}.xml"}, method = RequestMethod.GET)
	public void testRunByIdXml(
			@ApiParam(value = "Test Run ID. "
					+ EID_DESCRIPTION, example = EID_EXAMPLE, required = true) @PathVariable String id,
			HttpServletRequest request, HttpServletResponse response)
			throws StorageException, IOException, ObjectWithIdNotFoundException {
		streaming.asXml2(testRunDao, request, response, id);
	}

	@ApiOperation(value = "Get multiple Test Results as JSON", notes = "Transforms multiple Test Run Results to JSON. "
			+ TEST_RUN_DESCRIPTION, tags = {TEST_RESULTS_TAG_NAME})
	@RequestMapping(value = {TEST_RUNS_URL, TEST_RUNS_URL + ".json"}, method = RequestMethod.GET)
	public void testRunsJson(
			@ApiParam(value = OFFSET_DESCRIPTION) @RequestParam(required = false, defaultValue = "0") int offset,
			@ApiParam(value = LIMIT_DESCRIPTION) @RequestParam(required = false, defaultValue = "0") int limit,
			HttpServletRequest request,
			HttpServletResponse response) throws IOException, StorageException, ObjectWithIdNotFoundException {
		streaming.asJson2(testRunDao, request, response, new SimpleFilter(offset, limit));
	}

	@ApiOperation(value = "Get a single Test Result as JSON", notes = "Transforms one Test Run Results to JSON. "
			+ TEST_RUN_DESCRIPTION, tags = {TEST_RESULTS_TAG_NAME})
	@RequestMapping(value = {TEST_RUNS_URL + "/{id}", TEST_RUNS_URL + "/{id}.json"}, method = RequestMethod.GET)
	public void testRunByIdJson(
			@ApiParam(value = "Test Run ID. "
					+ EID_DESCRIPTION, example = EID_EXAMPLE, required = true) @PathVariable String id,
			HttpServletRequest request,
			HttpServletResponse response) throws IOException, StorageException, ObjectWithIdNotFoundException {
		streaming.asJson2(testRunDao, request, response, id);
	}

	@ApiOperation(value = "Generate a HTML Test Report", notes = "Generates a HTML report from the test results of one Test Run.", produces = "text/html", tags = {
			TEST_RESULTS_TAG_NAME})
	@ApiResponses(value = {
			@ApiResponse(code = 202, message = "Test Run exists", response = Void.class),
			@ApiResponse(code = 404, message = "Test Run does not exist", response = Void.class),
			@ApiResponse(code = 406, message = "Test Run not finished yet", response = Void.class),
	})
	@RequestMapping(value = {TEST_RUNS_URL + "/{id}.html"}, method = RequestMethod.GET)
	public void getReportById(
			@ApiParam(value = "Test Run ID. "
					+ EID_DESCRIPTION, example = EID_EXAMPLE, required = true) @PathVariable String id,
			@ApiParam(value = "Download report", example = "true", allowableValues = "true,false", defaultValue = "false") @RequestParam(value = "download", required = false) String download,
			HttpServletRequest request,
			HttpServletResponse response) throws LocalizableApiError {
		getByIdHtml(testRunDao, id, download, request, response);
	}

	@ApiOperation(value = "Get a Test Run's log by ID", notes = "Retrieves all messages that were logged during a Test Run.", tags = {
			TEST_RESULTS_TAG_NAME})
	@RequestMapping(value = {TEST_RUNS_URL + "/{id}/log"}, method = RequestMethod.GET)
	public void testRunLog(
			@ApiParam(value = "Test Run ID. "
					+ EID_DESCRIPTION, example = EID_EXAMPLE, required = true) @PathVariable String id,
			HttpServletResponse response) throws StorageException, IOException, LocalizableApiError {
		try {
			final TestRunDto dto = testRunDao.getById(EidConverter.toEid(id)).getDto();
			if (dto.getLogPath() != null) {
				response.setContentType(MediaType.TEXT_PLAIN_VALUE);
				final ServletOutputStream out = response.getOutputStream();
				// FIXME log path
				IOUtils.copy(new FileInputStream(dto.getLogPath()), out);
			}
		} catch (ObjectWithIdNotFoundException e) {
			throw new LocalizableApiError(e);
		}
	}

	@ApiOperation(value = "Get all attachments of a Test Result as JSON", notes = "Retrieves meta information about all attachments that were saved during a Test Run.", tags = {
			TEST_RESULTS_TAG_NAME})
	@ApiResponses(value = {
			@ApiResponse(code = 202, message = "Attachment exists", response = Void.class),
			@ApiResponse(code = 404, message = "Test Task does not exist", response = Void.class),
	})
	@RequestMapping(value = {
			API_BASE_URL + "/TestTaskResults/{id}/Attachments"}, method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody Collection<AttachmentCollection.Attachment> getAttachmentsAsJson(
			@PathVariable String id) throws ObjectWithIdNotFoundException, StorageException, IOException {
		final TestTaskResultDto testTaskResultDto = testTaskResultDao.getById(EidConverter.toEid(id)).getDto();
		return AttachmentCollection.create(testTaskResultDto.getAttachments());
	}

	@ApiOperation(value = "Get a Test Result's attachment by ID", notes = "Get an attachment which was saved during a Test Run. The mime type can not be predicted, "
			+ "but text/plain will be used as fallback if the mime type could not be detected during the test run.", tags = {
					TEST_RESULTS_TAG_NAME})
	@ApiResponses(value = {
			@ApiResponse(code = 204, message = "Attachment exists", response = Void.class),
			@ApiResponse(code = 404, message = "Attachment does not exist", response = Void.class),
	})
	@RequestMapping(value = {
			API_BASE_URL + "/TestTaskResults/{id}/Attachments/{attachmentId}"}, method = RequestMethod.GET)
	public void getAttachmentById(
			@PathVariable String id,
			@PathVariable String attachmentId,
			HttpServletResponse response) throws ObjectWithIdNotFoundException, StorageException, IOException {

		final TestTaskResultDto testTaskResultDto = testTaskResultDao.getById(EidConverter.toEid(id)).getDto();
		final AttachmentDto attachmentDto = testTaskResultDto.getAttachmentById(EidConverter.toEid(attachmentId));
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

	@ApiOperation(value = "Get the result from a single Test Task within a Test Run as XML", notes = "Returns the result from a single Test Task as XML. "
			+ TEST_TASK_RESULT_DESCRIPTION, tags = {TEST_RESULTS_TAG_NAME})
	@ApiResponses(value = {
			@ApiResponse(code = 202, message = "Test Task exists", response = Void.class),
			@ApiResponse(code = 404, message = "Test Task does not exist", response = Void.class),
	})
	@RequestMapping(value = {TEST_TASKS_URL + "/{id}.xml"}, method = RequestMethod.GET)
	public void testTaskResultByIdXml(
			@ApiParam(value = "Test Task ID. "
					+ EID_DESCRIPTION, example = EID_EXAMPLE, required = true) @PathVariable String id,
			HttpServletRequest request, HttpServletResponse response)
			throws StorageException, IOException, ObjectWithIdNotFoundException {
		streaming.asXml2(testTaskResultDao, request, response, id);
	}

	@ApiOperation(value = "Get the result from a single Test Task within a Test Run as JSON", notes = "Transforms the result from a single Test Task to JSON. "
			+ TEST_TASK_RESULT_DESCRIPTION, tags = {TEST_RESULTS_TAG_NAME})
	@ApiResponses(value = {
			@ApiResponse(code = 202, message = "Test Task exists", response = Void.class),
			@ApiResponse(code = 404, message = "Test Task does not exist", response = Void.class),
	})
	@RequestMapping(value = {TEST_TASKS_URL + "/{id}.json"}, method = RequestMethod.GET)
	public void testTaskResultByIdJson(
			@ApiParam(value = "Test Task ID. "
					+ EID_DESCRIPTION, example = EID_EXAMPLE, required = true) @PathVariable String id,
			HttpServletRequest request, HttpServletResponse response)
			throws StorageException, IOException, ObjectWithIdNotFoundException {
		streaming.asJson2(testTaskResultDao, request, response, id);
	}

	@ApiOperation(value = "Generate a HTML Test Report from a single Test Task within a Test Run", notes = "Generates a HTML report from one single result of Test Task the within a Test Run. "
			+ TEST_TASK_RESULT_NOTE, produces = "text/html", tags = {TEST_RESULTS_TAG_NAME})
	@ApiResponses(value = {
			@ApiResponse(code = 202, message = "Test Task exists", response = Void.class),
			@ApiResponse(code = 404, message = "Test Task does not exist", response = Void.class),
			@ApiResponse(code = 406, message = "Test Task not finished yet", response = Void.class),
	})
	@RequestMapping(value = {TEST_TASKS_URL + "/{id}.html"}, method = RequestMethod.GET)
	public void testTaskResultByIdHtml(
			@ApiParam(value = "Test Task ID. "
					+ EID_DESCRIPTION, example = EID_EXAMPLE, required = true) @PathVariable String id,
			@ApiParam(value = "Download report", example = "true", allowableValues = "true,false", defaultValue = "false") @RequestParam(value = "download", required = false) String download,
			HttpServletRequest request,
			HttpServletResponse response) throws LocalizableApiError {
		getByIdHtml(testTaskResultDao, id, download, request, response);
	}

	@ApiOperation(value = "Check if the Test Task exists", notes = "Checks if a Test Task has been completed and saved. "
			+ TEST_TASK_RESULT_NOTE, tags = {TEST_RESULTS_TAG_NAME})
	@ApiResponses(value = {
			@ApiResponse(code = 204, message = "Test Task exists", response = Void.class),
			@ApiResponse(code = 404, message = "Test Task does not exist", response = Void.class),
	})
	@RequestMapping(value = {TEST_TASKS_URL + "/{id}"}, method = RequestMethod.HEAD)
	public ResponseEntity testTaskResultexists(
			@ApiParam(value = "Test Task ID. "
					+ EID_DESCRIPTION, example = EID_EXAMPLE, required = true) @PathVariable String id)
			throws StorageException {
		final EID eid = EidConverter.toEid(id);
		return testTaskResultDao.exists(eid) ? new ResponseEntity(HttpStatus.NO_CONTENT)
				: new ResponseEntity(HttpStatus.NOT_FOUND);
	}

}
