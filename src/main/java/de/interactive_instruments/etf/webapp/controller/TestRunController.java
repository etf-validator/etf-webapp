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

import static de.interactive_instruments.etf.webapp.SwaggerConfig.TEST_RESULTS_TAG_NAME;
import static de.interactive_instruments.etf.webapp.SwaggerConfig.TEST_RUNS_TAG_NAME;
import static de.interactive_instruments.etf.webapp.WebAppConstants.API_BASE_URL;
import static de.interactive_instruments.etf.webapp.dto.DocumentationConstants.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import de.interactive_instruments.SUtils;
import de.interactive_instruments.TimedExpiredItemsRemover;
import de.interactive_instruments.etf.EtfConstants;
import de.interactive_instruments.etf.dal.dao.Dao;
import de.interactive_instruments.etf.dal.dao.WriteDao;
import de.interactive_instruments.etf.dal.dto.capabilities.TestObjectDto;
import de.interactive_instruments.etf.dal.dto.run.TestRunDto;
import de.interactive_instruments.etf.dal.dto.test.ExecutableTestSuiteDto;
import de.interactive_instruments.etf.model.EID;
import de.interactive_instruments.etf.model.EidHolder;
import de.interactive_instruments.etf.model.EidHolderWithParent;
import de.interactive_instruments.etf.testdriver.*;
import de.interactive_instruments.etf.webapp.conversion.EidConverter;
import de.interactive_instruments.etf.webapp.dto.ApiError;
import de.interactive_instruments.etf.webapp.dto.StartTestRunRequest;
import de.interactive_instruments.etf.webapp.helpers.User;
import de.interactive_instruments.exceptions.ExcUtils;
import de.interactive_instruments.exceptions.ObjectWithIdNotFoundException;
import de.interactive_instruments.exceptions.StorageException;
import de.interactive_instruments.exceptions.config.ConfigurationException;
import de.interactive_instruments.exceptions.config.InvalidPropertyException;
import io.swagger.annotations.*;

/**
 * Test run controller for starting and monitoring test runs
 */
@RestController
public class TestRunController implements TestRunEventListener {

	@Autowired
	DataStorageService dataStorageService;

	@Autowired
	private TestDriverController testDriverController;

	@Autowired
	private TestObjectController testObjectController;

	@Autowired
	private TestResultController testResultController;

	@Autowired
	private EtfConfigController etfConfig;

	@Autowired
	private StatusController statusController;

	@Autowired
	private StreamingService streamingService;

	private Timer timer;

	boolean simplifiedWorkflows;

	private Dao<TestRunDto> testRunDao;

	private final static String TEST_RUNS_URL = API_BASE_URL + "/TestRuns";

	public static int MAX_PARALLEL_RUNS;
	public static int MAX_QUEUE_SIZE;

	private TaskPoolRegistry<TestRunDto, TestRun> taskPoolRegistry;
	
	private final Logger logger = LoggerFactory.getLogger(TestRunController.class);

	public TestRunController() {}

	final static class TaskProgressDto {

		@ApiModelProperty(value = "Completed Test Steps", example = "39", dataType = "int")
		private String val;
		@ApiModelProperty(value = "Estimated total number of Test Steps. Additional Test Steps can be generated dynamically during a test run to analyze certain aspects in detail.", example = "103", dataType = "int")
		private String max;
		@ApiModelProperty(value = "Log messages", example = "[ \"Test Run started\", \"Assertion X failed\"]")
		private List<String> log;

		// Completed
		private TaskProgressDto(String max, List<String> log) {
			this.val = max;
			this.max = max;
			this.log = log;
		}

		public TaskProgressDto() {}

		static TaskProgressDto createCompletedMsg(TaskProgress p) {
			return new TaskProgressDto(
					String.valueOf(p.getMaxSteps()), new ArrayList<>());
		}

		static TaskProgressDto createAlreadyCompleted() {
			return new TaskProgressDto(String.valueOf(100), new ArrayList<String>(1) {
				{
					add("Already completed");
				}
			});
		}

