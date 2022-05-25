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

import java.util.List;

import javax.xml.validation.Schema;

import org.basex.core.BaseXException;
import org.slf4j.Logger;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import de.interactive_instruments.etf.dal.dto.Dto;
import de.interactive_instruments.etf.dal.dto.ModelItemTreeNode;
import de.interactive_instruments.etf.dal.dto.test.ExecutableTestSuiteDto;
import de.interactive_instruments.etf.dal.dto.test.TestModelItemDto;
import de.interactive_instruments.etf.model.EID;
import de.interactive_instruments.etf.model.EidFactory;
import de.interactive_instruments.exceptions.StorageException;

/**
 * Executable Test Suite Data Access Object
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
final class ExecutableTestSuiteDao extends AbstractBsxStreamWriteDao<ExecutableTestSuiteDto> {

    private final Schema schema;

    protected ExecutableTestSuiteDao(final BsxDsCtx ctx) throws StorageException {
        super(new TQuery(DataBaseType.BASE, "ExecutableTestSuite"), ctx,
                DsResultSet::getExecutableTestSuites);
        schema = ((BsxDataStorage) ctx).getSchema();
    }

    @Override
    protected void doCleanAfterDelete(final EID eid) throws BaseXException {}

    @Override
    public Class<ExecutableTestSuiteDto> getDtoType() {
        return ExecutableTestSuiteDto.class;
    }

    private static class ValidationErrorHandler implements ErrorHandler {

        private final Logger logger;

        private ValidationErrorHandler(final Logger logger) {
            this.logger = logger;
        }

        @Override
        public void warning(final SAXParseException exception) throws SAXException {

        }

        @Override
        public void error(final SAXParseException exception) throws SAXException {
            if (!exception.getMessage().startsWith("cvc-id")) {
                throw new SAXException(exception);
            }
        }

        @Override
        public void fatalError(final SAXParseException exception) throws SAXException {
            if (!exception.getMessage().startsWith("cvc-id")) {
                throw new SAXException(exception);
            }
        }
    }

    private void updateChildrenIds(final List<? extends TestModelItemDto> children, int maxDepth) {
        if (children != null && maxDepth > 0) {
            for (final ModelItemTreeNode child : children) {
                if (child instanceof Dto) {
                    ((Dto) child).setId(EidFactory.getDefault().createRandomId());
                }
                updateChildrenIds(child.getChildren(), --maxDepth);
            }
        }
    }

    @Override
    protected void doUpdateProperties(final ExecutableTestSuiteDto executableTestSuiteDto) {
        updateChildrenIds(executableTestSuiteDto.getChildren(), 8);
    }
}
