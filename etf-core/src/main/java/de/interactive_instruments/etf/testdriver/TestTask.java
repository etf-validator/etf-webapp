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

import de.interactive_instruments.etf.dal.dto.result.TestTaskResultDto;

/**
 * A task intended for tests.
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public interface TestTask extends Task<TestTaskResultDto> {

    /**
     * Run the task
     *
     * @throws Exception
     *             any exception that occures during the test run
     */
    void run() throws Exception;

    /**
     * Set the persistor object
     *
     * @throws IllegalStateException
     *             if already set
     * @param persistor
     *            persistor object
     */
    void setResulPersistor(final TestTaskResultPersistor persistor) throws IllegalStateException;

    TaskProgress getProgress();

    TestRunLogger getLogger();
}
