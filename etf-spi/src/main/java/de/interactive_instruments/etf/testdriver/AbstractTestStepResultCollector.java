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

import static de.interactive_instruments.etf.testdriver.AbstractTestStepResultCollector.TestStepResultCollectorState.*;

/**
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public abstract class AbstractTestStepResultCollector extends AbstractTestCollector {

    protected enum TestStepResultCollectorState {
        WRITING_TEST_STEP_RESULT,

        WRITING_CALLED_TEST_CASE_RESULT,

        CALLED_TEST_CASE_RESULT_FINISHED,

        WRITING_CALLED_TEST_STEP_RESULT,

        CALLED_TEST_STEP_RESULT_FINISHED,

        WRITING_TEST_ASSERTION_RESULT,

        TEST_ASSERTION_RESULT_FINISHED,

        TEST_STEP_RESULT_FINISHED,

        RELEASED,
    }

    private TestStepResultCollectorState currentState = TestStepResultCollectorState.WRITING_TEST_STEP_RESULT;

    protected final String testStepId;
    private final AbstractTestCollector parentCollector;

    protected AbstractTestStepResultCollector(final AbstractTestCollector parentCollector, final String testStepId) {
        this.testStepId = testStepId;
        this.parentCollector = parentCollector;
    }

    private void setState(final TestStepResultCollectorState newState) {
        logger.trace("Switching from state {} to state {} ", this.currentState, newState);
        this.currentState = newState;
    }

    @Override
    final public String startTestCase(final String testModelItemId, final long startTimestamp)
            throws IllegalArgumentException, IllegalStateException {
        try {
            switch (currentState) {
            case WRITING_TEST_STEP_RESULT:
                startInvokedTests();
            case CALLED_TEST_CASE_RESULT_FINISHED:
            case CALLED_TEST_STEP_RESULT_FINISHED:
                setState(WRITING_CALLED_TEST_CASE_RESULT);
                subCollector = createCalledTestCaseResultCollector(this, testModelItemId, startTimestamp);
                return subCollector.currentResultItemId();
            case WRITING_CALLED_TEST_CASE_RESULT:
            case WRITING_CALLED_TEST_STEP_RESULT:
                return subCollector.startTestCase(testModelItemId, startTimestamp);
            }
            throw new IllegalStateException(
                    "Illegal state transition: cannot start writing Test Case result when in " + currentState + " state");
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    final public String startTestStep(final String testModelItemId, final long startTimestamp)
            throws IllegalArgumentException, IllegalStateException {
        try {
            switch (currentState) {
            case TEST_STEP_RESULT_FINISHED:
                setState(WRITING_TEST_STEP_RESULT);
                return startTestStepResult(testModelItemId, startTimestamp);
            case WRITING_TEST_STEP_RESULT:
                startInvokedTests();
            case CALLED_TEST_CASE_RESULT_FINISHED:
            case CALLED_TEST_STEP_RESULT_FINISHED:
                setState(WRITING_CALLED_TEST_STEP_RESULT);
                subCollector = createCalledTestStepResultCollector(this, testModelItemId, startTimestamp);
                return subCollector.currentResultItemId();
            case WRITING_CALLED_TEST_CASE_RESULT:
            case WRITING_CALLED_TEST_STEP_RESULT:
                return subCollector.startTestStep(testModelItemId, startTimestamp);
            }
            throw new IllegalStateException(
                    "Illegal state transition: cannot start writing Test Step result when in " + currentState + " state");
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    final public String startTestAssertion(final String testModelItemId, final long startTimestamp)
            throws IllegalArgumentException, IllegalStateException {
        try {
            switch (currentState) {
            case CALLED_TEST_CASE_RESULT_FINISHED:
            case CALLED_TEST_STEP_RESULT_FINISHED:
                endInvokedTests();
            case WRITING_TEST_STEP_RESULT:
                startTestAssertionResults();
            case TEST_ASSERTION_RESULT_FINISHED:
                setState(WRITING_TEST_ASSERTION_RESULT);
                return startTestAssertionResult(testModelItemId, startTimestamp);
            case WRITING_CALLED_TEST_CASE_RESULT:
            case WRITING_CALLED_TEST_STEP_RESULT:
                return subCollector.startTestAssertion(testModelItemId, startTimestamp);
            }
            throw new IllegalStateException(
                    "Illegal state transition: cannot start writing Test Assertion result when in " + currentState + " state");
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    final public String doEnd(final String testModelItemId, final int status, final long stopTimestamp)
            throws IllegalArgumentException, IllegalStateException {
        try {
            switch (currentState) {
            case CALLED_TEST_CASE_RESULT_FINISHED:
            case CALLED_TEST_STEP_RESULT_FINISHED:
                endInvokedTests();
                setState(TEST_STEP_RESULT_FINISHED);
                return endTestStepResult(testModelItemId, status, stopTimestamp);
            case TEST_ASSERTION_RESULT_FINISHED:
                endTestAssertionResults();
            case WRITING_TEST_STEP_RESULT:
                if (testModelItemId.equals(testStepId)) {
                    final String id = endTestStepResult(testModelItemId, status, stopTimestamp);
                    parentCollector.releaseSubCollector(status);
                    setState(RELEASED);
                    return id;
                }
                setState(TEST_STEP_RESULT_FINISHED);
                return endTestStepResult(testModelItemId, status, stopTimestamp);
            case WRITING_TEST_ASSERTION_RESULT:
                setState(TEST_ASSERTION_RESULT_FINISHED);
                return endTestAssertionResult(testModelItemId, status, stopTimestamp);
            case WRITING_CALLED_TEST_CASE_RESULT:
            case WRITING_CALLED_TEST_STEP_RESULT:
                return subCollector.end(testModelItemId, status, stopTimestamp);
            }
            throw new IllegalStateException(
                    "Illegal state transition: cannot end result structure when in " + currentState + " state");
        } catch (final Exception e) {
            logger.error("An internal error occurred finishing result {} ", testModelItemId, e);
            notifyError();
            throw new IllegalStateException(e);
        }
    }

    /**
     * Called by a SubCollector
     */
    public void prepareSubCollectorRelease() {
        if (currentState == WRITING_CALLED_TEST_CASE_RESULT) {
            setState(CALLED_TEST_CASE_RESULT_FINISHED);
        } else if (currentState == WRITING_CALLED_TEST_STEP_RESULT) {
            setState(CALLED_TEST_STEP_RESULT_FINISHED);
        } else {
            throw new IllegalStateException(
                    "Illegal state transition: cannot release sub collector when in " + currentState + " state");
        }
    }

    @Override
    public int currentModelType() {
        switch (currentState) {
        case TEST_STEP_RESULT_FINISHED:
            return 3;
        case WRITING_TEST_STEP_RESULT:
        case CALLED_TEST_CASE_RESULT_FINISHED:
        case CALLED_TEST_STEP_RESULT_FINISHED:
        case TEST_ASSERTION_RESULT_FINISHED:
            return 4;
        case WRITING_TEST_ASSERTION_RESULT:
            return 5;
        case WRITING_CALLED_TEST_CASE_RESULT:
            return subCollector.currentModelType();
        case WRITING_CALLED_TEST_STEP_RESULT:
            return subCollector.currentModelType();
        }
        return -1;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("TestResultCollector {");
        sb.append("currentState=").append(currentState).append(", ");
        sb.append("aggregatedSubStatus=").append(getContextStatus()).append(", ");
        sb.append("subCollector=").append(subCollector != null ? subCollector.toString() : "none");
        sb.append('}');
        return sb.toString();
    }
}
