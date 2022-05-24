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
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.LoggerFactory;

import de.interactive_instruments.LogUtils;
import de.interactive_instruments.etf.dal.dto.result.TestTaskResultDto;
import de.interactive_instruments.etf.dal.dto.run.TestTaskDto;
import de.interactive_instruments.etf.model.EID;
import de.interactive_instruments.exceptions.ExcUtils;
import de.interactive_instruments.exceptions.InitializationException;
import de.interactive_instruments.exceptions.InvalidStateTransitionException;
import de.interactive_instruments.exceptions.config.ConfigurationException;

/**
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public abstract class AbstractTestTask implements TestTask {

    final protected TestTaskDto testTaskDto;
    private ArrayList<TaskStateEventListener> eventListeners;
    private Future<TestTaskResultDto> future;
    protected final AbstractTestTaskProgress progress;
    private TestTaskClassLoader classLoader;
    private TestTaskResultPersistor persistor;

    protected AbstractTestTask(
            final TestTaskDto testTaskDto,
            final AbstractTestTaskProgress progress,
            final ClassLoader classLoader) {
        this.testTaskDto = testTaskDto;
        this.progress = progress;
        // this.classLoader = Objects.requireNonNull(classLoader);
        this.classLoader = new TestTaskClassLoader(testTaskDto.getId(), Objects.requireNonNull(classLoader));
    }

    @Override
    public EID getId() {
        return testTaskDto.getId();
    }

    @Override
    public final void run() throws Exception {
        ensureClassloader();
        fireRunning();
        try {
            doRun();
        } catch (final Exception e) {
            // Check if the error message was already reported by the result collector
            if (!persistor.resultPersisted()) {
                // The test driver failed to report the issue, so we need to persist
                // the ugly exception here.
                if (e instanceof NullPointerException) {
                    persistor.getResultCollector().internalError(
                            new RuntimeException("An unexpected internal error occurred "
                                    + "and was logged for the administrator of this instance.", e));
                } else {
                    persistor.getResultCollector().internalError(e);
                }
            }
            fireFailed();
            return;
        }
        fireCompleted();
    }

    protected abstract void doRun() throws Exception;

    protected final void ensureClassloader() {
        Thread.currentThread().setContextClassLoader(this.classLoader);
    }

    @Override
    public TestTaskResultDto getResult() {
        return testTaskDto.getTestTaskResult();
    }

    protected abstract void doInit() throws ConfigurationException, InitializationException;

    @Override
    public final void init() throws ConfigurationException, InvalidStateTransitionException, InitializationException {
        ensureClassloader();
        fireInitializing();
        if (persistor == null) {
            throw new IllegalStateException("Result persistor not set");
        }
        doInit();
        getLogger().info("TestRunTask initialized");
        fireInitialized();
    }

    @Override
    public boolean isInitialized() {
        return progress.getState().isAtLeastInitialized();
    }

    protected abstract void doRelease();

    @Override
    public final void release() {
        try {
            fireFinalizing();
            getLogger().info("Releasing resources");
            ensureClassloader();
            doRelease();
            classLoader.close();
        } catch (InvalidStateTransitionException e) {
            ExcUtils.suppress(e);
        } catch (Exception e) {
            getLogger().warn("Releasing of resource failed: " + e.getMessage());
        } finally {
            future = null;
            eventListeners = null;
            persistor = null;
            Thread.currentThread().setContextClassLoader(null);
            classLoader = null;
        }
    }

    protected abstract void doCancel() throws InvalidStateTransitionException;

    protected void checkCancelStatus() throws InterruptedException {
        if (progress.getCurrentState() == TaskState.STATE.CANCELING ||
                Thread.currentThread().isInterrupted()) {
            try {
                cancel();
            } catch (InvalidStateTransitionException e) {
                getLogger().error("Unable to cancel task: " + e.getMessage());
            }
            throw new InterruptedException();
        }
    }

    @Override
    public final void cancel() throws InvalidStateTransitionException {
        if (this.getState() == STATE.CANCELING || this.getState() == STATE.CANCELED) {
            getLogger().info("Canceling TestRunTask." + getId());
            fireCanceling();
            ensureClassloader();
            doCancel();
            fireCanceled();
            release();
            getLogger().info("TestRunTask." + getId() + " canceled");
        }
    }

    @Override
    public TestRunLogger getLogger() {
        return persistor.getResultCollector().getLogger();
    }

    protected TestTaskResultPersistor getPersistor() {
        return Objects.requireNonNull(persistor, "Persistor not set");
    }

    protected TestResultCollector getCollector() {
        return getPersistor().getResultCollector();
    }

    @Override
    public TaskProgress getProgress() {
        return this.progress;
    }

    @Override
    public STATE getState() {
        return this.progress != null ? this.progress.getCurrentState() : STATE.INITIALIZING;
    }

    @Override
    public void setFuture(final Future<TestTaskResultDto> future) throws IllegalStateException {
        if (this.future != null) {
            throw new IllegalStateException("Call back object already set!");
        }
        this.future = future;
    }

    @Override
    public TestTaskResultDto call() throws Exception {
        run();
        return testTaskDto.getTestTaskResult();
    }

    @Override
    public TestTaskResultDto waitForResult() throws InterruptedException, ExecutionException {
        return this.future.get();
    }

    // STATE implementations
    ///////////////////////////

    /*
     * @Override public synchronized void addStateEventListener(TaskStateEventListener listener) {
     * if(this.eventListeners==null) { this.eventListeners=new ArrayList<>(); } this.eventListeners.add(listener); }
     */

    final private void changeState(TaskState.STATE state, boolean reqCondition) throws InvalidStateTransitionException {
        if (!reqCondition || progress.getCurrentState() == state) {
            final String errorMsg = "Illegal state transition in task " + this.testTaskDto.getId() +
                    " from " + progress.getCurrentState() + " to " + state;
            /* if (resultCollector != null) { getLogger().error(errorMsg); } */
            LoggerFactory.getLogger(this.getClass()).error(LogUtils.FATAL_MESSAGE, errorMsg);
            throw new InvalidStateTransitionException(errorMsg);
        }
        progress.setState(state);
        if (progress.getOldState() != null) {
            // TODO
            // getLogger().info("Changed state from {} to {}", this.oldState, this.currentState);
        } else {
            // TODO
            // getLogger().info("Setting state to {}", this.currentState);
        }
        if (this.eventListeners != null) {
            SwitchClassLoader.doIt(
                    this.classLoader,
                    () -> this.eventListeners
                            .forEach(l -> l.taskStateChangedEvent(this, progress.getCurrentState(), progress.getOldState())));
        }
    }

    @Override
    public void setResulPersistor(final TestTaskResultPersistor persistor) throws IllegalStateException {
        if (this.persistor != null) {
            throw new IllegalStateException("TestTaskResultPersistor already set");
        }
        this.persistor = Objects.requireNonNull(persistor, "TestTaskResultPersistor is null");
        final TestResultCollector resultCollector = persistor.getResultCollector();
        this.progress.setLogReader(resultCollector.getLogger());
        if (resultCollector instanceof AbstractTestResultCollector) {
            ((AbstractTestResultCollector) resultCollector).setTaskProgress(this.progress);
        }
    }

    /**
     * Sets the start timestamp and the state to INITIALIZING
     *
     * @throws InvalidStateTransitionException
     */
    private void fireInitializing() throws InvalidStateTransitionException {
        changeState(TaskState.STATE.INITIALIZING,
                (progress.getCurrentState() == TaskState.STATE.CREATED));
        progress.startInstant = Instant.now();
    }

    private final void fireInitialized() throws InvalidStateTransitionException {
        changeState(TaskState.STATE.INITIALIZED,
                (progress.getCurrentState() == TaskState.STATE.INITIALIZING));
    }

    private final void fireRunning() throws InvalidStateTransitionException {
        changeState(TaskState.STATE.RUNNING,
                (progress.getCurrentState() == TaskState.STATE.INITIALIZED));
    }

    private void fireCompleted() throws InvalidStateTransitionException {
        changeState(TaskState.STATE.COMPLETED,
                (progress.getCurrentState() == TaskState.STATE.RUNNING));
        progress.stopInstant = Instant.now();
    }

    final void fireFinalizing() throws InvalidStateTransitionException {
        changeState(TaskState.STATE.FINALIZING,
                (progress.getCurrentState() == TaskState.STATE.COMPLETED ||
                        progress.getCurrentState() == TaskState.STATE.CANCELED ||
                        progress.getCurrentState() == TaskState.STATE.FINALIZING ||
                        progress.getCurrentState() == TaskState.STATE.FAILED));
    }

    /**
     * Puts the task into a final state.
     *
     * Does not throw an exception!
     */
    final void fireFailed() {
        try {
            changeState(TaskState.STATE.FAILED,
                    (progress.getCurrentState() == TaskState.STATE.CREATED) ||
                            (progress.getCurrentState() == TaskState.STATE.INITIALIZING) ||
                            (progress.getCurrentState() == TaskState.STATE.INITIALIZED) ||
                            (progress.getCurrentState() == TaskState.STATE.RUNNING));
        } catch (final InvalidStateTransitionException e) {
            ExcUtils.suppress(e);
        }
        progress.stopInstant = Instant.now();
    }

    final protected void fireCanceling() throws InvalidStateTransitionException {
        changeState(TaskState.STATE.CANCELING,
                (progress.getCurrentState() == TaskState.STATE.CREATED ||
                        progress.getCurrentState() == TaskState.STATE.INITIALIZING ||
                        progress.getCurrentState() == TaskState.STATE.INITIALIZED ||
                        progress.getCurrentState() == TaskState.STATE.RUNNING));
    }

    /**
     * Puts the task into a final state.
     *
     * @throws InvalidStateTransitionException
     */
    final void fireCanceled() throws InvalidStateTransitionException {
        changeState(TaskState.STATE.CANCELED,
                (progress.getCurrentState() == TaskState.STATE.CANCELING));
        progress.stopInstant = Instant.now();
    }
}
