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

import static de.interactive_instruments.etf.webapp.SwaggerConfig.SERVICE_CAP_TAG_NAME;
import static de.interactive_instruments.etf.webapp.dto.DocumentationConstants.*;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import de.interactive_instruments.etf.webapp.helpers.SimpleFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Document;

import de.interactive_instruments.Credentials;
import de.interactive_instruments.IFile;
import de.interactive_instruments.SUtils;
import de.interactive_instruments.UriUtils;
import de.interactive_instruments.etf.dal.dao.Dao;
import de.interactive_instruments.etf.dal.dto.capabilities.ComponentDto;
import de.interactive_instruments.etf.dal.dto.capabilities.ResourceDto;
import de.interactive_instruments.etf.dal.dto.capabilities.TestObjectDto;
import de.interactive_instruments.etf.dal.dto.capabilities.TestObjectTypeDto;
import de.interactive_instruments.etf.webapp.WebAppConstants;
import de.interactive_instruments.etf.webapp.conversion.EidConverter;
import de.interactive_instruments.exceptions.ObjectWithIdNotFoundException;
import de.interactive_instruments.exceptions.StorageException;
import de.interactive_instruments.exceptions.config.ConfigurationException;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * @author J. Herrmann ( herrmann <aT) interactive-instruments (doT> de )
 */
@RestController
public class TestObjectTypeController {

	@Autowired
	private DataStorageService dataStorageService;

	@Autowired
	private StreamingService streaming;

	private Dao<TestObjectTypeDto> testObjectTypeDao;
	private final static String TEST_OBJECT_TYPES_URL = WebAppConstants.API_BASE_URL + "/TestObjectTypes";

	private final static String TEST_OBJECT_TYPE_DESCRIPTION = "The Test Object model is described in the "
			+ "[XML schema documentation](https://services.interactive-instruments.de/etf/schemadoc/capabilities_xsd.html#TestObjectType) "
			+ ETF_ITEM_COLLECTION_DESCRIPTION;

	private static class TypeCheck {
		private List<String> filenameExtensions;
		private List<String> mimeTypes;
		private String detectionExpression;

		TypeCheck(TestObjectTypeDto dto) {
			detectionExpression = dto.getDetectionExpression();
			mimeTypes = dto.getMimeTypes();
			filenameExtensions = dto.getFilenameExtensions();
		}

		boolean accept(final ResourceDto resource) throws IOException {
			final URI uri = resource.getUri();
			if (UriUtils.isFile(uri)) {
				final IFile file = new IFile(uri);
				if (file.isDirectory()) {
					// check each file
				} else {
					// check single file
				}
			} else {
				if (UriUtils.getContentLength(uri) > 300000000) {
					// File is too large
					return false;
				}
				final UriUtils.ContentAndType contentAndType = UriUtils.load(uri, null, false);

			}
			return false;
		}
	}

	@PostConstruct
	private void init() throws IOException, TransformerConfigurationException, StorageException {
		testObjectTypeDao = dataStorageService.getDao(TestObjectTypeDto.class);
	}

