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

import static de.interactive_instruments.etf.webapp.controller.WebAppUtils.API_BASE_URL;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;
import javax.validation.Valid;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;

import de.interactive_instruments.etf.model.EID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import de.interactive_instruments.IFile;
import de.interactive_instruments.SUtils;
import de.interactive_instruments.TimedExpiredItemsRemover;
import de.interactive_instruments.etf.EtfConstants;
import de.interactive_instruments.etf.component.ComponentNotLoadedException;
import de.interactive_instruments.etf.dal.dto.IncompleteDtoException;
import de.interactive_instruments.etf.dal.dto.capabilities.TestObjectDto;
import de.interactive_instruments.etf.dal.dto.run.TestRunDto;
import de.interactive_instruments.etf.dal.dto.run.TestTaskDto;
import de.interactive_instruments.etf.dal.dto.test.ExecutableTestSuiteDto;
import de.interactive_instruments.etf.model.EidFactory;
import de.interactive_instruments.etf.model.Parameterizable;
import de.interactive_instruments.etf.testdriver.*;
import de.interactive_instruments.etf.webapp.helpers.View;
import de.interactive_instruments.exceptions.*;
import de.interactive_instruments.exceptions.config.ConfigurationException;
import io.swagger.annotations.ApiOperation;

/**
 * Test run controller for starting and monitoring test runs
 */
@Controller
public class TestRunController implements TestRunEventListener {

	@Autowired
	private ServletContext servletContext;
	@Autowired
	private TestReportController store;
	@Autowired
	private TestDriverService testDriverService;
	@Autowired
	private TestObjectController testObjectController;
	@Autowired
	private TestReportController testReportController;
	private Timer timer;
	@Autowired
	private EtfConfigController etfConfig;

	boolean simplifiedWorkflows;

	private static String TESPROJECT_ID_KEY = "testProjectId";

	public static final String TESTRUNS_CREATE_QUICK = "testruns/create-direct";

	public final static int MAX_PARALLEL_RUNS = Runtime.getRuntime().availableProcessors();

	private final TaskPoolRegistry<TestRunDto> taskPoolRegistry = new TaskPoolRegistry<>(MAX_PARALLEL_RUNS, MAX_PARALLEL_RUNS);
	private final Logger logger = LoggerFactory.getLogger(TestRunController.class);

	public TestRunController() {}

	@PostConstruct
	public void init() throws ParseException, ConfigurationException, IOException, StorageException {
		logger.info(Runtime.getRuntime().availableProcessors() + " cores available.");

		// SEL dir
		System.setProperty("ETF_SEL_GROOVY",
				etfConfig.getPropertyAsFile(EtfConstants.ETF_PROJECTS_DIR).expandPath("sui").getPath());
		simplifiedWorkflows = "simplified".equals(etfConfig.getProperty(EtfConfigController.ETF_WORKFLOWS));

		timer = new Timer(true);
		// Trigger every 30 Minutes
		TimedExpiredItemsRemover timedExpiredItemsRemover = new TimedExpiredItemsRemover();
		timedExpiredItemsRemover.addExpirationItemHolder(this.testObjectController, 1, TimeUnit.HOURS);
		timedExpiredItemsRemover.addExpirationItemHolder((l, timeUnit) -> taskPoolRegistry.removeDone(), 0, TimeUnit.HOURS);
		timer.scheduleAtFixedRate(timedExpiredItemsRemover, 0, 30 * 60 * 1000);

		logger.info("Test Run controller initialized!");
	}

	@PreDestroy
	public void shutdown() {
		logger.info("Shutting down TestRunController");
		if (this.timer != null) {
			timer.cancel();
		}
	}

