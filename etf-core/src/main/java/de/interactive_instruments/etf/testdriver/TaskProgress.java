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

import java.util.Date;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public interface TaskProgress extends TaskState {

    /**
     * Number of known steps that have to be processed
     *
     * @return remaining steps
     */
    long getMaxSteps();

    /**
     * Number of completed steps
     *
     * @return completed steps
     */
    long getCurrentStepsCompleted();

    /**
     * Number of completed steps in percent
     *
     * @return progress as double
     */
    default double getPercentStepsCompleted() {
        if (getState().equals(STATE.COMPLETED)) {
            return 1;
        }
        return ((double) getCurrentStepsCompleted() / getMaxSteps()) * 100;
    }

    /**
     * Returns the date of the start of the task
     *
     * @return stat timestamp
     */
    Date getStartTimestamp();

    /**
     * Read access to the current log path
     *
     * @return Log Reader object
     */
    TestRunLogReader getLogReader();
}