	public void checkAndResolveTypes(final TestObjectDto dto) throws StorageException, ObjectWithIdNotFoundException {
		final List<TestObjectTypeDto> checkedTypes = new ArrayList<>();

		for (final ResourceDto resourceDto : dto.getResourceCollection()) {
			final URI resource = resourceDto.getUri();
			if (UriUtils.isFile(resource)) {
				// File
				dto.setTestObjectType(testObjectTypeDao.getById(
						EidConverter.toEid("EID5a60dded-0cb0-4977-9b06-16c6c2321d2e")).getDto());
			} else {

				dto.setRemoteResource(resource);

				// TODO put into detectors v2.0.1
				// WFS
				try {
					final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
					factory.setNamespaceAware(true);
					final Credentials credentials = Credentials.fromProperties(dto.properties());
					final URI reqURI = new URI(
							UriUtils.withoutQueryParameters(resource.toString()) + "?service=wfs&request=GetCapabilities");
					final DocumentBuilder builder = factory.newDocumentBuilder();
					final Document doc = builder.parse(UriUtils.openStream(reqURI, credentials));

					final XPathFactory xPathfactory = XPathFactory.newInstance();
					final XPath xpath = xPathfactory.newXPath();

					xpath.setNamespaceContext(new NamespaceContext() {
						public String getNamespaceURI(String prefix) {
							if (prefix == null) {
								throw new NullPointerException("Null prefix");
							} else if ("wfs".equals(prefix)) {
								return "http://www.opengis.net/wfs/2.0";
							} else if ("ows".equals(prefix)) {
								return "http://www.opengis.net/ows/1.1";
							}
							return XMLConstants.NULL_NS_URI;
						}

						// This method isn't necessary for XPath processing.
						public String getPrefix(String uri) {
							throw new UnsupportedOperationException();
						}

						// This method isn't necessary for XPath processing either.
						public Iterator getPrefixes(String uri) {
							throw new UnsupportedOperationException();
						}
					});
					final XPathExpression wfsTitle = xpath.compile("/wfs:WFS_Capabilities/ows:ServiceIdentification/ows:Title");
					final XPathExpression description = xpath
							.compile("/wfs:WFS_Capabilities/ows:ServiceIdentification/ows:Abstract");

					final String titleStr = (String) wfsTitle.evaluate(doc, XPathConstants.STRING);
					final String descriptionStr = (String) description.evaluate(doc, XPathConstants.STRING);

					boolean webService = false;
					if (!SUtils.isNullOrEmpty(titleStr)) {
						dto.setLabel(titleStr);
						webService = true;
					}
					if (!SUtils.isNullOrEmpty(descriptionStr)) {
						dto.setDescription(descriptionStr);
						webService = true;
					}

					if (webService) {
						// Web service
						dto.setTestObjectType(testObjectTypeDao.getById(
								EidConverter.toEid("EID9b6ef734-981e-4d60-aa81-d6730a1c6389")).getDto());
						// EidConverter.toEid("EID88311f83-818c-46ed-8a9a-cec4f3707365")).getDto());
						return;
					}
				} catch (Exception e) {
					// fallback: file
					dto.setTestObjectType(testObjectTypeDao.getById(
							EidConverter.toEid("EID5a60dded-0cb0-4977-9b06-16c6c2321d2e")).getDto());
				}

				// Service Feed
				try {
					final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
					factory.setNamespaceAware(true);
					final Credentials credentials = Credentials.fromProperties(dto.properties());
					final URI reqURI = resource;
					final DocumentBuilder builder = factory.newDocumentBuilder();
					final Document doc = builder.parse(UriUtils.openStream(reqURI, credentials));

					final XPathFactory xPathfactory = XPathFactory.newInstance();
					final XPath xpath = xPathfactory.newXPath();

					xpath.setNamespaceContext(new NamespaceContext() {
						public String getNamespaceURI(String prefix) {
							if (prefix == null) {
								throw new NullPointerException("Null prefix");
							} else if ("atom".equals(prefix)) {
								return "http://www.w3.org/2005/Atom";
							}
							return XMLConstants.NULL_NS_URI;
						}

						// This method isn't necessary for XPath processing.
						public String getPrefix(String uri) {
							throw new UnsupportedOperationException();
						}

						// This method isn't necessary for XPath processing either.
						public Iterator getPrefixes(String uri) {
							throw new UnsupportedOperationException();
						}
					});
					final XPathExpression feedTitle = xpath.compile("/atom:feed/atom:title");
					final XPathExpression feedDescription = xpath
							.compile("/atom:feed/atom:subtitle");

					final String titleStr = (String) feedTitle.evaluate(doc, XPathConstants.STRING);
					final String descriptionStr = (String) feedDescription.evaluate(doc, XPathConstants.STRING);

					boolean serviceFeed = false;
					if (!SUtils.isNullOrEmpty(titleStr)) {
						dto.setLabel(titleStr);
						serviceFeed = true;
					}
					if (!SUtils.isNullOrEmpty(descriptionStr)) {
						dto.setDescription(descriptionStr);
						serviceFeed = true;
					}

					if (serviceFeed) {
						// Service Feed
						dto.setTestObjectType(testObjectTypeDao.getById(
								EidConverter.toEid("EID49d881ae-b115-4b91-aabe-31d5791bce52")).getDto());
					} else {
						// file
						dto.setTestObjectType(testObjectTypeDao.getById(
								EidConverter.toEid("EID5a60dded-0cb0-4977-9b06-16c6c2321d2e")).getDto());
					}
				} catch (Exception e) {
					// fallback: file
					dto.setTestObjectType(testObjectTypeDao.getById(
							EidConverter.toEid("EID5a60dded-0cb0-4977-9b06-16c6c2321d2e")).getDto());
				}

				// TODO END
			}
		}
	}

	//
	// Rest interfaces
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@ApiOperation(value = "Get Test Object Type as JSON", notes = TEST_OBJECT_TYPE_DESCRIPTION, tags = {
			SERVICE_CAP_TAG_NAME})
	@RequestMapping(value = {TEST_OBJECT_TYPES_URL + "/{id}", TEST_OBJECT_TYPES_URL + "/{id}.json"}, method = RequestMethod.GET)
	public void testObjectTypesByIdJson(
			@PathVariable String id,
			HttpServletRequest request,
			HttpServletResponse response) throws IOException, StorageException, ObjectWithIdNotFoundException {
		streaming.asJson2(testObjectTypeDao, request, response, id);
	}

	@ApiOperation(value = "Get multiple Test Object Types as JSON", notes = TEST_OBJECT_TYPE_DESCRIPTION, tags = {
			SERVICE_CAP_TAG_NAME})
	@RequestMapping(value = {TEST_OBJECT_TYPES_URL, TEST_OBJECT_TYPES_URL + ".json"}, method = RequestMethod.GET)
	public void listTestObjectTypesJson(
			@ApiParam(value = OFFSET_DESCRIPTION) @RequestParam(required = false, defaultValue = "0") int offset,
			@ApiParam(value = LIMIT_DESCRIPTION) @RequestParam(required = false, defaultValue = "0") int limit,
			HttpServletRequest request,
			HttpServletResponse response)
			throws StorageException, ConfigurationException, IOException, ObjectWithIdNotFoundException {
		streaming.asJson2(testObjectTypeDao, request, response, new SimpleFilter(offset, limit));
	}

	@ApiOperation(value = "Get multiple Test Object Types as XML", notes = TEST_OBJECT_TYPE_DESCRIPTION, tags = {
			SERVICE_CAP_TAG_NAME}, produces = "text/xml")
	@ApiResponses(value = {
			@ApiResponse(code = 202, message = "EtfItemCollection with multiple Test Object Types")
	})
	@RequestMapping(value = {TEST_OBJECT_TYPES_URL + " .xml"}, method = RequestMethod.GET)
	public void listTestObjectTypesXml(@RequestParam(required = false, defaultValue = "0") int offset,
			@RequestParam(required = false, defaultValue = "0") int limit, HttpServletRequest request,
			HttpServletResponse response) throws IOException, StorageException, ObjectWithIdNotFoundException {
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
			@PathVariable String id,
			HttpServletRequest request,
			HttpServletResponse response) throws IOException, StorageException, ObjectWithIdNotFoundException {
		streaming.asXml2(testObjectTypeDao, request, response, id);
	}
}
