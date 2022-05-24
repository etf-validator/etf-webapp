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
package de.interactive_instruments.etf.testdriver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.interactive_instruments.IFile;
import de.interactive_instruments.Releasable;
import de.interactive_instruments.etf.component.ComponentInfo;
import de.interactive_instruments.etf.component.ComponentLoadingException;
import de.interactive_instruments.etf.component.ComponentNotLoadedException;
import de.interactive_instruments.etf.component.loaders.LoadingContext;
import de.interactive_instruments.exceptions.config.ConfigurationException;
import de.interactive_instruments.io.MultiFileFilter;
import de.interactive_instruments.properties.Properties;
import de.interactive_instruments.properties.PropertyHolder;

/**
 * Test Driver Loader which manages and loads the Test Drivers from Jar files
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
final class TestDriverLoader implements Releasable {

    private final ConcurrentMap<String, ComponentContainer> driverContainer = new ConcurrentHashMap<>(8);
    private final ConcurrentMap<String, TestDriver> testDrivers = new ConcurrentHashMap<>(8);
    private final ConcurrentMap<String, PropertyHolder> specificConfigurations = new ConcurrentHashMap<>(8);
    private PropertyHolder configuration = new Properties();
    private final IFile testDriverDir;
    private Logger logger = LoggerFactory.getLogger(TestDriverLoader.class);
    private long testDriverLasModified = 0;
    private LoadingContext loadingContext;

    private static class TestDriverJarFileFilter implements MultiFileFilter {
        @Override
        public boolean accept(final File pathname) {
            return pathname.getName().endsWith("jar");
        }
    }

    public TestDriverLoader(final IFile testDriverDir) throws ComponentLoadingException {
        this(testDriverDir, null);
    }

    public TestDriverLoader(final IFile testDriverDir, final LoadingContext loadingContext)
            throws ComponentLoadingException {
        this.testDriverDir = testDriverDir;
        this.loadingContext = loadingContext;
        recreateTestComponents();
    }

    private void recreateTestComponents() throws ComponentLoadingException {
        final List<IFile> latestTestDriverJars;
        try {
            // Get latest versions
            latestTestDriverJars = testDriverDir.getVersionedFilesInDir(new TestDriverJarFileFilter()).latest();
        } catch (IOException e) {
            throw new ComponentLoadingException("Failed to load components: " + e.getMessage());
        }
        for (final File testDriverJar : latestTestDriverJars) {
            if (isTestComponentPrepared(testDriverJar)) {
                continue;
            }
            final ComponentContainer testDriverContainer = new ComponentContainer(testDriverJar);
            this.driverContainer.put(testDriverContainer.getId(), testDriverContainer);
        }
        testDriverLasModified = testDriverDir.lastModified();
    }

    private boolean isTestComponentPrepared(final File jarFile) {
        for (ComponentContainer testDriverContainer : driverContainer.values()) {
            if (testDriverContainer.getJar().equals(jarFile)) {
                // driver already loaded
                return true;
            }
        }
        return false;
    }

    public void setSpecificConfig(String id, PropertyHolder config) {
        this.specificConfigurations.put(id, config);
    }

    public void setConfig(PropertyHolder config) {
        this.configuration = config;
    }

    /**
     * Load component
     *
     * @param id
     */
    public synchronized void load(final String id) throws ComponentLoadingException, ConfigurationException {
        if (this.testDrivers.containsKey(id)) {
            throw new ComponentLoadingException("TestDriver " + id + " already loaded");
        }
        final PropertyHolder config;
        if (specificConfigurations.containsKey(id)) {
            config = specificConfigurations.get(id);
        } else {
            config = this.configuration;
        }
        logger.info("Loading test driver \"{}\"", id);
        final ComponentContainer driverContainer = this.driverContainer.get(id);
        if (!driverContainer.getJar().exists()) {
            logger.info("Jar {} for test driver {} not found, parsing test drivers directory again",
                    id, driverContainer.getJar());
            driverContainer.release();
            recreateTestComponents();
        }
        this.testDrivers.put(id, this.driverContainer.get(id).loadAndInit(config, loadingContext));
    }

    /**
     * Load all components
     */
    public synchronized void load() throws ComponentLoadingException {
        if (testDriverDir.lastModified() != testDriverLasModified) {
            recreateTestComponents();
        }
        for (final ComponentContainer testDriverContainer : this.driverContainer.values()) {
            final String id = testDriverContainer.getId();
            try {
                load(id);
            } catch (final ComponentLoadingException e) {
                logger.error("Failed to load component ", e);
            } catch (ConfigurationException e) {
                logger.error("Failed to configure component ", e);
            }
        }
    }

    /**
     * Unload component
     *
     * @param id
     */
    public synchronized void release(final String id) {
        logger.info("Releasing test driver \"{}\"", id);
        this.driverContainer.get(id).release();
        this.testDrivers.remove(id);
    }

    /**
     * Release all components
     */
    @Override
    public synchronized void release() {
        this.driverContainer.keySet().forEach(id -> release(id));
    }

    /**
     * Reload component
     *
     * @param id
     */
    public synchronized void reload(String id) throws ComponentLoadingException, ConfigurationException {
        release(id);
        load(id);
    }

    public ComponentInfo getInfo(final String id) {
        return this.driverContainer.get(id).getInfo();
    }

    public Collection<ComponentInfo> getInfo() {
        final ArrayList<ComponentInfo> i = new ArrayList();
        this.driverContainer.values().forEach(d -> {
            if (d.getInfo() != null) {
                i.add(d.getInfo());
            }
        });
        if (i.isEmpty()) {
            return null;
        }
        return i;
    }

    public Collection<TestDriver> getTestDrivers() {
        return Collections.unmodifiableCollection(testDrivers.values());
    }

    public TestDriver getTestDriverById(final String id) throws ConfigurationException, ComponentNotLoadedException {
        final TestDriver factory = testDrivers.get(id);
        if (factory == null) {
            throw new ComponentNotLoadedException("Unknown TestRunTaskFactory " + id);
        }
        return factory;
    }

}
