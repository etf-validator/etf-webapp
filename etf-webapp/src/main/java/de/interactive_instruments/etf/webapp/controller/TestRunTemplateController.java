/**
 * Copyright 2010-2020 interactive instruments GmbH
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

import static de.interactive_instruments.etf.webapp.SwaggerConfig.SERVICE_CAP_TAG_NAME;
import static de.interactive_instruments.etf.webapp.WebAppConstants.API_BASE_URL;
import static de.interactive_instruments.etf.webapp.controller.EtfConfig.ETF_TEST_RUN_TEMPLATES_ALLOW_CREATION;
import static de.interactive_instruments.etf.webapp.dto.DocumentationConstants.*;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import springfox.documentation.annotations.ApiIgnore;

import de.interactive_instruments.etf.dal.dao.*;
import de.interactive_instruments.etf.dal.dto.IncompleteDtoException;
import de.interactive_instruments.etf.dal.dto.capabilities.TestRunTemplateDto;
import de.interactive_instruments.etf.model.EID;
import de.interactive_instruments.etf.webapp.conversion.EidConverter;
import de.interactive_instruments.etf.webapp.dto.ApiError;
import de.interactive_instruments.etf.webapp.dto.CreateTestRunTemplateRequest;
import de.interactive_instruments.etf.webapp.helpers.SimpleFilter;
import de.interactive_instruments.etf.webapp.helpers.User;
import de.interactive_instruments.exceptions.ObjectWithIdNotFoundException;
import de.interactive_instruments.exceptions.StorageException;
import de.interactive_instruments.exceptions.config.ConfigurationException;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * Controller for creating Test Run Template and applying them to Test Objects
 */
@RestController
public class TestRunTemplateController implements PreparedDtoResolver<TestRunTemplateDto> {

    @Autowired
    DataStorageService dataStorageService;

    @Autowired
    private TestObjectController testObjectController;

    @Autowired
    private TestObjectTypeController testObjectTypeController;

    @Autowired
    private TestDriverController testDriverController;

    @Autowired
    private EtfConfig etfConfig;

    @Autowired
    private StreamingService streaming;

    private final Cache<EID, TestRunTemplateDto> transientTestRunTemplates = Caffeine.newBuilder().expireAfterWrite(
            7, TimeUnit.MINUTES).build();

    boolean simplifiedWorkflows;

    private Dao<TestRunTemplateDto> testRunTemplateDao;

    private final static String TEST_RUN_TEMPLATES_URL = API_BASE_URL + "/TestRunTemplates";
    private final Logger logger = LoggerFactory.getLogger(TestRunTemplateController.class);

    private final static String TEST_RUN_TEMPLATE_DESC = "Test Run Templates are a set of "
            + "preselected Executable Test Suites and parameters. The Test Run Template model is described in the "
            + "[XML schema documentation](https://resources.etf-validator.net/schema/v2/doc/capabilities_xsd.html#TestRunTemplate). "
            + ETF_ITEM_COLLECTION_DESCRIPTION;

    public TestRunTemplateController() {}

    @PostConstruct
    public void init() throws ParseException, ConfigurationException, IOException {
        simplifiedWorkflows = "simplified".equals(etfConfig.getProperty(EtfConfig.ETF_WORKFLOWS));
        testRunTemplateDao = dataStorageService.getDao(TestRunTemplateDto.class);
        streaming.prepareCache(testRunTemplateDao, SimpleFilter.allItems());
        logger.info("Test Run Template controller initialized!");

    }

    @Override
    public PreparedDto<TestRunTemplateDto> getById(final EID id, final Filter filter)
            throws StorageException, ObjectWithIdNotFoundException {
        return testRunTemplateDao.getById(id, filter);
    }

    @Override
    public PreparedDtoCollection<TestRunTemplateDto> getByIds(final Set<EID> id, final Filter filter)
            throws StorageException, ObjectWithIdNotFoundException {
        return testRunTemplateDao.getByIds(id, filter);
    }

