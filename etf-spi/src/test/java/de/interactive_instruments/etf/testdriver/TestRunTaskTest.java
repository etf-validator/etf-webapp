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

import static de.interactive_instruments.etf.test.TestDtos.TR_DTO_1;
import static de.interactive_instruments.etf.test.TestDtos.TTR_DTO_1;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.*;

import de.interactive_instruments.IFile;
import de.interactive_instruments.etf.dal.dao.DataStorageRegistry;
import de.interactive_instruments.etf.dal.dao.StreamWriteDao;
import de.interactive_instruments.etf.dal.dto.result.TestTaskResultDto;
import de.interactive_instruments.etf.dal.dto.run.TestRunDto;
import de.interactive_instruments.etf.dal.dto.run.TestTaskDto;
import de.interactive_instruments.etf.test.DataStorageTestUtils;
import de.interactive_instruments.exceptions.InitializationException;
import de.interactive_instruments.exceptions.InvalidStateTransitionException;
import de.interactive_instruments.exceptions.config.ConfigurationException;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
@TestMethodOrder(value = MethodOrderer.Alphanumeric.class)
public class TestRunTaskTest {

    @BeforeAll
    public static void setUp()
            throws InvalidStateTransitionException, InitializationException, ConfigurationException, IOException {
        if (DataStorageRegistry.instance().get("default") == null) {
            DataStorageRegistry.instance().register(
                    DataStorageTestUtils.inMemoryStorage());
        }
    }

    @Test
    public DefaultTestRun test1_createTestTask() throws IOException {
        final TestRunDto tr_dto1 = TR_DTO_1.createCopy();
        final IFile testRunDir = IFile.createTempDir("etf-unittest");
        final IFile testRunLogDir = testRunDir.expandPath("log");
        testRunLogDir.mkdirs();
        final TestRunLogger runLogger = new DefaultTestRunLogger(testRunLogDir, "default");
        final DefaultTestRun testRun = new DefaultTestRun(tr_dto1, runLogger, testRunDir);
        final TestTaskDto testTaskDto = tr_dto1.getTestTasks().get(0);
        final UnitTestTestTask testTask = new UnitTestTestTask(0, testTaskDto);
        testRun.setTestTasks(Collections.singletonList(testTask));
        testTask.setResulPersistor(
                new DefaultTestTaskPersistor(testTaskDto,
                        TestResultCollectorFactory.getDefault().createTestResultCollector(runLogger, testTaskDto),
                        ((StreamWriteDao<TestTaskResultDto>) (DataStorageRegistry.instance().get("default")
                                .getDao(TestTaskResultDto.class)))));
        return testRun;
    }

    @Test
    public void test2_checkInitNotCalled() throws IOException {
        final DefaultTestRun testRun = test1_createTestTask();
        // submit task
        final TaskPoolRegistry registry = new TaskPoolRegistry(1, 1, 1);
        registry.submitTask(testRun);

        TestRunDto result = null;
        boolean exceptionThrown = false;
        try {
            result = testRun.waitForResult();
        } catch (ExecutionException | InterruptedException e) {
            if (e.getCause() instanceof InvalidStateTransitionException) {
                exceptionThrown = true;
            }
        }
        assertTrue(exceptionThrown);
    }

    @Test
    public void test3_checkStart() throws IOException {
        final DefaultTestRun testRun = test1_createTestTask();
        // submit task
        final TaskPoolRegistry registry = new TaskPoolRegistry(1, 1, 1);
        registry.submitTask(testRun);
        Assertions.assertThrows(IllegalStateException.class, () -> {
            // Exception call back object already set
            registry.submitTask(testRun);
        });
    }

    @Test
    public void test4_checkStart()
            throws ConfigurationException, InvalidStateTransitionException, InitializationException, IOException {
        final DefaultTestRun testRun = test1_createTestTask();

        // submit task
        final TaskPoolRegistry registry = new TaskPoolRegistry(1, 1, 1);
        registry.submitTask(testRun);
        testRun.init();

        TestRunDto result = null;
        try {
            result = testRun.waitForResult();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        assertNotNull(result);
        assertEquals(TTR_DTO_1.getId(), result.getTestTaskResults().get(0).getId());
    }
}
