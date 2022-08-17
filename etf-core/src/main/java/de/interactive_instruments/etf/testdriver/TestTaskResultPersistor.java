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

import java.io.InputStream;

import de.interactive_instruments.etf.dal.dto.result.TestTaskResultDto;
import de.interactive_instruments.exceptions.StorageException;

/**
 * The TestTaskResultPersistor provides a abstract layer for persisting the results of a Test Task.
 *
 * There are three ways to persist a result: - collect results with a {@link TestResultCollector} object - save a file
 * that already is in the ETF Test Task result XML format - Persist a Test Task result object
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public interface TestTaskResultPersistor {

    /**
     * Get a {@link TestResultCollector} object for persisting the results
     *
     * @return a TestResultCollector object for persisting the result
     */
    TestResultCollector getResultCollector();

    /**
     * Stream the result of Test Task
     *
     * @param resultStream
     *            the XML result stream
     * @throws IllegalStateException
     *             if result has already bean persisted
     * @throws StorageException
     *             if result can not be persisted
     */
    void streamResult(final InputStream resultStream) throws IllegalStateException, StorageException;

    /**
     * Set the result of Test Task
     *
     * @param testTaskResultDto
     *            the result Test Task Result
     * @throws IllegalStateException
     *             if result has already bean persisted
     * @throws StorageException
     *             if result can not be persisted
     */
    void setResult(final TestTaskResultDto testTaskResultDto) throws IllegalStateException, StorageException;

    /**
     * Check if result has already benn persisted
     *
     * @return true if result has already bean persisted, false otherwise
     */
    boolean resultPersisted();
}
