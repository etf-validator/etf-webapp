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
import static de.interactive_instruments.etf.dal.dao.basex.BsxTestUtils.getTestResourceFile;
import static de.interactive_instruments.etf.test.TestDtos.ETS_DTO_1;
import static org.junit.jupiter.api.Assertions.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import de.interactive_instruments.IFile;
import de.interactive_instruments.etf.dal.dao.*;
import de.interactive_instruments.etf.dal.dto.IncompleteDtoException;
import de.interactive_instruments.etf.dal.dto.capabilities.TestRunTemplateDto;
import de.interactive_instruments.etf.model.EID;
import de.interactive_instruments.etf.model.EidFactory;
import de.interactive_instruments.etf.model.ParameterSet;
import de.interactive_instruments.exceptions.InitializationException;
import de.interactive_instruments.exceptions.InvalidStateTransitionException;
import de.interactive_instruments.exceptions.ObjectWithIdNotFoundException;
import de.interactive_instruments.exceptions.StorageException;
import de.interactive_instruments.exceptions.config.ConfigurationException;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
@TestMethodOrder(value = MethodOrderer.Alphanumeric.class)
public class TestRunTemplateDaoTest {

    private static WriteDao<TestRunTemplateDto> writeDao;

    private static EID streamingTestRunTemplateId = EidFactory.getDefault()
            .createUUID("EIDbef57b18-61e0-4097-a40f-c524dab04c36");

    private static TestRunTemplateDto TRT_DTO_1;

    private final static Filter ALL = new Filter() {
        @Override
        public int offset() {
            return 0;
        }

        @Override
        public int limit() {
            return 2000;
        }
    };

    @BeforeAll
    public static void setUp() throws ConfigurationException, InvalidStateTransitionException, InitializationException,
            IOException, ObjectWithIdNotFoundException, IncompleteDtoException, URISyntaxException {
        BsxTestUtils.ensureInitialization();
        writeDao = ((WriteDao) DATA_STORAGE.getDao(TestRunTemplateDto.class));

        TRT_DTO_1 = new TestRunTemplateDto();
        TRT_DTO_1.setId(EidFactory.getDefault().createUUID("EID2383bc09-e33c-43ec-ad6d-3dba704c9865"));
        TRT_DTO_1.setItemHash("0");
        TRT_DTO_1.setLabel("TestRunTemplate.2 LABEL");
        TRT_DTO_1.setCreationDate(new Date());
        TRT_DTO_1.setRemoteResource(new URI("http://norui"));
        TRT_DTO_1.setDescription("TestRunTemplate.2 DESCRIPTION");
        TRT_DTO_1.setVersionFromStr("1.2.3");
        TRT_DTO_1.setAuthor("TestRunTemplate.2 author");
        TRT_DTO_1.properties().setProperty("TestRunTemplate.2 property name", "TestRunTemplate.2 property value");
        TRT_DTO_1.addExecutableTestSuite(ETS_DTO_1);
        final ParameterSet p = new ParameterSet();
        p.addParameter("1", "2");
        TRT_DTO_1.setParameters(p);
        TRT_DTO_1.ensureBasicValidity();
        BsxTestUtils.forceDelete(TRT_DTO_1);

        ExecutableTestSuiteDaoTest.setUp();

        BsxTestUtils.forceDeleteAndAdd(ETS_DTO_1);
    }

    @BeforeEach
    public void clean() {
        try {
            writeDao.delete(TRT_DTO_1.getId());
        } catch (ObjectWithIdNotFoundException | StorageException e) {}

    }

    @Test
    public void test_1_1_existsAndAddAndDelete() throws StorageException, ObjectWithIdNotFoundException {
        BsxTestUtils.existsAndAddAndDeleteTest(TRT_DTO_1);
    }

    @Test
    public void test_2_0_getById() throws StorageException, ObjectWithIdNotFoundException {
        assertFalse(writeDao.exists(TRT_DTO_1.getId()));
        writeDao.add(TRT_DTO_1);
        assertTrue(writeDao.exists(TRT_DTO_1.getId()));

        final PreparedDto<TestRunTemplateDto> preparedDto = writeDao.getById(TRT_DTO_1.getId());

        // Check internal ID
        assertEquals(TRT_DTO_1.getId(), preparedDto.getDtoId());
        final TestRunTemplateDto dto = preparedDto.getDto();
        assertNotNull(dto);
        assertEquals(TRT_DTO_1.getId(), dto.getId());
        assertEquals(TRT_DTO_1.toString(), dto.toString());
    }

