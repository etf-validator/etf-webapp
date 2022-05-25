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
import java.util.Collections;
import java.util.Objects;

import javax.xml.transform.TransformerConfigurationException;

import org.basex.core.BaseXException;
import org.basex.core.cmd.DropDB;

import de.interactive_instruments.IFile;
import de.interactive_instruments.SUtils;
import de.interactive_instruments.etf.EtfConstants;
import de.interactive_instruments.etf.dal.dto.capabilities.TestObjectDto;
import de.interactive_instruments.etf.dal.dto.run.TestRunDto;
import de.interactive_instruments.etf.model.EID;
import de.interactive_instruments.exceptions.InitializationException;
import de.interactive_instruments.exceptions.InvalidStateTransitionException;
import de.interactive_instruments.exceptions.ObjectWithIdNotFoundException;
import de.interactive_instruments.exceptions.StorageException;
import de.interactive_instruments.exceptions.config.ConfigurationException;
import de.interactive_instruments.exceptions.config.MissingPropertyException;
import de.interactive_instruments.properties.ConfigProperties;

/**
 * Test Run Data Access Object
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
final class TestRunDao extends AbstractBsxWriteDao<TestRunDto> {

    protected TestRunDao(final BsxDsCtx ctx) throws StorageException {
        super(new TQuery(DataBaseType.TEST_RUNS, "TestRun"), ctx, DsResultSet::getTestRuns);
        configProperties = new ConfigProperties("etf.webapp.base.url");
    }

    @Override
    protected void doInit() throws ConfigurationException, InitializationException, InvalidStateTransitionException {
        try {
            final XsltOutputTransformer reportTransformer = DsUtils.loadReportTransformer(this);
            outputFormatIdMap.put(reportTransformer.getId(), reportTransformer);
            final XsltOutputTransformer csvTransformer = new XsltOutputTransformer(
                    this, "DsResult2Csv", "text/csv", "xslt/DsResult2Csv.xsl");
            csvTransformer.getConfigurationProperties().setPropertiesFrom(getConfigurationProperties(), true);
            csvTransformer.init();
            outputFormatIdMap.put(csvTransformer.getId(), csvTransformer);
        } catch (IOException | TransformerConfigurationException e) {
            throw new InitializationException(e);
        }
    }

    @Override
    protected void doCleanBeforeDelete(final EID eid) {
        try {
            try {
                final String id = eid.getId();
                final IFile dir = configProperties
                        .getPropertyAsFile(EtfConstants.ETF_ATTACHMENT_DIR).secureExpandPathDown(id);
                if (!SUtils.isNullOrEmpty(id) && dir.exists()) {
                    dir.deleteDirectory();
                }
            } catch (MissingPropertyException | IOException e) {
                throw new IllegalStateException(e);
            }
            final TestRunDto testRunDto = getById(eid).getDto();
            ctx.deleteFiles(testRunDto.getTestTaskResults());
            try {
                final TestObjectDto testObjectDto = testRunDto.getTestTasks().get(0).getTestObject();
                if ("true".equals(testObjectDto.properties().getPropertyOrDefault("temporary", "false"))) {
                    ctx.deleteFiles(Collections.singleton(testObjectDto));
                    final File[] dir = configProperties.getPropertyAsFile("etf.testdata.dir")
                            .listFiles(f -> f.isDirectory() && Objects.equals(f.getName(), testObjectDto.getId().getId()));
                    if (dir != null && dir.length == 1) {
                        new IFile(dir[0]).deleteDirectory();
                    }
                }
            } catch (Exception e) {
                ctx.getLogger().warn("Ignoring error during clean ", e);
            }
        } catch (StorageException | ObjectWithIdNotFoundException e) {
            ctx.getLogger().warn("Ignoring error during clean ", e);
        }
    }

    @Override
    protected String dataBaseNameForType(final TestRunDto testRunDto) {
        return "r-" + testRunDto.getId().getId();
    }

    @Override
    protected String dataBaseNameFor(final EID eid) {
        return "r-" + eid.getId();
    }

    @Override
    public Class<TestRunDto> getDtoType() {
        return TestRunDto.class;
    }

    @Override
    public boolean isDisabled(final EID eid) {
        return false;
    }

    @Override
    protected void doCleanAfterDelete(final EID eid) throws BaseXException {
        new DropDB("r-" + eid.toUuid().toString()).execute(ctx.getBsxCtx());
    }
}
