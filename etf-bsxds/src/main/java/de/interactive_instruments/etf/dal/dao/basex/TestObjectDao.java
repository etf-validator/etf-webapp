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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Objects;

import org.basex.core.cmd.XQuery;

import de.interactive_instruments.IFile;
import de.interactive_instruments.SUtils;
import de.interactive_instruments.etf.dal.dto.capabilities.TestObjectDto;
import de.interactive_instruments.etf.model.EID;
import de.interactive_instruments.etf.model.EidFactory;
import de.interactive_instruments.exceptions.ExcUtils;
import de.interactive_instruments.exceptions.ObjectWithIdNotFoundException;
import de.interactive_instruments.exceptions.StorageException;
import de.interactive_instruments.exceptions.config.MissingPropertyException;

/**
 * Test Object Data Access Object
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
final class TestObjectDao extends AbstractBsxWriteDao<TestObjectDto> {

    public TestObjectDao(final BsxDsCtx ctx) throws IOException {
        super(new TQuery(DataBaseType.REUSABLE_TEST_OBJECTS,
                "(db:list()[starts-with(., 'r-')] ! db:open(.), db:open('o'))", "TestObject"), ctx,
                DsResultSet::getTestObjects);
    }

    @Override
    public Class<TestObjectDto> getDtoType() {
        return TestObjectDto.class;
    }

    @Override
    protected void updateProperty(final Collection<EID> ids, final String propertyXpath,
            final String newValue) {
        super.updateProperty(ids, propertyXpath, newValue);
    }

    @Override
    protected String dataBaseNameForType(final TestObjectDto testObjectDto) {
        if ("true".equals(testObjectDto.properties().getPropertyOrDefault("temporary", "false"))) {
            final String testRunId = testObjectDto.getReference();
            if (!SUtils.isNullOrEmpty(testRunId)) {
                final EID id = EidFactory.getDefault().createUUID(testRunId);
                return "r-" + id.toUuid().toString();
            }
            throw new IllegalStateException("testRunId not set in Test Object " + testObjectDto.toString());
        }
        return this.tQuery.defaultDatabaseName();
    }

    @Override
    protected String dataBaseNameFor(final EID eid) {
        try {
            final XQuery dbNameQuery = new XQuery(
                    "declare namespace etf = \"http://www.interactive-instruments.de/etf/2.0\";"
                            // test run db names
                            + " let $rn := db:list()[starts-with(., \"r-\")]"
                            // temp test objects
                            + " let $tT := ($rn ! db:open(.)/etf:TestObject)"
                            + " let $n := (db:open('o')/etf:TestObject, $tT)[@id = 'EID" + eid.getId() + "']"
                            + " return if ($n) then db:name($n)");
            final String dbName = dbNameQuery.execute(ctx.getBsxCtx());
            if ("".equals(dbName)) {
                return null;
            }
            return dbName;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to get Test Run ID", e);
        }
    }

    @Override
    protected boolean disableable(final EID eid) {
        try {
            deleteTestDataDir(eid);
            // Temporary test objects should not be disabled
            if (!"true".equals(getById(eid).getDto()
                    .properties().getPropertyOrDefault("temporary", "false"))) {
                return true;
            }
        } catch (BsxPreparedDtoException | StorageException | ObjectWithIdNotFoundException e) {
            ExcUtils.suppress(e);
        }
        return false;
    }

    private void deleteTestDataDir(final EID eid) {
        try {
            final File[] dir = configProperties.getPropertyAsFile("etf.testdata.dir")
                    .listFiles(f -> f.isDirectory() && Objects.equals(f.getName(), eid.getId()));
            if (dir != null && dir.length == 1) {
                new IFile(dir[0]).deleteDirectory();
            }
        } catch (MissingPropertyException | IOException e) {
            ExcUtils.suppress(e);
        }
    }

    @Override
    protected void doCleanAfterDelete(final EID eid) {
        deleteTestDataDir(eid);
    }

}
