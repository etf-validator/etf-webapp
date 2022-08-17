/**
 * Copyright 2010-2022 interactive instruments GmbH
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

import static de.interactive_instruments.etf.webapp.SwaggerConfig.TEST_OBJECTS_TAG_NAME;
import static de.interactive_instruments.etf.webapp.dto.DocumentationConstants.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import org.apache.commons.io.filefilter.RegexFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import springfox.documentation.annotations.ApiIgnore;

import de.interactive_instruments.*;
import de.interactive_instruments.etf.dal.dao.*;
import de.interactive_instruments.etf.dal.dto.capabilities.ResourceDto;
import de.interactive_instruments.etf.dal.dto.capabilities.TestObjectDto;
import de.interactive_instruments.etf.dal.dto.capabilities.TestObjectTypeDto;
import de.interactive_instruments.etf.detector.TestObjectTypeDetectorManager;
import de.interactive_instruments.etf.model.EID;
import de.interactive_instruments.etf.model.EidFactory;
import de.interactive_instruments.etf.webapp.WebAppConstants;
import de.interactive_instruments.etf.webapp.conversion.EidConverter;
import de.interactive_instruments.etf.webapp.dto.ApiError;
import de.interactive_instruments.etf.webapp.dto.CreateReusableTestObjectRequest;
import de.interactive_instruments.etf.webapp.dto.TObjectValidator;
import de.interactive_instruments.etf.webapp.dto.TestObjectCreationResponse;
import de.interactive_instruments.etf.webapp.helpers.SimpleFilter;
import de.interactive_instruments.etf.webapp.helpers.User;
import de.interactive_instruments.etf.webapp.helpers.View;
import de.interactive_instruments.exceptions.ObjectWithIdNotFoundException;
import de.interactive_instruments.exceptions.StorageException;
import de.interactive_instruments.exceptions.config.InvalidPropertyException;
import de.interactive_instruments.exceptions.config.MissingPropertyException;
import de.interactive_instruments.io.*;
import io.swagger.annotations.*;

/**
 * Test object controller used for managing test objects
 */
@RestController
public class TestObjectController implements DtoResolver<TestObjectDto> {

    @Autowired
    private EtfConfig etfConfig;

    @Autowired
    private StatusController statusController;

    @Autowired
    private DataStorageService dataStorageService;

    @Autowired
    private StreamingService streaming;

    @Autowired
    private TestObjectTypeDetectionService testObjectTypeDetectionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApplicationContext applicationContext;

    private Timer cleanTimer;
    // 7 minutes after start
    private final long initialDelay = 420000;

    public static final String PATH = "testobjects";
    public final static String TESTOBJECTS_URL = WebAppConstants.API_BASE_URL + "/TestObjects";
    // 7 minutes for adding resources
    private static final long T_CREATION_WINDOW = 7;
    private IFile testDataDir;
    private IFile mountedTestDataDir;
    private FileChangeListener mountedTestDataDirListener;
    private final Logger logger = LoggerFactory.getLogger(TestObjectController.class);
    private FileStorage fileStorage;
    private FileContentFilterHolder baseFilter;
    private WriteDao<TestObjectDto> testObjectDao;
    private final Cache<EID, TestObjectDto> transientTestObjects = Caffeine.newBuilder().expireAfterWrite(
            T_CREATION_WINDOW, TimeUnit.MINUTES).build();

    private final static String TEST_OBJECT_DESCRIPTION = "The Test Object model is described in the "
            + "[XML schema documentation](https://resources.etf-validator.net/schema/v2/doc/capabilities_xsd.html#TestObject). "
            + ETF_ITEM_COLLECTION_DESCRIPTION;

    private static class TestObjectCleaner implements ExpirationItemHolder {
        private final WriteDao<TestObjectDto> testObjectDao;
        private final IFile testDataDir;
        private final static Logger logger = LoggerFactory.getLogger(TestObjectCleaner.class);

        private TestObjectCleaner(final Dao<TestObjectDto> testObjectDao,
                final IFile testDataDir) {
            this.testObjectDao = (WriteDao<TestObjectDto>) testObjectDao;
            this.testDataDir = testDataDir;
        }