	/*
	@Override
	public void taskStateChangedEvent(TaskWithProgressIndication task, TaskState.STATE state, TaskState.STATE state1) {
		if (task != null && state != null) {
			logger.debug("Task " + task.getID() + " changed the state to " + state);
			if (state.isCompletedFailedOrCanceled() && task != null && task instanceof AbstractTestRunTask) {
				final AbstractTestRunTask testRunTask = ((AbstractTestRunTask) task);

				if (state.isFailed()) {
					final Exception tE = ((AbstractTaskProgress) testRunTask.getTaskProgress()).getException();
					if (tE != null) {
						logger.error("Task " + task.getID() + " failed with an exception ", tE);
					} else {
						logger.error("Task " + task.getID() + " failed without an exception");
					}
				} else if (state.isCanceled()) {
					logger.info("Task " + task.getID() + " canceled");
				} else if (state.isCompleted()) {
					if (testRunTask != null && testRunTask.getTestRun() != null &&
							testRunTask.getTestRun().getReport() != null) {
						try {
							logger.info("Saving report of task " + task.getID());
							this.store.updateReport(testRunTask.getTestRun().getReport());
						} catch (ObjectWithIdNotFoundException | StorageException | AssemblerException e) {
							logger.error("Unable to update report of test run task " + testRunTask.getID(), e);
						}
					} else {
						logger.error("Unable to update report of test run task " + testRunTask.getID());
					}
					try {
						Thread.sleep(000);
					} catch (InterruptedException e) {
						ExcUtils.supress(e);
					}

				}
			}
		}
	}
	*/

	private String configureModelAndRedirectDirect(final Model model) throws ObjectWithIdNotFoundException, StorageException {
		// TODO multiple ets IDs
		final String etsId;
		if (model.containsAttribute(TESPROJECT_ID_KEY)) {
			etsId = (String) model.asMap().get(TESPROJECT_ID_KEY);
		} else {
			etsId = ((TestRunDto) model.asMap().get("testRun")).getTestTasks().get(0).getExecutableTestSuite().getId().toString();
		}

		final ExecutableTestSuiteDto executableTestSuite = testDriverService.getExecutableTestSuiteById(WebAppUtils.toEid(etsId));
		final TestRunDto testRunDto = new TestRunDto();
		final TestTaskDto testTaskDto = new TestTaskDto();
		testTaskDto.setExecutableTestSuite(executableTestSuite);
		testTaskDto.setTestObject(new TestObjectDto());
		testRunDto.addTestTask(testTaskDto);
		testRunDto.setId(EidFactory.getDefault().createRandomId());
		model.addAttribute("testRun", testRunDto);
		model.addAttribute("executableTestSuiteId", executableTestSuite.getId());
		model.addAttribute("executableTestSuite", executableTestSuite);
		model.addAttribute("simplifiedTestObjectType", "data");

		/*
		for (final TestProjectDto testProj : testProjs) {
			if (testProj.getId().equals(projectId)) {
				final TestRunDtoBuilder testRunDtoBuilder = new TestRunDtoBuilder().setTestProject(testProj);
				final TestObjectDto testObjectDto = new TestObjectDtoBuilder().createTestObjectDto();
				if (testProj.getProperties().hasProperty(EtfConstants.ETF_TESTDOMAIN_PK)) {
					testObjectDto.setProperty(EtfConstants.ETF_TESTDOMAIN_PK,
							testProj.getProperties().getProperty(EtfConstants.ETF_TESTDOMAIN_PK));
				}
				// testObjectDto.setProperty("password","");
				// testObjectDto.setProperty("username","");
				testRunDtoBuilder.setTestObject(testObjectDto);
				model.addAttribute("testRun", testRunDtoBuilder.createTestRunDto());
				break;
			}
		}
		*/

		return TESTRUNS_CREATE_QUICK;
	}

	@RequestMapping(value = TESTRUNS_CREATE_QUICK, method = RequestMethod.GET)
	public String configureTransientTestObject(
			@RequestParam(required = true) String testProjectId,
			Model model)
			throws ConfigurationException, IOException, StorageException, ObjectWithIdNotFoundException {
		model.addAttribute(TESPROJECT_ID_KEY, testProjectId);
		return configureModelAndRedirectDirect(model);
	}

