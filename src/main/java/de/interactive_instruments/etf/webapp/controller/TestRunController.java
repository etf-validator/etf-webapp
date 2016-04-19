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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import de.interactive_instruments.IFile;
import de.interactive_instruments.TimedExpiredItemsRemover;
import de.interactive_instruments.concurrent.*;
import de.interactive_instruments.etf.EtfConstants;
import de.interactive_instruments.etf.component.ComponentNotLoadedException;
import de.interactive_instruments.etf.dal.assembler.AssemblerException;
import de.interactive_instruments.etf.dal.dto.plan.*;
import de.interactive_instruments.etf.dal.dto.result.TestReportDto;
import de.interactive_instruments.etf.driver.AbstractTestRunTask;
import de.interactive_instruments.etf.driver.TestRunTask;
import de.interactive_instruments.etf.model.item.EID;
import de.interactive_instruments.etf.model.item.EidFactory;
import de.interactive_instruments.etf.model.plan.TestObject;
import de.interactive_instruments.etf.model.result.TestReport;
import de.interactive_instruments.etf.webapp.dto.TestRunValidator;
import de.interactive_instruments.exceptions.ExcUtils;
import de.interactive_instruments.exceptions.InitializationException;
import de.interactive_instruments.exceptions.ObjectWithIdNotFoundException;
import de.interactive_instruments.exceptions.StoreException;
import de.interactive_instruments.exceptions.config.ConfigurationException;

/**
 * Test run controller for starting and monitoring test runs
 */
@Controller
public class TestRunController implements TaskStateEventListener {

	@Autowired
	private ServletContext servletContext;
	@Autowired
	private TestReportController store;
	@Autowired
	private TestRunTaskFactoryService taskFactory;
	@Autowired
	private TestObjectController testObjectController;
	@Autowired
	private TestReportController testReportDao;
	private Timer timer;
	@Autowired
	private EtfConfigController etfConfig;

	boolean simplifiedWorkflows;

	private static String TESPROJECT_ID_KEY = "testProjectId";

	public static final String TESTRUNS_CREATE_QUICK = "testruns/create-direct";

	@InitBinder
	private void initBinder(WebDataBinder binder) {
		binder.setValidator(new TestRunValidator());
	}

	public final static int MAX_PARALLEL_RUNS = Runtime.getRuntime().availableProcessors();

	private final TaskPoolRegistry<TestReport> taskPoolRegistry = new TaskPoolRegistry<TestReport>(MAX_PARALLEL_RUNS, MAX_PARALLEL_RUNS);
	// private final ConsoleAppender ca;
	private IFile tmpDir;
	private final Logger logger = LoggerFactory.getLogger(TestRunController.class);

	// Map for transferring the dtos between the controller steps
	private final ConcurrentMap<UUID, Object> transferDTO = new ConcurrentLinkedHashMap.Builder<UUID, Object>()
			.maximumWeightedCapacity(10)
			.build();

	public TestRunController() {}

	@PostConstruct
	public void init() throws ParseException, ConfigurationException, IOException, StoreException {
		logger.info(Runtime.getRuntime().availableProcessors() + " cores available.");
		logger.info(this.getClass().getName() + " initialized!");

		// SEL dir
		System.setProperty("ETF_SEL_GROOVY",
				etfConfig.getPropertyAsFile(EtfConstants.ETF_PROJECTS_DIR).expandPath("sui").getPath());
		simplifiedWorkflows = "simplified".equals(etfConfig.getProperty(EtfConfigController.ETF_WORKFLOWS));

		timer = new Timer(true);
		// Trigger every 30 Minutes
		TimedExpiredItemsRemover timedExpiredItemsRemover = new TimedExpiredItemsRemover();
		timedExpiredItemsRemover.addExpirationItemHolder(this.testObjectController.getTestObjStore(), 1, TimeUnit.HOURS);
		timedExpiredItemsRemover.addExpirationItemHolder((l, timeUnit) -> taskPoolRegistry.removeDone(), 0, TimeUnit.HOURS);
		timer.scheduleAtFixedRate(timedExpiredItemsRemover, 0, 30 * 60 * 1000);
	}

