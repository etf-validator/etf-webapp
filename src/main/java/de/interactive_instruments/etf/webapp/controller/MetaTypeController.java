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
import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerConfigurationException;

import de.interactive_instruments.etf.dal.dto.capabilities.TagDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import de.interactive_instruments.etf.dal.dao.Dao;
import de.interactive_instruments.etf.dal.dto.capabilities.ComponentDto;
import de.interactive_instruments.etf.dal.dto.capabilities.TestObjectDto;
import de.interactive_instruments.etf.dal.dto.capabilities.TestObjectTypeDto;
import de.interactive_instruments.etf.dal.dto.test.TestItemTypeDto;
import de.interactive_instruments.etf.dal.dto.translation.TranslationTemplateBundleDto;
import de.interactive_instruments.etf.model.OutputFormat;
import de.interactive_instruments.exceptions.ObjectWithIdNotFoundException;
import de.interactive_instruments.exceptions.StorageException;
import de.interactive_instruments.exceptions.config.ConfigurationException;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * @author J. Herrmann ( herrmann <aT) interactive-instruments (doT> de )
 */
@RestController
public class MetaTypeController {

	@Autowired
	ServletContext servletContext;

	@Autowired
	private TestDriverService testDriverService;

	@Autowired
	private DataStorageService dataStorageService;

	private final Logger logger = LoggerFactory.getLogger(MetaTypeController.class);
	private Dao<TestItemTypeDto> testItemTypeDao;
	private OutputFormat testItemTypeXmlOutputFormat;

	private Dao<TestObjectTypeDto> testObjectTypeDao;
	private OutputFormat testObjectTypeXmlOutputFormat;

	private Dao<TranslationTemplateBundleDto> translationTemplateBundleDao;
	private OutputFormat translationTemplateBundleXmlOutputFormat;

	private Dao<ComponentDto> componentDao;
	private OutputFormat componentXmlOutputFormat;

	private Dao<TagDto> tagDao;
	private OutputFormat tagXmlOutputFormat;

	@PostConstruct
	private void init() throws IOException, TransformerConfigurationException {
		translationTemplateBundleDao = dataStorageService.getDao(TranslationTemplateBundleDto.class);
		translationTemplateBundleXmlOutputFormat = translationTemplateBundleDao.getOutputFormats().values().iterator().next();

		testObjectTypeDao = dataStorageService.getDao(TestObjectTypeDto.class);
		testObjectTypeXmlOutputFormat = testObjectTypeDao.getOutputFormats().values().iterator().next();

		testItemTypeDao = dataStorageService.getDao(TestItemTypeDto.class);
		testItemTypeXmlOutputFormat = testItemTypeDao.getOutputFormats().values().iterator().next();

		componentDao = dataStorageService.getDao(ComponentDto.class);
		componentXmlOutputFormat = componentDao.getOutputFormats().values().iterator().next();

		tagDao = dataStorageService.getDao(TagDto.class);
		tagXmlOutputFormat = tagDao.getOutputFormats().values().iterator().next();

		logger.info("Meta Type controller initialized");
	}

	///////////////// Translation Template Bundles

	private final static String TEST_ITEM_TYPES_URL = API_BASE_URL + "/TestItemTypes";

	@ApiOperation(value = "Get all Test Item Types", tags = {"Service Capabilities"}, response = TestItemTypeDto.class)
	@RequestMapping(value = {TEST_ITEM_TYPES_URL + "/{id}.json"}, method = RequestMethod.GET, produces = "application/json")
	public void testItemTypeByIdJson(@PathVariable String id, HttpServletResponse response) throws IOException, StorageException, ObjectWithIdNotFoundException {
		streamAsJson2(testItemTypeDao, response, id);
	}

	@ApiOperation(value = "Get all Test Item Types", tags = {"Service Capabilities"}, response = TestItemTypeDto.class, responseContainer = "List")
	@RequestMapping(value = TEST_ITEM_TYPES_URL + ".json", method = RequestMethod.GET, produces = "application/json")
	public void listTestItemTypesJson(HttpServletResponse response) throws StorageException, ConfigurationException, IOException, ObjectWithIdNotFoundException {
		streamAsJson2(testItemTypeDao, response, null);
	}

	@ApiOperation(value = "Get all Test Item Types", tags = {"Service Capabilities"}, produces = "text/xml")
	@ApiResponses(@ApiResponse(code = 200, message = "EtfItemCollection with multiple Test Item Types", reference = "www.interactive-instruments.de"))
	@RequestMapping(value = {TEST_ITEM_TYPES_URL, TEST_ITEM_TYPES_URL + ".xml"}, method = RequestMethod.GET)
	public void listTestItemTypesXml(HttpServletResponse response) throws IOException, StorageException, ObjectWithIdNotFoundException {
		streamAsXml2(testItemTypeDao, testItemTypeXmlOutputFormat, response, null);
	}

