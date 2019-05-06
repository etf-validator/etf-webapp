/**
 * Copyright 2017-2019 European Union, interactive instruments GmbH
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
import static de.interactive_instruments.etf.webapp.WebAppConstants.API_BASE_URL;
import static de.interactive_instruments.etf.webapp.dto.DocumentationConstants.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
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
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import de.interactive_instruments.*;
import de.interactive_instruments.etf.EtfConstants;
import de.interactive_instruments.etf.dal.dao.Dao;
import de.interactive_instruments.etf.dal.dao.PreparedDto;
import de.interactive_instruments.etf.dal.dao.PreparedDtoCollection;
import de.interactive_instruments.etf.dal.dao.WriteDao;
import de.interactive_instruments.etf.dal.dao.basex.BsxPreparedDtoException;
import de.interactive_instruments.etf.dal.dto.Dto;
import de.interactive_instruments.etf.dal.dto.capabilities.TestObjectDto;
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
import de.interactive_instruments.exceptions.*;
import de.interactive_instruments.exceptions.config.ConfigurationException;
import de.interactive_instruments.properties.Properties;
import de.interactive_instruments.properties.PropertyHolder;
import io.swagger.annotations.*;

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

    private Timer cleanTimer;
    // 5 minutes after start
    private final long initialDelay = 300000;
    private TimedExpiredItemsRemover timedExpiredItemsRemover;
    // default 1 hour
    private String cacheMaxAgeSeconds = "3600";

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

    private final static String HTML_REPORT_NOTE = " If the Accept-Language header is set and translations are available in that "
            + "language, the report or parts of it will be returned in the requested language. English is used as fallback language. "
            + "Note: changing the Accept-Language header may not work in the Swagger User Interface.";

    private static class TestResultCleaner implements ExpirationItemHolder {
        private final WriteDao<TestRunDto> testRunDao;
        private final WriteDao<TestObjectDto> testObjectDao;
        private final static Logger logger = LoggerFactory.getLogger(TestResultCleaner.class);

        private TestResultCleaner(final Dao<TestRunDto> testRunDao,
                final Dao<TestObjectDto> testObjectDao) {
            this.testRunDao = (WriteDao<TestRunDto>) testRunDao;
            this.testObjectDao = (WriteDao<TestObjectDto>) testObjectDao;
        }

        private boolean removeTestRun(final TestRunDto testRunDto, final long maxLifeTimeMillis) {
            boolean removed = false;
            final long expirationTime = testRunDto.getStartTimestamp().getTime() + maxLifeTimeMillis;
            if (System.currentTimeMillis() > expirationTime) {
                final List<TestObjectDto> testObjects = testRunDto.getTestObjects();
                try {
                    testRunDao.delete(testRunDto.getId());
                    removed = true;
                } catch (final Exception e) {
                    logger.warn("Error deleting expired item ", e);
                }
                for (final TestObjectDto testObjectDto : testObjects) {
                    try {
                        testObjectDao.delete(testObjectDto.getId());
                    } catch (final Exception e) {
                        logger.warn("Error deleting expired item ", e);
                    }
                }
            }
            return removed;
        }

        @Override
        public void removeExpiredItems(final long maxLifeTime, final TimeUnit unit) {
            int removed = 0;
            try {
                // TODO filter dtos by timestamp
                final PreparedDtoCollection<TestRunDto> allTestRuns = testRunDao.getAll(new SimpleFilter());
                for (final TestRunDto testRunDto : allTestRuns) {
                    if (removeTestRun(testRunDto, unit.toMillis(maxLifeTime))) {
                        removed++;
                    }
                }
            } catch (BsxPreparedDtoException | StorageException e) {
                logger.warn("Using fallback mechanism for safe deletion");
                try {
                    final Set<EID> allTestRuns = testRunDao.getAll(new SimpleFilter()).keySet();
                    for (final EID id : allTestRuns) {
                        try {
                            final TestRunDto testRunDto = testRunDao.getById(id).getDto();
                            if (removeTestRun(testRunDto, unit.toMillis(maxLifeTime))) {
                                removed++;
                            }
                        } catch (ObjectWithIdNotFoundException | BsxPreparedDtoException | StorageException ign) {
                            ExcUtils.suppress(ign);
                        }
                    }
                } catch (BsxPreparedDtoException | StorageException ign) {
                    ExcUtils.suppress(ign);
                }
            }
            logger.info("{} items were cleaned.", removed);
        }
    }

    public TestResultController() {

    }

    @PostConstruct
    public void init() throws IOException, TransformerConfigurationException,
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

        final long exp = etfConfig.getPropertyAsLong(EtfConfigController.ETF_TESTREPORTS_LIFETIME_EXPIRATION);
        if (exp > 0) {
            cleanTimer = new Timer(true);
            // final TimedExpiredItemsRemover timedExpiredItemsRemover = new TimedExpiredItemsRemover();
            timedExpiredItemsRemover = new TimedExpiredItemsRemover();
            timedExpiredItemsRemover.addExpirationItemHolder(new TestResultCleaner(testRunDao,
                    dataStorageService.getDao(TestObjectDto.class)), exp, TimeUnit.MINUTES);
            logger.info("Test reports older than {} minutes are removed.", exp);
            final long expCacheSeconds = TimeUnit.MINUTES.toSeconds(exp);
            if (expCacheSeconds < Long.valueOf(cacheMaxAgeSeconds)) {
                cacheMaxAgeSeconds = String.valueOf(expCacheSeconds);
            }
            cleanTimer.scheduleAtFixedRate(timedExpiredItemsRemover,
                    TimeUnit.SECONDS.toMillis(TimeUtils.calcDelay(0, 9, 0)),
                    86400000);
        }

        logger.info("Result controller initialized!");
    }

    @PreDestroy
    private void shutdown() {
        testRunDao.release();

        if (this.cleanTimer != null) {
            cleanTimer.cancel();
        }
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

                // Set language
                final Locale locale;
                final String langParameter = request.getParameter("lang");
                if (!SUtils.isNullOrEmpty(langParameter)) {
                    locale = new Locale(langParameter);
                } else {
                    locale = LocaleContextHolder.getLocale();
                }
                final PropertyHolder properties = new Properties().setProperty("language", locale.getLanguage());

                if (Objects.equals(download, "true")) {
                    final String reportFileName;
                    if (preparedDto.getDto() instanceof TestRunDto) {
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
                    preparedDto.streamTo(testRunHtmlReportFormat, properties, out);
                } else {
                    response.setContentType(MediaType.TEXT_HTML_VALUE);
                    preparedDto.streamTo(testRunHtmlReportFormat, properties, out);
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

    private void setMaxAgeHeader(final HttpServletResponse response) {
        response.setHeader("Cache-Control", "public, max-age=" + cacheMaxAgeSeconds);
    }

    //
    // Rest interfaces
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @RequestMapping(value = {TEST_RUNS_URL + "/clean"}, method = RequestMethod.GET)
    public void clean(HttpServletResponse response) throws IOException {
        timedExpiredItemsRemover.run();
        response.setStatus(200);
        response.getWriter().write("OK");
    }

    @ApiOperation(value = "Get multiple Test Results as XML", notes = TEST_RUN_DESCRIPTION, tags = {TEST_RESULTS_TAG_NAME})
    @RequestMapping(value = {TEST_RUNS_URL + ".xml"}, method = RequestMethod.GET)
    public void testRunsXml(
            @ApiParam(value = OFFSET_DESCRIPTION) @RequestParam(required = false, defaultValue = "0") int offset,
            @ApiParam(value = LIMIT_DESCRIPTION) @RequestParam(required = false, defaultValue = "0") int limit,
            HttpServletRequest request,
            HttpServletResponse response) throws StorageException, IOException, ObjectWithIdNotFoundException {
        setMaxAgeHeader(response);
        streaming.asXml2(testRunDao, request, response, new SimpleFilter(offset, limit));
    }

    @ApiOperation(value = "Get a single Test Result as XML", notes = TEST_RUN_DESCRIPTION, tags = {TEST_RESULTS_TAG_NAME})
    @RequestMapping(value = {TEST_RUNS_URL + "/{id}.xml"}, method = RequestMethod.GET)
    public void testRunByIdXml(
            @ApiParam(value = "Test Run ID. "
                    + EID_DESCRIPTION, example = EID_EXAMPLE, required = true) @PathVariable String id,
            HttpServletRequest request, HttpServletResponse response)
            throws StorageException, IOException, ObjectWithIdNotFoundException {
        setMaxAgeHeader(response);
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
        setMaxAgeHeader(response);
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
        setMaxAgeHeader(response);
        streaming.asJson2(testRunDao, request, response, id);
    }

    @ApiOperation(value = "Generate a HTML Test Report", notes = "Generates a HTML report from the test results of one Test Run."
            + HTML_REPORT_NOTE, produces = "text/html", tags = {
                    TEST_RESULTS_TAG_NAME})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Accept-Language", value = "Report language", dataType = "string", paramType = "header")})
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
        setMaxAgeHeader(response);
        getByIdHtml(testRunDao, id, download, request, response);
    }

    @ApiOperation(value = "Get a Test Run's log by ID", notes = "Retrieves all messages that were logged during a Test Run.", tags = {
            TEST_RESULTS_TAG_NAME})
    @RequestMapping(value = {TEST_RUNS_URL + "/{id}/log"}, method = RequestMethod.GET)
    public void testRunLog(
            @ApiParam(value = "Test Run ID. "
                    + EID_DESCRIPTION, example = EID_EXAMPLE, required = true) @PathVariable String id,
            HttpServletResponse response) throws StorageException, IOException, LocalizableApiError {
        setMaxAgeHeader(response);
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
        setMaxAgeHeader(response);
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
        setMaxAgeHeader(response);
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
        setMaxAgeHeader(response);
        streaming.asJson2(testTaskResultDao, request, response, id);
    }

    @ApiOperation(value = "Generate a HTML Test Report from a single Test Task within a Test Run. ", notes = "Generates a HTML report from one single result of Test Task the within a Test Run. "
            + TEST_TASK_RESULT_NOTE + HTML_REPORT_NOTE, produces = "text/html", tags = {TEST_RESULTS_TAG_NAME})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Accept-Language", value = "Report language", dataType = "string", paramType = "header")})
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
        setMaxAgeHeader(response);
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