	@RequestMapping(value = "/testruns/start-direct", method = RequestMethod.POST)
	public String startDirect(
			@ModelAttribute("testRun") @Valid TestRunDto testRunDto,
			@ModelAttribute("executableTestSuiteId") @Valid String etsId,
			BindingResult testRunBindingResult,
			// @ModelAttribute("testObject") @Valid TestObjectDto testObject,
			BindingResult testObjectBindingResult,
			RedirectAttributes redirectAttributes,
			MultipartHttpServletRequest request,
			Model model) throws IncompleteDtoException, URISyntaxException, StorageException, NoSuchAlgorithmException, ParseException, IOException, ObjectWithIdNotFoundException, ConfigurationException, ComponentLoadingException, TestRunInitializationException {

		final ExecutableTestSuiteDto executableTestSuite = testDriverService.getExecutableTestSuiteById(
				WebAppUtils.toEid(request.getParameterMap().get("properties.asMap[etsId]")[0]));

		final TestTaskDto testTaskDto = new TestTaskDto();
		for (final Parameterizable.Parameter parameter : executableTestSuite.getParameters().getParameters()) {
			final String value = request.getParameterMap().get("properties.asMap[" + parameter.getName() + "]")[0];
			testTaskDto.getArguments().setValue(parameter.getName(), value);
		}

		final TestObjectDto testObjectDto = new TestObjectDto();
		testObjectDto.setId(EidFactory.getDefault().createRandomId());
		testObjectDto.setTestObjectTypes(executableTestSuite.getSupportedTestObjectTypes());
		testTaskDto.setExecutableTestSuite(executableTestSuite);
		testTaskDto.setTestObject(testObjectDto);
		testRunDto.addTestTask(testTaskDto);
		testRunDto.setId(EidFactory.getDefault().createRandomId());

		testObjectDto.properties().setProperty("expires", "true");
		testObjectDto.properties().setProperty("tempObject", "true");
		testObjectDto.setId(EidFactory.getDefault().createRandomId());

		final MultipartFile multipartFile = request.getFile("testObjFile");
		testObjectDto.setLabel(multipartFile.getOriginalFilename());
		if (multipartFile != null) {
			if (multipartFile.isEmpty()) {
				testObjectBindingResult.reject("l.upload.invalid", new Object[]{"File is empty or corrupt"},
						"Unable to use file: {0}");
			}
			testObjectController.addFileTestData(testObjectDto, testObjectBindingResult, request, model);
		}

		if (testObjectBindingResult.hasErrors() || testRunBindingResult.hasErrors()) {
			model.addAttribute(BindingResult.class.getName() + ".testObject", testObjectBindingResult);
			model.addAttribute(BindingResult.class.getName() + ".testRun", testRunBindingResult);
			return configureModelAndRedirectDirect(model);
		}
		return start(testRunDto, testRunBindingResult, redirectAttributes, model);
	}

	private TestRun initAndSubmit(TestRunDto testRunDto)
			throws StorageException, ComponentNotLoadedException, ObjectWithIdNotFoundException,
			ConfigurationException, InvalidStateTransitionException, InitializationException, ComponentLoadingException, TestRunInitializationException, IncompleteDtoException {
		final TestRun testRun = testDriverService.create(testRunDto);
		if (testRun == null) {
			throw new ConfigurationException("Unable to create a new test run task");
		}
		testRun.addTestRunEventListener(this);

		testRun.init();

		/*
		// Check if the test object has changed since the last run
		// and update the test object
		final TestObject tO = testRunTask.getTestRun().getTestObject();
		if (testRunTask.getTestRun().isTestObjectResourceUpdateRequired() &&
				testObjectController.getTestObjStore().exists(tO.getId())) {
			testObjectController.getTestObjStore().update(tO);
		}
		*/

		testReportController.storeTestRun(testRunDto);

		logger.info("TestRun " + testRunDto.getLabel() + "." + testRunDto.getId() + " initialized");

		taskPoolRegistry.submitTask(testRun);

		return testRun;
	}

