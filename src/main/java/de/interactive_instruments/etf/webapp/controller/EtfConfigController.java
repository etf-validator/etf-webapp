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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.jar.Manifest;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import de.interactive_instruments.IFile;
import de.interactive_instruments.II_Constants;
import de.interactive_instruments.SUtils;
import de.interactive_instruments.etf.EtfConstants;
import de.interactive_instruments.exceptions.config.InvalidPropertyException;
import de.interactive_instruments.exceptions.config.MissingPropertyException;
import de.interactive_instruments.properties.PropertyHolder;

/**
 * ETF Configuration object which holds the etf-config.properties
 * and defaults.
 *
 * @author J. Herrmann ( herrmann <aT) interactive-instruments (doT> de )
 */
@RestController
public class EtfConfigController implements PropertyHolder {

	public static final String ETF_WEBAPP_BASE_URL = "etf.webapp.base.url";
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
	public static final String ETF_BSX_RECREATE_CONFIG = "etf.bsx.recreate.config";
	public static final String ETF_HELP_PAGE_URL = "etf.help.page";

	public static final String ETF_META_CONTACT_TEXT = "etf.meta.contact.text";
	public static final String ETF_META_DISCLAIMER_TEXT = "etf.meta.legalnotice.disclaimer.text";
	public static final String ETF_META_COPYRIGHT_TEXT = "etf.meta.legalnotice.copyrightnotice.text";
	public static final String ETF_META_PRIVACYSTATEMENT_TEXT = "etf.meta.privacystatement.text";

	public static final String ETF_SUBMIT_ERRORS = "etf.errors.autoreport";

	@Autowired
	private ServletContext servletContext;

	@Autowired
	@Qualifier("etfConfigProperties")
	private Properties configProperties;

	// @Autowired
	// @Qualifier("etfSecurityProperties")
	// private Properties secProperties;

	private IFile etfDir;

	private String version = "unknown";
	private static EtfConfigController instance = null;

	private final Logger logger = LoggerFactory.getLogger(EtfConfigController.class);

	private final Map<String, String> defaultProperties = Collections.unmodifiableMap(new HashMap<String, String>() {
		{
			put(ETF_WEBAPP_BASE_URL, "http://localhost:8080/etf-webapp");
			put(ETF_BRANDING_TEXT, "");
			put(ETF_META_CONTACT_TEXT, "");
			put(ETF_META_DISCLAIMER_TEXT, "");
			put(ETF_META_COPYRIGHT_TEXT, "");
			put(ETF_META_PRIVACYSTATEMENT_TEXT, "");
			put(ETF_TESTOBJECT_ALLOW_PRIVATENET_ACCESS, "false");
			put(ETF_REPORT_COMPARISON, "false");
			put(ETF_TESTOBJECT_UPLOADED_LIFETIME_EXPIRATION, "360");
			put(ETF_TESTREPORTS_LIFETIME_EXPIRATION, "43800");
			put(ETF_WORKFLOWS, "default");
			put(ETF_HELP_PAGE_URL, "https://services.interactive-instruments.de/etf-user-manual");
			put(ETF_BSX_RECREATE_CONFIG, "true");
			put(EtfConstants.ETF_PROJECTS_DIR, "projects");
			put(EtfConstants.ETF_REPORTSTYLES_DIR, "reportstyles");
			put(EtfConstants.ETF_TESTDRIVERS_DIR, "td");
			put(EtfConstants.ETF_DATASOURCE_DIR, "ds");
			put(EtfConstants.ETF_APPENDICES_DIR, "ds/obj/");
			put(EtfConstants.ETF_BACKUP_DIR, "bak");
			put(ETF_TESTDATA_DIR, "testdata");
			put(ETF_TESTDATA_UPLOAD_DIR, "http_uploads");
			put(ETF_SUBMIT_ERRORS, "false");
		}
	});

	private final Set<String> filePathPropertyKeys = Collections.unmodifiableSet(new LinkedHashSet<String>() {
		{
			add(EtfConstants.ETF_PROJECTS_DIR);
			add(EtfConstants.ETF_REPORTSTYLES_DIR);
			add(EtfConstants.ETF_APPENDICES_DIR);
			add(EtfConstants.ETF_TESTDRIVERS_DIR);
			add(EtfConstants.ETF_DATASOURCE_DIR);
			add(EtfConstants.ETF_BACKUP_DIR);
			add(ETF_TESTDATA_DIR);
			add(ETF_TESTDATA_DIR);
			add(ETF_TESTDATA_UPLOAD_DIR);
		}
	});