	@ApiOperation(value = "Get all Test Item Types", tags = {"Service Capabilities"}, produces = "text/xml")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "EtfItemCollection with one Test Item Type", reference = "www.interactive-instruments.de"),
			@ApiResponse(code = 404, message = "Test Item Type not found")
	})
	@RequestMapping(value = {TEST_ITEM_TYPES_URL + "/{id}", TEST_ITEM_TYPES_URL + "/{id}.xml"}, method = RequestMethod.GET)
	public void testItemTypeByIdXml(@PathVariable String id, HttpServletResponse response) throws IOException, StorageException, ObjectWithIdNotFoundException {
		streamAsXml2(testItemTypeDao, testItemTypeXmlOutputFormat, response, id);
	}

	///////////////// Translation Template Bundles

	private final static String TRANSLATION_TEMP_BUNDLE_URL = API_BASE_URL + "/TranslationTemplateBundles";

	@ApiOperation(value = "Get all Translation Template Bundles", tags = {"Service Capabilities"}, response = TranslationTemplateBundleDto.class)
	@RequestMapping(value = {TRANSLATION_TEMP_BUNDLE_URL + "/{id}.json"}, method = RequestMethod.GET, produces = "application/json")
	public TranslationTemplateBundleDto translationTemplateBundleByIdJson(@PathVariable String id) throws IOException, StorageException, ObjectWithIdNotFoundException {
		return translationTemplateBundleDao.getById(WebAppUtils.toEid(id)).getDto();
	}

	@ApiOperation(value = "Get all Translation Template Bundles", tags = {"Service Capabilities"}, response = TranslationTemplateBundleDto.class, responseContainer = "List")
	@RequestMapping(value = TRANSLATION_TEMP_BUNDLE_URL + ".json", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody Collection<TranslationTemplateBundleDto> listTranslationTemplateBundlesJson() throws StorageException, ConfigurationException {
		return translationTemplateBundleDao.getAll(ALL_FILTER).asCollection();
	}

	@ApiOperation(value = "Get all Translation Template Bundles", tags = {"Service Capabilities"}, produces = "text/xml")
	@ApiResponses(@ApiResponse(code = 200, message = "EtfItemCollection with multiple Translation Template Bundles", reference = "www.interactive-instruments.de"))
	@RequestMapping(value = {TRANSLATION_TEMP_BUNDLE_URL, TRANSLATION_TEMP_BUNDLE_URL + ".xml"}, method = RequestMethod.GET)
	public void listTranslationTemplateBundlesXml(HttpServletResponse response) throws IOException, StorageException, ObjectWithIdNotFoundException {
		streamAsXml2(translationTemplateBundleDao, translationTemplateBundleXmlOutputFormat, response, null);
	}

	@ApiOperation(value = "Get all Translation Template Bundles", tags = {"Service Capabilities"}, produces = "text/xml")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "EtfItemCollection with one Translation Template Bundle", reference = "www.interactive-instruments.de"),
			@ApiResponse(code = 404, message = "Translation Template Bundle not found")
	})
	@RequestMapping(value = {TRANSLATION_TEMP_BUNDLE_URL + "/{id}", TRANSLATION_TEMP_BUNDLE_URL + "/{id}.xml"}, method = RequestMethod.GET)
	public void translationTemplateBundleByIdXml(@PathVariable String id, HttpServletResponse response) throws IOException, StorageException, ObjectWithIdNotFoundException {
		streamAsXml2(translationTemplateBundleDao, translationTemplateBundleXmlOutputFormat, response, id);
	}

	///////////////// Translation Template Bundles

	private final static String TEST_OBJECT_TYPES_URL = API_BASE_URL + "/TestObjectTypes";

	@ApiOperation(value = "Get all Test Object Types", tags = {"Service Capabilities"}, response = ComponentDto.class)
	@RequestMapping(value = {TEST_OBJECT_TYPES_URL + "/{id}.json"}, method = RequestMethod.GET, produces = "application/json")
	public TestObjectTypeDto testObjectTypesByIdJson(@PathVariable String id) throws IOException, StorageException, ObjectWithIdNotFoundException {
		return testObjectTypeDao.getById(WebAppUtils.toEid(id)).getDto();
	}

	@ApiOperation(value = "Get all Test Object Types", tags = {"Service Capabilities"}, response = TestObjectDto.class, responseContainer = "List")
	@RequestMapping(value = TEST_OBJECT_TYPES_URL + ".json", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody Collection<TestObjectTypeDto> listTestObjectTypesJson() throws StorageException, ConfigurationException {
		return testObjectTypeDao.getAll(ALL_FILTER).asCollection();
	}

	@ApiOperation(value = "Get all Test Object Types", tags = {"Service Capabilities"}, produces = "text/xml")
	@ApiResponses(@ApiResponse(code = 200, message = "EtfItemCollection with multiple Test Object Types", reference = "www.interactive-instruments.de"))
	@RequestMapping(value = {TEST_OBJECT_TYPES_URL, TEST_OBJECT_TYPES_URL + " .xml"}, method = RequestMethod.GET)
	public void listTestObjectTypesXml(HttpServletResponse response) throws IOException, StorageException, ObjectWithIdNotFoundException {
		streamAsXml2(testObjectTypeDao, testObjectTypeXmlOutputFormat, response, null);
	}

	@ApiOperation(value = "Get all Test Object Types", tags = {"Service Capabilities"}, produces = "text/xml")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "EtfItemCollection with one Test Object Type", reference = "www.interactive-instruments.de"),
			@ApiResponse(code = 404, message = "Test Object Type not found")
	})
	@RequestMapping(value = {TEST_OBJECT_TYPES_URL + "/{id}", TEST_OBJECT_TYPES_URL + "/{id}.xml"}, method = RequestMethod.GET)
	public void testObjectTypesByIdXml(@PathVariable String id, HttpServletResponse response) throws IOException, StorageException, ObjectWithIdNotFoundException {
		streamAsXml2(testObjectTypeDao, testObjectTypeXmlOutputFormat, response, id);
	}

	/////////////////

	private final static String COMPONENTS_URL = API_BASE_URL + "/Components";

	@ApiOperation(value = "Get all Framework Components", tags = {"Service Capabilities"}, response = ComponentDto.class)
	@RequestMapping(value = {COMPONENTS_URL + "/{id}.json"}, method = RequestMethod.GET, produces = "application/json")
	public ComponentDto componentsByIdJson(@PathVariable String id) throws IOException, StorageException, ObjectWithIdNotFoundException {
		return componentDao.getById(WebAppUtils.toEid(id)).getDto();
	}

	@ApiOperation(value = "Get all Framework Components", tags = {"Service Capabilities"}, response = ComponentDto.class, responseContainer = "List")
	@RequestMapping(value = COMPONENTS_URL + ".json", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody Collection<ComponentDto> listComponentsJson() throws StorageException, ConfigurationException {
		return componentDao.getAll(ALL_FILTER).asCollection();
	}

	@ApiOperation(value = "Get all Framework Components", tags = {"Service Capabilities"}, produces = "text/xml")
	@ApiResponses(@ApiResponse(code = 200, message = "EtfItemCollection with multiple Components", reference = "www.interactive-instruments.de"))
	@RequestMapping(value = {COMPONENTS_URL, COMPONENTS_URL + ".xml"}, method = RequestMethod.GET, produces = "text/xml")
	public void listComponentsXml(HttpServletResponse response) throws IOException, StorageException, ObjectWithIdNotFoundException {
		streamAsXml2(componentDao, testObjectTypeXmlOutputFormat, response, null);
	}

	@ApiOperation(value = "Get all Framework Components", tags = {"Service Capabilities"}, produces = "text/xml")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "EtfItemCollection with one Component", reference = "www.interactive-instruments.de"),
			@ApiResponse(code = 404, message = "Component not found")
	})
	@RequestMapping(value = {COMPONENTS_URL + "/{id}", COMPONENTS_URL + "/{id}.xml"}, method = RequestMethod.GET, produces = "text/xml")
	public void componentsByIdXml(@PathVariable String id, HttpServletResponse response) throws IOException, StorageException, ObjectWithIdNotFoundException {
		streamAsXml2(componentDao, testObjectTypeXmlOutputFormat, response, id);
	}

	/////////////////

	private final static String TAGS_URL = API_BASE_URL + "/Tags";

	@ApiOperation(value = "Get all Tags", tags = {"Service Capabilities"}, response = ComponentDto.class)
	@RequestMapping(value = {TAGS_URL + "/{id}.json"}, method = RequestMethod.GET, produces = "application/json")
	public TagDto tagsByIdJson(@PathVariable String id) throws IOException, StorageException, ObjectWithIdNotFoundException {
		return tagDao.getById(WebAppUtils.toEid(id)).getDto();
	}

	@ApiOperation(value = "Get all Tags", tags = {"Service Capabilities"}, response = ComponentDto.class, responseContainer = "List")
	@RequestMapping(value = TAGS_URL + ".json", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody Collection<TagDto> listTagsJson() throws StorageException, ConfigurationException {
		return tagDao.getAll(ALL_FILTER).asCollection();
	}

	@ApiOperation(value = "Get all Tags", tags = {"Service Capabilities"}, produces = "text/xml")
	@ApiResponses(@ApiResponse(code = 200, message = "EtfItemCollection with multiple Tags", reference = "www.interactive-instruments.de"))
	@RequestMapping(value = {TAGS_URL, TAGS_URL + ".xml"}, method = RequestMethod.GET, produces = "text/xml")
	public void listTagsXml(HttpServletResponse response) throws IOException, StorageException, ObjectWithIdNotFoundException {
		streamAsXml2(tagDao, tagXmlOutputFormat, response, null);
	}

	@ApiOperation(value = "Get all Tags", tags = {"Service Capabilities"}, produces = "text/xml")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "EtfItemCollection with one Tag", reference = "www.interactive-instruments.de"),
			@ApiResponse(code = 404, message = "Component not found")
	})
	@RequestMapping(value = {TAGS_URL + "/{id}", TAGS_URL + "/{id}.xml"}, method = RequestMethod.GET, produces = "text/xml")
	public void tagsByIdXml(@PathVariable String id, HttpServletResponse response) throws IOException, StorageException, ObjectWithIdNotFoundException {
		streamAsXml2(tagDao, tagXmlOutputFormat, response, id);
	}
}
