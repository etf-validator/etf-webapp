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

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.LoggerFactory;

import de.interactive_instruments.IFile;
import de.interactive_instruments.LogUtils;
import de.interactive_instruments.SUtils;
import de.interactive_instruments.TimeUtils;
import de.interactive_instruments.etf.dal.dto.run.TestRunDto;
import de.interactive_instruments.etf.model.EID;
import de.interactive_instruments.exceptions.ExcUtils;
import de.interactive_instruments.exceptions.InitializationException;
import de.interactive_instruments.exceptions.InvalidStateTransitionException;
import de.interactive_instruments.exceptions.config.ConfigurationException;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
final class DefaultTestRun implements TestRun, TaskProgress {

    private TestRunDto testRunDto;
    private final IFile testRunAttachmentDir;
    private STATE currentState = STATE.CREATED;
    private STATE oldState;
    private TestRunLogger testRunLogger;
    private List<TestTask> testTasks;
    private List<TestRunEventListener> eventListeners = new ArrayList<>(3);
    private final static long waitTime = 1500;
    private int currentRunIndex = 0;
    private boolean initialized = false;
    private Instant startInstant;
    private Instant stopInstant;
    private Future<TestRunDto> future;
    private static final ClassLoader loader = Thread.currentThread().getContextClassLoader();

    public DefaultTestRun(final TestRunDto testRunDto, final TestRunLogger testRunLogger, final IFile testRunAttachmentDir) {
        this.testRunDto = testRunDto;
        this.testRunAttachmentDir = testRunAttachmentDir;
        this.testRunLogger = testRunLogger;
        testRunDto.setLogPath(testRunLogger.getLogFile().getAbsolutePath());
    }

    @Override
    public EID getId() {
        return testRunDto.getId();
    }

    @Override
    public void setFuture(final Future<TestRunDto> future) throws IllegalStateException {
        if (this.future != null) {
            throw new IllegalStateException(
                    "Attempt to override the call-back object");
        }
        this.future = future;
    }

    @Override
    public TestRunDto waitForResult() throws InterruptedException, ExecutionException {
        return future.get();
    }

    @Override
    public TestRunDto getResult() {
        return testRunDto;
    }

    @Override
    public String getLabel() {
        return testRunDto.getLabel();
    }

    @Override
    public List<TestTask> getTestTasks() {
        return testTasks;
    }

    void setTestTasks(final List<TestTask> testTasks) {
        this.testTasks = testTasks;
    }

    @Override
    public void start() throws Exception {
        fireRunning();
        if (!SUtils.isNullOrEmpty(this.testRunDto.getDefaultLang())) {
            new ThreadLocal<Locale>().set(new Locale(this.testRunDto.getDefaultLang()));
        }
        for (; currentRunIndex < testTasks.size(); currentRunIndex++) {
            fireTestTaskInitializing(testTasks.get(currentRunIndex));
            testTasks.get(currentRunIndex).init();
            fireTestTaskRunning(testTasks.get(currentRunIndex));
            testTasks.get(currentRunIndex).run();
            fireTestTaskCompleted(testTasks.get(currentRunIndex));
            testTasks.get(currentRunIndex).release();
        }
        fireCompleted();
    }

    private void fireTestTaskCompleted(final TestTask testTask) {
        for (final TestRunEventListener eventListener : eventListeners) {
            eventListener.taskStateChangedEvent(testTask, STATE.COMPLETED, STATE.RUNNING);
        }

    }

    private void fireTestTaskRunning(final TestTask testTask) {
        for (final TestRunEventListener eventListener : eventListeners) {
            eventListener.taskStateChangedEvent(testTask, STATE.RUNNING, STATE.INITIALIZED);
        }
    }

    private void fireTestTaskInitializing(final TestTask testTask) {
        for (final TestRunEventListener eventListener : eventListeners) {
            eventListener.taskStateChangedEvent(testTask, STATE.INITIALIZING, STATE.CREATED);
        }
    }

    @Override
    public final TestRunDto call() throws Exception {
        Thread.currentThread().setName("TestRun." + getId());
        try {
            testRunLogger.info("Starting TestRun." + getId() + " at " +
                    TimeUtils.dateToIsoString(new Date(System.currentTimeMillis() + waitTime)));
            Thread.sleep(waitTime);
            start();
            return testRunDto.createCopy();
        } catch (Exception e) {
            if (!currentState.isCanceling()) {
                testRunLogger.info("Handling unexpected exception in TestRunTask.{} :", getId(), e);
                handleException(e);
            } else {
                fireFailed();
                testRunLogger.info("Ignoring exception thrown by TestRunTask.{} during cancellation phase.", getId());
            }
            throw e;
        } finally {
            try {
                testRunLogger.info("Duration: {}", TimeUtils.milisAsHrMins(getDuration().toMillis()));
            } catch (final Exception e) {
                testRunLogger.info("Internal error", e);
            }
            testRunLogger.info("TestRun finished");
            release();
        }
    }

    public final Duration getDuration() {
        if (stopInstant != null) {
            return Duration.between(startInstant, stopInstant);
        }
        return Duration.between(startInstant, Instant.now());
    }

    protected void doCancel() throws InvalidStateTransitionException {
        for (int i = currentRunIndex; i < testTasks.size(); i++) {
            try {
                testTasks.get(i).cancel();
            } catch (InvalidStateTransitionException e) {
                ExcUtils.suppress(e);
            }
        }
    }

    protected void checkCancelStatus() throws InterruptedException {
        if (this.currentState == TaskState.STATE.CANCELING ||
                Thread.currentThread().isInterrupted()) {
            try {
                cancel();
            } catch (InvalidStateTransitionException e) {
                testRunLogger.error("Unable to cancel task: " + e.getMessage());
            }
            throw new InterruptedException();
        }
    }