	@PostConstruct
	private void init()
			throws IOException, MissingPropertyException, URISyntaxException {
		version = getManifest().getMainAttributes().getValue("Implementation-Version");
		if (version == null) {
			version = "unknown";
		}

		logger.info(EtfConstants.ETF_ASCII +
				"ETF WebApp " + version + SUtils.ENDL +
				II_Constants.II_COPYRIGHT);

		System.setProperty("java.awt.headless", "true");
		logger.info("file.encoding is set to " + System.getProperty("file.encoding"));
		if (!"UTF-8".equalsIgnoreCase(System.getProperty("file.encoding"))) {
			logger.warn("The file encoding should be set to UTF-8 for "
					+ "instance in the JAVA_OPTS ( -Dfile.encoding=UTF-8 ) !");
		}

		final String propertiesFilePath = System.getenv("ETF_WEBAPP_PROPERTIES_FILE");
		if (!SUtils.isNullOrEmpty(propertiesFilePath)) {
			logger.info("ETF_WEBAPP_PROPERTIES_FILE is set, using property file " + propertiesFilePath);
			final IFile propertiesFile = new IFile(propertiesFilePath, "ETF_WEBAPP_PROPERTIES_FILE");
			propertiesFile.expectIsReadable();
			configProperties.clear();
			configProperties.load(new FileInputStream(propertiesFile));

		}

		final String propertyFileVersion = configProperties.getProperty("etf.config.properties.version");
		if (propertyFileVersion == null) {
			throw new RuntimeException("Required \"etf.config.properties.version\" property not found!");
		}
		try {
			Integer.parseInt(propertyFileVersion);
		} catch (NumberFormatException e) {
			throw new RuntimeException("Required \"etf.config.properties.version\" is not an integer!");
		}

		if ("1".equals(propertyFileVersion)) {
			logger.warn("Please upgrade your etf configuration file to version 2!");
		}

		if (Integer.parseInt(propertyFileVersion) > 2) {
			throw new RuntimeException("Config Property file version not supported");
		}

		// Environment variable ETF_DIR will overwrite the java property
		// etf.dir
		final String sysEtfDir = System.getenv("ETF_DIR");
		if (!SUtils.isNullOrEmpty(sysEtfDir)) {
			logger.info("ETF_DIR is set, using path " + sysEtfDir);
			etfDir = new IFile(sysEtfDir, "ETF_DIR");
		} else {
			if (configProperties.getProperty(ETF_DIR) != null) {
				etfDir = new IFile(configProperties.getProperty(ETF_DIR), "ETF_DIR");
			} else {
				etfDir = new IFile(servletContext.getRealPath("/WEB-INF/etf"), "ETF_DIR");
			}
		}

		// Change every path variable to absolute paths
		for (final String filePathPropertyKey : filePathPropertyKeys) {
			final String path = getProperty(filePathPropertyKey);
			final File f = new File(path);
			// Correct pathes to absolute paths
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

		// Set report style lang
		final String lang = Locale.getDefault().getLanguage();
		final IFile reportStyleDir = getPropertyAsFile(EtfConstants.ETF_REPORTSTYLES_DIR);
		final IFile langXsl = reportStyleDir.expandPath("lang/" + lang + ".xsl");
		if (langXsl.exists()) {
			langXsl.copyTo(reportStyleDir.expandPath("current.xsl").getAbsolutePath());
			logger.info("Report translation file set to \"" + lang + "\"");
		} else {
			logger.info(
					"Report translation file is not available for language \"" + lang + "\" - using default");
		}
		configProperties.forEach((k, v) -> {
			logger.info(k + " = " + v);
		});

		instance = this;

		// Add information if Opbeat is activated but no privacy statement set
		if ("true".equals(configProperties.getProperty(ETF_SUBMIT_ERRORS, "false")) &&
				SUtils.isNullOrEmpty(configProperties.getProperty(ETF_META_PRIVACYSTATEMENT_TEXT))) {
			configProperties.setProperty(ETF_META_PRIVACYSTATEMENT_TEXT,
					"The administrator of this ETF web application instance activated Opbeat "
							+ "(https://opbeat.com) which helps interactive instruments to reproduce issues in the "
							+ "user interface and improve the ETF web application. Therefore, the last user action, "
							+ "an error message and parts of the stacktrace are transferred to Opbeat in case of "
							+ "an issue.");
		}
	}

	public static EtfConfigController getInstance() {
		return instance;
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
			}
		}
		// in development mode
		return new Manifest();
	}

	@PreDestroy
	public void release() {
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

	@RequestMapping(value = "/v0/configuration", method = RequestMethod.GET, produces = "application/json")
	private @ResponseBody Set<Map.Entry<String, String>> getConfiguration() {
		return namePropertyPairs();
	}

	@RequestMapping(value = "/v0/configuration", method = RequestMethod.POST, produces = "application/json")
	private @ResponseBody Set<Map.Entry<String, String>> getConfiguration(
			@RequestBody Set<Map.Entry<String, String>> newConfiguration)
					throws InvalidPropertyException {

		// No path properties are allowed
		for (Map.Entry<String, String> e : newConfiguration) {
			if (filePathPropertyKeys.contains(e.getKey())) {
				throw new InvalidPropertyException("Path properties are not allowed");
			}
		}

		newConfiguration.forEach(p -> {
			this.configProperties.setProperty(p.getKey(), p.getValue());
		});

		return getConfiguration();
	}

}
