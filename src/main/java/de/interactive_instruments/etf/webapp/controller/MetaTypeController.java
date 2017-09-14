/**
 * Copyright 2017 European Union, interactive instruments GmbH
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

import static de.interactive_instruments.etf.webapp.SwaggerConfig.SERVICE_CAP_TAG_NAME;
import static de.interactive_instruments.etf.webapp.dto.DocumentationConstants.*;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import de.interactive_instruments.etf.dal.dao.Dao;
import de.interactive_instruments.etf.dal.dto.capabilities.ComponentDto;
import de.interactive_instruments.etf.dal.dto.capabilities.TagDto;
import de.interactive_instruments.etf.dal.dto.capabilities.TestObjectTypeDto;
import de.interactive_instruments.etf.dal.dto.test.TestItemTypeDto;
import de.interactive_instruments.etf.dal.dto.translation.TranslationTemplateBundleDto;
import de.interactive_instruments.etf.webapp.WebAppConstants;
import de.interactive_instruments.etf.webapp.conversion.EidConverter;
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
public class MetaTypeController {

	@Autowired
	ServletContext servletContext;

	@Autowired
	private DataStorageService dataStorageService;

	@Autowired
	private StreamingService streaming;

	private final Logger logger = LoggerFactory.getLogger(MetaTypeController.class);
	private Dao<TestItemTypeDto> testItemTypeDao;

	private Dao<TestObjectTypeDto> testObjectTypeDao;

	private Dao<TranslationTemplateBundleDto> translationTemplateBundleDao;

	private Dao<ComponentDto> componentDao;

	private Dao<TagDto> tagDao;

	@PostConstruct
	private void init() throws IOException, TransformerConfigurationException {
		translationTemplateBundleDao = dataStorageService.getDao(TranslationTemplateBundleDto.class);
		testObjectTypeDao = dataStorageService.getDao(TestObjectTypeDto.class);
		tagDao = dataStorageService.getDao(TagDto.class);

		testItemTypeDao = dataStorageService.getDao(TestItemTypeDto.class);
		componentDao = dataStorageService.getDao(ComponentDto.class);

		streaming.prepareCache(tagDao, new SimpleFilter());
		streaming.prepareCache(translationTemplateBundleDao, new SimpleFilter());
		logger.info("Meta Type controller initialized");
	}

	private final static String TEST_ITEM_TYPEL_DESCRIPTION = "The Test Item Type model is described in the  "
			+ "[XML schema documentation](https://services.interactive-instruments.de/etf/schemadoc/test_xsd.html#TestItemType). "
			+ ETF_ITEM_COLLECTION_DESCRIPTION;
	private final static String TRANSLATION_TEMP_BUNDLE_DESCRIPTION = "The Translation Template Bundle model is described as "
			+ "[XML schema documentation](https://services.interactive-instruments.de/etf/schemadoc/basicTypes_xsd.html#TranslationTemplateBundle). "
			+ ETF_ITEM_COLLECTION_DESCRIPTION;
	private final static String COMPONENT_DESCRIPTION = "The Components model is described in the "
			+ "[XML schema documentation](https://services.interactive-instruments.de/etf/schemadoc/capabilities_xsd.html#Component). "
			+ ETF_ITEM_COLLECTION_DESCRIPTION;
	private final static String TAG_DESCRIPTION = "The Tag model is described in the "
			+ "[XML schema documentation](https://services.interactive-instruments.de/etf/schemadoc/capabilities_xsd.html#Tag). "
			+ ETF_ITEM_COLLECTION_DESCRIPTION;

	///////////////// Test Item Types

	private final static String TEST_ITEM_TYPES_URL = WebAppConstants.API_BASE_URL + "/TestItemTypes";

	@ApiOperation(value = "Get Test Item Type as JSON", notes = TEST_ITEM_TYPEL_DESCRIPTION, tags = {SERVICE_CAP_TAG_NAME})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Item Type"),
			@ApiResponse(code = 404, message = "Item Type not found")
	})
	@RequestMapping(value = {TEST_ITEM_TYPES_URL + "/{id}", TEST_ITEM_TYPES_URL + "/{id}.json"}, method = RequestMethod.GET)
	public void testItemTypeByIdJson(
			@ApiParam(value = EID_DESCRIPTION, example = EID_EXAMPLE, required = true) @PathVariable String id,
			HttpServletRequest request,
			HttpServletResponse response) throws IOException, StorageException, ObjectWithIdNotFoundException {
		streaming.asJson2(testItemTypeDao, request, response, id);
	}

	@ApiOperation(value = "Get multiple Test Item Types as JSON", notes = TEST_ITEM_TYPEL_DESCRIPTION, tags = {
			SERVICE_CAP_TAG_NAME})
	@ApiResponses(@ApiResponse(code = 200, message = "EtfItemCollection with multiple Test Item Types"))
	@RequestMapping(value = TEST_ITEM_TYPES_URL + ".json", method = RequestMethod.GET)
	public void listTestItemTypesJson(
			@ApiParam(value = OFFSET_DESCRIPTION, example = "0") @RequestParam(required = false, defaultValue = "0") int offset,
			@ApiParam(value = LIMIT_DESCRIPTION) @RequestParam(required = false, defaultValue = "0") int limit,
			HttpServletRequest request,
			HttpServletResponse response)
			throws StorageException, ConfigurationException, IOException, ObjectWithIdNotFoundException {
		streaming.asJson2(testItemTypeDao, request, response, new SimpleFilter(offset, limit));
	}

	@ApiOperation(value = "Get multiple Test Item Types as XML", notes = TEST_ITEM_TYPEL_DESCRIPTION, tags = {
			SERVICE_CAP_TAG_NAME}, produces = "text/xml")
	@ApiResponses(@ApiResponse(code = 200, message = "EtfItemCollection with multiple Test Item Types"))
	@RequestMapping(value = {TEST_ITEM_TYPES_URL + ".xml"}, method = RequestMethod.GET)
	public void listTestItemTypesXml(
			@ApiParam(value = OFFSET_DESCRIPTION) @RequestParam(required = false, defaultValue = "0") int offset,
			@ApiParam(value = LIMIT_DESCRIPTION) @RequestParam(required = false, defaultValue = "0") int limit,
			HttpServletRequest request,
			HttpServletResponse response) throws IOException, StorageException, ObjectWithIdNotFoundException {
		streaming.asXml2(testItemTypeDao, request, response, new SimpleFilter(offset, limit));
	}

	@ApiOperation(value = "Get Test Item Type as XML", notes = TEST_ITEM_TYPEL_DESCRIPTION, tags = {
			SERVICE_CAP_TAG_NAME}, produces = "text/xml")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Item Type"),
			@ApiResponse(code = 404, message = "Item Type not found")
	})
	@RequestMapping(value = {TEST_ITEM_TYPES_URL + "/{id}.xml"}, method = RequestMethod.GET)
	public void testItemTypeByIdXml(
			@ApiParam(value = EID_DESCRIPTION, example = EID_EXAMPLE, required = true) @PathVariable String id,
			HttpServletRequest request,
			HttpServletResponse response) throws IOException, StorageException, ObjectWithIdNotFoundException {
		streaming.asXml2(testItemTypeDao, request, response, id);
	}

	@ApiOperation(value = "Check if Test Item Type exists", tags = {SERVICE_CAP_TAG_NAME})
	@ApiResponses(value = {
			@ApiResponse(code = 204, message = "Test Item Type exists"),
			@ApiResponse(code = 404, message = "Test Item Type does not exist")
	})
	@RequestMapping(value = {TEST_ITEM_TYPES_URL + "/{id}"}, method = RequestMethod.HEAD)
	public ResponseEntity<String> existsTestItemType(
			@ApiParam(value = EID_DESCRIPTION, example = EID_EXAMPLE, required = true) @PathVariable String id)
			throws IOException, StorageException, ObjectWithIdNotFoundException {
		return testItemTypeDao.exists(EidConverter.toEid(id)) ? new ResponseEntity(HttpStatus.NO_CONTENT)
				: new ResponseEntity(HttpStatus.NOT_FOUND);
	}

	///////////////// Translation Template Bundles

	private final static String TRANSLATION_TEMP_BUNDLE_URL = WebAppConstants.API_BASE_URL + "/TranslationTemplateBundles";

	@ApiOperation(value = "Get Translation Template Bundle as JSON", notes = TRANSLATION_TEMP_BUNDLE_DESCRIPTION, tags = {
			SERVICE_CAP_TAG_NAME})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Translation Template Bundle"),
			@ApiResponse(code = 404, message = "Translation Template Bundle not found")
	})
	@RequestMapping(value = {TRANSLATION_TEMP_BUNDLE_URL + "/{id}",
			TRANSLATION_TEMP_BUNDLE_URL + "/{id}.json"}, method = RequestMethod.GET)
	public void translationTemplateBundleByIdJson(
			@ApiParam(value = EID_DESCRIPTION, example = EID_EXAMPLE, required = true) @PathVariable String id,
			HttpServletRequest request,
			HttpServletResponse response) throws IOException, StorageException, ObjectWithIdNotFoundException {
		streaming.asJson2(translationTemplateBundleDao, request, response, id);
	}

	@ApiOperation(value = "Get multiple Translation Template Bundles as JSON", notes = TRANSLATION_TEMP_BUNDLE_DESCRIPTION, tags = {
			SERVICE_CAP_TAG_NAME})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "EtfItemCollection with multiple Translation Template Bundles"),
	})
	@RequestMapping(value = {TRANSLATION_TEMP_BUNDLE_URL, TRANSLATION_TEMP_BUNDLE_URL + ".json"}, method = RequestMethod.GET)
	public void listTranslationTemplateBundlesJson(
			@ApiParam(value = OFFSET_DESCRIPTION) @RequestParam(required = false, defaultValue = "0") int offset,
			@ApiParam(value = LIMIT_DESCRIPTION) @RequestParam(required = false, defaultValue = "0") int limit,
			HttpServletRequest request,
			HttpServletResponse response)
			throws StorageException, ConfigurationException, IOException, ObjectWithIdNotFoundException {
		streaming.asJson2(translationTemplateBundleDao, request, response, new SimpleFilter(offset, limit));
	}

	@ApiOperation(value = "Get multiple Translation Template Bundles as XML", notes = TRANSLATION_TEMP_BUNDLE_DESCRIPTION, tags = {
			SERVICE_CAP_TAG_NAME})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "EtfItemCollection with multiple Translation Template Bundles"),
	})
	@RequestMapping(value = {TRANSLATION_TEMP_BUNDLE_URL + ".xml"}, method = RequestMethod.GET)
	public void listTranslationTemplateBundlesXml(
			@ApiParam(value = OFFSET_DESCRIPTION) @RequestParam(required = false, defaultValue = "0") int offset,
			@ApiParam(value = LIMIT_DESCRIPTION) @RequestParam(required = false, defaultValue = "0") int limit,
			HttpServletRequest request,
			HttpServletResponse response) throws IOException, StorageException, ObjectWithIdNotFoundException {
		streaming.asXml2(translationTemplateBundleDao, request, response, new SimpleFilter(offset, limit));
	}

	@ApiOperation(value = "Get Translation Template Bundle as XML", notes = TRANSLATION_TEMP_BUNDLE_DESCRIPTION, tags = {
			SERVICE_CAP_TAG_NAME}, produces = "text/xml")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Translation Template Bundle"),
			@ApiResponse(code = 404, message = "Translation Template Bundle not found")
	})
	@RequestMapping(value = {TRANSLATION_TEMP_BUNDLE_URL + "/{id}.xml"}, method = RequestMethod.GET)
	public void translationTemplateBundleByIdXml(
			@ApiParam(value = EID_DESCRIPTION, example = EID_EXAMPLE, required = true) @PathVariable String id,
			HttpServletRequest request,
			HttpServletResponse response) throws IOException, StorageException, ObjectWithIdNotFoundException {
		streaming.asXml2(translationTemplateBundleDao, request, response, id);
	}

	@ApiOperation(value = "Check if Translation Template Bundle exists", tags = {SERVICE_CAP_TAG_NAME})
	@ApiResponses(value = {
			@ApiResponse(code = 204, message = "Translation Template Bundle exists"),
			@ApiResponse(code = 404, message = "Translation Template Bundle does not exist")
	})
	@RequestMapping(value = {TRANSLATION_TEMP_BUNDLE_URL + "/{id}"}, method = RequestMethod.HEAD)
	public ResponseEntity<String> existsTranslationTemplateBundle(
			@ApiParam(value = EID_DESCRIPTION, example = EID_EXAMPLE, required = true) @PathVariable String id)
			throws IOException, StorageException, ObjectWithIdNotFoundException {
		return translationTemplateBundleDao.exists(EidConverter.toEid(id)) ? new ResponseEntity(HttpStatus.NO_CONTENT)
				: new ResponseEntity(HttpStatus.NOT_FOUND);
	}

	///////////////// Components

	public final static String COMPONENTS_URL = WebAppConstants.API_BASE_URL + "/Components";

	@ApiOperation(value = "Get Framework Component as JSON", notes = COMPONENT_DESCRIPTION, tags = {SERVICE_CAP_TAG_NAME})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Component"),
			@ApiResponse(code = 404, message = "Component not found")
	})
	@RequestMapping(value = {COMPONENTS_URL + "/{id}", COMPONENTS_URL + "/{id}.json"}, method = RequestMethod.GET)
	public void componentsByIdJson(
			@ApiParam(value = EID_DESCRIPTION, example = EID_EXAMPLE, required = true) @PathVariable String id,
			HttpServletRequest request,
			HttpServletResponse response) throws IOException, StorageException, ObjectWithIdNotFoundException {
		streaming.asJson2(componentDao, request, response, id);
	}

	@ApiOperation(value = "Get multiple Framework Components as JSON", notes = COMPONENT_DESCRIPTION, tags = {
			SERVICE_CAP_TAG_NAME})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "EtfItemCollection with multiple Components"),
	})
	@RequestMapping(value = {COMPONENTS_URL, COMPONENTS_URL + ".json"}, method = RequestMethod.GET)
	public void listComponentsJson(
			@ApiParam(value = OFFSET_DESCRIPTION) @RequestParam(required = false, defaultValue = "0") int offset,
			@ApiParam(value = LIMIT_DESCRIPTION) @RequestParam(required = false, defaultValue = "0") int limit,
			HttpServletRequest request,
			HttpServletResponse response)
			throws StorageException, ConfigurationException, IOException, ObjectWithIdNotFoundException {
		streaming.asJson2(componentDao, request, response, new SimpleFilter(offset, limit));
	}

	@ApiOperation(value = "Get multiple Framework Components as XML", notes = COMPONENT_DESCRIPTION, tags = {
			SERVICE_CAP_TAG_NAME}, produces = "text/xml")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "EtfItemCollection with multiple Components"),
	})
	@RequestMapping(value = {COMPONENTS_URL + ".xml"}, method = RequestMethod.GET)
	public void listComponentsXml(
			@ApiParam(value = OFFSET_DESCRIPTION) @RequestParam(required = false, defaultValue = "0") int offset,
			@ApiParam(value = LIMIT_DESCRIPTION) @RequestParam(required = false, defaultValue = "0") int limit,
			HttpServletRequest request,
			HttpServletResponse response) throws IOException, StorageException, ObjectWithIdNotFoundException {
		streaming.asXml2(componentDao, request, response, new SimpleFilter(offset, limit));
	}

	@ApiOperation(value = "Get Framework Component as XML", notes = COMPONENT_DESCRIPTION, tags = {
			SERVICE_CAP_TAG_NAME}, produces = "text/xml")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Component"),
			@ApiResponse(code = 404, message = "Component not found")
	})
	@RequestMapping(value = {COMPONENTS_URL + "/{id}.xml"}, method = RequestMethod.GET)
	public void componentsByIdXml(
			@ApiParam(value = EID_DESCRIPTION, example = EID_EXAMPLE, required = true) @PathVariable String id,
			HttpServletRequest request,
			HttpServletResponse response) throws IOException, StorageException, ObjectWithIdNotFoundException {
		streaming.asXml2(componentDao, request, response, id);
	}

	@ApiOperation(value = "Check if Framework Component exists", tags = {SERVICE_CAP_TAG_NAME})
	@ApiResponses(value = {
			@ApiResponse(code = 204, message = "Component exists"),
			@ApiResponse(code = 404, message = "Component does not exist")
	})
	@RequestMapping(value = {COMPONENTS_URL + "/{id}"}, method = RequestMethod.HEAD)
	public ResponseEntity<String> existsComponent(
			@ApiParam(value = EID_DESCRIPTION, example = EID_EXAMPLE, required = true) @PathVariable String id)
			throws IOException, StorageException, ObjectWithIdNotFoundException {
		return componentDao.exists(EidConverter.toEid(id)) ? new ResponseEntity(HttpStatus.NO_CONTENT)
				: new ResponseEntity(HttpStatus.NOT_FOUND);
	}

	///////////////// Tags

	private final static String TAGS_URL = WebAppConstants.API_BASE_URL + "/Tags";

	@ApiOperation(value = "Get Tag as JSON", notes = TAG_DESCRIPTION, tags = {SERVICE_CAP_TAG_NAME})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Tag"),
			@ApiResponse(code = 404, message = "Tag not found")
	})
	@RequestMapping(value = {TAGS_URL + "/{id}", TAGS_URL + "/{id}.json"}, method = RequestMethod.GET)
	public void tagsByIdJson(
			@ApiParam(value = EID_DESCRIPTION, example = EID_EXAMPLE, required = true) @PathVariable String id,
			HttpServletRequest request,
			HttpServletResponse response) throws IOException, StorageException, ObjectWithIdNotFoundException {
		streaming.asJson2(tagDao, request, response, id);
	}

	@ApiOperation(value = "Get multiple Tags as JSON", notes = TAG_DESCRIPTION, tags = {SERVICE_CAP_TAG_NAME})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "EtfItemCollection with multiple Tags")
	})
	@RequestMapping(value = {TAGS_URL, TAGS_URL + ".json"}, method = RequestMethod.GET)
	public void listTagsJson(
			@ApiParam(value = OFFSET_DESCRIPTION) @RequestParam(required = false, defaultValue = "0") int offset,
			@ApiParam(value = LIMIT_DESCRIPTION) @RequestParam(required = false, defaultValue = "0") int limit,
			HttpServletRequest request,
			HttpServletResponse response)
			throws StorageException, ConfigurationException, IOException, ObjectWithIdNotFoundException {
		streaming.asJson2(tagDao, request, response, new SimpleFilter(offset, limit));
	}

	@ApiOperation(value = "Get multiple Tags as XML", notes = TAG_DESCRIPTION, tags = {
			SERVICE_CAP_TAG_NAME}, produces = "text/xml")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "EtfItemCollection with multiple Tags")
	})
	@RequestMapping(value = {TAGS_URL + ".xml"}, method = RequestMethod.GET)
	public void listTagsXml(
			@ApiParam(value = OFFSET_DESCRIPTION) @RequestParam(required = false, defaultValue = "0") int offset,
			@ApiParam(value = LIMIT_DESCRIPTION) @RequestParam(required = false, defaultValue = "0") int limit,
			HttpServletRequest request,
			HttpServletResponse response) throws IOException, StorageException, ObjectWithIdNotFoundException {
		streaming.asXml2(tagDao, request, response, new SimpleFilter(offset, limit));
	}

	@ApiOperation(value = "Get Tag as XML", notes = TAG_DESCRIPTION, tags = {SERVICE_CAP_TAG_NAME}, produces = "text/xml")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Tag"),
			@ApiResponse(code = 404, message = "Tag not found")
	})
	@RequestMapping(value = {TAGS_URL + "/{id}.xml"}, method = RequestMethod.GET)
	public void tagsByIdXml(
			@ApiParam(value = EID_DESCRIPTION, example = EID_EXAMPLE, required = true) @PathVariable String id,
			HttpServletRequest request,
			HttpServletResponse response) throws IOException, StorageException, ObjectWithIdNotFoundException {
		streaming.asXml2(tagDao, request, response, id);
	}

	@ApiOperation(value = "Check if Tag exists", tags = {SERVICE_CAP_TAG_NAME})
	@ApiResponses(value = {
			@ApiResponse(code = 204, message = "Tag exists"),
			@ApiResponse(code = 404, message = "Tag does not exist")
	})
	@RequestMapping(value = {TAGS_URL + "/{id}"}, method = RequestMethod.HEAD)
	public ResponseEntity<String> existsTag(
			@ApiParam(value = EID_DESCRIPTION, example = EID_EXAMPLE, required = true) @PathVariable String id)
			throws IOException, StorageException, ObjectWithIdNotFoundException {
		return tagDao.exists(EidConverter.toEid(id)) ? new ResponseEntity(HttpStatus.NO_CONTENT)
				: new ResponseEntity(HttpStatus.NOT_FOUND);
	}
}
