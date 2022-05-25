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
import static de.interactive_instruments.etf.webapp.dto.DocumentationConstants.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import de.interactive_instruments.etf.component.loaders.LoadingContext;
import de.interactive_instruments.etf.dal.dao.*;
import de.interactive_instruments.etf.dal.dto.capabilities.TestObjectTypeDto;
import de.interactive_instruments.etf.detector.TestObjectTypeDetectorManager;
import de.interactive_instruments.etf.model.EID;
import de.interactive_instruments.etf.model.EidMap;
import de.interactive_instruments.etf.webapp.WebAppConstants;
import de.interactive_instruments.etf.webapp.helpers.SimpleFilter;
import de.interactive_instruments.exceptions.ObjectWithIdNotFoundException;
import de.interactive_instruments.exceptions.StorageException;
import de.interactive_instruments.exceptions.config.ConfigurationException;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
@RestController
public class TestObjectTypeController implements DtoResolver<TestObjectTypeDto> {

    @Autowired
    private DataStorageService dataStorageService;

    @Autowired
    private StreamingService streaming;

    @Autowired
    private LoadingContext loadingContext;

    private final Logger logger = LoggerFactory.getLogger(TestObjectTypeController.class);

    private Dao<TestObjectTypeDto> testObjectTypeDao;
    private EidMap<TestObjectTypeDto> supportedTypes;
    private final static String TEST_OBJECT_TYPES_URL = WebAppConstants.API_BASE_URL + "/TestObjectTypes";

    private final static String TEST_OBJECT_TYPE_DESCRIPTION = "The Test Object model is described in the "
            + "[XML schema documentation](https://resources.etf-validator.net/schema/v2/doc/capabilities_xsd.html#TestObjectType) "
            + ETF_ITEM_COLLECTION_DESCRIPTION;

    @PostConstruct
    private void init() throws IOException {
        testObjectTypeDao = dataStorageService.getDao(TestObjectTypeDto.class);
        supportedTypes = TestObjectTypeDetectorManager.getSupportedTypes();
        ((WriteDao) testObjectTypeDao).deleteAllExisting(supportedTypes.keySet());
        ((WriteDao) testObjectTypeDao).addAll(supportedTypes.values());
        loadingContext.getItemRegistry().register(supportedTypes.values());

        streaming.prepareCache(testObjectTypeDao, SimpleFilter.allItems());
        logger.info("Test Object Type controller initialized");
    }

    @Override
    public TestObjectTypeDto getById(final EID id)
            throws StorageException, ObjectWithIdNotFoundException {
        return supportedTypes.get(id);
    }

    @Override
    public Collection<TestObjectTypeDto> getByIds(final Set<EID> ids)
            throws StorageException, ObjectWithIdNotFoundException {
        return supportedTypes.getAll(ids).values();
    }

    //
    // Rest interfaces
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @ApiOperation(value = "Get Test Object Type as JSON", notes = TEST_OBJECT_TYPE_DESCRIPTION, tags = {
            SERVICE_CAP_TAG_NAME})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Test Object Type"),
            @ApiResponse(code = 404, message = "Test Object Type not found")
    })
    @RequestMapping(value = {TEST_OBJECT_TYPES_URL + "/{id}", TEST_OBJECT_TYPES_URL + "/{id}.json"}, method = RequestMethod.GET)
    public void testObjectTypesByIdJson(
            @ApiParam(value = EID_DESCRIPTION, example = EID_EXAMPLE, required = true) @PathVariable String id,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException, ObjectWithIdNotFoundException {
        streaming.asJson2(testObjectTypeDao, request, response, id);
    }

    @ApiOperation(value = "Get multiple Test Object Types as JSON", notes = TEST_OBJECT_TYPE_DESCRIPTION, tags = {
            SERVICE_CAP_TAG_NAME})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "EtfItemCollection with multiple Test Object Types")
    })
    @RequestMapping(value = {TEST_OBJECT_TYPES_URL, TEST_OBJECT_TYPES_URL + ".json"}, method = RequestMethod.GET)
    public void listTestObjectTypesJson(
            @ApiParam(value = OFFSET_DESCRIPTION) @RequestParam(required = false, defaultValue = "0") int offset,
            @ApiParam(value = LIMIT_DESCRIPTION) @RequestParam(required = false, defaultValue = "0") int limit,
            HttpServletRequest request,
            HttpServletResponse response)
            throws ConfigurationException, IOException, ObjectWithIdNotFoundException {
        streaming.asJson2(testObjectTypeDao, request, response, new SimpleFilter(offset, limit));
    }

    @ApiOperation(value = "Get multiple Test Object Types as XML", notes = TEST_OBJECT_TYPE_DESCRIPTION, tags = {
            SERVICE_CAP_TAG_NAME}, produces = "text/xml")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "EtfItemCollection with multiple Test Object Types")
    })
    @RequestMapping(value = {TEST_OBJECT_TYPES_URL + ".xml"}, method = RequestMethod.GET)
    public void listTestObjectTypesXml(
            @RequestParam(required = false, defaultValue = "0") int offset,
            @RequestParam(required = false, defaultValue = "0") int limit,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException, ObjectWithIdNotFoundException {
        streaming.asXml2(testObjectTypeDao, request, response, new SimpleFilter(offset, limit));
    }

    @ApiOperation(value = "Get Test Object Type as XML", notes = TEST_OBJECT_TYPE_DESCRIPTION, tags = {
            SERVICE_CAP_TAG_NAME}, produces = "text/xml")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Test Object Type"),
            @ApiResponse(code = 404, message = "Test Object Type not found")
    })
    @RequestMapping(value = {TEST_OBJECT_TYPES_URL + "/{id}.xml"}, method = RequestMethod.GET)
    public void testObjectTypesByIdXml(
            @ApiParam(value = EID_DESCRIPTION, example = EID_EXAMPLE, required = true) @PathVariable String id,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException, ObjectWithIdNotFoundException {
        streaming.asXml2(testObjectTypeDao, request, response, id);
    }

}
