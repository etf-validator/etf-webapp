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
package de.interactive_instruments.etf.dal.dao.basex;

import static de.interactive_instruments.etf.dal.dao.basex.BsxTestUtils.DATA_STORAGE;
import static de.interactive_instruments.etf.test.TestDtos.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.jupiter.api.*;

import de.interactive_instruments.etf.dal.dao.PreparedDto;
import de.interactive_instruments.etf.dal.dao.WriteDao;
import de.interactive_instruments.etf.dal.dto.result.TestTaskResultDto;
import de.interactive_instruments.etf.dal.dto.run.TestRunDto;
import de.interactive_instruments.etf.dal.dto.run.TestTaskDto;
import de.interactive_instruments.etf.test.TestDtos;
import de.interactive_instruments.exceptions.InitializationException;
import de.interactive_instruments.exceptions.InvalidStateTransitionException;
import de.interactive_instruments.exceptions.ObjectWithIdNotFoundException;
import de.interactive_instruments.exceptions.StorageException;
import de.interactive_instruments.exceptions.config.ConfigurationException;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
@TestMethodOrder(value = MethodOrderer.Alphanumeric.class)
public class TestRunDaoTest {

    private static WriteDao<TestRunDto> writeDao;

    @BeforeAll
    public static void setUp() throws ConfigurationException, InvalidStateTransitionException, InitializationException,
            StorageException, ObjectWithIdNotFoundException, IOException {
        BsxTestUtils.ensureInitialization();
        writeDao = ((WriteDao) DATA_STORAGE.getDao(TestRunDto.class));

        TestTaskResultDaoTest.setUp();

        final WriteDao<TestTaskResultDto> tagDao = (WriteDao<TestTaskResultDto>) DATA_STORAGE.getDao(TestTaskResultDto.class);
        BsxTestUtils.forceDeleteAndAdd(TO_DTO_1);
    }

    @AfterAll
    public static void tearDown() throws StorageException, ConfigurationException, InitializationException,
            InvalidStateTransitionException, ObjectWithIdNotFoundException {
        TestTaskResultDaoTest.tearDown();
        BsxTestUtils.forceDelete(writeDao, TR_DTO_1.getId());
    }

    @BeforeEach
    public void clean() {
        try {
            writeDao.delete(TR_DTO_1.getId());
        } catch (ObjectWithIdNotFoundException | StorageException e) {}
    }

    @Test
    public void test_2_0_add_and_get() throws StorageException, ObjectWithIdNotFoundException {
        // Ensure structure
        TR_DTO_1.setTestTasks(new ArrayList<TestTaskDto>() {
            {
                this.add(TestDtos.TASK_DTO_1);
                this.add(TestDtos.TASK_DTO_2);
            }
        });
        TASK_DTO_1.setTestTaskResult(TTR_DTO_1);
        TASK_DTO_2.setTestTaskResult(TTR_DTO_2);

        BsxTestUtils.addTest(TR_DTO_1);
        BsxTestUtils.forceDeleteAndAdd(TTR_DTO_1);
        BsxTestUtils.forceDeleteAndAdd(TTR_DTO_2);

        final PreparedDto<TestRunDto> preparedDto = BsxTestUtils.getByIdTest(TR_DTO_1);

        assertNotNull(preparedDto.getDto().getTestTasks());
        assertEquals(2, preparedDto.getDto().getTestTasks().size());

        assertEquals("FAILED", preparedDto.getDto().getTestTasks().get(0).getTestTaskResult().getResultStatus().toString());

        writeDao.delete(TR_DTO_1.getId());
        assertFalse(writeDao.exists(TR_DTO_1.getId()));
        // Must be deleted as well
        BsxTestUtils.notExistsOrDisabled(TTR_DTO_1);
        BsxTestUtils.notExistsOrDisabled(TTR_DTO_2);
    }
}
