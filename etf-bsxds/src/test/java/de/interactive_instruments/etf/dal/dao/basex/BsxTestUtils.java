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

import static org.junit.jupiter.api.Assertions.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import de.interactive_instruments.IFile;
import de.interactive_instruments.etf.EtfConstants;
import de.interactive_instruments.etf.dal.dao.Dao;
import de.interactive_instruments.etf.dal.dao.Filter;
import de.interactive_instruments.etf.dal.dao.PreparedDto;
import de.interactive_instruments.etf.dal.dao.WriteDao;
import de.interactive_instruments.etf.dal.dto.Dto;
import de.interactive_instruments.etf.model.Disableable;
import de.interactive_instruments.etf.model.EID;
import de.interactive_instruments.etf.model.EidFactory;
import de.interactive_instruments.etf.model.OutputFormat;
import de.interactive_instruments.etf.test.XmlTestUtils;
import de.interactive_instruments.exceptions.*;
import de.interactive_instruments.exceptions.config.ConfigurationException;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
class BsxTestUtils {

    final static BsxDataStorage DATA_STORAGE = new BsxDataStorage();

    static IFile DATA_STORAGE_DIR;
    static IFile ATTACHMENTS_DIR;

    static String toStrWithTrailingZeros(int i) {
        return String.format("%05d", i);
    }

    static String toStrWithTrailingZeros(long i) {
        return String.format("%05d", i);
    }

    static void ensureInitialization()
            throws ConfigurationException, InvalidStateTransitionException, InitializationException, IOException {
        if (!DATA_STORAGE.isInitialized()) {

            DATA_STORAGE_DIR = new IFile("./build/tmp/ds-base");
            DATA_STORAGE_DIR.mkdirs();
            ATTACHMENTS_DIR = new IFile("./build/tmp/ds-attachments");
            ATTACHMENTS_DIR.mkdirs();

            final IFile styleDir = new IFile("./src/main/resources/xslt/default/style");
            styleDir.expectDirIsReadable();

            assertTrue(DATA_STORAGE_DIR != null && DATA_STORAGE_DIR.exists());
            DATA_STORAGE.getConfigurationProperties()
                    .setProperty(EtfConstants.ETF_INTERNAL_DATABASE_DIR, DATA_STORAGE_DIR.getAbsolutePath())
                    .setProperty(EtfConstants.ETF_ATTACHMENT_DIR, ATTACHMENTS_DIR.getAbsolutePath())
                    .setProperty("etf.webapp.base.url", styleDir.getAbsolutePath())
                    .setProperty("etf.api.base.url", "http://localhost/etf-webapp/v2");
            DATA_STORAGE.init();
            DATA_STORAGE.reset();
        }
    }

    static void forceDelete(final Dto dto) throws StorageException {
        forceDelete(getDao(dto), dto.getId());
    }

    static void forceDelete(final Dao dao, final EID eid) throws StorageException {
        try {
            ((WriteDao) dao).delete(eid);
        } catch (ObjectWithIdNotFoundException e) {
            ExcUtils.suppress(e);
        }
        if (Disableable.class.isAssignableFrom(dao.getDtoType())) {
            if (dao.exists(eid)) {
                assertTrue(dao.isDisabled(eid));
            }
        } else {
            assertFalse(dao.exists(eid));
            assertFalse(dao.isDisabled(eid));
        }
    }

    static void notExistsOrDisabled(final Dto dto) {
        final WriteDao dao = getDao(dto);
        if (Disableable.class.isAssignableFrom(dao.getDtoType())) {
            if (dao.exists(dto.getId())) {
                assertTrue(dao.isDisabled(dto.getId()));
            }
        } else {
            assertFalse(dao.exists(dto.getId()));
            assertFalse(dao.isDisabled(dto.getId()));
        }
    }

    static void existsAndDisabled(final Dto dto) {
        final WriteDao dao = getDao(dto);
        if (Disableable.class.isAssignableFrom(dao.getDtoType())) {
            assertTrue(dao.exists(dto.getId()));
            assertTrue(dao.isDisabled(dto.getId()));
        } else {
            fail("Not disableable");
        }
    }

    static void notExists(final Dto dto) {
        final WriteDao dao = getDao(dto);
        assertFalse(dao.exists(dto.getId()));
        assertFalse(dao.isDisabled(dto.getId()));
    }

    static void existsAndNotDisabled(final Dto dto) {
        final WriteDao dao = getDao(dto);
        assertTrue(dao.exists(dto.getId()));
        assertFalse(dao.isDisabled(dto.getId()));
    }

    static WriteDao getDao(final Dto dto) {
        assertNotNull(dto);
        final Dao dao = DATA_STORAGE.getDao(dto.getClass());
        assertNotNull(dao);
        assertTrue(dao.isInitialized());
        return (WriteDao) dao;
    }

    static void forceDeleteAndAdd(final Dto dto) throws StorageException, ObjectWithIdNotFoundException {
        forceDeleteAndAdd(dto, true);
    }

