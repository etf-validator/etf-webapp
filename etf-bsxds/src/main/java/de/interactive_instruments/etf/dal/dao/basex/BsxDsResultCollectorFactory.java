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

import de.interactive_instruments.IFile;
import de.interactive_instruments.etf.dal.dao.DataStorageRegistry;
import de.interactive_instruments.etf.dal.dto.run.TestTaskDto;
import de.interactive_instruments.etf.testdriver.TestResultCollector;
import de.interactive_instruments.etf.testdriver.TestResultCollectorFactory;
import de.interactive_instruments.etf.testdriver.TestRunLogger;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public class BsxDsResultCollectorFactory implements TestResultCollectorFactory {

    private final BsxDataStorage dataStorage = (BsxDataStorage) DataStorageRegistry.instance()
            .get(BsxDataStorage.class.getName());

    @Override
    public TestResultCollector createTestResultCollector(final TestRunLogger testRunLogger, final TestTaskDto testTaskDto) {
        // ATTACHMENT_DIR / TESTRUN_ID / TESTTASK_ID
        final IFile testTaskAttachmentDir = dataStorage.getAttachmentDir()
                .secureExpandPathDown(testTaskDto.getParent().getId().getId())
                .secureExpandPathDown(testTaskDto.getId().getId());
        return new BsxDsResultCollector(dataStorage,
                testRunLogger,
                testTaskAttachmentDir.secureExpandPathDown("TmpTestTaskResult-EID" + testTaskDto.getId().getId() + ".xml"),
                testTaskAttachmentDir,
                testTaskDto);
    }
}