    //
    // Rest interfaces
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @ApiOperation(value = "Get Test Run Template as JSON", notes = TEST_RUN_TEMPLATE_DESC, tags = {
            SERVICE_CAP_TAG_NAME})
    @RequestMapping(value = {TEST_RUN_TEMPLATES_URL + "/{id}",
            TEST_RUN_TEMPLATES_URL + "/{id}.json"}, method = RequestMethod.GET, produces = "application/json")
    public void testObjectByIdJson(
            @ApiParam(value = "ID of Test Run Template that needs to be fetched", example = "EID-1ffe6ea2-5c29-4ce9-9a7e-f4d9d71119e8", required = true) @PathVariable String id,
            @ApiParam(value = FIELDS_DESCRIPTION) @RequestParam(required = false, defaultValue = "*") String fields,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException, LocalizableApiError, ObjectWithIdNotFoundException {
        streaming.asJson2(testRunTemplateDao, request, response, id, SimpleFilter.singleItemFilter(fields));
    }

    @ApiOperation(value = "Get multiple Test Run Templates as JSON", notes = TEST_RUN_TEMPLATE_DESC, tags = {
            SERVICE_CAP_TAG_NAME})
    @RequestMapping(value = {TEST_RUN_TEMPLATES_URL, TEST_RUN_TEMPLATES_URL + ".json"}, method = RequestMethod.GET)
    public void listTestRunTemplatesJson(
            @ApiParam(value = OFFSET_DESCRIPTION) @RequestParam(required = false, defaultValue = "0") int offset,
            @ApiParam(value = LIMIT_DESCRIPTION) @RequestParam(required = false, defaultValue = "0") int limit,
            @ApiParam(value = FIELDS_DESCRIPTION) @RequestParam(required = false, defaultValue = "*") String fields,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        streaming.asJson2(testRunTemplateDao, request, response, SimpleFilter.filterItems(offset, limit, fields));
    }

    @ApiOperation(value = "Get multiple Test Run Templates as XML", notes = TEST_RUN_TEMPLATE_DESC, tags = {
            SERVICE_CAP_TAG_NAME}, produces = "text/xml")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "EtfItemCollection with multiple Test Run Templates")
    })
    @RequestMapping(value = {TEST_RUN_TEMPLATES_URL + ".xml"}, method = RequestMethod.GET)
    public void listTestRunTemplatesXml(
            @ApiParam(value = OFFSET_DESCRIPTION) @RequestParam(required = false, defaultValue = "0") int offset,
            @ApiParam(value = LIMIT_DESCRIPTION) @RequestParam(required = false, defaultValue = "0") int limit,
            @ApiParam(value = FIELDS_DESCRIPTION) @RequestParam(required = false, defaultValue = "*") String fields,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        streaming.asXml2(testRunTemplateDao, request, response, SimpleFilter.filterItems(offset, limit, fields));
    }

    @ApiOperation(value = "Get Test Run Template as XML", notes = TEST_RUN_TEMPLATE_DESC, tags = {
            SERVICE_CAP_TAG_NAME}, produces = "text/xml")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Test Run Template"),
            @ApiResponse(code = 404, message = "Test Run Template not found")
    })
    @RequestMapping(value = {TEST_RUN_TEMPLATES_URL + "/{id}.xml"}, method = RequestMethod.GET)
    public void testRunTemplateByIdXml(
            @ApiParam(value = "ID of Test Run Template that needs to be fetched", example = "EID-1ffe6ea2-5c29-4ce9-9a7e-f4d9d71119e8", required = true) @PathVariable String id,
            @ApiParam(value = FIELDS_DESCRIPTION) @RequestParam(required = false, defaultValue = "*") String fields,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException, ObjectWithIdNotFoundException, LocalizableApiError {
        streaming.asXml2(testRunTemplateDao, request, response, id, SimpleFilter.singleItemFilter(fields));
    }

    @ApiOperation(value = "Check if the Test Run Template exists", notes = TEST_RUN_TEMPLATE_DESC, tags = {
            SERVICE_CAP_TAG_NAME})
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Test Run Template exists", response = Void.class),
            @ApiResponse(code = 404, message = "Test Run Template does not exist", response = Void.class),
    })
    @RequestMapping(value = {TEST_RUN_TEMPLATES_URL + "/{id}"}, method = RequestMethod.HEAD)
    public ResponseEntity exists(
            @ApiParam(value = "Test Run Template ID. "
                    + EID_DESCRIPTION, example = EID_EXAMPLE, required = true) @PathVariable String id) {
        final EID eid = EidConverter.toEid(id);
        return testRunTemplateDao.available(eid) ? new ResponseEntity(HttpStatus.NO_CONTENT)
                : new ResponseEntity(HttpStatus.NOT_FOUND);
    }

    @ApiOperation(value = "Create a new Test Run Template", notes = "Please note that this function can be deactivated by the Administrator and the interface then always responds"
            + " with a 403 status code. A Test Run Template bundles a set of Executable Test Suites,"
            + " including their descriptive properties and arguments, and allows them to be applied to test objects."
            + " At least one Executable Test Suite must be specified during creation."
            + " If more than one Executable Test Suites is specified, then there must be at least one"
            + " Test Object Type that is supported by all Executable Test Suites."
            + " If a fixed Test Object is provided its type must of course also be supported."
            + " Specifying the test object is optional. If the test object is not specified when the template is created, "
            + " it must be specified when the template is applied. Otherwise, the test object cannot be overwritten "
            + " after it has been specified in the template."
            + " Arguments for the template are automatically taken from all Executable Test Suites."
            + " If there are Parameters with the same name but different default values"
            + " in two Executable Test Suites, these must be explicitly overridden."
            + " Properties are only adopted in the Test Run Template if they are identical in all Executable Test Suites. "
            + " Otherwise, they must be specified explicitly."
            + "\n\n"
            + " Example for creating a Test Run Template :<br/>"
            + "\n\n"
            + "    {\n"
            + "        \"label\": \"Metadata Full Conformance\",\n"
            + "        \"defaultParameterValues\": {\n"
            + "            \"tests_to_execute\": \".*\"\n"
            + "        },\n"
            + "        \"executableTestSuiteIds\": [\n"
            + "            \"EIDec7323d5-d8f0-4cfe-b23a-b826df86d58c\",\n"
            + "            \"EID9a31ecfc-6673-43c0-9a31-b4595fb53a98\"\n"
            + "        ]\n"
            + "    }\n"
            + "\n\n"
            + "  ", tags = {SERVICE_CAP_TAG_NAME})
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Test Run Template created"),
            @ApiResponse(code = 400, message = "Invalid request", response = ApiError.class),
            @ApiResponse(code = 403, message = "Test Run Template creation deactivated", response = ApiError.class),
            @ApiResponse(code = 404, message = "At least one specified Executable Test Suite, a Test Object Type or a Test Object could not be found", response = ApiError.class),
            @ApiResponse(code = 409, message = "Type of the Test Object not supported by the Executable Test Suites or"
                    + " due to a conflict, Parameters with different default values must be explicitly overwritten", response = ApiError.class),
            @ApiResponse(code = 500, message = "Internal error", response = ApiError.class),
    })
    @RequestMapping(value = TEST_RUN_TEMPLATES_URL, method = RequestMethod.POST)
    public void create(@RequestBody @Valid CreateTestRunTemplateRequest createTestRunTemplateRequest,
            BindingResult result,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException, LocalizableApiError, IncompleteDtoException, ObjectWithIdNotFoundException {

        if (!"true".equals(etfConfig.getProperty(ETF_TEST_RUN_TEMPLATES_ALLOW_CREATION))) {
            throw new LocalizableApiError("l.json.testruntemplate.creation.forbidden", false, 403);
        }
        if (result.hasErrors()) {
            throw new LocalizableApiError(result.getFieldError());
        }

        createTestRunTemplateRequest.init(testDriverController, testObjectTypeController, testObjectController);
        final TestRunTemplateDto testRunTemplate = createTestRunTemplateRequest.toTestRunTemplate();
        testRunTemplate.setAuthor(User.getUser(request));
        testRunTemplate.ensureBasicValidity();
        ((WriteDao<TestRunTemplateDto>) this.testRunTemplateDao).add(testRunTemplate);

        response.setStatus(HttpStatus.CREATED.value());
        streaming.asJson2(testRunTemplateDao,
                request,
                response,
                testRunTemplate.getId().getId(),
                SimpleFilter.singleItemFilter("id"));
    }

    @RequestMapping(value = {TEST_RUN_TEMPLATES_URL}, method = RequestMethod.POST, consumes = "multipart/form-data")
    public void uploadData(
            @ApiIgnore final MultipartHttpServletRequest request, @ApiIgnore HttpServletResponse response)
            throws LocalizableApiError, IOException,
            IncompleteDtoException, ObjectWithIdNotFoundException {

        // TODO temporary stream this into the local store
        final Collection<List<MultipartFile>> uploadFiles = request.getMultiFileMap().values();
        if (uploadFiles.isEmpty()) {
            throw new LocalizableApiError("l.invalid.data", false, 400);
        }
        final MultipartFile file = uploadFiles.iterator().next().iterator().next();
        final TestRunTemplateDto testRunTemplate = ((StreamWriteDao<TestRunTemplateDto>) this.testRunTemplateDao)
                .add(file.getInputStream(), Optional.empty());
        transientTestRunTemplates.put(testRunTemplate.getId(), testRunTemplate);
        streaming.asJson2(testRunTemplateDao,
                request,
                response,
                testRunTemplate.getId().getId(),
                SimpleFilter.singleItemFilter("*"));
        ((StreamWriteDao<TestRunTemplateDto>) this.testRunTemplateDao).delete(testRunTemplate.getId());
    }
}