	@RequestMapping(value = "/testruns/create", method = RequestMethod.GET)
	public String configure(
			Model model)
			throws ConfigurationException, IOException, StorageException, ObjectWithIdNotFoundException {
		return configureModelAndRedirect(model);
	}

	private String configureModelAndRedirect(final Model model) throws StorageException, ConfigurationException, ObjectWithIdNotFoundException {
		if ("simplified".equals(etfConfig.getProperty("etf.workflows"))) {
			return configureModelAndRedirectDirect(model);
		}

		final Collection<ExecutableTestSuiteDto> executableTestSuites = testDriverService.getExecutableTestSuites();

		if (!model.containsAttribute("testRun")) {
			final TestRunDto testRun = new TestRunDto();
			final TestTaskDto testTask = new TestTaskDto();
			testTask.setTestObject(new TestObjectDto());
			testRun.addTestTask(testTask);
			model.addAttribute("testRun", testRun);
		}
		// Set currently used test objects
		final HashSet<String> blockedTestObjects = new HashSet<>();
		for (final TaskWithProgress<TestRunDto> testRun : taskPoolRegistry.getTasks()) {
			if (!testRun.getTaskProgress().getState().isCompletedFailedCanceledOrFinalizing()) {
				testRun.getResult().getTestTasks().forEach(task -> blockedTestObjects.add(task.getTestObject().getId().toString()));
			}
		}
		final List<TestObjectDto> unusedTestObjects = testObjectController.getTestObjects().stream().filter(tO -> !blockedTestObjects.contains(tO.getId().toString())).collect(Collectors.toList());
		model.addAttribute("allTestObjectsInUse", unusedTestObjects.isEmpty() && !blockedTestObjects.isEmpty());
		model.addAttribute("noTestObjectsCreated", blockedTestObjects.isEmpty() && unusedTestObjects.isEmpty());
		model.addAttribute("testObjects", unusedTestObjects);

		model.addAttribute("testProjects", executableTestSuites);
		model.addAttribute("noProjectsAvailable", executableTestSuites.isEmpty());

		return "testruns/create";
	}

	@RequestMapping(value = "/testruns/start", method = RequestMethod.POST)
	public synchronized String start(
			@Valid @ModelAttribute("testRun") TestRunDto testRunDto, BindingResult result,
			RedirectAttributes redirectAttributes,
			Model model) throws ObjectWithIdNotFoundException, StorageException, ConfigurationException, TestRunInitializationException, IncompleteDtoException, ComponentLoadingException {
		if (result.hasErrors()) {
			return configureModelAndRedirect(model);
		}
		// Remove finished test runs
		taskPoolRegistry.removeDone();

		testRunDto.setId(EidFactory.getDefault().createRandomId());
		final TestObjectDto to;
		if (Objects.equals("true", testRunDto.getTestObjects().get(0).properties().getProperty("tempObject"))) {
			to = testRunDto.getTestObjects().get(0);
		} else {
			to = testObjectController.getTestObjectById(testRunDto.getTestObjects().get(0).getId());
		}

		// Check if test object is already in usage
		for (final TaskWithProgress<TestRunDto> testRun : taskPoolRegistry.getTasks()) {
			if (!testRun.getTaskProgress().getState().isCompletedFailedCanceledOrFinalizing() &&
					to.getId().equals(testRun.getResult().getTestObjects().get(0).getId())) {
				logger.info("Rejecting test start, test object " +
						to.getId() + " is in use");
				result.reject("testObject.lock",
						"Test Object \"" + to.getLabel() +
								"\" is already used in Test Run \"" +
								testRun.getResult().getLabel() + "\"" +
								"and is locked until the Test Run will be finished.");
				model.addAttribute(BindingResult.class.getName() + ".testRun", result);
				return configureModelAndRedirect(model);
			}
		}

		testRunDto.getTestTasks().get(0).setTestObject(to);
		// testRun.setTestReport(testReportController.createReport(testRun.getLabel(), to));

		final TestRun testRun;
		try {
			testRun = initAndSubmit(testRunDto);
		} catch (StorageException | ComponentNotLoadedException | ObjectWithIdNotFoundException | InvalidStateTransitionException | InitializationException | ConfigurationException e) {
			result.reject("testRun.failed.startup", "Startup failed: " + e.getMessage());
			model.addAttribute(BindingResult.class.getName() + ".testRun", result);
			model.addAttribute(BindingResult.class.getName() + ".testRunDto", result);
			model.addAttribute(BindingResult.class.getName() + ".testObject", result);
			return configureModelAndRedirect(model);
		}

		redirectAttributes.addAttribute("id", testRun.getId().toString()).addFlashAttribute("message", "Test started");

		return "redirect:/testruns/{id}";
	}

