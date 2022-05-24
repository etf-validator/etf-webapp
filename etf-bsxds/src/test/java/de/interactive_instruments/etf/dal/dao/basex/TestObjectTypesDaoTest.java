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

import static de.interactive_instruments.etf.dal.dao.basex.BsxTestUtils.*;
import static de.interactive_instruments.etf.test.TestDtos.TOT_DTO_1;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.UUID;

import org.junit.jupiter.api.*;

import de.interactive_instruments.etf.dal.dao.Filter;
import de.interactive_instruments.etf.dal.dao.PreparedDtoCollection;
import de.interactive_instruments.etf.dal.dao.WriteDao;
import de.interactive_instruments.etf.dal.dto.capabilities.TestObjectTypeDto;
import de.interactive_instruments.etf.detector.TestObjectTypeDetectorManager;
import de.interactive_instruments.etf.model.DefaultEidMap;
import de.interactive_instruments.etf.model.EidFactory;
import de.interactive_instruments.etf.model.EidMap;
import de.interactive_instruments.etf.model.capabilities.TestObjectType;
import de.interactive_instruments.exceptions.*;
import de.interactive_instruments.exceptions.config.ConfigurationException;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
@TestMethodOrder(value = MethodOrderer.Alphanumeric.class)
public class TestObjectTypesDaoTest {

    private static final String TO_DTO_1_REPLACED_ID = "aa1b77e2-59d5-3ce6-bbe2-eb2b4c4ae61d";
    final int maxDtos = 250;
    private static WriteDao<TestObjectTypeDto> writeDao;

    @BeforeAll
    public static void setUp() throws ConfigurationException, InvalidStateTransitionException, InitializationException,
            StorageException, ObjectWithIdNotFoundException, IOException {
        BsxTestUtils.ensureInitialization();
        writeDao = ((WriteDao) DATA_STORAGE.getDao(TestObjectTypeDto.class));
    }

    @BeforeEach
    public void clean() throws StorageException {
        BsxTestUtils.forceDelete(writeDao, TOT_DTO_1.getId());
    }

    @Test
    public void test_1_0_add() throws StorageException, ObjectWithIdNotFoundException {
        assertTrue(writeDao.isInitialized());

        notExistsOrDisabled(TOT_DTO_1);
        writeDao.add(TOT_DTO_1);
        existsAndNotDisabled(TOT_DTO_1);
    }

    @Test
    public void test_1_1_addExisting() throws StorageException, ObjectWithIdNotFoundException {
        try {
            test_1_0_add();
        } catch (Exception e) {
            ExcUtils.suppress(e);
        }
        existsAndNotDisabled(TOT_DTO_1);
        boolean exceptionThrown = false;
        try {
            writeDao.add(TOT_DTO_1);
        } catch (StorageException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    private PreparedDtoCollection<TestObjectTypeDto> getAll() throws StorageException {
        return writeDao.getAll(new Filter() {
            @Override
            public int offset() {
                return 0;
            }

            @Override
            public int limit() {
                return 15000;
            }
        });
    }

    @Test
    public void test_8_1_checkCaching() throws StorageException, ObjectWithIdNotFoundException {
        assertTrue(writeDao.isInitialized());

        // Main set
        final EidMap<TestObjectType> TOTS1 = new DefaultEidMap<>();
        final int max1 = 30;
        for (int i = 0; i < max1; i++) {
            final TestObjectTypeDto tot = TOT_DTO_1.createCopy();
            tot.setLabel("TOT--" + String.valueOf(i));
            tot.setId(EidFactory.getDefault().createAndPreserveUUID(new UUID(0, i)));
            TOTS1.put(tot.getId(), tot);
        }
        // Delete and add multiple times
        writeDao.deleteAllExisting(TOTS1.keySet());
        int before = getAll().size();
        for (int i = 0; i < 5; i++) {
            writeDao.deleteAllExisting(TOTS1.keySet());
            writeDao.addAll(TOTS1.asCollection());
            assertEquals(before + max1, getAll().size());
        }

        final PreparedDtoCollection<TestObjectTypeDto> result = getAll();
        assertEquals(before + max1, result.size());
        for (int i = 0; i < max1; i++) {
            final TestObjectTypeDto res = result.get(EidFactory.getDefault().createAndPreserveUUID(new UUID(0, i)));
            assertNotNull(res);
            assertEquals("TOT--" + String.valueOf(i), res.getLabel());
        }
    }

    // Needs further investigation
    // @Test
    public void test_8_2_checkCaching() throws StorageException, ObjectWithIdNotFoundException {
        final EidMap<TestObjectTypeDto> types = TestObjectTypeDetectorManager.getTypes(
                "88311f83-818c-46ed-8a9a-cec4f3707365",
                "db12feeb-0086-4006-bc74-28f4fdef0171",
                "9b6ef734-981e-4d60-aa81-d6730a1c6389",
                "bc6384f3-2652-4c7b-bc45-20cec488ecd0",
                "8a560e6a-043f-42ca-b0a3-31b115899593",
                "bae0df71-0553-438d-938f-028b53ba8aa7",
                "9981e87e-d642-43b3-ad5f-e77469075e74",
                "d1836a8d-9909-4899-a0bc-67f512f5f5ac",
                "380b969c-215e-46f8-a4e9-16f002f7d6c3",
                "ae35f7cd-86d9-475a-aa3a-e0bfbda2bb5f",
                "df841ddd-20d4-4551-8bc2-a4f7267e39e0",
                "dac58b52-3ffd-4eb5-96e3-64723d8f0f51",
                "824596fa-ec04-4314-bf1a-f1e6ee119bf0",
                "4d4bffed-0a18-43d3-98f4-f5e7055b02e4",
                "adeb8bc4-c49b-4704-ba88-813aea5de31d",
                "f897f313-55f0-4e51-928a-0e9869f5a1d6",
                "18bcbc68-56b9-4e8e-b0d1-90de324d0cc8",
                "b2a780a8-5bba-4780-bcd5-c8c909ac407d",
                "4b0fb35d-10f0-47df-bc0b-6d4548035ae2",
                "9b101002-e65e-4d96-ac45-fcb95ac6f507",
                "49d881ae-b115-4b91-aabe-31d5791bce52",
                "bec4dd69-72b9-498e-a693-88e3d59d2552",
                "810fce18-4bf5-4c6c-a972-6962bbe3b76b",
                "e1d4a306-7a78-4a3b-ae2d-cf5f0810853e",
                "a8a1b437-0ebf-454c-8204-bcf0b8548d8c",
                "c8aaacd7-df33-4d64-89af-fabeae63a958",
                "123b2f9b-c9f4-4379-8bf1-e9a656a14bd0",
                "057d7919-d7b8-4d77-adb8-0d3118b3d220",
                "3e3639b1-f6b7-4d62-9160-963cfb2ea300",
                "d9371e42-2bf4-420c-84a5-4ab9055a8706",
                "5a60dded-0cb0-4977-9b06-16c6c2321d2e");

        writeDao.deleteAllExisting(types.keySet());

        writeDao.addAll(types.asCollection());

        writeDao.deleteAllExisting(types.keySet());

        writeDao.addAll(types.asCollection());

        writeDao.deleteAllExisting(types.keySet());

        writeDao.addAll(types.asCollection());
    }

}
