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

/**
 * Test Run event listener
 *
 * A Listener interface for realising the Observer pattern. Registered clients that implement this interface will be
 * informed about state changes in Test Run objects (see {@link TestRun}).
 *
 * <img src="TestRunEventListener.svg" alt="Class UML">
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public interface TestRunEventListener {

    /**
     * Sends the changed state to an observing object
     *
     * @param t
     *            task
     * @param actualState
     *            actual task state
     * @param oldState
     *            old task state
     */
    void taskStateChangedEvent(
            TestTask t, TaskState.STATE actualState, TaskState.STATE oldState);

    /**
     * Sends the changed state to an observing object
     *
     * @param testRun
     *            Test Run
     * @param actualState
     *            actual task state
     * @param oldState
     *            old task state
     */
    void taskRunChangedEvent(
            TestRun testRun, TaskState.STATE actualState, TaskState.STATE oldState);
}