	@RequestMapping(value = "/testruns/{id}", method = RequestMethod.GET)
	public String show(@PathVariable String id, Model model) throws Exception {
		TaskWithProgress<TestRunDto> testRun = null;
		try {
			testRun = taskPoolRegistry.getTaskById(WebAppUtils.toEid(id));
		} catch (ObjectWithIdNotFoundException e) {
			// throw new ObjectWithIdNotFoundException("Testrun not found or already completed");
		}
		if (testRun == null) {
			return "redirect:/testruns/{id}/result";
		}
		model.addAttribute("testRun", testRun);

		/*
		if (testRun != null) {
			if (testRun.getTaskProgress().getState() == TaskState.STATE.COMPLETED) {
				logger.info("TestRun already finished, redirecting to results");
				return "redirect:/testruns/{id}/result";
			}
			logger.info("Presenting TestRun status");
			model.addAttribute("testRunTask", testRun);
		}
		*/
		return "testruns/show";
	}

	Set<EID> getTestRunIds() {
		return taskPoolRegistry.getTasks().stream().map(
				TaskWithProgress::getId).collect(Collectors.toSet());
	}

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String root(Model model) {
		return status(model);
	}

	@RequestMapping(value = "/testruns/status", method = RequestMethod.GET)
	public String status(Model model) {
		model.addAttribute("testRuns", taskPoolRegistry.getTasks());
		model.addAttribute("testDriversInfo", testDriverService.getTestDriverInfo());
		return "testruns/status";
	}

	@RequestMapping(value = "/testruns/{id}/cancel", method = RequestMethod.GET)
	public String cancel(@PathVariable String id) {
		try {
			logger.info("Killing Test Run " + id);
			Thread.sleep(1000);
			taskPoolRegistry.cancelTask(WebAppUtils.toEid(id));
		} catch (Exception e) {
			logger.info("Killin Test Run " + id + " failed ", e);
			ExcUtils.suppress(e);
		}
		return "redirect:/testruns/status";
	}