    static void forceDeleteAndAdd(final Dto dto, boolean check) throws StorageException, ObjectWithIdNotFoundException {
        final WriteDao dao = getDao(dto);

        forceDelete(dao, dto.getId());
        try {
            dao.add(dto);
        } catch (StorageException e) {
            ExcUtils.suppress(e);
        }
        assertTrue(dao.exists(dto.getId()));
        if (check) {
            assertNotNull(dao.getById(dto.getId()).getDto());
        }
    }

    static void existsAndAddAndDeleteTest(final Dto dto) throws StorageException, ObjectWithIdNotFoundException {
        final WriteDao dao = getDao(dto);

        assertFalse(dao.exists(dto.getId()));
        dao.add(dto);
        assertTrue(dao.exists(dto.getId()));
        dao.delete(dto.getId());
        assertFalse(dao.exists(dto.getId()));
    }

    static void addTest(final Dto dto) throws StorageException, ObjectWithIdNotFoundException {
        final WriteDao dao = getDao(dto);
        assertTrue(!dao.exists(dto.getId()) || dao.isDisabled(dto.getId()));
        dao.add(dto);
        assertTrue(dao.exists(dto.getId()) && !dao.isDisabled(dto.getId()));
    }

    static <T extends Dto> PreparedDto<T> getByIdTest(final T dto) throws StorageException, ObjectWithIdNotFoundException {
        return getByIdTest(dto, null);
    }

    static <T extends Dto> PreparedDto<T> getByIdTest(final T dto, final Filter filter)
            throws StorageException, ObjectWithIdNotFoundException {
        final WriteDao dao = getDao(dto);

        final PreparedDto<T> preparedDto = dao.getById(dto.getId(), filter);
        // Check internal ID
        assertEquals(dto.getId(), preparedDto.getDtoId());
        final T queriedDto = preparedDto.getDto();
        assertNotNull(dto);
        assertEquals(dto.getId(), queriedDto.getId());
        assertEquals(dto.getDescriptiveLabel(), queriedDto.getDescriptiveLabel());

        // Check compareTo
        final PreparedDto<T> preparedDto2 = dao.getById(dto.getId(), filter);
        assertEquals(0, preparedDto2.compareTo(preparedDto));
        // Will execute the query
        assertEquals(preparedDto2.getDtoId(), preparedDto2.getDto().getId());
        assertEquals(0, preparedDto2.compareTo(preparedDto));

        return preparedDto;
    }

    static <T extends Dto> PreparedDto<T> addAndGetByIdTest(final T dto)
            throws StorageException, ObjectWithIdNotFoundException {
        return addAndGetByIdTest(dto, null);
    }

    static <T extends Dto> PreparedDto<T> addAndGetByIdTest(final T dto, final Filter filter)
            throws StorageException, ObjectWithIdNotFoundException {
        addTest(dto);
        return getByIdTest(dto, filter);
    }

    public static String trimAllWhitespace(String str) {
        if (!hasLength(str)) {
            return str;
        }
        int len = str.length();
        StringBuilder sb = new StringBuilder(str.length());
        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
            if (!Character.isWhitespace(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static void compareStreamingContentRaw(final Dto dto, final String path, final String format)
            throws ObjectWithIdNotFoundException, IOException {
        addTest(dto);
        final WriteDao dao = getDao(dto);
        final PreparedDto preparedDto = dao.getById(dto.getId());

        final IFile tmpFile = IFile.createTempFile("etf", ".raw");
        tmpFile.deleteOnExit();
        final FileOutputStream fop = new FileOutputStream(tmpFile);
        final OutputFormat outputFormat = (OutputFormat) dao.getOutputFormats().get(
                EidFactory.getDefault().createUUID(dao.getDtoType().getSimpleName() + format));

        preparedDto.streamTo(outputFormat, null, fop);

        final IFile cmpResult = getTestResourceFile(path);
        assertTrue(cmpResult.exists());
        assertEquals(trimAllWhitespace(cmpResult.readContent().toString()),
                trimAllWhitespace(tmpFile.readContent().toString()));
    }

    public static void compareStreamingContentXml(final Dto dto, final String path, final String format)
            throws ObjectWithIdNotFoundException, IOException {
        addTest(dto);
        final WriteDao dao = getDao(dto);
        final PreparedDto preparedDto = dao.getById(dto.getId());

        final IFile tmpFile = IFile.createTempFile("etf", ".xml");
        tmpFile.deleteOnExit();
        final FileOutputStream fop = new FileOutputStream(tmpFile);
        final OutputFormat outputFormat = (OutputFormat) dao.getOutputFormats().get(
                EidFactory.getDefault().createUUID(dao.getDtoType().getSimpleName() + format));

        preparedDto.streamTo(outputFormat, null, fop);

        final IFile cmpResult = getTestResourceFile(path);
        assertTrue(cmpResult.exists());
        XmlTestUtils.compare(cmpResult.readContent().toString(),
                tmpFile.readContent().toString());
    }

    public static IFile getTestResourceFile(final String path) {
        final URL resource = BsxDataStorage.class.getClassLoader().getResource(path);
        if (resource != null) {
            try {
                return new IFile(resource.toURI());
            } catch (URISyntaxException e) {
                ExcUtils.suppress(e);
            }
        }
        return new IFile("src/test/resources").secureExpandPathDown(path);
    }

    private static boolean hasLength(CharSequence str) {
        return (str != null && str.length() > 0);
    }

}
