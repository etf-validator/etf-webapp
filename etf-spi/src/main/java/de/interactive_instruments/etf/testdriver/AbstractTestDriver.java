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

import java.util.Collection;

import de.interactive_instruments.etf.component.loaders.LoadingContext;
import de.interactive_instruments.etf.component.loaders.NullLoadingContext;
import de.interactive_instruments.etf.dal.dto.test.ExecutableTestSuiteDto;
import de.interactive_instruments.exceptions.InitializationException;
import de.interactive_instruments.exceptions.InvalidStateTransitionException;
import de.interactive_instruments.exceptions.config.ConfigurationException;
import de.interactive_instruments.properties.ConfigProperties;
import de.interactive_instruments.properties.ConfigPropertyHolder;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public abstract class AbstractTestDriver implements TestDriver {

    final protected ConfigProperties configProperties;
    private boolean initialized = false;
    protected LoadingContext loadingContext = NullLoadingContext.instance();
    protected ExecutableTestSuiteLoader loader;

    protected AbstractTestDriver(final ConfigProperties configProperties) {
        this.configProperties = configProperties;
    }

    @Override
    public ConfigPropertyHolder getConfigurationProperties() {
        return configProperties;
    }

    @Override
    public final void setLoadingContext(final LoadingContext loadingContext) {
        this.loadingContext = loadingContext;
    }

    @Override
    public Collection<ExecutableTestSuiteDto> getExecutableTestSuites() {
        return loader.getExecutableTestSuites();
    }

    @Override
    public final void init() throws ConfigurationException, InitializationException, InvalidStateTransitionException {
        if (this.initialized) {
            throw new IllegalStateException("Test Driver already initialized");
        }
        this.configProperties.expectAllRequiredPropertiesSet();
        doInit();
        if (this.loader != null) {
            this.loader.setLoadingContext(this.loadingContext);
            this.loader.init();
        }
        this.initialized = true;
    }

    protected abstract void doInit() throws ConfigurationException, InitializationException, InvalidStateTransitionException;

    @Override
    public final boolean isInitialized() {
        return initialized;
    }

    @Override
    public final void release() {
        doRelease();
        if (this.loader != null) {
            this.loader.release();
        }
        this.initialized = false;
    }

    protected abstract void doRelease();
}
