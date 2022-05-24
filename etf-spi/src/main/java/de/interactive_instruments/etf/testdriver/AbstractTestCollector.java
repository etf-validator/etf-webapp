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

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public abstract class AbstractTestCollector implements BasicTestResultCollector {

    protected AbstractTestCollector subCollector = null;
    protected final static Logger logger = LoggerFactory.getLogger(TestResultCollector.class);

    private int levels[] = new int[]{6, 6, 6, 6, 6, 6, 6};
    private int level;

    protected String doStartTestTask(final String resultedFrom, final long startTimestamp)
            throws IllegalArgumentException, IllegalStateException {
        throw new UnsupportedOperationException(
                "Operation not supported by collector, illegal delegation from parent collector");
    }

    @Override
    public final String startTestTask(final String resultedFrom, final long startTimestamp)
            throws IllegalArgumentException, IllegalStateException {
        if (subCollector == null) {
            level = 1;
            levels[level] = 6;
        }
        return doStartTestTask(resultedFrom, startTimestamp);
    }

    protected String doStartTestModule(final String resultedFrom, final long startTimestamp)
            throws IllegalArgumentException, IllegalStateException {
        throw new UnsupportedOperationException(
                "Operation not supported by collector, illegal delegation from parent collector");
    }

    @Override
    public final String startTestModule(final String resultedFrom, final long startTimestamp)
            throws IllegalArgumentException, IllegalStateException {
        if (subCollector == null) {
            level = 2;
            levels[level] = 6;
        }
        return doStartTestModule(resultedFrom, startTimestamp);
    }

    protected String doStartTestCaseResult(final String resultedFrom, final long startTimestamp) throws Exception {
        throw new UnsupportedOperationException(
                "Operation not supported by collector, illegal delegation from parent collector");
    }

    protected final String startTestCaseResult(final String resultedFrom, final long startTimestamp) throws Exception {
        if (subCollector == null) {
            level = 3;
            levels[level] = 6;
        }
        return doStartTestCaseResult(resultedFrom, startTimestamp);
    }

    protected String endTestCaseResult(final String testModelItemId, final int status, final long stopTimestamp)
            throws Exception {
        throw new UnsupportedOperationException(
                "Operation not supported by collector, illegal delegation from parent collector");
    }

    abstract protected String doStartTestStepResult(final String resultedFrom, final long startTimestamp) throws Exception;

    protected final String startTestStepResult(final String resultedFrom, final long startTimestamp) throws Exception {
        if (subCollector == null) {
            level = 4;
            levels[level] = 6;
        }
        return doStartTestStepResult(resultedFrom, startTimestamp);
    }

    abstract protected String endTestStepResult(final String testModelItemId, final int status, final long stopTimestamp)
            throws Exception;

    abstract protected String doStartTestAssertionResult(final String resultedFrom, final long startTimestamp) throws Exception;

    protected final String startTestAssertionResult(final String resultedFrom, final long startTimestamp) throws Exception {
        if (subCollector == null) {
            level = 5;
            levels[level] = 6;
        }
        return doStartTestAssertionResult(resultedFrom, startTimestamp);
    }

    abstract protected String endTestAssertionResult(final String testModelItemId, final int status, final long stopTimestamp)
            throws Exception;

    @Override
    final public String end(final String testModelItemId, final int status, final long stopTimestamp)
            throws IllegalArgumentException, IllegalStateException {
        final boolean noSubCollector = subCollector == null;
        final String id = doEnd(testModelItemId, status, stopTimestamp);
        if (noSubCollector) {
            setStatusAndParentStatus(status);
            --level;
        }
        return id;
    }

    @Override
    final public String end(final String testModelItemId, final long stopTimestamp)
            throws IllegalArgumentException, IllegalStateException {
        final int lastStatus = getContextStatus();
        final boolean noSubCollector = subCollector == null;
        final String id = doEnd(testModelItemId, lastStatus, stopTimestamp);
        if (noSubCollector) {
            setStatusAndParentStatus(lastStatus);
            --level;
        }
        return id;
    }

    abstract protected String doEnd(final String testModelItemId, final int status, final long stopTimestamp)
            throws IllegalArgumentException, IllegalStateException;

    abstract protected void startInvokedTests();

    abstract protected void endInvokedTests();

    abstract protected void startTestAssertionResults();

    abstract protected void endTestAssertionResults();

    abstract protected void doAddMessage(final String s);

    abstract protected void doAddMessage(final String s, final Map<String, String> map);

    abstract protected void doAddMessage(final String s, final String... strings);

    @Override
    public final void addMessage(final String s) {
        if (subCollector != null) {
            subCollector.doAddMessage(s);
        } else {
            doAddMessage(s);
        }
    }

    @Override
    public final void addMessage(final String s, final Map<String, String> map) {
        if (subCollector != null) {
            subCollector.doAddMessage(s, map);
        } else {
            doAddMessage(s, map);
        }
    }

    @Override
    public final void addMessage(final String s, final String... strings) {
        if (subCollector != null) {
            subCollector.doAddMessage(s, strings);
        } else {
            doAddMessage(s, strings);
        }
    }

    protected void notifyError() {
        logger.error("Releasing collector due to an error");
        release();
    }

    abstract void prepareSubCollectorRelease();

    /**
     * Called by a SubCollector
     */
    final void releaseSubCollector(final int status) {
        prepareSubCollectorRelease();
        mergeResultFromCollector(subCollector);
        subCollector = null;
        setStatusAndParentStatus(status);
    }

    final protected int getContextStatus() {
        if (subCollector != null) {
            return subCollector.getContextStatus();
        }
        return this.levels[level];
    }

    private void setStatusAndParentStatus(final int newStatus) {
        this.levels[level] = newStatus;
        final int s = 10 * this.levels[level - 1] + newStatus;
        switch (s) {
        case 00:
        case 03:
            // Ignore PASSED 0 - NOT_APPLICABLE
        case 10:
        case 11:
        case 12:
        case 13:
        case 14:
        case 15:
        case 16:
        case 17:
            // Ignore FAILED 1 - *
        case 20:
        case 22:
        case 23:
        case 24:
        case 25:
        case 26:
        case 27:
            // Ignore SKIPPED 2 - * (except FAILED)
        case 33:
        case 34:
        case 35:
        case 36:
        case 37:
            // Ignore NOT_APPLICABLE 3 - * (except PASSED, FAILED, SKIPPED)
        case 40:
        case 44:
        case 46:
        case 47:
            // Ignore INFO 4 - * (except FAILED, SKIPPED, NOT_APPLICABLE, WARNING)
        case 50:
        case 54:
        case 55:
        case 56:
        case 57:
            // Ignore WARNING 5 - * (except FAILED, SKIPPED, NOT_APPLICABLE)
        case 70:
            break;
        default:
            this.levels[level - 1] = newStatus;
            break;
        }
    }

    abstract protected AbstractTestCollector createCalledTestCaseResultCollector(final AbstractTestCollector parentCollector,
            final String testModelItemId, final long startTimestamp);

    abstract protected AbstractTestCollector createCalledTestStepResultCollector(final AbstractTestCollector parentCollector,
            final String testModelItemId, final long startTimestamp);

    abstract protected void mergeResultFromCollector(final AbstractTestCollector collector);

    abstract protected String currentResultItemId();

}
