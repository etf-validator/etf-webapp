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

import static de.interactive_instruments.etf.EtfConstants.ETF_DATASOURCE_DIR;
import static de.interactive_instruments.etf.EtfConstants.ETF_TESTDRIVERS_DIR;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.Manifest;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;

import ch.qos.logback.classic.Level;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import de.interactive_instruments.IFile;
import de.interactive_instruments.II_Constants;
import de.interactive_instruments.LogUtils;
import de.interactive_instruments.SUtils;
import de.interactive_instruments.etf.EtfConstants;
import de.interactive_instruments.exceptions.ExcUtils;
import de.interactive_instruments.exceptions.config.MissingPropertyException;
import de.interactive_instruments.properties.PropertyHolder;
import de.interactive_instruments.properties.PropertyUtils;

/**
 * ETF Configuration object which holds the etf-config.properties
 * and defaults.
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
@RestController
public class EtfConfigController implements PropertyHolder {

	@FunctionalInterface
	public interface EtfConfigPropertyChangeListener {
		void propertyChanged(final String propertyName, final String oldValue, final String newValue);
	}

	public static final String ETF_WEBAPP_BASE_URL = "etf.webapp.base.url";
	public static final String ETF_API_BASE_URL = "etf.api.base.url";
	public static final String ETF_API_ALLOW_ORIGIN = "etf.api.allow.origin";
	public static final String ETF_BRANDING_TEXT = "etf.branding.text";
	public static final String ETF_TESTOBJECT_ALLOW_PRIVATENET_ACCESS = "etf.testobject.allow.privatenet.access";
	// in minutes
	public static final String ETF_TESTOBJECT_UPLOADED_LIFETIME_EXPIRATION = "etf.testobject.uploaded.lifetime.expiration";
	public static final String ETF_REPORT_COMPARISON = "etf.report.comparison";
	// in minutes
	public static final String ETF_TESTREPORTS_LIFETIME_EXPIRATION = "etf.testreports.lifetime.expiration";
	public static final String ETF_WORKFLOWS = "etf.workflows";
	public static final String ETF_TESTDATA_DIR = "etf.testdata.dir";
	public static final String ETF_TESTDATA_UPLOAD_DIR = "etf.testdata.upload.dir";
	public static final String ETF_DIR = "etf.dir";
	public static final String ETF_FEED_DIR = "etf.feed.dir";
	public static final String ETF_BSX_RECREATE_CONFIG = "etf.bsx.recreate.config";
	public static final String ETF_HELP_PAGE_URL = "etf.help.page";

	public static final String ETF_META_CONTACT_TEXT = "etf.meta.contact.text";
	public static final String ETF_META_DISCLAIMER_TEXT = "etf.meta.legalnotice.disclaimer.text";
	public static final String ETF_META_COPYRIGHT_TEXT = "etf.meta.legalnotice.copyrightnotice.text";
	public static final String ETF_META_PRIVACYSTATEMENT_TEXT = "etf.meta.privacystatement.text";

	public static final String ETF_SUBMIT_ERRORS = "etf.errors.autoreport";

	private static final String ETF_CONFIG_PROPERTY_FILENAME = "etf-config.properties";
	private static final String ETF_CONFIG_DIR_NAME = "config";

	@Autowired
	private ServletContext servletContext;

	private final Properties configProperties = new Properties();

	private IFile etfDir;

	private final List<EtfConfigPropertyChangeListener> listeners = new ArrayList<>();

	private static String requiredConfigVersion = "2";

	private String version = "unknown";
	private static EtfConfigController instance = null;

	private final Logger logger = LoggerFactory.getLogger(EtfConfigController.class);

	private static final Map<String, String> defaultProperties = Collections.unmodifiableMap(new HashMap<String, String>() {
		{
			put(ETF_WEBAPP_BASE_URL, "http://localhost:8080/etf-webapp");
			put(ETF_BRANDING_TEXT, "");
			put(ETF_TESTOBJECT_ALLOW_PRIVATENET_ACCESS, "false");
			put(ETF_REPORT_COMPARISON, "false");
			put(ETF_TESTOBJECT_UPLOADED_LIFETIME_EXPIRATION, "360");
			put(ETF_TESTREPORTS_LIFETIME_EXPIRATION, "43800");
			put(ETF_HELP_PAGE_URL,
					"http://docs.etf-validator.net/User_manuals/Simplified_workflows.html");
			put(ETF_BSX_RECREATE_CONFIG, "true");
			put(ETF_SUBMIT_ERRORS, "false");
			put(ETF_WORKFLOWS, "simplified");
			put(EtfConstants.ETF_PROJECTS_DIR, "projects");
			put(EtfConstants.ETF_REPORTSTYLES_DIR, "reportstyles");
			put(ETF_TESTDRIVERS_DIR, "td");
			put(EtfConstants.ETF_DATASOURCE_DIR, "ds");
			put(EtfConstants.ETF_ATTACHMENT_DIR, "ds/attachments/");
			put(EtfConstants.ETF_BACKUP_DIR, "bak");
			// put(ETF_FEED_DIR, ".feed");
			put(ETF_TESTDATA_DIR, "testdata");
			put(ETF_TESTDATA_UPLOAD_DIR, "http_uploads");
		}
	});

	private static final Set<String> filePathPropertyKeys = Collections.unmodifiableSet(new LinkedHashSet<String>() {
		{
			add(EtfConstants.ETF_PROJECTS_DIR);
			// add(EtfConstants.ETF_REPORTSTYLES_DIR);
			add(EtfConstants.ETF_ATTACHMENT_DIR);
			add(ETF_TESTDRIVERS_DIR);
			add(EtfConstants.ETF_DATASOURCE_DIR);
			add(EtfConstants.ETF_BACKUP_DIR);
			add(ETF_TESTDATA_DIR);
			add(ETF_TESTDATA_UPLOAD_DIR);
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
				logger.warn("Skipping directory '" + dir.getAbsolutePath() +
						"' which does not contain a '" + ETF_CONFIG_PROPERTY_FILENAME +
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
		System.setProperty("http.agent", "ETF validator (version: " + version + " )");

		System.setProperty("java.awt.headless", "true");
		final String encoding = System.getProperty("file.encoding");
		logger.info("file.encoding is set to " + encoding);
		if (!"UTF-8".equalsIgnoreCase(System.getProperty("file.encoding"))) {
			System.setProperty("file.encoding", "UTF-8");
			// Print as error and sleep for 3 seconds, so it is noticed by Admins
			logger.error(LogUtils.ADMIN_MESSAGE,
					"The file encoding must be set to UTF-8 " +
							"(for instance by adding   -Dfile.encoding=UTF-8   to the JAVA_OPTS)");
			try {
				Thread.sleep(3000);
			} catch (final InterruptedException ign) {
				ExcUtils.suppress(ign);
			}
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
		for (final String filePathPropertyKey : filePathPropertyKeys) {
			final String path = getProperty(filePathPropertyKey);
			final File f = new File(path);
			// Correct paths to absolute paths
			if (!f.isAbsolute()) {
				final IFile absPath = etfDir.expandPath(path);
				absPath.expectIsReadable();
				configProperties.setProperty(filePathPropertyKey, absPath.getAbsolutePath());
			}
		}

		// Basex data source
		final IFile bsxConfigDir = getPropertyAsFile(EtfConstants.ETF_DATASOURCE_DIR).expandPath("db");
		System.setProperty("org.basex.path", bsxConfigDir.getAbsolutePath());
		if (bsxConfigDir.exists() && hasProperty(ETF_BSX_RECREATE_CONFIG) &&
				"true".equals(getProperty(ETF_BSX_RECREATE_CONFIG))) {
			final IFile bsxConfigFile = bsxConfigDir.expandPath(".basex");
			logger.info("Deleting basex config file " + bsxConfigFile.getAbsolutePath());
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

		// Set CORS
		final String cors = configProperties.getProperty(ETF_API_ALLOW_ORIGIN);
		if (SUtils.isNullOrEmpty(cors)) {
			configProperties.setProperty(ETF_API_ALLOW_ORIGIN, configProperties.getProperty(ETF_WEBAPP_BASE_URL));
		}

		if (!this.getProperty(ETF_WORKFLOWS).equals("simplified")) {
			logger.error("Workflow types other than 'simplified', are not supported yet!");
			throw new RuntimeException("Workflow types other than 'simplified' are not supported yet!");
		}

		configProperties.forEach((k, v) -> logger.info(k + " = " + v));
		instance = this;
	}

	private IFile createInitialDirectoryStructure(final IFile dir) throws IOException {
		etfDir = dir;
		logger.info("Creating a new ETF data directory in {} ", etfDir);
		etfDir.mkdirs();
		etfDir.expectDirIsWritable();
		filePathPropertyKeys.forEach(d -> etfDir.expandPath(defaultProperties.get(d)).mkdirs());

		final IFile tdDir = etfDir.expandPath(defaultProperties.get(ETF_TESTDRIVERS_DIR));
		tdDir.mkdirs();

		etfDir.expandPath(defaultProperties.get(ETF_DATASOURCE_DIR)).mkdirs();
		etfDir.expandPath(defaultProperties.get(ETF_DATASOURCE_DIR)).expandPath("obj").mkdirs();
		etfDir.expandPath(defaultProperties.get(ETF_DATASOURCE_DIR)).expandPath("attachments").mkdirs();
		etfDir.expandPath(defaultProperties.get(ETF_DATASOURCE_DIR)).expandPath("db").mkdirs();
		etfDir.expandPath(defaultProperties.get(ETF_DATASOURCE_DIR)).expandPath("db/data").mkdirs();
		etfDir.expandPath(defaultProperties.get(ETF_DATASOURCE_DIR)).expandPath("db/repo").mkdirs();

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

		// Copy test drivers (will be automatically downloaded in future releases)
		final String tdDirName = "/testdrivers";
		final Set<String> tds = servletContext.getResourcePaths(tdDirName);
		for (final String td : tds) {
			final String tdName = td.substring(tdDirName.length());
			final IFile tdJar = new IFile(tdDir, tdName);
			final InputStream jarStream = servletContext.getResourceAsStream(td);
			try (final FileOutputStream out = new FileOutputStream(tdJar)) {
				IOUtils.copy(jarStream, out);
			} catch (final IOException e) {
				tdJar.delete();
				logger.error("Could not copy test driver: ", e);
			} finally {
				jarStream.close();
			}
		}

		return checkDirForConfig(etfDir);
	}

	public static EtfConfigController getInstance() {
		return instance;
	}

	public void registerPropertyChangeListener(final EtfConfigPropertyChangeListener listener) {
		this.listeners.add(listener);
	}

	public String getVersion() {
		return version;
	}

	private Manifest getManifest() {
		final InputStream inputStream = servletContext.getResourceAsStream("/META-INF/MANIFEST.MF");
		if (inputStream != null) {
			try {
				return new Manifest(inputStream);
			} catch (IOException e) {
				logger.error("Manifest not available; ", e);
			} finally {
				IFile.closeQuietly(inputStream);
			}
		}
		// in development mode
		return new Manifest();
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

	//
	// Rest interfaces
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private static String[] logLevels = {"OFF", "ERROR", "WARN", "INFO", "DEBUG", "TRACE", "ALL"};

	@RequestMapping(value = "/v2/admin/log", method = RequestMethod.GET, produces = "application/json")
	private @ResponseBody List<String> logFile(
			@RequestParam(value = "max", required = false) String maxLinesStr,
			@RequestParam(value = "search", required = false) String search) throws IOException {
		final long defaultMax = 45;
		long maxLines = defaultMax;
		if (!SUtils.isNullOrEmpty(maxLinesStr)) {
			maxLines = Long.valueOf(maxLinesStr);
			if (maxLines < 0) {
				maxLines = defaultMax;
			}
		}
		final File relEtfLog = new File("etf.log");
		final File logFile = relEtfLog.exists() ? relEtfLog : new File(PropertyUtils.getenvOrProperty(
				"ETF_DIR", "./"), "logs/etf.log");
		if (logFile.exists()) {
			try (ReversedLinesFileReader reader = new ReversedLinesFileReader(logFile, StandardCharsets.UTF_8)) {
				int i = 0;
				String line;
				final LinkedList<String> output = new LinkedList<>();

				if (SUtils.isNullOrEmpty(search)) {
					while ((line = reader.readLine()) != null && i++ < maxLines) {
						output.addFirst(line);
					}
				} else {
					final int linesAfterMatch = 30;
					final int linesBeforeMatch = 15;

					final LinkedList<String> tmpOutput = new LinkedList<>();
					final Pattern pattern = Pattern.compile(search);

					int remainingLinesAfterMatch = 0;
					while ((line = reader.readLine()) != null && i++ < maxLines) {
						if (remainingLinesAfterMatch > 0) {
							// Add remaining lines after a match
							if (pattern.matcher(line).find()) {
								remainingLinesAfterMatch += linesAfterMatch;
							} else {
								--remainingLinesAfterMatch;
							}
							output.add(line);
						} else {
							if (pattern.matcher(line).find()) {
								remainingLinesAfterMatch = linesAfterMatch;
								if (tmpOutput.isEmpty()) {
									output.add(line);
								} else {
									output.addAll(tmpOutput);
									output.addFirst(line);
									tmpOutput.clear();
								}
							} else {
								tmpOutput.addFirst(line);
								if (tmpOutput.size() > linesBeforeMatch) {
									tmpOutput.removeLast();
								}
							}
						}
					}
				}
				return output;
			}
		} else {
			logger.warn("Log file '{}' not found", logFile.getAbsolutePath());
		}
		return Collections.EMPTY_LIST;
	}

	@RequestMapping(value = "/v2/admin/loglevel", method = RequestMethod.POST, produces = "application/json")
	private ResponseEntity<String> logLevel(
			@RequestBody String logLevel) {
		if (!SUtils.isNullOrEmpty(logLevel)) {
			try {
				final ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(
						Logger.ROOT_LOGGER_NAME);
				final ch.qos.logback.classic.Level newLevel = ch.qos.logback.classic.Level.toLevel(logLevel.toUpperCase(),
						ch.qos.logback.classic.Level.ALL);
				final ch.qos.logback.classic.Level currentLevel = rootLogger.getLevel();
				if (currentLevel == newLevel) {
					return new ResponseEntity("NO CHANGE", HttpStatus.FOUND);
				} else if (newLevel == Level.ALL) {
					return new ResponseEntity("Unknown log level", HttpStatus.BAD_REQUEST);
				}
				rootLogger.setLevel(newLevel);
				logger.info("Set log level to {} ", newLevel);
				return new ResponseEntity("OK", HttpStatus.OK);
			} catch (final ClassCastException e) {
				logger.error(LogUtils.FATAL_MESSAGE, "Failed to change log level: ", e);
				return new ResponseEntity("Could not change logger", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			return new ResponseEntity("Empty log level", HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/v2/admin/configuration", method = RequestMethod.GET, produces = "application/json")
	private @ResponseBody Set<Map.Entry<String, String>> getConfiguration() {
		return namePropertyPairs();
	}

	@RequestMapping(value = "/v2/admin/configuration", method = RequestMethod.POST, produces = "application/json")
	private @ResponseBody Set<Map.Entry<String, String>> getConfiguration(
			@RequestBody Set<Map.Entry<String, String>> newConfiguration)
			throws LocalizableApiError {

		// No path properties are allowed
		for (Map.Entry<String, String> e : newConfiguration) {
			if (filePathPropertyKeys.contains(e.getKey())) {
				logger.error("Denied attempt to overwrite path property '{}' in configuration", e.getKey());
				throw new LocalizableApiError("l.overwriting.path.properties.not.allowed", false, HttpStatus.FORBIDDEN.value());
			}
		}

		newConfiguration.forEach(p -> {
			this.configProperties.setProperty(p.getKey(), p.getValue());
		});

		return getConfiguration();
	}

}
