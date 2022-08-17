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
package de.interactive_instruments.etf.testdriver;

import java.util.List;

import de.interactive_instruments.etf.dal.dto.run.TestRunDto;
import de.interactive_instruments.exceptions.InitializationException;
import de.interactive_instruments.exceptions.InvalidStateTransitionException;
import de.interactive_instruments.exceptions.config.ConfigurationException;

/**
 * Test Run
 *
 * A Test Run bundles multiple Test Tasks (@see TestTask). A Test Run object can be started by submitting it to the
 * TaskPoolRegistry (see {@link TaskPoolRegistry#submitTask(Task)}).
 *
 * <img src="TestRun.svg" alt="Class UML">
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public interface TestRun extends Task<TestRunDto> {

    String getLabel();

    List<TestTask> getTestTasks();

    void start() throws ConfigurationException, InvalidStateTransitionException, InitializationException, Exception;

    void addTestRunEventListener(final TestRunEventListener testRunEventListener);

    TaskProgress getProgress();
}