    @Test
    public void test_2_1_delete() throws StorageException, ObjectWithIdNotFoundException {
        writeDao.deleteAllExisting(Collections.singleton(TRT_DTO_1.getId()));
        assertFalse(writeDao.exists(TRT_DTO_1.getId()));
        assertFalse(writeDao.isDisabled(TRT_DTO_1.getId()));
        assertFalse(writeDao.available(TRT_DTO_1.getId()));

        boolean exceptionThrown = false;
        try {
            writeDao.getById(TRT_DTO_1.getId()).getDto();
        } catch (ObjectWithIdNotFoundException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        writeDao.add(TRT_DTO_1);
        assertTrue(writeDao.exists(TRT_DTO_1.getId()));
        assertFalse(writeDao.isDisabled(TRT_DTO_1.getId()));
        assertTrue(writeDao.available(TRT_DTO_1.getId()));
        assertEquals(TRT_DTO_1.getLabel(), writeDao.getById(TRT_DTO_1.getId()).getDto().getLabel());
    }

    @Test
    public void test_2_2_doubleAdd() throws StorageException, ObjectWithIdNotFoundException {
        writeDao.deleteAllExisting(Collections.singleton(TRT_DTO_1.getId()));
        assertFalse(writeDao.exists(TRT_DTO_1.getId()));
        assertFalse(writeDao.isDisabled(TRT_DTO_1.getId()));
        assertFalse(writeDao.available(TRT_DTO_1.getId()));

        writeDao.add(TRT_DTO_1);
        assertTrue(writeDao.exists(TRT_DTO_1.getId()));
        assertFalse(writeDao.isDisabled(TRT_DTO_1.getId()));
        assertTrue(writeDao.available(TRT_DTO_1.getId()));
        assertEquals(TRT_DTO_1.getLabel(), writeDao.getById(TRT_DTO_1.getId()).getDto().getLabel());

        boolean exceptionThrown = false;
        try {
            writeDao.add(TRT_DTO_1);
        } catch (StorageException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
        assertTrue(writeDao.exists(TRT_DTO_1.getId()));
        assertFalse(writeDao.isDisabled(TRT_DTO_1.getId()));
        assertTrue(writeDao.available(TRT_DTO_1.getId()));
        assertEquals(TRT_DTO_1.getLabel(), writeDao.getById(TRT_DTO_1.getId()).getDto().getLabel());
    }

    @Test
    public void test_7_1_testStreamIntoStore()
            throws StorageException, ObjectWithIdNotFoundException, FileNotFoundException {
        writeDao.deleteAllExisting(Collections.singleton(streamingTestRunTemplateId));
        final IFile testRunTemplateFile = getTestResourceFile("database/testruntemplate.xml");
        ((StreamWriteDao<TestRunTemplateDto>) writeDao).add(new FileInputStream(testRunTemplateFile), Optional.empty());
        assertEquals("TESTRUNTEMPLATE.LABEL.1", writeDao.getById(streamingTestRunTemplateId).getDto().getLabel());
        final PreparedDtoCollection<TestRunTemplateDto> collectionResult = writeDao.getAll(ALL);
        assertTrue(collectionResult.keySet().contains(streamingTestRunTemplateId));
    }

    @Test
    public void test_7_2_testDoubleStreamIntoStore()
            throws StorageException, ObjectWithIdNotFoundException, FileNotFoundException {

        final int sizeBefore = writeDao.getAll(ALL).size();
        final IFile testRunTemplateFile = getTestResourceFile("database/testruntemplate.xml");
        ((StreamWriteDao<TestRunTemplateDto>) writeDao).add(new FileInputStream(testRunTemplateFile), Optional.empty());

        assertEquals("TESTRUNTEMPLATE.LABEL.1", writeDao.getById(streamingTestRunTemplateId).getDto().getLabel());
        final PreparedDtoCollection<TestRunTemplateDto> collectionResult = writeDao.getAll(ALL);
        assertEquals(sizeBefore, collectionResult.size());
        assertTrue(collectionResult.keySet().contains(streamingTestRunTemplateId));
    }

}