		static TaskProgressDto createTerminateddMsg(int max) {
			return new TaskProgressDto(String.valueOf(max), new ArrayList<String>(1) {
				{
					add("Terminated");
				}
			});
		}

		// Still running
		private TaskProgressDto(final TaskProgress p, final long pos) {
			this.val = String.valueOf(p.getCurrentStepsCompleted());
			if (p.getCurrentStepsCompleted() >= p.getMaxSteps()) {
				this.max = String.valueOf(p.getMaxSteps() + p.getCurrentStepsCompleted());
			} else {
				this.max = String.valueOf(p.getMaxSteps());
			}
			this.log = p.getLogReader().getLogMessages(pos);
		}

		public String getVal() {
			return val;
		}

		public String getMax() {
			return max;
		}

		public List<String> getLog() {
			return log;
		}
	}

	@ApiModel(description = "Simplified Test Run view")
	private static class TestRunsJsonView {
		@ApiModelProperty(value = EID_DESCRIPTION, example = EID_EXAMPLE)
		public final String id;

		@ApiModelProperty(value = TEST_RUN_LABEL_DESCRIPTION, example = TEST_RUN_LABEL_EXAMPLE)
		public final String label;

		@ApiModelProperty(value = "Number of Test Tasks. " + TEST_TASK_DESCRIPTION, example = "3", dataType = "int")
		public final int testTaskCount;

		@ApiModelProperty(value = "Start timestamp in milliseconds, measured between the time the test run was started"
				+ " and midnight, January 1, 1970 UTC(coordinated universal time).", example = "1488469744783")
		public final Date startTimestamp;

		@ApiModelProperty(value = "Percentage of overall completed Test Steps", example = "0.879")
		public final double percentStepsCompleted;

		public TestRunsJsonView(final TestRun t) {
			id = t.getId().getId();
			label = t.getLabel();
			testTaskCount = t.getTestTasks().size();
			startTimestamp = t.getProgress().getStartTimestamp();
			final double p = t.getProgress().getPercentStepsCompleted();
			percentStepsCompleted = p < 0.001 ? 0.001 : p;
		}
	}

	@PostConstruct
	public void init() throws ParseException, ConfigurationException, IOException, StorageException {
		logger.info(Runtime.getRuntime().availableProcessors() + " cores available.");

		// SEL dir
		System.setProperty("ETF_SEL_GROOVY",
				etfConfig.getPropertyAsFile(EtfConstants.ETF_PROJECTS_DIR).expandPath("sui").getPath());
		simplifiedWorkflows = "simplified".equals(etfConfig.getProperty(EtfConfigController.ETF_WORKFLOWS));
		testRunDao = dataStorageService.getDao(TestRunDto.class);

		timer = new Timer(true);
		// Trigger every 30 Minutes
		final TimedExpiredItemsRemover timedExpiredItemsRemover = new TimedExpiredItemsRemover();
		timedExpiredItemsRemover.addExpirationItemHolder(
				(l, timeUnit) -> taskPoolRegistry.removeDone(),
				0, TimeUnit.HOURS);
		// 7,5 minutes
		timer.scheduleAtFixedRate(timedExpiredItemsRemover, 450000, 450000);

		String maxThreads = etfConfig.getProperty("etf.testruns.threads.max");
		try {
			MAX_PARALLEL_RUNS = Integer.parseInt(maxThreads);
		}catch(NumberFormatException e) {
			if("auto".equals(maxThreads)) {
				MAX_PARALLEL_RUNS = Runtime.getRuntime().availableProcessors();
			}
			else {
				throw new RuntimeException(maxThreads+" is not a valid value for etf.testruns.max.threads");
			}
		}
		String maxQueues = etfConfig.getProperty("etf.testruns.queued.max");
		try {
			MAX_QUEUE_SIZE = Integer.parseInt(maxQueues);
		}catch(NumberFormatException e) {
			if("auto".equals(maxQueues)) {
				MAX_QUEUE_SIZE = Runtime.getRuntime().availableProcessors() * 3;
			}
			else {
				throw new RuntimeException(maxQueues+" is not a valid value for etf.testruns.max.queued");
			}
		}
		taskPoolRegistry = new TaskPoolRegistry<>(MAX_PARALLEL_RUNS,MAX_PARALLEL_RUNS,MAX_QUEUE_SIZE);
		
		logger.info("Test Run controller initialized!");
	}

