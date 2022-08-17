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
package de.interactive_instruments.etf.testdriver;

import java.util.List;

import de.interactive_instruments.Configurable;
import de.interactive_instruments.Releasable;
import de.interactive_instruments.etf.component.ComponentInfo;
import de.interactive_instruments.etf.component.ComponentLoadingException;
import de.interactive_instruments.etf.component.loaders.LoadingContext;
import de.interactive_instruments.etf.dal.dto.run.TestRunDto;
import de.interactive_instruments.etf.model.EID;
import de.interactive_instruments.exceptions.ObjectWithIdNotFoundException;
import de.interactive_instruments.exceptions.config.ConfigurationException;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public interface TestDriverManager extends Configurable, Releasable {

    /**
     * Create a new Test Run
     *
     * @param testRunDto
     *            Test Run Dto
     * @param collectorFactory
     *            TestResultCollectorFactory
     * @return executable TestRun object
     * @throws TestRunInitializationException
     *             if the initialization failed
     */
    TestRun createTestRun(final TestRunDto testRunDto, final TestResultCollectorFactory collectorFactory)
            throws TestRunInitializationException;

    /**
     * Create a new Test Run, use default TestResultCollectorFactory
     *
     * @param testRunDto
     *            Test Run Dto
     * @return executable TestRun object
     * @throws TestRunInitializationException
     *             if the initialization failed
     */
    default TestRun createTestRun(final TestRunDto testRunDto) throws TestRunInitializationException {
        return createTestRun(testRunDto, TestResultCollectorFactory.getDefault());
    }

    /**
     * Must be called before {@link #init()} is called
     *
     * @param context
     *            LoadingContext to resolve external dependencies
     */
    void setLoadingContext(final LoadingContext context);

    List<ComponentInfo> getTestDriverInfo();

    void loadAll() throws ComponentLoadingException, ConfigurationException;

    void load(final EID testDriverId) throws ObjectWithIdNotFoundException, ComponentLoadingException, ConfigurationException;

    void unload(final EID testDriverId) throws ObjectWithIdNotFoundException;

    void reload(final EID testDriverId) throws ObjectWithIdNotFoundException, ComponentLoadingException, ConfigurationException;

    static TestDriverManager getDefault() {
        return TestDriverManagerLoader.instance();
    }
}
