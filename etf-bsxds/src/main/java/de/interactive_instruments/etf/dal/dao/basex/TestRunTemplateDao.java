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

import java.util.Collection;

import de.interactive_instruments.etf.dal.dto.capabilities.TestRunTemplateDto;
import de.interactive_instruments.etf.model.EID;
import de.interactive_instruments.exceptions.ObjectWithIdNotFoundException;
import de.interactive_instruments.exceptions.StorageException;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
final class TestRunTemplateDao extends AbstractBsxStreamWriteDao<TestRunTemplateDto> {

    protected TestRunTemplateDao(final BsxDsCtx ctx) throws StorageException {
        super(new TQuery(DataBaseType.BASE, "TestRunTemplate"), ctx,
                DsResultSet::getTestRunTemplates);
    }

    @Override
    protected void doCleanAfterDelete(final EID eid) {}

    @Override
    protected void doDeleteOrDisable(final Collection<EID> eids, final boolean clean)
            throws StorageException, ObjectWithIdNotFoundException {
        for (final EID eid : eids) {
            // ID checks are done in doDelete()
            doDelete(eid, clean);
        }
    }

    @Override
    public Class<TestRunTemplateDto> getDtoType() {
        return TestRunTemplateDto.class;
    }

}