	@PreDestroy
	public void shutdown() {
		logger.info("Shutting down TestRunController");
		if (this.timer != null) {
			timer.cancel();
		}
	}

	void addMetaData(final Model model) {
		model.addAttribute("testRuns", taskPoolRegistry.getTasks());
		model.addAttribute("testDriversInfo", testDriverController.getTestDriverInfo());
	}

	private void initAndSubmit(TestRunDto testRunDto) throws LocalizableApiError {
		try {
			final TestRun testRun = testDriverController.create(testRunDto);
			Objects.requireNonNull(testRun, "Test Driver created invalid TestRun").addTestRunEventListener(this);
			testRun.init();

			// Check if the test object has changed since the last run
			// and update the test object
			// todo
			/*
			final TestObject tO = testRunTask.getTestRun().getTestObject();
			if (testRunTask.getTestRun().isTestObjectResourceUpdateRequired() &&
					testObjectController.getTestObjStore().exists(tO.getId())) {
				testObjectController.getTestObjStore().update(tO);
			}
			*/
			testResultController.storeTestRun(testRunDto);
			logger.info("TestRun " + testRunDto.getDescriptiveLabel() + " initialized");
			taskPoolRegistry.submitTask(testRun);
		} catch (Exception e) {
			throw new LocalizableApiError(
					"l.internal.testrun.initialization.error",
					true, 500, e);
		}
	}

	@Override
	public void taskStateChangedEvent(final TestTask testTask, final TaskState.STATE current, final TaskState.STATE old) {
		logger.trace("TaskStateChanged event received from Test Task {} : {} -> {}", testTask.getId(),
				old == null ? "first light" : old, current);

	}

	@Override
	public void taskRunChangedEvent(final TestRun testRun, final TaskState.STATE current, final TaskState.STATE old) {
		logger.trace("TaskStateChanged event received from Test Run {} : {} -> {} (Test Run label: {})", testRun.getId(),
				old == null ? "first light" : old, current, testRun.getLabel());
		if (current.isCompleted()) {
			try {
				testResultController.updateTestRun(testRun);
			} catch (StorageException | ObjectWithIdNotFoundException e) {
				final String identifier = testRun != null ? testRun.getLabel() : "";
				logger.error("Test Run " + identifier + " could not be updated");
			}
		}
	}