    @Override
    public final void cancel() throws InvalidStateTransitionException {
        if (this.getState() == STATE.CANCELING || this.getState() == STATE.CANCELED) {
            testRunLogger.info("Canceling TestRun." + getId());
            fireCanceling();
            doCancel();
            fireCanceled();
            release();
            testRunLogger.info("TestRun." + getId() + " canceled");
        }
    }

    protected final void handleException(final Throwable e) {
        testRunLogger.error("Exception: ", e);
        fireFailed();
    }

    @Override
    public void addTestRunEventListener(final TestRunEventListener testRunEventListener) {
        this.eventListeners.add(testRunEventListener);
    }

    @Override
    public TaskProgress getProgress() {
        return this;
    }

    @Override
    public void init() throws ConfigurationException, InitializationException, InvalidStateTransitionException {
        fireInitializing();
        /* // TODO for (final TestTask testTask : testTasks) { testTask.init(); } */
        fireInitialized();
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void release() {
        try {
            fireFinalizing();
        } catch (final InvalidStateTransitionException e) {
            ExcUtils.suppress(e);
        }
        for (int i = currentRunIndex; i < testTasks.size(); i++) {
            try {
                if (testTasks.get(i).getState().isRunningOrInitializing()) {
                    testTasks.get(i).cancel();
                }
            } catch (InvalidStateTransitionException e) {
                ExcUtils.suppress(e);
            }
            try {
                testTasks.get(i).release();
            } catch (Exception e) {
                ExcUtils.suppress(e);
            }
        }
        this.future = null;
        this.testTasks = Collections.EMPTY_LIST;
        this.testRunDto.setTestTasks(Collections.emptyList());
        this.eventListeners = null;
        this.testRunLogger = null;
        Thread.currentThread().setContextClassLoader(null);
    }

    @Override
    public STATE getState() {
        return currentState;
    }

    public void addStateEventListener(final TestRunEventListener listener) {
        this.eventListeners.add(listener);
    }

    /**
     * Change TestRun State
     *
     * @param state
     * @param reqCondition
     * @throws InvalidStateTransitionException
     */
    final private void changeState(STATE state, boolean reqCondition) throws InvalidStateTransitionException {
        if (!reqCondition || this.currentState == state) {
            final String errorMsg = "Illegal state transition in task " + this.getId() +
                    " from " + this.currentState + " to " + state;
            // testRunLogger.error(errorMsg);
            LoggerFactory.getLogger(this.getClass()).error(LogUtils.FATAL_MESSAGE, errorMsg);
            throw new InvalidStateTransitionException(errorMsg);
        }
        synchronized (this) {
            this.oldState = this.currentState;
            this.currentState = state;
            SwitchClassLoader.doIt(loader,
                    () -> this.eventListeners.forEach(l -> l.taskRunChangedEvent(this, this.currentState, this.oldState)));
        }
        if (this.oldState != null) {
            testRunLogger.info("Changed state from {} to {}", this.oldState, this.currentState);
        } else {
            testRunLogger.info("Setting state to {}", this.currentState);
        }
    }

    /**
     * Sets the start timestamp and the state to INITIALIZING
     *
     * @throws InvalidStateTransitionException
     */
    private void fireInitializing() throws InvalidStateTransitionException {
        startInstant = Instant.now();
        changeState(STATE.INITIALIZING,
                (currentState == STATE.CREATED));
        this.startInstant = Instant.now();
    }

    protected final void fireInitialized() throws InvalidStateTransitionException {
        changeState(STATE.INITIALIZED,
                (currentState == STATE.INITIALIZING));
    }

    protected final void fireRunning() throws InvalidStateTransitionException {
        changeState(STATE.RUNNING,
                (currentState == STATE.INITIALIZED));
    }

    private void fireCompleted() throws InvalidStateTransitionException {
        changeState(STATE.COMPLETED,
                (currentState == STATE.RUNNING));
        this.stopInstant = Instant.now();
    }

    final void fireFinalizing() throws InvalidStateTransitionException {
        changeState(STATE.FINALIZING,
                (currentState == STATE.COMPLETED ||
                        currentState == STATE.CANCELED ||
                        currentState == STATE.FAILED));
    }

    /**
     * Puts the task into a final state.
     *
     * Does not throw an exception!
     */
    final void fireFailed() {
        try {
            changeState(STATE.FAILED,
                    (currentState == STATE.CREATED) ||
                            (currentState == STATE.INITIALIZING) ||
                            (currentState == STATE.INITIALIZED) ||
                            (currentState == STATE.RUNNING));
        } catch (InvalidStateTransitionException e) {
            ExcUtils.suppress(e);
        }
        this.stopInstant = Instant.now();
    }

    final protected void fireCanceling() throws InvalidStateTransitionException {
        changeState(STATE.CANCELING,
                (currentState == STATE.CREATED ||
                        currentState == STATE.INITIALIZING ||
                        currentState == STATE.INITIALIZED ||
                        currentState == STATE.FAILED ||
                        currentState == STATE.RUNNING));
    }

    /**
     * Puts the task into a final state.
     *
     * @throws InvalidStateTransitionException
     */
    final void fireCanceled() throws InvalidStateTransitionException {
        changeState(STATE.CANCELED,
                (currentState == STATE.CANCELING));
        this.stopInstant = Instant.now();
    }

    @Override
    public long getMaxSteps() {
        return Math.max(testTasks.size(), currentRunIndex);
    }

    @Override
    public long getCurrentStepsCompleted() {
        return currentRunIndex;
    }

    @Override
    public Date getStartTimestamp() {
        return Date.from(startInstant);
    }

    @Override
    public TestRunLogReader getLogReader() {
        return testRunLogger;
    }
}
