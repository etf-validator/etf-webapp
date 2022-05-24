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

import java.time.Instant;
import java.util.Date;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public class AbstractTestTaskProgress implements TaskProgress {

    private STATE currentState = STATE.CREATED;
    private STATE oldState = null;
    protected Instant startInstant;
    protected Instant stopInstant;
    private long stepsCompleted = 0;
    private long maxSteps = -1;
    private TestRunLogReader logReader;

    void setLogReader(TestRunLogReader logReader) {
        this.logReader = logReader;
    }

    protected void initMaxSteps(final long maxSteps) {
        if (this.maxSteps != -1) {
            throw new IllegalArgumentException("Max steps already set");
        }
        if (maxSteps <= 0) {
            throw new IllegalArgumentException("Invalid max value: " + maxSteps);
        }
        this.maxSteps = maxSteps;
    }

    protected void advance() {
        stepsCompleted++;
        if (stepsCompleted >= maxSteps) {
            maxSteps = stepsCompleted + 1;
        }
    }

    void setState(final STATE currentState) {
        this.oldState = this.currentState;
        this.currentState = currentState;
    }

    public STATE getCurrentState() {
        return currentState;
    }

    public STATE getOldState() {
        return oldState;
    }

    @Override
    public long getMaxSteps() {
        return maxSteps;
    }

    @Override
    public long getCurrentStepsCompleted() {
        return stepsCompleted;
    }

    @Override
    public Date getStartTimestamp() {
        return Date.from(startInstant);
    }

    @Override
    public TestRunLogReader getLogReader() {
        return logReader;
    }

    @Override
    public TaskState.STATE getState() {
        return this.currentState;
    }
}
