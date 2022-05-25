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

import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;

import de.interactive_instruments.etf.dal.dao.StreamWriteDao;
import de.interactive_instruments.etf.dal.dto.result.TestTaskResultDto;
import de.interactive_instruments.etf.dal.dto.run.TestTaskDto;
import de.interactive_instruments.exceptions.StorageException;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public class DefaultTestTaskPersistor implements TestTaskResultPersistor, TestTaskEndListener {

    private final TestTaskDto testTaskDto;
    private final TestResultCollector collector;
    private final StreamWriteDao<TestTaskResultDto> writeDao;
    private boolean persisted = false;
    private static final ClassLoader loader = Thread.currentThread().getContextClassLoader();

    public DefaultTestTaskPersistor(
            final TestTaskDto testTaskDto,
            final TestResultCollector collector,
            final StreamWriteDao<TestTaskResultDto> writeDao) {
        this.testTaskDto = Objects.requireNonNull(testTaskDto, "Test Task is null");
        this.writeDao = Objects.requireNonNull(writeDao, "DAO is null");
        this.collector = Objects.requireNonNull(collector, "Collector is null");
        this.collector.registerTestTaskEndListener(this);
    }

    private void checkState() {
        if (resultPersisted()) {
            throw new IllegalStateException("Result already persisted");
        }
    }

    private void setResultInTask(final TestTaskResultDto testTaskResultDto) {
        testTaskDto.setTestTaskResult(testTaskResultDto);
        persisted = true;
    }

    @Override
    public TestResultCollector getResultCollector() {
        return collector;
    }

    @Override
    public void streamResult(final InputStream resultStream) throws StorageException {
        checkState();
        SwitchClassLoader.FunctWithException<StorageException> changeClassLoader = () -> setResultInTask(
                writeDao.add(resultStream, Optional.ofNullable(testTaskDto.getParent())));
        changeClassLoader.doIt();
    }

    @Override
    public void setResult(final TestTaskResultDto testTaskResultDto) throws StorageException {
        checkState();
        writeDao.add(testTaskResultDto);
        setResultInTask(testTaskResultDto);
    }

    @Override
    public boolean resultPersisted() {
        return persisted;
    }

    @Override
    public void testTaskFinished(final TestTaskResultDto testTaskResultDto) {
        setResultInTask(testTaskResultDto);
    }
}
