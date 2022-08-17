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
import static de.interactive_instruments.etf.test.TestDtos.TTB_DTO_1;
import static org.junit.jupiter.api.Assertions.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;

import de.interactive_instruments.IFile;
import de.interactive_instruments.etf.dal.dao.PreparedDto;
import de.interactive_instruments.etf.dal.dao.StreamWriteDao;
import de.interactive_instruments.etf.dal.dao.WriteDao;
import de.interactive_instruments.etf.dal.dao.exceptions.StoreException;
import de.interactive_instruments.etf.dal.dto.IncompleteDtoException;
import de.interactive_instruments.etf.dal.dto.translation.TranslationTemplateBundleDto;
import de.interactive_instruments.etf.model.EID;
import de.interactive_instruments.etf.model.EidFactory;
import de.interactive_instruments.exceptions.InitializationException;
import de.interactive_instruments.exceptions.InvalidStateTransitionException;
import de.interactive_instruments.exceptions.ObjectWithIdNotFoundException;
import de.interactive_instruments.exceptions.StorageException;
import de.interactive_instruments.exceptions.config.ConfigurationException;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
@TestMethodOrder(value = MethodOrderer.Alphanumeric.class)
public class TranslationTemplateBundleDaoTest {

    private static WriteDao<TranslationTemplateBundleDto> writeDao;

    @BeforeAll
    public static void setUp() throws ConfigurationException, InvalidStateTransitionException, InitializationException,
            IOException {
        BsxTestUtils.ensureInitialization();
        writeDao = ((WriteDao) DATA_STORAGE.getDao(TranslationTemplateBundleDto.class));
    }

    @BeforeEach
    public void clean() {
        try {
            writeDao.delete(TTB_DTO_1.getId());
        } catch (ObjectWithIdNotFoundException | StorageException e) {}
    }

    @Test
    public void test_1_1_existsAndAddAndDelete() throws StorageException, ObjectWithIdNotFoundException {
        assertNotNull(writeDao);
        assertTrue(writeDao.isInitialized());
        assertFalse(writeDao.exists(TTB_DTO_1.getId()));
        writeDao.add(TTB_DTO_1);
        assertTrue(writeDao.exists(TTB_DTO_1.getId()));
        writeDao.delete(TTB_DTO_1.getId());
        assertFalse(writeDao.exists(TTB_DTO_1.getId()));
    }

    @Test
    public void test_2_getById() throws StorageException, ObjectWithIdNotFoundException {
        assertFalse(writeDao.exists(TTB_DTO_1.getId()));
        writeDao.add(TTB_DTO_1);
        assertTrue(writeDao.exists(TTB_DTO_1.getId()));
        final PreparedDto<TranslationTemplateBundleDto> preparedDto = writeDao.getById(TTB_DTO_1.getId());

        // Check internal ID
        assertEquals(TTB_DTO_1.getId(), preparedDto.getDtoId());
        final TranslationTemplateBundleDto dto = preparedDto.getDto();
        assertNotNull(dto);
        assertEquals(TTB_DTO_1.getId(), dto.getId());

        assertNotNull(dto.getTranslationTemplateCollection("TR.Template.1"));
        assertArrayEquals(new String[]{"de", "en"},
                dto.getTranslationTemplateCollection("TR.Template.1").getLanguages().toArray(new String[2]));

        assertNull(dto.getTranslationTemplate("en", "TR.Template.1"));
        assertNotNull(dto.getTranslationTemplate("TR.Template.1", "en"));
        assertEquals("TR.Template.1", dto.getTranslationTemplate("TR.Template.1", "en").getName());
        assertEquals(Locale.ENGLISH.toLanguageTag(), dto.getTranslationTemplate("TR.Template.1", "en").getLanguage());
        assertEquals("TR.Template.1 with three tokens: {TOKEN.3} {TOKEN.1} {TOKEN.2}",
                dto.getTranslationTemplate("TR.Template.1", "en").getStrWithTokens());

        assertNull(dto.getTranslationTemplate("de", "TR.Template.2"));
        assertNotNull(dto.getTranslationTemplate("TR.Template.2", "de"));
        assertEquals("TR.Template.2", dto.getTranslationTemplate("TR.Template.2", "de").getName());
        assertEquals(Locale.GERMAN.toLanguageTag(), dto.getTranslationTemplate("TR.Template.2", "de").getLanguage());
        assertEquals("TR.Template.2 mit drei tokens: {TOKEN.5} {TOKEN.4} {TOKEN.6}",
                dto.getTranslationTemplate("TR.Template.2", "de").getStrWithTokens());

        writeDao.delete(TTB_DTO_1.getId());
        assertFalse(writeDao.exists(TTB_DTO_1.getId()));
    }

    @Test
    public void test_7_0_stream_file_to_store()
            throws StorageException, ObjectWithIdNotFoundException, FileNotFoundException, IncompleteDtoException,
            URISyntaxException {
        final IFile testObjectXmlFile = getTestResourceFile(
                "database/translationtemplatebundle.xml");
        final EID id = EidFactory.getDefault().createAndPreserveStr("70a263c0-0ad7-42f2-9d4d-0d8a4ca71b52");

        final TranslationTemplateBundleDto translationTemplateBundle = ((StreamWriteDao<TranslationTemplateBundleDto>) writeDao)
                .add(new FileInputStream(testObjectXmlFile), Optional.empty());
        translationTemplateBundle.ensureBasicValidity();

        assertEquals(id.getId(), translationTemplateBundle.getId().getId());

    }

    @Test
    public void test_7_1_stream_file_to_store() {
        Assertions.assertThrows(StoreException.class, () -> {

            final IFile testObjectXmlFile = getTestResourceFile(
                    "database/invalidtranslationtemplatebundle.xml");
            ((StreamWriteDao<TranslationTemplateBundleDto>) writeDao).add(new FileInputStream(testObjectXmlFile),
                    Optional.empty());
        });
    }
}
