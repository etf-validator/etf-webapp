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

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;
import javax.validation.Valid;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.tika.mime.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import de.interactive_instruments.*;
import de.interactive_instruments.etf.dal.basex.dao.BasexTestObjectDao;
import de.interactive_instruments.etf.dal.dao.TestObjectDao;
import de.interactive_instruments.etf.dal.dto.item.VersionDataDto;
import de.interactive_instruments.etf.dal.dto.plan.TestObjectDto;
import de.interactive_instruments.etf.dal.dto.plan.TestObjectDtoBuilder;
import de.interactive_instruments.etf.model.item.EID;
import de.interactive_instruments.etf.model.item.EidFactory;
import de.interactive_instruments.etf.model.item.ModelItemDefaultMap;
import de.interactive_instruments.etf.model.plan.TestObject;
import de.interactive_instruments.etf.model.plan.TestObjectResource;
import de.interactive_instruments.etf.model.plan.TestObjectResourceType;
import de.interactive_instruments.etf.webapp.dto.TObjectValidator;
import de.interactive_instruments.exceptions.ExcUtils;
import de.interactive_instruments.exceptions.ObjectWithIdNotFoundException;
import de.interactive_instruments.exceptions.StoreException;
import de.interactive_instruments.exceptions.config.ConfigurationException;
import de.interactive_instruments.exceptions.config.MissingPropertyException;
import de.interactive_instruments.io.FileHashVisitor;
import de.interactive_instruments.io.GmlAndXmlFilter;
import de.interactive_instruments.properties.Properties;

/**
 * Test object controller used for managing test objects
 */
@Controller
public class TestObjectController {

	@Autowired
	private ServletContext servletContext;

	@Autowired
	private TestRunTaskFactoryService taskFactory;

	@Autowired
	private EtfConfigController etfConfig;

	private TestObjectDao testObjStore;

	private IFile testDataDir;
	private IFile tmpUploadDir;
	private final Logger logger = LoggerFactory.getLogger(TestObjectController.class);