        @Override
        public void removeExpiredItems(final long maxLifeTime, final TimeUnit unit) {
            int removed = 0;
            try {
                final PreparedDtoCollection<TestObjectDto> all = testObjectDao.getAll(SimpleFilter.allItems());
                for (final TestObjectDto testObjectDto : all) {
                    if ("true".equals(testObjectDto.properties().getPropertyOrDefault("temporary", "false"))) {
                        final long expirationTime = testObjectDto.getCreationDate().getTime() + unit.toMillis(maxLifeTime);
                        if (System.currentTimeMillis() > expirationTime) {
                            final Map<String, ResourceDto> res = testObjectDto.getResources();
                            if (res != null) {
                                for (final ResourceDto resourceDto : res.values()) {
                                    final URI uri = resourceDto.getUri();
                                    if (UriUtils.isFile(uri)) {
                                        final IFile dir = testDataDir.secureExpandPathDown(uri.getPath());
                                        try {
                                            removed++;
                                            dir.deleteDirectory();
                                        } catch (IOException e) {
                                            logger.warn("Error deleting test data directory ", e);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (final StorageException e) {
                logger.warn("Error deleting expired item ", e);
            }
            logger.info("{} items were cleaned.", removed);
        }
    }

    private static class MountedTestDataDirListener implements FileChangeListener {
        private final WriteDao<TestObjectDto> testObjectDao;
        private final IFile mountedTestDataDir;
        private final static Logger logger = LoggerFactory.getLogger(MountedTestDataDirListener.class);
        private final TestObjectController callback;

        private MountedTestDataDirListener(final Dao<TestObjectDto> testObjectDao,
                final IFile mountedTestDataDir, final TestObjectController callback) {
            this.testObjectDao = (WriteDao<TestObjectDto>) testObjectDao;
            this.mountedTestDataDir = mountedTestDataDir;
            this.callback = callback;
            if (mountedTestDataDir.canWrite()) {
                logger.warn("The mounted test data dir should not be writable by ETF. Path: {}",
                        mountedTestDataDir.getAbsolutePath());
            }
            filesChanged(null, Collections.EMPTY_SET);
        }

        @Override
        public void filesChanged(final Map<Path, WatchEvent.Kind> eventMap, final Set<Path> dirs) {
            final Set<IFile> mountedTestDataDirs = Collections
                    .unmodifiableSet(new TreeSet<>(this.mountedTestDataDir.listDirs()));
            final Set<IFile> usedMountedTestDataDirs = new TreeSet<>();
            try {
                final Collection<TestObjectDto> testObjects = testObjectDao.getAll(SimpleFilter.allItems()).asCollection();
                for (final TestObjectDto testObject : testObjects) {
                    final URI dataDirUri = testObject.getResourceByName("data");
                    if (dataDirUri != null) {
                        final IFile dataDir = new IFile(dataDirUri);
                        if (dataDir.exists()) {
                            if (mountedTestDataDirs.contains(dataDir)) {
                                // check for modification
                                if (new Date(dataDir.lastModified()).after(testObject.getLastUpdateDate())) {
                                    try {
                                        callback.testObjectTypeDetectionService.checkAndResolveTypes(testObject, null);
                                        usedMountedTestDataDirs.add(dataDir);
                                        testObject.setLastUpdateDateNow();
                                        this.testObjectDao.update(testObject, null);
                                        logger.info("Test Object {} changed ", testObject.getLabel());
                                    } catch (IOException | ObjectWithIdNotFoundException e) {
                                        try {
                                            this.testObjectDao.delete(testObject.getId());
                                            logger.info("Unusable Test Object {} is removed ", testObject.getLabel());
                                        } catch (ObjectWithIdNotFoundException notFound) {
                                            logger.error("Error deleting Test Object", notFound);
                                        }
                                    }
                                } else {
                                    usedMountedTestDataDirs.add(dataDir);
                                }
                            }
                        } else {
                            try {
                                this.testObjectDao.delete(testObject.getId());
                                logger.info("Test Object {} removed ", testObject.getLabel());
                            } catch (ObjectWithIdNotFoundException notFound) {
                                logger.error("Error deleting Test Object", notFound);
                            }
                        }
                    }
                }
            } catch (final StorageException e) {
                logger.error("Error retrieving Test Objects", e);
            }
            final Set<IFile> newMountedTestDataDirs = new HashSet<>(mountedTestDataDirs);
            newMountedTestDataDirs.removeAll(usedMountedTestDataDirs);
            for (final IFile newMountedTestDataDir : newMountedTestDataDirs) {
                final TestObjectDto testObject = new TestObjectDto();
                final String preparedName = IFile.sanitize(newMountedTestDataDir.getName());
                final String name = preparedName.length() <= 3 ? "__" + preparedName + "__" : preparedName;
                testObject.setLabel(name);
                testObject.setDescription(name);
                testObject.setId(EidFactory.getDefault().createRandomId());
                testObject.setVersionFromStr("1.0.1");
                testObject.setLastUpdateDate(new Date());
                testObject.addResource(new ResourceDto("data", newMountedTestDataDir.toURI()));
                try {
                    callback.createWithFileResources(testObject, null);
                    callback.testObjectTypeDetectionService.checkAndResolveTypes(testObject, null);
                    testObject.setLastUpdateDateNow();
                    final FileOwnerAttributeView ownerAttributeView = Files.getFileAttributeView(newMountedTestDataDir.toPath(),
                            FileOwnerAttributeView.class);
                    final UserPrincipal owner = ownerAttributeView.getOwner();
                    testObject.setLastEditor(owner.getName());
                    testObject.setLocalPath(".");
                    testObject.properties().setProperty("data.downloadable", "false");
                    testObjectDao.add(testObject);
                    logger.info("Test Object {} added", testObject.getLabel());
                } catch (IOException | InvalidPropertyException | ObjectWithIdNotFoundException e) {
                    logger.error("Error creating Test Object in directory {}", newMountedTestDataDir, e);
                } catch (LocalizableApiError e) {
                    logger.error("Error creating Test Object in directory {} : {}", newMountedTestDataDir.getAbsolutePath(),
                            callback.applicationContext.getMessage(e.getId(),
                                    e.getArgumentValueArr(), null,
                                    LocaleContextHolder.getLocale()));
                }
            }
        }
    }

    @InitBinder
    private void initBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.zzz");
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, false));
        binder.setValidator(new TObjectValidator());
    }

    @PreDestroy
    private void shutdown() {
        testObjectDao.release();
        if (this.cleanTimer != null) {
            cleanTimer.cancel();
        }
        if (mountedTestDataDirListener != null) {
            DirWatcher.unregister(mountedTestDataDirListener);
            mountedTestDataDirListener = null;
        }
    }

    private static class AvailableTestObjectTypesFilter implements FileContentFilterHolder {

        private ContentTypeFilter contentTypeFilter;
        private MultiFileFilter filenameFilter;

        AvailableTestObjectTypesFilter(final Collection<TestObjectTypeDto> supportedTypes) {
            contentTypeFilter = ContentTypeFilter.mergeOr(supportedTypes.stream().map(
                    TestObjectTypeDto::contentTypeFilter).collect(Collectors.toList()));
            filenameFilter = MultiFileFilter.mergeOr(supportedTypes.stream().map(
                    TestObjectTypeDto::filenameFilter).filter(Optional::isPresent).map(Optional::get)
                    .collect(Collectors.toSet()));
        }

        @Override
        public ContentTypeFilter contentTypeFilter() {
            return contentTypeFilter;
        }

        @Override
        public Optional<MultiFileFilter> filenameFilter() {
            return Optional.of(filenameFilter);
        }
    }

    @PostConstruct
    public void init() throws IOException, JAXBException, MissingPropertyException, InvalidPropertyException {

        testDataDir = etfConfig.getPropertyAsFile(EtfConfig.ETF_TESTDATA_DIR);
        testDataDir.ensureDir();
        logger.info("TEST_DATA_DIR " + testDataDir.getAbsolutePath());

        final IFile tmpUploadDir = etfConfig.getPropertyAsFile(EtfConfig.ETF_TESTDATA_UPLOAD_DIR);
        if (tmpUploadDir.exists()) {
            tmpUploadDir.deleteDirectory();
        }
        tmpUploadDir.mkdir();

        // TODO provide different filters for each fileStorage
        baseFilter = new AvailableTestObjectTypesFilter(TestObjectTypeDetectorManager.getSupportedTypes().values());

        // TODO provide file storages for each Test Object Type
        fileStorage = new FileStorage(testDataDir, tmpUploadDir, baseFilter);
        fileStorage.setMaxStorageSize(etfConfig.getPropertyAsLong(EtfConfig.ETF_TEST_OBJECT_MAX_SIZE));

        logger.info("TMP_HTTP_UPLOADS: " + tmpUploadDir.getAbsolutePath());

        testObjectDao = ((WriteDao<TestObjectDto>) dataStorageService.getDao(TestObjectDto.class));

        final long exp = etfConfig.getPropertyAsLong(EtfConfig.ETF_TESTOBJECT_UPLOADED_LIFETIME_EXPIRATION);
        if (exp > 0) {
            cleanTimer = new Timer(true);
            final TimedExpiredItemsRemover timedExpiredItemsRemover = new TimedExpiredItemsRemover();
            timedExpiredItemsRemover.addExpirationItemHolder(new TestObjectCleaner(testObjectDao, testDataDir), exp,
                    TimeUnit.MINUTES);
            cleanTimer.scheduleAtFixedRate(timedExpiredItemsRemover,
                    TimeUnit.SECONDS.toMillis(TimeUtils.calcDelay(0, 9, 0)),
                    86400000);
            logger.info("Temporary Test Objects older than {} minutes are removed.", exp);
        }

        if (etfConfig.hasProperty(EtfConfig.ETF_MOUNTED_TESTDATA_DIR)) {
            mountedTestDataDir = etfConfig.getPropertyAsFile(EtfConfig.ETF_MOUNTED_TESTDATA_DIR);
            mountedTestDataDirListener = new MountedTestDataDirListener(testObjectDao, mountedTestDataDir, this);
            DirWatcher.register(mountedTestDataDir.toPath(), mountedTestDataDirListener);
        }
        logger.info("Test Object controller initialized!");
    }

    @Override
    public TestObjectDto getById(final EID id) throws StorageException, ObjectWithIdNotFoundException {
        final TestObjectDto transientTestObject = transientTestObjects.getIfPresent(id);
        final TestObjectDto testObject;
        if (transientTestObject != null) {
            testObject = transientTestObject;
        } else {
            testObject = testObjectDao.getById(id).getDto();
        }
        testObjectTypeDetectionService.resetTestObjectTypes(
                testObject, testObject.getTestObjectTypes());
        return testObject;
    }

    @Override
    public Collection<TestObjectDto> getByIds(final Set<EID> ids)
            throws StorageException, ObjectWithIdNotFoundException {
        final PreparedDtoCollection<TestObjectDto> testObjects = this.testObjectDao.getByIds(ids, SimpleFilter.allItems());
        final Collection<TestObjectDto> resultCollection = testObjects.asCollection();
        resultCollection.forEach(tO -> testObjectTypeDetectionService.resetTestObjectTypes(tO, tO.getTestObjectTypes()));
        return resultCollection;
    }

    private void createWithUrlResources(final TestObjectDto testObject) throws LocalizableApiError {

        String hash;
        try {
            final URI serviceEndpoint = testObject.getResourceByName("serviceEndpoint");

            if (etfConfig.getProperty("etf.testobject.allow.privatenet.access").equals("false")) {
                if (UriUtils.isPrivateNet(serviceEndpoint)) {
                    throw new LocalizableApiError("l.rejected.private.subnet.access", false, 403);
                }
            }

            hash = UriUtils.hashFromContent(serviceEndpoint,
                    Credentials.fromProperties(testObject.properties()));
        } catch (final UriUtils.ConnectionException e) {
            if (e.getResponseCode() == 400 && e.getUrl() != null) {
                hash = "0000000000000400";
            } else if ((e.getResponseCode() == 403 || e.getResponseCode() == 401) && e.getUrl() != null) {
                throw new LocalizableApiError("l.url.secured", false, 400, e, e.getUrl().getHost());
            } else if (e.getResponseCode() >= 401 && e.getResponseCode() < 500) {
                throw new LocalizableApiError("l.url.client.error", e);
            } else if (e.getResponseCode() != -1) {
                throw new LocalizableApiError("l.url.server.error", e);
            } else if (e.getCause() instanceof UnknownHostException && e.getUrl() != null) {
                throw new LocalizableApiError("l.unknown.host", false, 400, e, e.getUrl().getHost());
            } else {
                throw new LocalizableApiError("l.invalid.url", e);
            }
        } catch (IllegalArgumentException | IOException e) {
            throw new LocalizableApiError("l.invalid.url", e);
        }
        testObject.setItemHash(hash);
    }

    private void createWithFileResources(final TestObjectDto testObject,
            final Collection<List<MultipartFile>> uploadFiles)
            throws IOException, LocalizableApiError, InvalidPropertyException {

        if (uploadFiles != null && !uploadFiles.isEmpty()) {
            long size = 0;
            for (final List<MultipartFile> uploadFileL : uploadFiles) {
                for (final MultipartFile multipartFile : uploadFileL) {
                    size += multipartFile.getSize();
                }
            }
            if (size > etfConfig.getPropertyAsLong(EtfConfig.ETF_MAX_UPLOAD_SIZE)) {
                throw new LocalizableApiError("l.max.upload.size.exceeded", false, 400);
            }
        }

        // Regex
        final String regex = testObject.properties().getProperty("regex");
        final MultiFileFilter combinedFileFilter;
        final RegexFileFilter additionalRegexFilter;
        if (SUtils.isNullOrEmpty(regex)) {
            additionalRegexFilter = null;
            combinedFileFilter = baseFilter.filenameFilter().get();
        } else {
            additionalRegexFilter = new RegexFileFilter(regex);
            combinedFileFilter = baseFilter.filenameFilter().get().and(additionalRegexFilter);
        }

        final IFile testObjectDir;
        final URI resURI = testObject.getResourceByName("data");
        final String resourceName;
        if (resURI != null) {
            if (UriUtils.isFile(resURI) && mountedTestDataDir != null) {
                // Relative path in the mounted test object directory
                logger.warn("Mounter Test Data Dir {} ", mountedTestDataDir.getAbsolutePath());
                logger.warn("Res URI {} ", resURI.toString());
                testObjectDir = mountedTestDataDir.secureExpandPathDown(resURI.getPath());
                testObjectDir.expectDirIsReadable();
                resourceName = "data";
            } else {
                // URL
                final Credentials credentials = Credentials.fromProperties(testObject.properties());
                final FileStorage.DownloadCmd downloadCmd = fileStorage.download(
                        testObject, additionalRegexFilter, credentials, resURI);
                testObjectDir = downloadCmd.download();
                resourceName = "download." + testObject.getResourcesSize();
            }
        } else if (uploadFiles != null && !uploadFiles.isEmpty()) {
            final FileStorage.UploadCmd uploadCmd = this.fileStorage.upload(testObject, additionalRegexFilter, uploadFiles);
            testObjectDir = uploadCmd.upload();
            resourceName = "upload." + testObject.getResourcesSize();
        } else {
            throw new LocalizableApiError("l.testobject.required", false, 400);
        }

        // Add new resource
        testObject.addResource(new ResourceDto(resourceName, testObjectDir.toURI()));

        final FileHashVisitor v = new FileHashVisitor(combinedFileFilter);
        Files.walkFileTree(new File(testObject.getResourceByName(resourceName)).toPath(),
                EnumSet.of(FileVisitOption.FOLLOW_LINKS), 5, v);
        if (v.getFileCount() == 0) {
            if (regex != null && !regex.isEmpty()) {
                throw new LocalizableApiError("l.testObject.regex.null.selection", false, 400, regex);
            } else {
                throw new LocalizableApiError("l.testObject.testdir.no.usable.file.found", false, 400, v.getSkippedFiles());
            }
        }
        if (v.getSize() == 0) {
            if (v.getFileCount() == 1) {
                throw new LocalizableApiError("l.testObject.one.file.with.zero.size", false, 400, v.getFileCount());
            } else {
                throw new LocalizableApiError("l.testObject.multiple.files.with.zero.size", false, 400, v.getFileCount());
            }
        }

        testObject.setItemHash(v.getHash());
        testObject.properties().setProperty("indexed", "false");
        testObject.properties().setProperty("files", String.valueOf(v.getFileCount()));
        testObject.properties().setProperty("size", String.valueOf(v.getSize()));
        testObject.properties().setProperty("sizeHR", FileUtils.byteCountToDisplayRoundedSize(v.getSize(),
                2, LocaleContextHolder.getLocale()));
        if (v.getSkippedFiles() > 0) {
            testObject.properties().setProperty("skippedFiles", String.valueOf(v.getSkippedFiles()));
        }
        if (v.getEmptyFiles() > 0) {
            testObject.properties().setProperty("emptyFiles", String.valueOf(v.getEmptyFiles()));
        }
    }

    // Main entry point for Test Run contoller
    public void initResourcesAndAdd(final TestObjectDto testObject, final Set<EID> supportedTestObjectTypes)
            throws IOException, ObjectWithIdNotFoundException, LocalizableApiError, InvalidPropertyException {

        // If the ID is null, the Test Object references external data
        if (testObject.getId() == null) {
            // Provide a new ID
            testObject.setId(EidFactory.getDefault().createRandomId());

            // If the TestObject possess resources, it is either a service based TestObject
            // or it is a file based Test Object with either a relative path to TestData or
            // an URL to testdata that need to be downloaded.
            testObject.setItemHash("0");
            if (testObject.getResourceByName("serviceEndpoint") != null) {
                // Reference service
                createWithUrlResources(testObject);
            } else {
                // Download referenced files if there is a "data" resource
                createWithFileResources(testObject, null);
            }
            if (testObject.getAuthor() == null) {
                testObject.setAuthor("unknown");
            }
        }
        testObjectTypeDetectionService.checkAndResolveTypes(testObject, supportedTestObjectTypes);
        // otherwise it contains all required types.

        testObject.setVersionFromStr("1.0.1");
        testObject.setLastUpdateDate(new Date());
        if (testObject.getLastEditor() == null) {
            final String author = testObject.getAuthor();
            testObject.setLastEditor(SUtils.isNullOrEmpty(author) ? "unknown" : author);
        }
        testObject.setLocalPath(".");
        testObject.properties().setProperty("data.downloadable", "false");

        testObjectDao.add(testObject);
    }

    //
    // Rest interfaces
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @ApiOperation(value = "Get Test Object as JSON", notes = TEST_OBJECT_DESCRIPTION, tags = {TEST_OBJECTS_TAG_NAME})
    @RequestMapping(value = {TESTOBJECTS_URL + "/{id}",
            TESTOBJECTS_URL + "/{id}.json"}, method = RequestMethod.GET, produces = "application/json")
    public void testObjectByIdJson(@PathVariable String id,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException, ObjectWithIdNotFoundException, LocalizableApiError {
        if (transientTestObjects.getIfPresent(EidConverter.toEid(id)) != null) {
            throw new LocalizableApiError("l.temporary.testobject.access", false, 404);
        }
        streaming.asJson2(testObjectDao, request, response, id);
    }

    @ApiOperation(value = "Get multiple Test Objects as JSON", notes = TEST_OBJECT_DESCRIPTION, tags = {TEST_OBJECTS_TAG_NAME})
    @RequestMapping(value = {TESTOBJECTS_URL, TESTOBJECTS_URL + ".json"}, method = RequestMethod.GET)
    public void listTestObjectsJson(
            @ApiParam(value = OFFSET_DESCRIPTION) @RequestParam(required = false, defaultValue = "0") int offset,
            @ApiParam(value = LIMIT_DESCRIPTION) @RequestParam(required = false, defaultValue = "0") int limit,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {
        streaming.asJson2(testObjectDao, request, response, new SimpleFilter(offset, limit), 20);
    }

    @ApiOperation(value = "Get multiple Test Objects as XML", notes = TEST_OBJECT_DESCRIPTION, tags = {
            TEST_OBJECTS_TAG_NAME}, produces = "text/xml")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "EtfItemCollection with multiple Test Objects", reference = "www.interactive-instruments.de")
    })
    @RequestMapping(value = {TESTOBJECTS_URL + ".xml"}, method = RequestMethod.GET)
    public void listTestObjectXml(
            @RequestParam(required = false, defaultValue = "0") int offset,
            @RequestParam(required = false, defaultValue = "0") int limit,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        streaming.asXml2(testObjectDao, request, response, new SimpleFilter(offset, limit));
    }

    @ApiOperation(value = "Get Test Object as XML", notes = TEST_OBJECT_DESCRIPTION, tags = {
            TEST_OBJECTS_TAG_NAME}, produces = "text/xml")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Test Object", reference = "www.interactive-instruments.de"),
            @ApiResponse(code = 404, message = "Test Object not found")
    })
    @RequestMapping(value = {TESTOBJECTS_URL + "/{id}.xml"}, method = RequestMethod.GET)
    public void testObjectByIdXml(
            @ApiParam(value = "ID of Test Object that needs to be fetched", example = "EID-1ffe6ea2-5c29-4ce9-9a7e-f4d9d71119e8", required = true) @PathVariable String id,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException, ObjectWithIdNotFoundException, LocalizableApiError {
        if (transientTestObjects.getIfPresent(EidConverter.toEid(id)) != null) {
            throw new LocalizableApiError("l.temporary.testobject.access", false, 404);
        }
        streaming.asXml2(testObjectDao, request, response, id);
    }

    @ApiOperation(value = "Delete Test Object", tags = {TEST_OBJECTS_TAG_NAME})
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Test Object deleted"),
            @ApiResponse(code = 404, message = "Test Object not found")
    })
    @RequestMapping(value = TESTOBJECTS_URL + "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<String> delete(@PathVariable String id, HttpServletResponse response)
            throws ObjectWithIdNotFoundException, IOException {
        final ResponseEntity<String> exists = exists(id);
        if (!HttpStatus.NOT_FOUND.equals(exists.getStatusCode())) {
            final EID eid = EidConverter.toEid(id);
            final TestObjectDto testObject = this.testObjectDao.getById(eid).getDto();
            if ("true".equals(testObject.properties().getProperty("etf.uploaded"))) {
                final URI resource = testObject.getResourceByName("data");
                final IFile dir = new IFile(resource);
                dir.delete();
            }
            this.testObjectDao.delete(eid);
        }
        return exists;
    }

    @ApiOperation(value = "Check if Test Object exists", notes = "Please note that this interface will always return HTTP status code '404' for temporary Test Object IDs.", tags = {
            TEST_OBJECTS_TAG_NAME})
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Test Object exists"),
            @ApiResponse(code = 404, message = "Test Object does not exist")
    })
    @RequestMapping(value = {TESTOBJECTS_URL + "/{id}"}, method = RequestMethod.HEAD)
    public ResponseEntity<String> exists(
            @PathVariable String id) {
        return testObjectDao.exists(EidConverter.toEid(id)) ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @ApiOperation(value = "Create a new Test Object", notes = "Based on whether a test object is specified with the "
            + "'testobject' property, the service will either create a reusable Test Object with the provided properties or "
            + "create a TEMPORARY Test Object which can be used for one Test Run."
            + "On success the service will return it's ID which afterwards can be used to start a new Test Run. "
            + "If the ID of a temporary Test Object is not used within 5 minutes, the Test Object and all uploaded data will "
            + "be deleted automatically. "
            + "PLEASE NOTE: A TEMPORARY Test Object will not be persisted as long as it is not used in a Test Run. "
            + "A TEMPORARY Test Object can not be retrieved or deleted but can only be referenced from a 'Test Run Request' to start a new Test Run. "
            + "The property 'data.downloadable' of a TEMPORARY Test Object is always set to true. "
            + "Also note that the Swagger UI does only allow single file uploads in contrast to the API which allows multi file uploads.", tags = {
                    TEST_OBJECTS_TAG_NAME}, consumes = "multipart/form-data", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "fileupload", dataType = "__file", paramType = "form"),
            @ApiImplicitParam(name = "testobject", dataType = "object", dataTypeClass = CreateReusableTestObjectRequest.class, paramType = "form"),
    })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Temporary Test Object created", response = TestObjectCreationResponse.class),
            @ApiResponse(code = 201, message = "Reusable Test Object created", response = TestObjectCreationResponse.class),
            @ApiResponse(code = 400, message = "Test Object creation failed", response = ApiError.class),
            @ApiResponse(code = 403, message = "Test Object creation forbidden", response = ApiError.class),
            @ApiResponse(code = 413, message = "Uploaded test data are too large", response = ApiError.class)
    })
    @RequestMapping(value = {TESTOBJECTS_URL}, method = RequestMethod.POST, consumes = "multipart/form-data")
    public ResponseEntity<TestObjectCreationResponse> uploadData(
            @ApiIgnore final MultipartHttpServletRequest request, @ApiIgnore UriComponentsBuilder b)
            throws LocalizableApiError, InvalidPropertyException, ObjectWithIdNotFoundException, IOException {

        final TestObjectCreationResponse testObjectUploadResponse;
        // there seems to be no way to solve this reasonably with annotations..
        final String testObjectStr = request.getParameter("testobject");
        if (!SUtils.isNullOrEmpty(testObjectStr) && testObjectStr.length() > 3) {
            if (!"organisation-internal".equals(View.getWorkflowType())) {
                throw new LocalizableApiError("l.json.testobject.creation.forbidden", false, 403);
            }
            testObjectUploadResponse = createReusableTestObject(request);
            final UriComponents uriComponents = b.path(TESTOBJECTS_URL + "/{id}")
                    .buildAndExpand(testObjectUploadResponse.getId());
            return ResponseEntity.created(uriComponents.toUri()).body(testObjectUploadResponse);
        } else {
            statusController.ensureStatusNotMajor();
            testObjectUploadResponse = createAdHocTestObject(request);
            return ResponseEntity.ok(testObjectUploadResponse);
        }
    }

    private TestObjectCreationResponse createAdHocTestObject(final MultipartHttpServletRequest request)
            throws InvalidPropertyException, ObjectWithIdNotFoundException {
        final TestObjectDto testObject = new TestObjectDto();
        testObject.properties().setProperty("temporary", "true");
        final TestObjectCreationResponse testObjectUploadResponse = createAndDetectTestObject(request, testObject);
        this.transientTestObjects.put(testObject.getId(), testObject);
        testObject.setLabel(testObjectUploadResponse.getNameForUpload());
        return testObjectUploadResponse;
    }

    private TestObjectCreationResponse createReusableTestObject(final MultipartHttpServletRequest request)
            throws IOException, ObjectWithIdNotFoundException, InvalidPropertyException {
        final String testObjectStr = request.getParameter("testobject");
        final CreateReusableTestObjectRequest testObjectRequest = objectMapper.readValue(testObjectStr,
                CreateReusableTestObjectRequest.class);
        final TestObjectDto testObject;
        try {
            testObject = testObjectRequest.toTestObject();
        } catch (URISyntaxException e) {
            throw new LocalizableApiError(e);
        }
        final TestObjectCreationResponse testObjectUploadResponse;
        if (testObject.getResourceByName("serviceEndpoint") == null || !request.getMultiFileMap().isEmpty()) {
            testObjectUploadResponse = createAndDetectTestObject(request, testObject);
        } else {
            createWithUrlResources(testObject);
            testObjectUploadResponse = new TestObjectCreationResponse(testObject, Collections.emptyList());
            final String label = testObject.getLabel();
            final String description = testObject.getDescription();
            testObjectTypeDetectionService.checkAndResolveTypes(testObject, null);
            final TestObjectTypeDto type = testObject.getTestObjectTypes().iterator().next();
            if (testObject.getResourceByName("serviceEndpoint") != null &&
                    !type.isInstanceOf(EidFactory.getDefault().createUUID("88311f83-818c-46ed-8a9a-cec4f3707365"))) {
                throw new LocalizableApiError("l.testobject.not.service", false, 400, type.getLabel());
            }
            if (!label.equals("auto")) {
                testObject.setLabel(label);
            }
            if (SUtils.isNullOrEmpty(testObject.getLabel()) || testObject.getLabel().equals("auto")) {
                testObject.setLabel("Test Object " + testObject.getId().getId());
            }
            if (!label.equals("auto")) {
                testObject.setDescription(description);
            }
            if (SUtils.isNullOrEmpty(testObject.getDescription()) || testObject.getDescription().equals("auto")) {
                testObject.setDescription("n/a");
            }
            testObject.setAuthor(User.getUser(request));
            testObject.setCreationDateNowIfNotSet();
            testObject.setLastUpdateDateNow();
            testObject.setLastEditor(User.getUser(request));
            testObject.setVersionFromStr("1.0.1");
        }
        testObjectDao.add(testObject);
        return testObjectUploadResponse;
    }

    private TestObjectCreationResponse createAndDetectTestObject(final MultipartHttpServletRequest request,
            final TestObjectDto testObject)
            throws InvalidPropertyException, ObjectWithIdNotFoundException {
        testObject.setId(EidFactory.getDefault().createRandomId());
        try {
            // Create from upload
            createWithFileResources(testObject, request.getMultiFileMap().values());
            testObjectTypeDetectionService.checkAndResolveTypes(testObject, null);
        } catch (StorageException e) {
            throw new LocalizableApiError(e);
        } catch (IOException e) {
            throw new LocalizableApiError(e);
        }
        testObject.setLastUpdateDateNow();
        testObject.setLastEditor(User.getUser(request));
        return new TestObjectCreationResponse(testObject, request.getMultiFileMap().values());
    }

    @ApiOperation(value = "Get all Test Object resources", notes = "Download the resources of the Test Object."
            + " The creator of the object can prevent this setting the property 'data.downloadable' to 'false'."
            + " Temporary test objects cannot be downloaded, 'data.downloadable' is set to 'false' by default.", tags = {
                    TEST_OBJECTS_TAG_NAME})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Test Object resources returned"),
            @ApiResponse(code = 400, message = "Invalid Test Object ID", response = ApiError.class),
            @ApiResponse(code = 403, message = "Resource download forbidden"),
            @ApiResponse(code = 404, message = "Test Object not found", response = ApiError.class)
    })
    @RequestMapping(value = {TESTOBJECTS_URL + "/{id}/data"}, method = RequestMethod.GET)
    public void getResources(
            @ApiParam(value = EID_DESCRIPTION, example = EID_EXAMPLE, required = true) @PathVariable String id,
            final HttpServletResponse response)
            throws IOException, ObjectWithIdNotFoundException, LocalizableApiError {
        final TestObjectDto testObject = testObjectDao.getById(EidConverter.toEid(id)).getDto();
        if ("true".equals(testObject.properties().getPropertyOrDefault("data.downloadable", "false"))) {
            if (testObject.getResourcesSize() == 1) {
                final URI uri = testObject.getResourceCollection().iterator().next().getUri();
                if (!UriUtils.isFile(uri)) {
                    // stream url
                    UriUtils.stream(uri, response.getOutputStream());
                } else {
                    // compress one file/dir
                    final IFile file = new IFile(uri);
                    response.setContentType("application/zip");
                    response.setHeader("Content-disposition",
                            "attachment; filename=\"TestObject." + id + ".zip\"");
                    file.compressTo(response.getOutputStream());
                }
            } else {
                // compress multiple files
                final List<IFile> tmpDownloadedFiles = new ArrayList<>();
                try {
                    final List<IFile> files = new ArrayList<>();
                    for (final ResourceDto resource : testObject.getResourceCollection()) {
                        final URI uri = resource.getUri();
                        if (!UriUtils.isFile(uri)) {
                            // download
                            final IFile tmpFile = UriUtils.download(uri);
                            tmpDownloadedFiles.add(tmpFile);
                            files.add(tmpFile);
                        } else {
                            files.add(new IFile(uri));
                        }
                    }
                    response.setContentType("application/zip");
                    response.setHeader("Content-disposition",
                            "attachment; filename=\"TestObject." + id + ".zip\"");
                    IFile.compressTo(files, response.getOutputStream());
                } finally {
                    tmpDownloadedFiles.forEach(f -> f.delete());
                }
            }
        } else {
            response.setStatus(403);
            response.getWriter().print(
                    "Data download forbidden through \"data.downloadable\" property");
        }
    }
}
