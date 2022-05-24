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
package de.interactive_instruments.etf.testdriver;

import static de.interactive_instruments.etf.test.TestDtos.TTR_DTO_1;

import de.interactive_instruments.etf.dal.dto.run.TestTaskDto;
import de.interactive_instruments.exceptions.InitializationException;
import de.interactive_instruments.exceptions.InvalidStateTransitionException;
import de.interactive_instruments.exceptions.config.ConfigurationException;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public class UnitTestTestTask extends AbstractTestTask {

    private final int failInStage;

    protected UnitTestTestTask(
            final int failInStage,
            final TestTaskDto testTaskDto) {
        super(testTaskDto, new UnitTestTestTaskProgress(), UnitTestTestTask.class.getClassLoader());
        this.failInStage = failInStage;
    }

    @Override
    protected void doRun() throws Exception {
        this.progress.advance();
        if (failInStage > 1) {
            throw new IllegalStateException("FAIL");
        }
        this.progress.advance();
    }

    @Override
    protected void doInit() throws ConfigurationException, InitializationException {
        if (failInStage > 2) {
            throw new InitializationException("FAIL");
        }
        testTaskDto.setTestTaskResult(TTR_DTO_1);
    }

    @Override
    protected void doRelease() {
        if (failInStage > 3) {
            throw new IllegalStateException("FAIL");
        }

    }

    @Override
    protected void doCancel() throws InvalidStateTransitionException {
        if (failInStage > 4) {
            throw new InvalidStateTransitionException("FAIL");
        }
    }
}