	@InitBinder
	private void initBinder(WebDataBinder binder) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.zzz");
		binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, false));
		binder.setValidator(new TObjectValidator());
	}

	@PreDestroy
	private void shutdown() {
		testObjStore.release();
	}

	@PostConstruct
	public void init() throws IOException, JAXBException, MissingPropertyException {
		testDataDir = etfConfig.getPropertyAsFile(EtfConfigController.ETF_TESTDATA_DIR);
		testDataDir.ensureDir();
		logger.info("TEST_DATA_DIR " + testDataDir.getAbsolutePath());

		tmpUploadDir = etfConfig.getPropertyAsFile(EtfConfigController.ETF_TESTDATA_UPLOAD_DIR);
		if (tmpUploadDir.exists()) {
			tmpUploadDir.deleteDirectory();
		}
		tmpUploadDir.mkdir();
		logger.info("TMP_HTTP_UPLOADS: " + tmpUploadDir.getAbsolutePath());
		testObjStore = new BasexTestObjectDao();
		logger.info(this.getClass().getName() + " initialized!");
	}

	private class HiddenFileFilter implements FileFilter {
		@Override
		public boolean accept(File file) {
			return !file.isHidden();
		}
	}

	TestObjectDao getTestObjStore() {
		return testObjStore;
	}

	Collection<TestObjectDto> getTestObjects() throws StoreException {
		return testObjStore.getAll();
	}

	private String showCreateWebservice(Model model, TestObjectDto dto) {
		model.addAttribute("testObject", dto);
		return "testobjects/create-http-to";
	}

	private List<String> getTestObjDirs() {
		List<String> testObjDirs = new ArrayList<>();
		File[] files = this.testDataDir.listFiles();
		if (files != null && files.length != 0) {
			Arrays.sort(files);
			for (final File file : files) {
				if (file.isDirectory()) {
					testObjDirs.add(file.getName());
				}
			}
		}
		return testObjDirs;
	}

	private String showCreateDoc(Model model, TestObjectDto dto) {
		model.addAttribute("testObjDirs", getTestObjDirs());
		if (dto.getResources() != null) {
			dto.getResources().clear();
		}
		model.addAttribute("testObject", dto);
		return "testobjects/create-file-to";
	}

	// VIEWS
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@RequestMapping(value = "/testobjects", method = RequestMethod.GET)
	public String overview(Model model) throws StoreException, ConfigurationException {

		final Map<EID, TestObjectResourceType> resTypes = taskFactory.getTestObjectResourceTypes();
		if (resTypes != null) {
			model.addAttribute("resourceSchemes", resTypes.values().parallelStream().map(TestObjectResourceType::getUriScheme).collect(Collectors.toSet()));
		}
		model.addAttribute("testObjects", getTestObjects());
		return "testobjects/overview";
	}

	@RequestMapping(value = "/testobjects/create-http-to", method = RequestMethod.GET)
	public String createWebserviceTo(Model model) {
		return showCreateWebservice(model,
				new TestObjectDtoBuilder().setId(EidFactory.getDefault().createRandomUuid()).setType(TestObject.TestObjectType.EXECUTABLE).setProperties(new Properties()).setResources(new ModelItemDefaultMap<TestObjectResource>()).createTestObjectDto());
	}

	@RequestMapping(value = "/testobjects/create-file-to", method = RequestMethod.GET)
	public String createFileTo(Model model) {
		return showCreateDoc(model,
				new TestObjectDtoBuilder().setId(EidFactory.getDefault().createRandomUuid()).setType(TestObject.TestObjectType.TEXT).setProperties(new Properties()).setResources(new ModelItemDefaultMap<TestObjectResource>()).createTestObjectDto());
	}

	@RequestMapping(value = "/testobjects/{id}/delete", method = RequestMethod.GET)
	public String delete(@PathVariable EID id) throws StoreException, ObjectWithIdNotFoundException {

		this.testObjStore.delete(id);

		return "redirect:/testobjects";
	}

	// Todo: use same create method and allow upload via seperate dialog
	@RequestMapping(value = "/testobjects/add-http-to", method = RequestMethod.POST)
	public String addWebservice(
			@ModelAttribute("testObject") @Valid TestObjectDto testObject,
			BindingResult result,
			MultipartHttpServletRequest request,
			Model model) throws IOException, URISyntaxException, StoreException, ParseException, NoSuchAlgorithmException {
		if (result.hasErrors()) {
			return showCreateWebservice(model, testObject);
		}

		final VersionDataDto vd = new VersionDataDto();
		final URI serviceEndpoint = testObject.getResourceById("serviceEndpoint");
		final String hash;
		try {
			if (etfConfig.getProperty("etf.testobject.allow.privatenet.access").equals("false")) {
				if (UriUtils.isPrivateNet(serviceEndpoint)) {
					result.reject("l.rejected.private.subnet.access",
							"Access to the private subnet was rejected by a configuration setting!");
					return showCreateWebservice(model, testObject);
				}
			}
			hash = UriUtils.hashFromContent(serviceEndpoint,
					Credentials.fromProperties(testObject.getProperties()));
		} catch (IllegalArgumentException | IOException e) {
			result.reject("l.invalid.url", new Object[]{e.getMessage()},
					"The URL is unaccessible: {0}");
			return showCreateWebservice(model, testObject);
		}

		vd.setItemHash(hash.getBytes());
		testObject.setVersionData(vd);
		testObject.setId(EidFactory.getDefault().createRandomUuid());

		testObjStore.create(testObject);

		return "redirect:/testobjects";
	}

	private String getSimpleRandomNumber(final int length) {
		// Create new directory in testDataDir with a short random suffix
		final char[] chars = "1234567890".toCharArray();
		final StringBuilder sb = new StringBuilder();
		final Random random = new Random();
		for (int i = 0; i < length; i++) {
			char c = chars[random.nextInt(chars.length)];
			sb.append(c);
		}
		return sb.toString();
	}

	// Todo: use same create method and allow upload via seperate dialog
	@RequestMapping(value = "/testobjects/add-file-to", method = RequestMethod.POST)
	public String addFileTestData(
			@ModelAttribute("testObject") @Valid TestObjectDto testObject,
			BindingResult result,
			MultipartHttpServletRequest request,
			Model model) throws IOException, URISyntaxException, StoreException, ParseException, NoSuchAlgorithmException {
		if (SUtils.isNullOrEmpty(testObject.getLabel())) {
			throw new IllegalArgumentException("Label is empty");
		}

		if (result.hasErrors()) {
			return showCreateDoc(model, testObject);
		}

		final GmlAndXmlFilter filter;
		final String regex = testObject.getProperty("regex");
		if (regex != null && !regex.isEmpty()) {
			filter = new GmlAndXmlFilter(new RegexFileFilter(regex));
		} else {
			filter = new GmlAndXmlFilter();
		}

		// Transfer uploaded data
		final MultipartFile multipartFile = request.getFile("testObjFile");
		if (multipartFile != null && !multipartFile.isEmpty()) {
			// Transfer file to tmpUploadDir
			final IFile testObjFile = this.tmpUploadDir.secureExpandPathDown(
					testObject.getLabel() + "_" + multipartFile.getOriginalFilename());
			testObjFile.expectFileIsWritable();
			multipartFile.transferTo(testObjFile);
			final String type;
			try {
				type = MimeTypeUtils.detectMimeType(testObjFile);
				if (!type.equals("application/xml") && !type.equals("application/zip")) {
					throw new IllegalArgumentException(type + " is not supported");
				}
			} catch (Exception e) {
				result.reject("l.upload.invalid", new Object[]{e.getMessage()},
						"Unable to use file: {0}");
				return showCreateDoc(model, testObject);
			}

			// Create directory for test data file
			final IFile testObjectDir = testDataDir.secureExpandPathDown(
					testObject.getLabel() + ".upl." + getSimpleRandomNumber(4));
			testObjectDir.ensureDir();

			if (type.equals("application/zip")) {
				// Unzip files to test directory
				try {
					testObjFile.unzipTo(testObjectDir, filter);
				} catch (IOException e) {
					try {
						testObjectDir.delete();
					} catch (Exception de) {
						ExcUtils.supress(de);
					}
					result.reject("l.decompress.failed", new Object[]{e.getMessage()},
							"Unable to decompress file: {0}");
					return showCreateDoc(model, testObject);
				} finally {
					// delete zip file
					testObjFile.delete();
				}
			} else {
				// Move XML to test directory
				testObjFile.copyTo(testObjectDir.getPath() + File.separator + multipartFile.getOriginalFilename());
			}
			testObject.addResource("data", testObjectDir.toURI());
			testObject.getProperties().setProperty("uploaded", "true");
		} else {
			final URI resURI = testObject.getResourceById("data");
			if (resURI == null) {
				throw new StoreException("Workflow error. Data path resource not set.");
			}
			final IFile absoluteTestObjectDir = testDataDir.secureExpandPathDown(
					resURI.getPath());
			testObject.getResources().clear();
			testObject.getResources().put(EidFactory.getDefault().createFromStrAsStr("data"),
					absoluteTestObjectDir.toURI());

			// Check if file exists
			final IFile sourceDir = new IFile(new File(testObject.getResourceById("data")));
			try {
				sourceDir.expectDirIsReadable();
			} catch (Exception e) {
				result.reject("l.testObject.testdir.insufficient.rights", new Object[]{e.getMessage()},
						"Insufficient rights to read directory: {0}");
				return showCreateDoc(model, testObject);
			}
			testObject.getProperties().setProperty("uploaded", "false");
		}

		final FileHashVisitor v = new FileHashVisitor(filter);
		Files.walkFileTree(new File(testObject.getResourceById("data")).toPath(),
				EnumSet.of(FileVisitOption.FOLLOW_LINKS), 5, v);

		if (v.getFileCount() == 0) {
			if (regex != null && !regex.isEmpty()) {
				result.reject("l.testObject.regex.null.selection", new Object[]{regex},
						"No files were selected with the regular expression \"{0}\"!");
			} else {
				result.reject("l.testObject.testdir.no.xml.gml.found",
						"No file were found in the directory with a gml or xml file extension");
			}
			return showCreateDoc(model, testObject);
		}
		VersionDataDto vd = new VersionDataDto();
		vd.setItemHash(v.getHash());
		testObject.getProperties().setProperty("files", String.valueOf(v.getFileCount()));
		testObject.getProperties().setProperty("size", String.valueOf(v.getSize()));
		testObject.getProperties().setProperty("sizeHR", FileUtils.byteCountToDisplaySize(v.getSize()));
		testObject.setVersionData(vd);

		testObject.setId(EidFactory.getDefault().createRandomUuid());

		testObjStore.create(testObject);

		return "redirect:/testobjects";
	}

	//
	// Rest interfaces
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@RequestMapping(value = "/rest/testobjects/{name}}", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody TestObjectDto get(@PathVariable String name) throws StoreException, ObjectWithIdNotFoundException {
		return testObjStore.getDtoByName(name);
	}

	@RequestMapping(value = "/rest/testobjects", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody Collection<TestObjectDto> list() throws StoreException {
		return getTestObjects();
	}
}