	/**
	 * Cleanup and redirect to the ReportStoreController
	 */
	@RequestMapping(value = "/testruns/{id}/result", method = RequestMethod.GET)
	public String showResult(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
		try {
			// Future call!
			final TestRunDto testRunDto = ((TestRun) taskPoolRegistry.getTaskById(WebAppUtils.toEid(id))).waitForResult();
			logger.info("Releasing testrun " + id +
					", persisting and redirecting to report results ");

			taskPoolRegistry.release(WebAppUtils.toEid(id));
			redirectAttributes.addAttribute("id", id.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "redirect:/reports/{id}";
	}

	@Override
	public void taskStateChangedEvent(final TestTask testTask, final TaskState.STATE current, final TaskState.STATE old) {
		logger.trace("TaskStateChanged event received: {} {} -> {} " + testTask.getId(), old, current);
	}

	@Override
	public void taskRunChangedEvent(final TestRun testRun, final TaskState.STATE current, final TaskState.STATE old) {
		logger.trace("TaskStateChanged event received: {} ({}) {} -> {} " + testRun.getLabel(), testRun.getId(), old, current);
		if (current.isCompleted()) {
			try {
				testReportController.updateTestRun(testRun);
			} catch (StorageException | ObjectWithIdNotFoundException e) {
				final String identifier = testRun != null ? testRun.getLabel() : "";
				logger.error("Test Run " + identifier + " could not be updated");
			}
		}
	}

	//
	// Rest interfaces
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	static class TaskProgressDto {

		private String val;
		private String max;
		private List<String> log;

		// Completed
		private TaskProgressDto(String max, List<String> log) {
			this.val = max;
			this.max = max;
			this.log = log;
		}

		public TaskProgressDto() {}

		static TaskProgressDto createCompletedMsg(Progress p) {
			return new TaskProgressDto(
					String.valueOf(p.getMaxSteps()), new ArrayList<>());
		}

		static TaskProgressDto createTerminateddMsg(int max) {
			return new TaskProgressDto(String.valueOf(max), new ArrayList<String>(1) {
				{
					add("Terminated");
				}
			});
		}

		// Still running
		private TaskProgressDto(final Progress p, final long pos) {
			this.val = String.valueOf(p.getCurrentStepsCompleted());
			if (p.getCurrentStepsCompleted() >= p.getMaxSteps()) {
				this.max = String.valueOf(p.getMaxSteps() + p.getCurrentStepsCompleted());
			}
			this.max = String.valueOf(p.getMaxSteps());
			this.log = p.getLogger().getLogMessages(pos);
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

	@ApiOperation(value = "Get the Test Run progress by ID", tags = {"Test Runs"})
	@RequestMapping(value = API_BASE_URL + "/TestRuns/{id}.json", params = "view=progress", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public TaskProgressDto progressLog(
			@PathVariable String id,
			@RequestParam(value = "pos", required = false) String strPos) throws ObjectWithIdNotFoundException {

		long position = 0;
		if (!SUtils.isNullOrEmpty(strPos)) {
			position = Long.valueOf(strPos);
			if (position < 0) {
				position = 0;
			}
		}

		final TaskWithProgress<TestRunDto> testRun = taskPoolRegistry.getTaskById(WebAppUtils.toEid(id));
		final TaskState.STATE state = testRun.getState();

		if (state == TaskState.STATE.FAILED || state == TaskState.STATE.CANCELED) {
			// Log the internal error and release the task
			try {
				((TestRun) testRun).waitForResult();
			} catch (Exception e) {
				logger.error("TestRun failed with an internal error", e);
				taskPoolRegistry.release(WebAppUtils.toEid(id));
			}
		} else if (state.isCompleted() || state.isFinalizing()) {
			// The Client should already be informed, that the task finished, but just send again
			// JSON, which indicates that the task has been completed (with val==max)
			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			logger.info("Notifying web client");
			return TaskProgressDto.createCompletedMsg(testRun.getTaskProgress());
		} else {
			return new TaskProgressDto(testRun.getTaskProgress(), position);
		}

		// The task is running, but does not provide any new information, so just respond
		// with an empty obj
		return new TaskProgressDto();
	}

	private static class TestRunsJsonView {
		public final String id;
		public final String label;
		public final int testTaskCount;
		public final Date startTimestamp;
		public final double percentStepsCompleted;

		public TestRunsJsonView(final TaskWithProgress<TestRunDto> t) {
			id = t.getId().getId();
			label = ((TestRun) t).getLabel();
			testTaskCount = ((TestRun) t).getTestTasks().size();
			startTimestamp = t.getTaskProgress().getStartTimestamp();
			percentStepsCompleted = t.getTaskProgress().getPercentStepsCompleted();
		}
	}

	@ApiOperation(value = "Get the progress of all Test Runs", tags = {"Test Runs"})
	@RequestMapping(value = API_BASE_URL + "/TestRuns.json", params = "view=progress", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody List<TestRunsJsonView> listTestRunsJson() throws StorageException, ConfigurationException {
		final List<TestRunsJsonView> testRunsJsonViews = new ArrayList<TestRunsJsonView>();
		taskPoolRegistry.getTasks().forEach(t -> testRunsJsonViews.add(new TestRunsJsonView(t)));
		return testRunsJsonViews;
	}

}