	@PreDestroy
	public void shutdown() {
		logger.info("Shutting down TestRunController");
		if (this.timer != null) {
			timer.cancel();
		}
	}

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
						} catch (ObjectWithIdNotFoundException | StoreException | AssemblerException e) {
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

	private String configureModelAndRedirectDirect(final Model model) throws StoreException, ConfigurationException {
		final List<TestProjectDto> testProjs = taskFactory.getAvailableProjects();

		final String projectId;
		if (model.containsAttribute(TESPROJECT_ID_KEY)) {
			projectId = (String) model.asMap().get(TESPROJECT_ID_KEY);
		} else {
			projectId = ((TestRunDto) model.asMap().get("testRun")).getTestProject().getId().toString();
		}

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
		return TESTRUNS_CREATE_QUICK;
	}

	@RequestMapping(value = TESTRUNS_CREATE_QUICK, method = RequestMethod.GET)
	public String configureTransientTestObject(
			@RequestParam(required = true) String testProjectId,
			Model model)
					throws ConfigurationException, IOException, StoreException {
		model.addAttribute(TESPROJECT_ID_KEY, testProjectId);
		return configureModelAndRedirectDirect(model);
	}

	@RequestMapping(value = "/testruns/start-direct", method = RequestMethod.POST)
	public String startDirect(
			@ModelAttribute("testRun") @Valid TestRunDto testRun,
			BindingResult testRunBindingResult,
			@ModelAttribute("testObject") @Valid TestObjectDto testObject,
			BindingResult testObjectBindingResult,
			RedirectAttributes redirectAttributes,
			MultipartHttpServletRequest request,
			Model model)
					throws ConfigurationException, IOException, StoreException, ParseException, NoSuchAlgorithmException,
					URISyntaxException, ComponentNotLoadedException, ObjectWithIdNotFoundException, InitializationException,
					InvalidStateTransitionException {
		final MultipartFile multipartFile = request.getFile("testObjFile");
		testRun.getTestObject().setProperty("expires", "true");
		testRun.getTestObject().setProperty("tempObject", "true");
		testRun.getTestObject().setId(EidFactory.getDefault().createRandomUuid());
		testRun.getTestObject().setLabel("temporary-" + testRun.getTestObject().getId());

		if (multipartFile != null && !multipartFile.isEmpty()) {
			testObjectController.addFileTestData(testRun.getTestObject(), testObjectBindingResult, request, model);
		}

		if (testObjectBindingResult.hasErrors() || testRunBindingResult.hasErrors()) {
			model.addAttribute(BindingResult.class.getName() + ".testObject", testObjectBindingResult);
			model.addAttribute(BindingResult.class.getName() + ".testRun", testRunBindingResult);
			return configureModelAndRedirectDirect(model);
		}
		return start(testRun, testRunBindingResult, redirectAttributes, model);
	}

	private TestRunTask initAndSubmit(TestRunDto testRunDto)
			throws StoreException, ComponentNotLoadedException, ObjectWithIdNotFoundException,
			ConfigurationException, InvalidStateTransitionException, InitializationException {
		final TestRunTask testRunTask = taskFactory.create(testRunDto, this);
		if (testRunTask == null) {
			throw new ConfigurationException("Unable to create a new test run task");
		}

		testRunTask.init();

		// Check if the test object has changed since the last run
		// and update the test object
		final TestObject tO = testRunTask.getTestRun().getTestObject();
		if (testRunTask.getTestRun().isTestObjectResourceUpdateRequired() &&
				testObjectController.getTestObjStore().exists(tO.getId())) {
			testObjectController.getTestObjStore().update(tO);
		}

		logger.info("TestRun " + testRunDto.getLabel() + "." + testRunDto.getId() + " prepared");
		taskPoolRegistry.submitTask(testRunTask);

		return testRunTask;
	}

	@RequestMapping(value = "/testruns/create", method = RequestMethod.GET)
	public String configure(
			Model model)
					throws ConfigurationException, IOException, StoreException {
		return configureModelAndRedirect(model);
	}

	private String configureModelAndRedirect(final Model model) throws StoreException, ConfigurationException {
		if ("simplified".equals(etfConfig.getProperty("etf.workflows"))) {
			return configureModelAndRedirectDirect(model);
		}

		final List<TestProjectDto> testProjs = taskFactory.getAvailableProjects();

		if (!model.containsAttribute("testRun")) {
			final TestRunDto testRun = new TestRunDto();
			testRun.setTestObject(new TestObjectDtoBuilder().createTestObjectDto());
			model.addAttribute("testRun", testRun);
		}
		final HashSet<String> blockedTestObjects = new HashSet<>();
		for (TaskWithProgressIndication<TestReport> t : taskPoolRegistry.getTasks()) {
			if (!t.getTaskProgress().getState().isCompletedFailedCanceledOrFinalizing()) {
				blockedTestObjects.add(((TestRunTask) t).getTestRun().getTestObject().getId().toString());
			}
		}
		// Set currently used test objects
		final List<TestObjectDto> unusedTestObjects = testObjectController.getTestObjects().stream().filter(tO -> !blockedTestObjects.contains(tO.getId().toString())).collect(Collectors.toList());
		model.addAttribute("allTestObjectsInUse", unusedTestObjects.isEmpty() && !blockedTestObjects.isEmpty());
		model.addAttribute("noTestObjectsCreated", blockedTestObjects.isEmpty() && unusedTestObjects.isEmpty());
		model.addAttribute("testObjects", unusedTestObjects);

		model.addAttribute("testProjects", testProjs);
		model.addAttribute("noProjectsAvailable", testProjs.isEmpty());

		return "testruns/create";
	}

	@RequestMapping(value = "/testruns/start", method = RequestMethod.POST)
	public synchronized String start(
			@Valid @ModelAttribute("testRun") TestRunDto testRun, BindingResult result,
			RedirectAttributes redirectAttributes,
			Model model) throws StoreException, ObjectWithIdNotFoundException, ConfigurationException {
		if (result.hasErrors()) {
			return configureModelAndRedirect(model);
		}
		// Remove finished test runs
		taskPoolRegistry.removeDone();

		testRun.setId(EidFactory.getDefault().createRandomUuid());
		final TestObjectDto to;
		if (Objects.equals("true", testRun.getTestObject().getProperty("tempObject"))) {
			to = testRun.getTestObject();
		} else {
			to = testObjectController.getTestObjStore().getDtoById(testRun.getTestObject().getId());
		}

		// Check if test object is already in usage
		for (TaskWithProgressIndication<TestReport> t : taskPoolRegistry.getTasks()) {
			if (!t.getTaskProgress().getState().isCompletedFailedCanceledOrFinalizing() &&
					to.getId().equals(((TestRunTask) t).getTestRun().getTestObject().getId())) {
				logger.info("Rejecting test start, test object " +
						to.getId() + " is in use");
				result.reject("testObject.lock",
						"Das Testobject \"" + to.getLabel() +
								"\" wird bereits im Testlauf \"" +
								((TestRunTask) t).getTestRun().getLabel() + "\" benutzt " +
								"und ist gesperrt, bis dieser beendet wurde.");
				model.addAttribute(BindingResult.class.getName() + ".testRun", result);
				return configureModelAndRedirect(model);
			}
		}

		testRun.setTestObject(to);
		testRun.setTestReport(testReportDao.createReport(testRun.getLabel(), to));

		final TestRunTask testRunTask;
		try {
			testRunTask = initAndSubmit(testRun);
		} catch (StoreException | ComponentNotLoadedException | ObjectWithIdNotFoundException | InvalidStateTransitionException | InitializationException | ConfigurationException e) {
			result.reject("testRun.failed.startup", "Startup failed: " + e.getMessage());
			model.addAttribute(BindingResult.class.getName() + ".testRun", result);
			model.addAttribute(BindingResult.class.getName() + ".testRunDto", result);
			model.addAttribute(BindingResult.class.getName() + ".testObject", result);
			return configureModelAndRedirect(model);
		}

		redirectAttributes.addAttribute("id", testRunTask.getID()).addFlashAttribute("message", "Test started");

		return "redirect:/testruns/{id}";
	}

	@RequestMapping(value = "/testruns/{id}", method = RequestMethod.GET)
	public String show(@PathVariable UUID id, Model model) throws Exception {
		final TestRunTask testRunTask;
		try {
			testRunTask = (TestRunTask) taskPoolRegistry.getTaskById(id);
		} catch (ObjectWithIdNotFoundException e) {
			throw new ObjectWithIdNotFoundException("Testrun not found or already completed");
		}

		if (testRunTask != null) {
			if (testRunTask.getTaskProgress().getState() == TaskState.STATE.COMPLETED) {
				logger.info("TestRun already finished, redirecting to results");
				return "redirect:/testruns/{id}/result";
			}
			logger.info("Presenting TestRun status");
			model.addAttribute("testRunTask", testRunTask);
		}
		return "testruns/show";
	}

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String root(Model model) {
		return status(model);
	}

	@RequestMapping(value = "/testruns/status", method = RequestMethod.GET)
	public String status(Model model) {
		model.addAttribute("tasks", taskPoolRegistry.getTasks());
		model.addAttribute("testDriversInfo", taskFactory.getTestDriverInfo());
		return "testruns/status";
	}

	@RequestMapping(value = "/testruns/{id}/cancel", method = RequestMethod.GET)
	public String cancel(@PathVariable String id, @RequestParam Map<String, String> allRequestParams, Model model) {
		try {
			final UUID _id = UUID.fromString(id);
			logger.info("Killing testrun " + id);
			Thread.sleep(1000);
			taskPoolRegistry.cancelTask(_id);
		} catch (Exception e) {
			logger.info("Killin of testrun " + id + " failed ", e);
			ExcUtils.supress(e);
		}
		return "redirect:/testruns/status";
	}

	/**
	 * Cleanup and redirect to the ReportStoreController
	 */
	@RequestMapping(value = "/testruns/{id}/result", method = RequestMethod.GET)
	public String showResult(@PathVariable UUID id, Model model, RedirectAttributes redirectAttributes) {
		try {
			// Future call!
			final TestReport testReport = taskPoolRegistry.getTaskById(id).getTaskProgress().waitForResult();
			logger.info("Releasing testrun " + id +
					", persisting and redirecting to report results " + testReport.getId());

			taskPoolRegistry.release(id);
			redirectAttributes.addAttribute("id", testReport.getId().toString());
			return "redirect:/reports/{id}";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
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

		static TaskProgressDto createCompletedMsg(TaskProgress p) {
			return new TaskProgressDto(String.valueOf(p.getMaxSteps()), p.getLastMessages());
		}

		static TaskProgressDto createTerminateddMsg(int max) {
			return new TaskProgressDto(String.valueOf(max), new ArrayList<String>(1) {
				{
					add("Terminated");
				}
			});
		}

		// Still running
		private TaskProgressDto(TaskProgress p) {
			this.val = String.valueOf(p.getCurrentStepsCompleted());
			if (p.getCurrentStepsCompleted() >= p.getMaxSteps()) {
				this.max = String.valueOf(p.getMaxSteps() + p.getCurrentStepsCompleted());
			}
			this.max = String.valueOf(p.getMaxSteps());
			this.log = p.getLastMessages();
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

	@RequestMapping(value = "/rest/testruns/{id}/progress", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public TaskProgressDto progressLog(@PathVariable UUID id) throws ObjectWithIdNotFoundException {

		TaskProgress<TestReport> taskProgress = taskPoolRegistry.getTaskById(id).getTaskProgress();

		TaskState.STATE state = taskProgress.getState();

		if (state == TaskState.STATE.FAILED || state == TaskState.STATE.CANCELED) {
			// Log the internal error and release the task
			try {
				taskProgress.waitForResult();
			} catch (Exception e) {
				logger.error("TestRun failed with an internal error", e);
				taskPoolRegistry.release(id);
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
			return TaskProgressDto.createCompletedMsg(taskProgress);
		} else if (taskProgress.isStateChanged()) {
			return new TaskProgressDto(taskProgress);
		}

		// The task is running, but does not provide any new information, so just respond
		// with an empty obj
		return new TaskProgressDto();
	}

	static class SimpleTestRunDto {
		private String testRunLabel;
		private String testObjectId;
		private String testProjectId;

		public String getTestRunLabel() {
			return testRunLabel;
		}

		public EID getTestObjectId() {
			return EidFactory.getDefault().createFromStrAsUUID(testObjectId);
		}

		public EID getTestProjectId() {
			return EidFactory.getDefault().createFromStrAsUUID(testProjectId);
		}
	}

	static class SimpleTestRunInfoDto {
		private String testRunId;
		private String testReportId;

		SimpleTestRunInfoDto(EID testRunId, EID testReportId) {
			this.testRunId = testRunId.toString();
			this.testReportId = testReportId.toString();
		}

		public String getTestRunId() {
			return testRunId;
		}

		public String getTestReportId() {
			return testReportId;
		}

		static SimpleTestRunInfoDto create(TestRunTask testRunTask) {
			return new SimpleTestRunInfoDto(testRunTask.getTestRun().getId(),
					testRunTask.getTestRun().getReport().getId());
		}
	}

	@RequestMapping(value = "/rest/testruns/start", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public SimpleTestRunInfoDto start(@RequestBody SimpleTestRunDto dto) throws ObjectWithIdNotFoundException, StoreException, ConfigurationException, InitializationException, InvalidStateTransitionException, ComponentNotLoadedException {

		TestRunDto testRunDto = new TestRunDto();
		testRunDto.setLabel(dto.getTestRunLabel());
		testRunDto.setId(EidFactory.getDefault().createRandomUuid());
		final TestObjectDto to = testObjectController.getTestObjStore().getDtoById(dto.getTestObjectId());
		testRunDto.setTestObject(to);
		final TestReportDto rep = testReportDao.createReport(testRunDto.getLabel(), testRunDto.getTestObject());
		testRunDto.setTestReport(rep);
		final TestProjectDto tp = taskFactory.getProjectById(dto.getTestProjectId());
		testRunDto.setTestProject(tp);
		taskPoolRegistry.removeDone();
		testRunDto.setId(EidFactory.getDefault().createRandomUuid());

		final TestRunTask testRunTask = initAndSubmit(testRunDto);

		return SimpleTestRunInfoDto.create(testRunTask);
	}

}