	//
	// Rest interfaces
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@ApiOperation(value = "Get the Test Run progress as JSON", notes = "Retrieve one Test Run status including log messages, the estimated total number of Test Steps and the number of already executed Test Steps", produces = "application/json", tags = {
			TEST_RUNS_TAG_NAME})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Task progress returned", response = TaskProgressDto.class),
			@ApiResponse(code = 404, message = "Test Run not found", response = Void.class),
	})
	@RequestMapping(value = API_BASE_URL + "/TestRuns/{id}/progress", method = RequestMethod.GET)
	@ResponseBody
	public TaskProgressDto progressLog(
			@ApiParam(value = "Test Run ID. "
					+ EID_DESCRIPTION, example = EID_EXAMPLE, required = true) @PathVariable String id,
			@ApiParam(value = "The position in the logs from where to resume. "
					+ "The client may submit his current cached log message size to this interface, "
					+ "so that the service can skip the known messages and return only new ones. "
					+ "Example: the client received 3 log messages and should therefore invoke this interface with pos=3. "
					+ "In the meantime the service logged a total of 13 messages. As the client knows the first three "
					+ "messages the service will skip the first 3 messages and return the 10 new messages."
					+ "The test run completed when the value of the val property and the value of the pos property are equal. ", example = "13", required = false, defaultValue = "0") @RequestParam(value = "pos", required = false) String strPos,
			final HttpServletResponse response) throws StorageException {

		long position = 0;
		if (!SUtils.isNullOrEmpty(strPos)) {
			position = Long.valueOf(strPos);
			if (position < 0) {
				position = 0;
			}
		}

		final TestRun testRun;
		final EID eid = EidConverter.toEid(id);
		try {
			testRun = taskPoolRegistry.getTaskById(eid);
		} catch (ObjectWithIdNotFoundException e) {
			if (testRunDao.exists(eid)) {
				logger.info("Notifying web client about already finished Test Run");
				return TaskProgressDto.createAlreadyCompleted();
			} else {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			}
			return null;
		}
		final TaskState.STATE state = testRun.getState();

		if (state == TaskState.STATE.FAILED || state == TaskState.STATE.CANCELED) {
			// Log the internal error and release the task
			try {
				testRun.waitForResult();
			} catch (Exception e) {
				logger.error("TestRun failed with an internal error", e);
				taskPoolRegistry.release(EidConverter.toEid(id));
			}
		} else if (state.isCompleted() || state.isFinalizing()) {
			// The Client should already be informed, that the task finished, but just send again
			// JSON, which indicates that the task has been completed (with val==max)
			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			logger.info("Test Run completed, notifying web client");
			return TaskProgressDto.createCompletedMsg(testRun.getProgress());
		} else {
			// Return updated information
			return new TaskProgressDto(testRun.getProgress(), position);
		}

		// The task is running, but does not provide any new information, so just respond
		// with an empty obj
		return new TaskProgressDto();
	}

	@ApiOperation(value = "Get the progress of all Test Runs", notes = "Retrieve status information about all non-completed Test Runs", tags = {
			TEST_RUNS_TAG_NAME})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "OK"),
	})
	@RequestMapping(value = API_BASE_URL + "/TestRuns", params = "view=progress", method = RequestMethod.GET)
	public @ResponseBody List<TestRunsJsonView> listTestRunsJson() throws StorageException, ConfigurationException {
		final List<TestRunsJsonView> testRunsJsonViews = new ArrayList<TestRunsJsonView>();
		taskPoolRegistry.getTasks().forEach(t -> testRunsJsonViews.add(new TestRunsJsonView(t)));
		return testRunsJsonViews;
	}

	@ApiOperation(value = "Check if the Test Run exists", notes = "Checks whether a Test Run is running or has already been completed and a report has been saved. ", tags = {
			TEST_RESULTS_TAG_NAME, TEST_RUNS_TAG_NAME})
	@ApiResponses(value = {
			@ApiResponse(code = 204, message = "Test Run exists", response = Void.class),
			@ApiResponse(code = 404, message = "Test Run does not exist", response = Void.class),
	})
	@RequestMapping(value = {TEST_RUNS_URL + "/{id}"}, method = RequestMethod.HEAD)
	public ResponseEntity exists(
			@ApiParam(value = "Test Run ID. "
					+ EID_DESCRIPTION, example = EID_EXAMPLE, required = true) @PathVariable String id)
			throws StorageException {
		final EID eid = EidConverter.toEid(id);
		return taskPoolRegistry.contains(eid) || testRunDao.exists(eid) ? new ResponseEntity(HttpStatus.NO_CONTENT)
				: new ResponseEntity(HttpStatus.NOT_FOUND);
	}

	@ApiOperation(value = "Cancel and delete a Test Run", notes = "Cancels a running Test Run or deletes an already completed and saved report.", response = Void.class, tags = {
			TEST_RESULTS_TAG_NAME, TEST_RUNS_TAG_NAME})
	@ApiResponses(value = {
			@ApiResponse(code = 204, message = "Test Run deleted", responseHeaders = {
					@ResponseHeader(name = "action", description = "Set to 'canceled' if the Test Run was canceled or "
							+ "'deleted' if a persisted Test Run was removed")}),
			@ApiResponse(code = 404, message = "Test Run not found", response = ApiError.class)
	})
	@RequestMapping(value = TEST_RUNS_URL + "/{id}", method = RequestMethod.DELETE)
	public ResponseEntity delete(
			@ApiParam(value = "Test Run ID. "
					+ EID_DESCRIPTION, example = EID_EXAMPLE, required = true) @PathVariable String id)
			throws LocalizableApiError {
		final EID eid = EidConverter.toEid(id);
		final HttpHeaders responseHeaders = new HttpHeaders();
		try {
			if (taskPoolRegistry.contains(eid)) {
				responseHeaders.set("action", "canceled");
				taskPoolRegistry.cancelTask(eid);
				try {
					((WriteDao) testRunDao).delete(eid);
				} catch (ObjectWithIdNotFoundException | StorageException ignore) {
					ExcUtils.suppress(ignore);
				}
				return new ResponseEntity(responseHeaders, HttpStatus.NO_CONTENT);
			} else if (testRunDao.exists(EidConverter.toEid(id))) {
				responseHeaders.set("action", "deleted");
				((WriteDao) testRunDao).delete(eid);
				return new ResponseEntity(responseHeaders, HttpStatus.NO_CONTENT);
			}
		} catch (ObjectWithIdNotFoundException e) {
			throw new LocalizableApiError(e);
		} catch (StorageException e) {
			throw new LocalizableApiError(e);
		}
		return new ResponseEntity(HttpStatus.NOT_FOUND);
	}

	@ApiOperation(value = "Start a new Test Run", notes = "Start a new Test Run by specifying one or multiple Executable Test Suites "
			+ "that shall be used to test one Test Object with specified test parameters. "
			+ "If data for a Test Object need to be uploaded, the Test Object POST interface "
			+ "needs to be used to create a new temporary Test Object. "
			+ "The temporary Test Object or any other existing Test Object can be referenced by "
			+ "setting exclusively the 'id' in the StartTestRunRequest's 'testObject' property. "
			+ "If data do not need to be uploaded or a web service is tested, a temporary Test Object "
			+ "can be created directly with this interface, by defining at least the "
			+ "'resources' property of the 'testObject' but omit except the 'id' property."
			+ "\n\n"
			+ "Example for starting a Test Run for a service Test:  <br/>"
			+ "\n\n"
			+ "    {\n"
			+ "        \"label\": \"Test run on 15:00 - 01.01.2017 with Conformance class Conformance Class: Download Service - Pre-defined WFS\",\n"
			+ "        \"executableTestSuiteIds\": [\"EID174edf55-699b-446c-968c-1892a4d8d5bd\"],\n"
			+ "        \"arguments\": {},\n"
			+ "        \"testObject\": {\n"
			+ "            \"resources\": {\n"
			+ "                \"serviceEndpoint\": \"http://example.com/service?request=GetCapabilities&service=WFS\"\n"
			+ "            }\n"
			+ "        }\n"
			+ "    }\n"
			+ "\n\n"
			+ "Example for starting a Test Run for a file-based Test, using a temporary Test Object:<br/>"
			+ "\n\n"
			+ "    {\n"
			+ "        \"label\": \"Test run on 15:00 - 01.01.2017 with Conformance class INSPIRE Profile based on EN ISO 19115 and EN ISO 19119\",\n"
			+ "        \"executableTestSuiteIds\": [\"EIDec7323d5-d8f0-4cfe-b23a-b826df86d58c\"],\n"
			+ "        \"arguments\": {\n"
			+ "            \"files_to_test\": \".*\",\n"
			+ "            \"tests_to_execute\": \".*\"\n"
			+ "        },\n"
			+ "        \"testObject\": {\n"
			+ "            \"id\": \"b502260f-1054-432e-8cd5-4a61302dfdba\"\n"
			+ "        }\n"
			+ "    }\n"
			+ "\n\n"
			+ "Where \"EIDb502260f-1054-432e-8cd5-4a61302dfdba\" is the ID of the previous created temporary Test Object."
			+ "\n\n"
			+ "Example for starting a Test Run for a file-based Test, referencing Test data in the web:<br/>"
			+ "\n\n"
			+ "    {\n"
			+ "        \"label\": \"Test run on 15:00 - 01.01.2017 with Conformance class INSPIRE Profile based on EN ISO 19115 and EN ISO 19119\",\n"
			+ "        \"executableTestSuiteIds\": [\"EIDec7323d5-d8f0-4cfe-b23a-b826df86d58c\"],\n"
			+ "        \"arguments\": {\n"
			+ "            \"files_to_test\": \".*\",\n"
			+ "            \"tests_to_execute\": \".*\"\n"
			+ "        },\n"
			+ "        \"testObject\": {\n"
			+ "            \"resources\": {\n"
			+ "                \"data\": \"http://example.com/test-data.xml\"\n"
			+ "            }\n"
			+ "        }\n"
			+ "    }\n"
			+ "\n\n", tags = {TEST_RUNS_TAG_NAME})
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Test Run created"),
			@ApiResponse(code = 400, message = "Invalid request", response = ApiError.class),
			@ApiResponse(code = 404, message = "Test Object or Executable Test Suite with ID not found", response = ApiError.class),
			@ApiResponse(code = 409, message = "Test Object already in use", response = ApiError.class),
			@ApiResponse(code = 500, message = "Internal error", response = ApiError.class),
	})
	@RequestMapping(value = TEST_RUNS_URL, method = RequestMethod.POST)
	public void start(@RequestBody @Valid StartTestRunRequest testRunRequest, BindingResult result, HttpServletRequest request,
			HttpServletResponse response)
			throws LocalizableApiError, InvalidPropertyException {

		statusController.ensureStatusNotMajor();

		if (result.hasErrors()) {
			throw new LocalizableApiError(result.getFieldError());
		}

		// Remove finished test runs
		taskPoolRegistry.removeDone();

		try {
			final TestRunDto testRunDto = testRunRequest.toTestRun(testObjectController, testDriverController);
			testRunDto.setDefaultLang(LocaleContextHolder.getLocale().getLanguage());

			final TestObjectDto tO = testRunDto.getTestObjects().get(0);

			tO.setAuthor(User.getUser(request));

			// Add all Test Object Types supported by the first ETS
			final Set<EID> requiredTestObjectTypeIds = new HashSet<>();
			final Iterator<ExecutableTestSuiteDto> etsIterator = testRunDto.getExecutableTestSuites().iterator();

			requiredTestObjectTypeIds
					.addAll(EidHolderWithParent.getAllIdsAndParentIds(etsIterator.next().getSupportedTestObjectTypes()));
			// now iterate over the other ETS and delete all Test Object Types that are not supported by the first ETS
			while (etsIterator.hasNext()) {
				final Set<EID> supportedIds = EidHolder.getAllIds(etsIterator.next().getSupportedTestObjectTypes());
				requiredTestObjectTypeIds.removeIf(eid -> !supportedIds.contains(eid));
			}
			// if the list is now empty, the Test Suites are incompatible
			if (requiredTestObjectTypeIds.isEmpty()) {
				throw new LocalizableApiError("l.ets.supported.testObject.type.incompatible", false, 400);
			}
			testObjectController.initResourcesAndAdd(tO, requiredTestObjectTypeIds);

			// Check if test object is already in use
			for (TestRun tR : taskPoolRegistry.getTasks()) {
				if (!tR.getProgress().getState().isCompletedFailedCanceledOrFinalizing() &&
						tR.getResult() != null && tR.getResult().getTestObjects() != null &&
						tR.getResult().getTestObjects().get(0) != null &&
						tO.getId().equals(tR.getResult().getTestObjects().get(0).getId())) {
					logger.info("Rejecting test start: test object " + tO.getId() + " is in use");
					throw new LocalizableApiError("l.testObject.lock", false, 409, tO.getLabel());
				}
			}

			// this will save the Dto
			initAndSubmit(testRunDto);

			response.setStatus(HttpStatus.CREATED.value());
			streamingService.asJson2(testRunDao, request, response, testRunDto.getId().getId());
		} catch (URISyntaxException e) {
			throw new LocalizableApiError(e);
		} catch (ObjectWithIdNotFoundException e) {
			throw new LocalizableApiError(e);
		} catch (StorageException e) {
			throw new LocalizableApiError(e);
		} catch (IOException e) {
			throw new LocalizableApiError(e);
		}
	}

}
