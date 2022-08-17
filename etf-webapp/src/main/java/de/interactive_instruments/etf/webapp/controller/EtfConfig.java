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

import static de.interactive_instruments.etf.EtfConstants.*;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.Manifest;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import de.interactive_instruments.*;
import de.interactive_instruments.etf.EtfConstants;
import de.interactive_instruments.exceptions.ExcUtils;
import de.interactive_instruments.exceptions.config.InvalidPropertyException;
import de.interactive_instruments.exceptions.config.MissingPropertyException;
import de.interactive_instruments.properties.PropertyHolder;
import de.interactive_instruments.properties.PropertyUtils;

/**
 * ETF Configuration object which holds the etf-config.properties and defaults.
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
@Component
public class EtfConfig implements PropertyHolder {

    @FunctionalInterface
    interface EtfConfigPropertyChangeListener {
        void propertyChanged(final String propertyName, final String oldValue, final String newValue);
    }

    public static final String ETF_WEBAPP_BASE_URL = "etf.webapp.base.url";
    public static final String ETF_CSS_URL = "etf.webapp.css.url";
    public static final String ETF_API_BASE_URL = "etf.api.base.url";
    public static final String ETF_API_ALLOW_ORIGIN = "etf.api.allow.origin";
    public static final String ETF_BRANDING_TEXT = "etf.branding.text";
    public static final String ETF_TESTOBJECT_ALLOW_PRIVATENET_ACCESS = "etf.testobject.allow.privatenet.access";
    // in minutes
    public static final String ETF_TESTOBJECT_UPLOADED_LIFETIME_EXPIRATION = "etf.testobject.temporary.lifetime.expiration";
    public static final String ETF_REPORT_COMPARISON = "etf.report.comparison";
    // in minutes
    public static final String ETF_TESTREPORTS_LIFETIME_EXPIRATION = "etf.testreports.lifetime.expiration";
    public static final String ETF_TEST_RUN_TEMPLATES_ALLOW_CREATION = "etf.testruntemplates.allow.creation";
    public static final String ETF_WORKFLOWS = "etf.workflows";
    public static final String ETF_TESTDATA_DIR = "etf.testdata.dir";
    public static final String ETF_MOUNTED_TESTDATA_DIR = "etf.mounted.testdata.dir";
    public static final String ETF_TESTDATA_UPLOAD_DIR = "etf.testdata.upload.dir";

    public static final String ETF_DIR = "etf.dir";
    public static final String ETF_FEED_DIR = "etf.feed.dir";
    public static final String ETF_HELP_PAGE_URL = "etf.help.page";
    public static final String ETF_TCONF_PAGE_URL = "etf.tconf.page";

    public static final String ETF_META_CONTACT_TEXT = "etf.meta.contact.text";
    public static final String ETF_META_DISCLAIMER_TEXT = "etf.meta.legalnotice.disclaimer.text";
    public static final String ETF_META_COPYRIGHT_TEXT = "etf.meta.legalnotice.copyrightnotice.text";
    public static final String ETF_META_PRIVACYSTATEMENT_TEXT = "etf.meta.privacystatement.text";

    public static final String ETF_SUBMIT_ERRORS = "etf.errors.autoreport";
    public static final String ETF_STACKTRACE_SHOW = "etf.stacktrace.show";
    public static final String ETF_MAX_UPLOAD_SIZE = "etf.max.upload.size";
    public static final String ETF_TEST_OBJECT_MAX_SIZE = "etf.testobject.max.size";
    public static final String ETF_ALLOWED_ENCODINGS = "etf.testobject.allow.encodings";

    private static final String ETF_CONFIG_PROPERTY_FILENAME = "etf-config.properties";
    private static final String ETF_CONFIG_DIR_NAME = "config";

    @Autowired
    private ServletContext servletContext;

    @Autowired
    private ApplicationContext appContext;

    protected final Properties configProperties = new Properties();

    private IFile etfDir;

    protected final List<EtfConfigPropertyChangeListener> listeners = new ArrayList<>();

    private final static String requiredConfigVersion = "2";

    private String version = "unknown";
    private static EtfConfig instance = null;

    private final Logger logger = LoggerFactory.getLogger(EtfConfig.class);

    private static final Map<String, String> defaultProperties = Collections.unmodifiableMap(new HashMap<String, String>() {
        {
            put(ETF_WEBAPP_BASE_URL, "http://localhost:8080/etf-webapp");
            put(ETF_BRANDING_TEXT, "ETF");
            put(ETF_TESTOBJECT_ALLOW_PRIVATENET_ACCESS, "false");
            put(ETF_TEST_OBJECT_MAX_SIZE, "5368709120");
            put(ETF_REPORT_COMPARISON, "false");
            // 8 h
            put(ETF_TESTOBJECT_UPLOADED_LIFETIME_EXPIRATION, "480");
            // 8 days
            put(ETF_TESTREPORTS_LIFETIME_EXPIRATION, "11520");
            put(ETF_HELP_PAGE_URL,
                    "https://docs.etf-validator.net/v2.0/User_manuals/Simplified_workflows.html");
            put(ETF_TCONF_PAGE_URL, "false");
            put(ETF_SUBMIT_ERRORS, "false");
            put(ETF_STACKTRACE_SHOW, "false");
            put(ETF_MAX_UPLOAD_SIZE, "auto");
            put(ETF_WORKFLOWS, "simplified");
            put(EtfConstants.ETF_PROJECTS_DIR, "projects");
            put(EtfConstants.ETF_REPORTSTYLES_DIR, "reportstyles");
            put(ETF_TESTDRIVERS_DIR, "td/lib");
            put(EtfConstants.ETF_INTERNAL_DATABASE_DIR, "db");
            put(EtfConstants.ETF_TESTDRIVERS_STORAGE_DIR, "td/data");
            put(EtfConstants.ETF_ATTACHMENT_DIR, "attachments");
            put(EtfConstants.ETF_BACKUP_DIR, "bak");
            // put(ETF_FEED_DIR, ".feed");
            put(ETF_TESTDATA_DIR, "testdata");
            put(ETF_TESTDATA_UPLOAD_DIR, "http_uploads");
            put(ETF_ALLOWED_ENCODINGS, "all");
        }
    });

    protected static final Set<String> FILE_PATH_PROPERTY_KEYS = Collections.unmodifiableSet(new LinkedHashSet<String>() {
        {
            add(EtfConstants.ETF_PROJECTS_DIR);
            add(EtfConstants.ETF_ATTACHMENT_DIR);
            add(ETF_TESTDRIVERS_DIR);
            add(EtfConstants.ETF_INTERNAL_DATABASE_DIR);
            add(EtfConstants.ETF_TESTDRIVERS_STORAGE_DIR);
            add(EtfConstants.ETF_BACKUP_DIR);
            add(ETF_TESTDATA_DIR);
            add(ETF_TESTDATA_UPLOAD_DIR);
            add(ETF_MOUNTED_TESTDATA_DIR);
            // add(ETF_FEED_DIR);
        }
    });

    private IFile checkDirForConfig(final IFile dir) {
        if (dir.exists()) {
            final IFile configFile = dir.expandPath(ETF_CONFIG_PROPERTY_FILENAME);
            final IFile configFallbackFile = dir.expandPath(ETF_CONFIG_DIR_NAME).expandPath(ETF_CONFIG_PROPERTY_FILENAME);
            if (configFile.exists() && configFile.length() > 0) {
                return configFile;
            } else if (configFallbackFile.exists() && configFallbackFile.length() > 0) {
                return configFallbackFile;
            } else {
                logger.warn("Directory '" + dir.getAbsolutePath() +
                        "' does not contain a '" + ETF_CONFIG_PROPERTY_FILENAME +
                        "' configuration file or a '" + ETF_CONFIG_DIR_NAME + "' subdirectory containing the '" +
                        ETF_CONFIG_PROPERTY_FILENAME + "' file.");
                return null;
            }
        }
        return null;
    }

    @PostConstruct
    private void init()
            throws IOException, MissingPropertyException, URISyntaxException {
        version = getManifest().getMainAttributes().getValue("Implementation-Version");
        if (version == null) {
            version = "unknown";
        } else if (version.contains("-SNAPSHOT")
                && !SUtils.isNullOrEmpty(getManifest().getMainAttributes().getValue("Build-Time"))) {
            version = version.replace("-SNAPSHOT",
                    "-b" + getManifest().getMainAttributes().getValue("Build-Time").substring(2));
        }

        logger.info(EtfConstants.ETF_ASCII +
                "ETF WebApp " + version + SUtils.ENDL +
                II_Constants.II_COPYRIGHT);

        // Set HTTP Client to ETF
        System.setProperty("http.ii.agent", "ETF validator (" + version + ")");

        System.setProperty("java.awt.headless", "true");
        final String encoding = System.getProperty("file.encoding");
        logger.info("file.encoding is set to " + encoding);
        if (!"UTF-8".equalsIgnoreCase(System.getProperty("file.encoding"))) {
            System.setProperty("file.encoding", "UTF-8");
            // Print as error and sleep for 3 seconds, so it is noticed by Admins
            logger.error(LogUtils.ADMIN_MESSAGE,
                    "The file encoding must be set to UTF-8 " +
                            "(for instance by adding   -Dfile.encoding=UTF-8   to the JAVA_OPTS)");
        }

        final long allocatedMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
        final long presumableFreeMemory = Runtime.getRuntime().maxMemory() - allocatedMemory;
        logger.info("Allocated memory: {}", FileUtils.byteCountToDisplaySize(allocatedMemory));
        logger.info("Presumable free memory: {}", FileUtils.byteCountToDisplaySize(presumableFreeMemory));
        final long ONE_GB = 1073741824;
        if (presumableFreeMemory < ONE_GB) {
            logger.warn(LogUtils.ADMIN_MESSAGE,
                    "Less than 1 GB presumable free memory detected. ETF may not work properly.");
        }

        // Find property file
        final IFile propertiesFile;
        final String sysEnv = System.getenv("ETF_DIR");
        if (!SUtils.isNullOrEmpty(sysEnv)) {
            logger.info("Using environment variable ETF_DIR for the ETF data directory {}", sysEnv);
            final IFile sysEnvEtfDir = new IFile(sysEnv, "ETF_DIR");
            final IFile detectedPropertiesFile = checkDirForConfig(sysEnvEtfDir);
            if (detectedPropertiesFile != null) {
                propertiesFile = detectedPropertiesFile;
            } else {
                propertiesFile = createInitialDirectoryStructure(sysEnvEtfDir);
            }
        } else {
            final String propertiesFilePath = PropertyUtils.getenvOrProperty("ETF_WEBAPP_PROPERTIES_FILE", null);
            final String configFileIdentifier = "ETF_CONFIG_PROPERTY_FILE";
            if (!SUtils.isNullOrEmpty(propertiesFilePath)) {
                logger.info("Using environment variable ETF_WEBAPP_PROPERTIES_FILE for property file {}", propertiesFilePath);
                if (propertiesFilePath.contains(ETF_CONFIG_PROPERTY_FILENAME)) {
                    propertiesFile = new IFile(propertiesFilePath, configFileIdentifier);
                } else {
                    // Be gentle, user accidentally selected the dir
                    propertiesFile = new IFile(propertiesFilePath, configFileIdentifier)
                            .expandPath(ETF_CONFIG_PROPERTY_FILENAME);
                }
                propertiesFile.expectIsReadable();
            } else {
                // Check root directories
                IFile detectedPropertiesFile = null;
                for (final File rootFile : File.listRoots()) {
                    detectedPropertiesFile = checkDirForConfig(new IFile(rootFile, configFileIdentifier));
                    if (detectedPropertiesFile != null) {
                        break;
                    }
                }

                // Check for the /etc/etf directory on Linux
                if (detectedPropertiesFile == null && (SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_MAC)) {
                    detectedPropertiesFile = checkDirForConfig(new IFile("/etc/etf/", configFileIdentifier));
                }

                // Check for a etf directory in home
                if (detectedPropertiesFile == null) {
                    final String etfHomeDirName = SystemUtils.IS_OS_WINDOWS ? "etf" : ".etf";
                    if (System.getProperty("user.home") == null) {
                        final String errMesg = "Neither the ETF_DIR, the ETF_WEBAPP_PROPERTIES_FILE nor the home directory "
                                + "are set as environment variables.";
                        logger.error(errMesg);
                        throw new RuntimeException(errMesg);
                    }
                    final IFile homeConfigDir = new IFile(System.getProperty("user.home")).expandPath(etfHomeDirName);
                    detectedPropertiesFile = checkDirForConfig(homeConfigDir);

                    if (detectedPropertiesFile == null) {
                        // Not found, check the ProgammData directory on windows
                        if (SystemUtils.IS_OS_WINDOWS) {
                            // Folders in ALLUSERSPROFILE may be uploaded to the server after logoff.
                            // Alternative: combine with LOCALAPPDATA ?
                            // https://www.microsoft.com/security/portal/mmpc/shared/variables.aspx
                            final IFile programDataDir = new IFile(System.getenv("ALLUSERSPROFILE")).expandPath("etf");
                            detectedPropertiesFile = checkDirForConfig(programDataDir);
                            if (detectedPropertiesFile == null) {
                                // Create the directories in the ProgammData directory on Windows
                                detectedPropertiesFile = createInitialDirectoryStructure(programDataDir);
                            }
                        } else {
                            // Create the directories in the home directory on Linux
                            detectedPropertiesFile = createInitialDirectoryStructure(homeConfigDir);
                        }
                    }
                }
                propertiesFile = detectedPropertiesFile;
            }
        }
        logger.info("Using configuration file: {}", propertiesFile);
        propertiesFile.expectFileIsReadable();
        configProperties.load(new FileInputStream(propertiesFile));

        final String propertyFileVersion = configProperties.getProperty("etf.config.properties.version");
        if (propertyFileVersion == null) {
            throw new RuntimeException("Required \"etf.config.properties.version\" property not found in configuration file!");
        }
        if (!requiredConfigVersion.equals(propertyFileVersion)) {
            throw new RuntimeException("Configuration Property file version " + propertyFileVersion + " not supported. "
                    + "Version " + requiredConfigVersion + " expected.");
        }

        // Check if etfDir is set in configuration, otherwise default to directory with the config file
        if (configProperties.getProperty(ETF_DIR) != null) {
            etfDir = new IFile(configProperties.getProperty(ETF_DIR), "ETF_DIR");
        } else {
            // configuration file is in a subdirectory of the etf dir.
            etfDir = new IFile(propertiesFile.getParentFile().getParentFile());
        }

        // Change every path variable to absolute paths
        for (final String filePathPropertyKey : FILE_PATH_PROPERTY_KEYS) {
            final String path;
            if (SystemUtils.IS_OS_WINDOWS) {
                final String p = getProperty(filePathPropertyKey);
                if (p != null) {
                    final String cleanP = p.replaceAll("\\\\", "/");
                    if (p.contains("\\")) {
                        logger.warn("Replacing path {} with {}", p, cleanP);
                    }
                    path = Paths.get(getProperty(filePathPropertyKey)).normalize().toString();
                } else {
                    path = null;
                }
            } else {
                path = getProperty(filePathPropertyKey);
            }
            if (!SUtils.isNullOrEmpty(path)) {
                final File f = new File(path);
                // Correct paths to absolute paths
                if (!f.isAbsolute()) {
                    final IFile absPath = etfDir.expandPath(path);
                    absPath.expectIsReadable();
                    configProperties.setProperty(filePathPropertyKey, absPath.getAbsolutePath());
                }
            }
        }
        // ETF_MOUNTED_TESTDATA_DIR !=
        if (hasProperty(ETF_MOUNTED_TESTDATA_DIR) &&
                getPropertyAsFile(ETF_MOUNTED_TESTDATA_DIR).equals(getProperty(ETF_TESTDATA_DIR))) {
            throw new RuntimeException(ETF_MOUNTED_TESTDATA_DIR + " and " + ETF_TESTDATA_DIR +
                    " cannot be set to the same directory");
        }

        // Check if this version ships newer test drivers and update them
        updateTestDrivers();

        // Basex data source
        final IFile bsxConfigDir = getPropertyAsFile(EtfConstants.ETF_INTERNAL_DATABASE_DIR).expandPath("db");
        System.setProperty("org.basex.path", bsxConfigDir.getAbsolutePath());
        if (bsxConfigDir.exists()) {
            final IFile bsxConfigFile = bsxConfigDir.expandPath(".basex");
            if (!bsxConfigFile.delete() && bsxConfigFile.exists()) {
                throw new IOException(
                        "Unable to delete .basex configuration file: " + bsxConfigFile.getAbsolutePath());
            }
        }

        // Add default properties
        defaultProperties.entrySet().forEach(p -> configProperties.putIfAbsent(p.getKey(), p.getValue()));

        if (this.getProperty(ETF_WEBAPP_BASE_URL).contains("//localhost")) {
            logger.warn(LogUtils.ADMIN_MESSAGE,
                    "The ETF_WEBAPP_BASE_URL property must not be set to 'localhost' in a production environment");
        }

        // Set API base url
        final String apiBaseUrl = configProperties.getProperty(ETF_API_BASE_URL);
        if (SUtils.isNullOrEmpty(apiBaseUrl)) {
            configProperties.setProperty(ETF_API_BASE_URL, configProperties.getProperty(ETF_WEBAPP_BASE_URL) + "/v2");
        }

        // Set CSS url
        final String cssUrl = configProperties.getProperty(ETF_CSS_URL);
        if (SUtils.isNullOrEmpty(cssUrl)) {
            configProperties.setProperty(ETF_CSS_URL, configProperties.getProperty(ETF_WEBAPP_BASE_URL) + "/css");
        }

        // Set CORS
        final String cors = configProperties.getProperty(ETF_API_ALLOW_ORIGIN);
        if (SUtils.isNullOrEmpty(cors)) {
            configProperties.setProperty(ETF_API_ALLOW_ORIGIN, configProperties.getProperty(ETF_WEBAPP_BASE_URL));
        }

        // Workflow and Template creation
        if (this.getProperty(ETF_WORKFLOWS).equals("organisation-internal")) {
            configProperties.setProperty(ETF_TEST_RUN_TEMPLATES_ALLOW_CREATION,
                    configProperties.getProperty(ETF_TEST_RUN_TEMPLATES_ALLOW_CREATION, "true"));
        } else if (this.getProperty(ETF_WORKFLOWS).equals("simplified")) {
            configProperties.setProperty(ETF_TEST_RUN_TEMPLATES_ALLOW_CREATION,
                    configProperties.getProperty(ETF_TEST_RUN_TEMPLATES_ALLOW_CREATION, "false"));
        } else {
            logger.error("Unknown workflow type '" + this.getProperty(ETF_WORKFLOWS) + "'");
            throw new RuntimeException("Unknown workflow type '" + this.getProperty(ETF_WORKFLOWS) + "'");
        }

        if (!this.getProperty(ETF_ALLOWED_ENCODINGS).equals("all")) {
            final String encodings = this.getProperty(ETF_ALLOWED_ENCODINGS);
            final String[] encodingsArr = encodings.split(",");
            boolean encodingsKnown = true;
            for (final String enc : encodingsArr) {
                try {
                    Charset.forName(enc);
                } catch (UnsupportedCharsetException e) {
                    logger.error("{}: encoding {} is unknown", ETF_ALLOWED_ENCODINGS, enc);
                    encodingsKnown = false;
                }
            }
            if (encodingsKnown) {
                System.setProperty("ii.file.encodings.allowed", encodings);
            } else {
                logger.error("{}: configuration option ignored", ETF_ALLOWED_ENCODINGS);
            }
        }

        // Max upload size
        try {
            final Object multipartResolverObj = appContext.getBean("multipartResolver");
            if (multipartResolverObj != null) {
                final CommonsMultipartResolver multipartResolver = (CommonsMultipartResolver) multipartResolverObj;
                final String maxUploadSize = configProperties.getProperty(ETF_MAX_UPLOAD_SIZE);
                if (maxUploadSize.equalsIgnoreCase("auto")) {
                    logger.info("Automatic setting max upload size based on presumable free memory");
                    final long maxTestDatabaseSize = presumableFreeMemory * 2;
                    final long maxCompressedFileUploadSize = maxTestDatabaseSize / 15;
                    multipartResolver.setMaxUploadSize(maxCompressedFileUploadSize);
                    configProperties.setProperty(ETF_MAX_UPLOAD_SIZE, String.valueOf(maxCompressedFileUploadSize));
                } else {
                    multipartResolver.setMaxUploadSize(Long.valueOf(maxUploadSize));
                }
            }
        } catch (final NoSuchBeanDefinitionException e) {
            logger.error("MultipartResolver not found: max upload size cannot be checked.");
            // Fallback limit 100 MB, only checked in the web interface
            configProperties.setProperty(ETF_MAX_UPLOAD_SIZE, "104857600");
        }
        try {
            final long maxUploadSize = getPropertyAsLong(ETF_MAX_UPLOAD_SIZE);
            final long maxObjectSize = getPropertyAsLong(ETF_TEST_OBJECT_MAX_SIZE);
            if (maxUploadSize > maxObjectSize) {
                logger.warn("The value of the {} property should be set to value greater "
                        + "than the value {} of the {} property.",
                        ETF_TEST_OBJECT_MAX_SIZE, maxUploadSize, ETF_MAX_UPLOAD_SIZE);
                configProperties.setProperty(ETF_TEST_OBJECT_MAX_SIZE, configProperties.getProperty(ETF_MAX_UPLOAD_SIZE));
            }
        } catch (InvalidPropertyException e) {
            // Should never happen
            ExcUtils.suppress(e);
        }

        plausabilityCheckMinutes(ETF_TESTREPORTS_LIFETIME_EXPIRATION);
        plausabilityCheckMinutes(ETF_TESTOBJECT_UPLOADED_LIFETIME_EXPIRATION);

        final IFile etsDir = getPropertyAsFile(EtfConstants.ETF_PROJECTS_DIR);
        if (etsDir.listFiles().length == 0) {
            logger.warn("The project directory at {} is empty.", etsDir.getAbsolutePath());
        }

        configProperties.forEach((k, v) -> logger.info(k + " = " + v));
        instance = this;
    }

    private void plausabilityCheckMinutes(final String property) {
        final long minutes;
        final String defaultVal = defaultProperties.get(property);
        try {
            minutes = this.getPropertyAsLong(property);
        } catch (InvalidPropertyException e) {
            logger.error("{} : not a number : {}. Setting default value: {}",
                    property, this.getProperty(property), defaultVal);
            configProperties.setProperty(property, defaultVal);
            return;
        }
        if (minutes < 0) {
            logger.error("{} : a negative value is not allowed: {}. Setting default value: {}",
                    property, minutes, defaultVal);
            configProperties.setProperty(property, defaultVal);
        } else if (minutes < 20 && logger.isDebugEnabled()) {
            // Values less than 20 minutes are allowed in debug mode
            logger.error("{} : a value less than 20 minutes ( {}) can interfere with the Test "
                    + "Runs. Setting default value: {}",
                    property, minutes, defaultVal);
            configProperties.setProperty(property, defaultVal);
        } else if (minutes > 131400) {
            logger.warn("{} : a value higher than 3 month might be be very optimistic: {}",
                    property, minutes);
            configProperties.setProperty(property, defaultVal);
        }
    }

    private IFile createInitialDirectoryStructure(final IFile dir) throws IOException {

        etfDir = dir;
        logger.info("Creating a new ETF data directory in {} ", etfDir);
        etfDir.mkdirs();
        etfDir.expectDirIsWritable();
        FILE_PATH_PROPERTY_KEYS.forEach(d -> {
            if (defaultProperties.containsKey(d)) {
                etfDir.expandPath(defaultProperties.get(d)).mkdirs();
            }
        });

        etfDir.expandPath(defaultProperties.get(ETF_ATTACHMENT_DIR)).mkdirs();
        etfDir.expandPath(defaultProperties.get(ETF_TESTDRIVERS_DIR)).mkdirs();
        etfDir.expandPath(defaultProperties.get(ETF_TESTDRIVERS_STORAGE_DIR)).mkdirs();

        etfDir.expandPath(defaultProperties.get(ETF_INTERNAL_DATABASE_DIR)).mkdirs();
        etfDir.expandPath(defaultProperties.get(ETF_INTERNAL_DATABASE_DIR)).expandPath("obj").mkdirs();
        etfDir.expandPath(defaultProperties.get(ETF_INTERNAL_DATABASE_DIR)).expandPath("db").mkdirs();
        etfDir.expandPath(defaultProperties.get(ETF_INTERNAL_DATABASE_DIR)).expandPath("db/data").mkdirs();
        etfDir.expandPath(defaultProperties.get(ETF_INTERNAL_DATABASE_DIR)).expandPath("db/repo").mkdirs();

        // Copy config template to ETF_CONFIG_DIR_NAME / ETF_CONFIG_PROPERTY_FILENAME
        final IFile configFileDir = etfDir.expandPath(ETF_CONFIG_DIR_NAME);
        configFileDir.mkdirs();
        final InputStream stream = servletContext.getResourceAsStream("/WEB-INF/classes/" + ETF_CONFIG_PROPERTY_FILENAME);
        if (stream == null) {
            // Debugging and running jettyRun instead of jettyRunWar ?
            throw new RuntimeException("Unknown internal error: "
                    + "Could not find template etf configuration file. Servlet Context: " + servletContext);
        }
        final IFile newConfigFile = new IFile(configFileDir, ETF_CONFIG_PROPERTY_FILENAME);
        try (final FileOutputStream out = new FileOutputStream(newConfigFile)) {
            IOUtils.copy(stream, out);
        } catch (final IOException e) {
            throw new RuntimeException("Could not copy template configuration file: ", e);
        } finally {
            stream.close();
        }
        updateTestDrivers();
        return checkDirForConfig(etfDir);
    }

    private void updateTestDrivers() throws IOException {
        final IFile tdDir = etfDir.expandPath(defaultProperties.get(ETF_TESTDRIVERS_DIR));
        tdDir.mkdirs();
        if (tdDir.secureExpandPathDown(".etf_do_not_touch_drivers").exists()) {
            logger.debug("Drivers are not touched.");
        } else {
            final IFile.VersionedFileList latestDriverVersions = tdDir.getVersionedFilesInDir();
            // Copy test drivers
            final String tdDirName = "/testdrivers";
            final Set<String> tds = servletContext.getResourcePaths(tdDirName);
            if (tds != null) {
                for (final String td : tds) {
                    final String testDriverName = td.substring(tdDirName.length());
                    if (!SUtils.isNullOrEmpty(testDriverName) && latestDriverVersions.isNewer(testDriverName)) {
                        logger.info("Installing Test Driver " + testDriverName);
                        final IFile tdJar = new IFile(tdDir, testDriverName);
                        try (InputStream jarStream = servletContext.getResourceAsStream(td);
                                final FileOutputStream out = new FileOutputStream(tdJar)) {
                            IOUtils.copy(jarStream, out);
                        } catch (final IOException e) {
                            tdJar.delete();
                            logger.error("Could not copy test driver: ", e);
                        }
                    }
                }
            }
        }
    }

    public static EtfConfig getInstance() {
        return instance;
    }

    public void registerPropertyChangeListener(final EtfConfigPropertyChangeListener listener) {
        this.listeners.add(listener);
    }

    public String getVersion() {
        return version;
    }

    private Manifest getManifest() {
        try (final InputStream inputStream = servletContext.getResourceAsStream("/META-INF/MANIFEST.MF")) {
            if (inputStream != null) {
                return new Manifest(inputStream);
            }
        } catch (IOException e) {
            logger.error("Manifest not available; ", e);
        }
        // in development mode
        return new Manifest();
    }

    protected static Properties update(final EtfConfig holder, final String key, final String value) {
        final String oldValue = holder.configProperties.getProperty(key, null);
        holder.configProperties.setProperty(key, value);
        holder.listeners.forEach(l -> l.propertyChanged(key, oldValue, value));
        return holder.configProperties;
    }

    protected static Map<Object, Object> manifest(final EtfConfig holder) {
        return holder.getManifest().getMainAttributes();
    }

    @PreDestroy
    public void release() {
        this.listeners.clear();
        logger.info("Bye");
        instance = null;
    }

    @Override
    public String getProperty(final String key) {
        final String property = configProperties.getProperty(key);
        return property != null ? property : defaultProperties.get(key);
    }

    @Override
    public Set<String> getPropertyNames() {
        return this.configProperties.stringPropertyNames();
    }

    @Override
    public boolean hasProperty(final String key) {
        return getProperty(key) != null;
    }

    @Override
    public Set<Map.Entry<String, String>> namePropertyPairs() {
        Set<? extends Map.Entry<?, ?>> set = Collections.unmodifiableSet(configProperties.entrySet());
        return (Set<Map.Entry<String, String>>) set;
    }

    @Override
    public int size() {
        return this.configProperties.size();
    }

}
