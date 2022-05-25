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
package de.interactive_instruments.etf.dal.dao.basex;

import static de.interactive_instruments.etf.dal.dao.basex.BsxTestUtils.DATA_STORAGE;
import static de.interactive_instruments.etf.test.TestDtos.ASSERTION_TYPE_1;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.*;

import de.interactive_instruments.etf.dal.dao.PreparedDto;
import de.interactive_instruments.etf.dal.dao.WriteDao;
import de.interactive_instruments.etf.dal.dto.test.TestItemTypeDto;
import de.interactive_instruments.exceptions.InitializationException;
import de.interactive_instruments.exceptions.InvalidStateTransitionException;
import de.interactive_instruments.exceptions.ObjectWithIdNotFoundException;
import de.interactive_instruments.exceptions.StorageException;
import de.interactive_instruments.exceptions.config.ConfigurationException;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public class TestItemTypeDaoTest {

    private static WriteDao<TestItemTypeDto> writeDao;

    @BeforeAll
    public static void setUp() throws ConfigurationException, InvalidStateTransitionException, InitializationException,
            StorageException, IOException {
        BsxTestUtils.ensureInitialization();
        writeDao = ((WriteDao) DATA_STORAGE.getDao(TestItemTypeDto.class));
    }

    @BeforeEach
    public void clean() {
        try {
            writeDao.delete(ASSERTION_TYPE_1.getId());
        } catch (ObjectWithIdNotFoundException | StorageException e) {}
    }

    @Test
    public void test_1_1_existsAndAddAndDelete() throws StorageException, ObjectWithIdNotFoundException {
        BsxTestUtils.existsAndAddAndDeleteTest(ASSERTION_TYPE_1);
    }

    @Test
    public void test_2_0_getById() throws StorageException, ObjectWithIdNotFoundException {
        final PreparedDto<TestItemTypeDto> preparedDto = BsxTestUtils.addAndGetByIdTest(ASSERTION_TYPE_1);

        writeDao.delete(ASSERTION_TYPE_1.getId());
        assertFalse(writeDao.exists(ASSERTION_TYPE_1.getId()));
    }

}
