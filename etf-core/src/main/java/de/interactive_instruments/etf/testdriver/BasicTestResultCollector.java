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

import de.interactive_instruments.Releasable;
import de.interactive_instruments.etf.dal.dto.result.TestResultStatus;

/**
 * Basic Test Result Collector interface
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public interface BasicTestResultCollector extends Releasable {

    /**
     * Called when a Test Task is run
     *
     * @param testTaskId
     *            Test Task EID
     * @param startTimestamp
     *            start timestamp
     *
     * @return eid of the recorded test result item
     *
     * @throws IllegalArgumentException
     *             if the eid is invalid or the test can't be started in the current context
     * @throws IllegalStateException
     *             if test already has been started or ended
     */
    String startTestTask(final String testTaskId, long startTimestamp) throws IllegalArgumentException, IllegalStateException;

    /**
     * Called when a Test Module is run
     *
     * @param testModuleId
     *            Test Module EID
     * @param startTimestamp
     *            start timestamp
     *
     * @return eid of the recorded test result item
     *
     * @throws IllegalArgumentException
     *             if the eid is invalid or the test can't be started in the current context
     * @throws IllegalStateException
     *             if test already has been started or ended
     */
    String startTestModule(final String testModuleId, long startTimestamp)
            throws IllegalArgumentException, IllegalStateException;

    /**
     * Called when a Test Case is run
     *
     * @param testCaseId
     *            Test Case EID
     * @param startTimestamp
     *            start timestamp
     *
     * @return eid of the recorded test result item
     *
     * @throws IllegalArgumentException
     *             if the eid is invalid or the test can't be started in the current context
     * @throws IllegalStateException
     *             if test already has been started or ended
     */
    String startTestCase(final String testCaseId, long startTimestamp) throws IllegalArgumentException, IllegalStateException;

    /**
     * Called when a Test Step is run
     *
     * @param testStepId
     *            Test Step EID
     * @param startTimestamp
     *            start timestamp
     *
     * @return eid of the recorded test result item
     *
     * @throws IllegalArgumentException
     *             if the eid is invalid or the test can't be started in the current context
     * @throws IllegalStateException
     *             if test already has been started or ended
     */
    String startTestStep(final String testStepId, long startTimestamp) throws IllegalArgumentException, IllegalStateException;

    /**
     * Called when a Test Assertion is run
     *
     * @param testAssertionId
     *            Test Assertion EID
     * @param startTimestamp
     *            start timestamp
     *
     * @return eid of the recorded test result item
     *
     * @throws IllegalArgumentException
     *             if the eid is invalid or the test can't be started in the current context
     * @throws IllegalStateException
     *             if test already has been started or ended
     */
    String startTestAssertion(final String testAssertionId, long startTimestamp)
            throws IllegalArgumentException, IllegalStateException;

    /**
     * Called just after a test item has been run
     *
     * @param testModelItemId
     *            Test Model Item EID
     * @param status
     *            {@link TestResultStatus} as integer
     * @param stopTimestamp
     *            stop timestamp
     *
     * @return eid of the recorded test result item
     *
     * @throws IllegalArgumentException
     *             if test already has been ended
     * @throws IllegalStateException
     *             if test already has been ended or hasn't been started yet
     */
    String end(final String testModelItemId, final int status, long stopTimestamp)
            throws IllegalArgumentException, IllegalStateException;

    /**
     * Called just after a test item has been run. The status is automatically determined from previous end() calls.
     *
     * Note: if the status cannot be determined or the collector is on the lowest test level (Test Assertion), the status
     * will be set to 'UNDEFINED'!
     *
     * @param testModelItemId
     *            Test Model Item EID
     * @param stopTimestamp
     *            stop timestamp
     *
     * @return eid of the recorded test result item
     *
     * @throws IllegalArgumentException
     *             if test already has been ended
     * @throws IllegalStateException
     *             if test already has been ended or hasn't been started yet
     */
    String end(final String testModelItemId, long stopTimestamp)
            throws IllegalArgumentException, IllegalStateException;

    /**
     * Add a message
     *
     * @param translationTemplateId
     *            Translation Template ID
     */
    void addMessage(final String translationTemplateId);

    /**
     * Add a message with translation parameters as token value pairs
     *
     * @param translationTemplateId
     *            Translation Template ID
     * @param tokenValuePairs
     *            Translation Template message as token value pair
     */
    void addMessage(final String translationTemplateId, final Map<String, String> tokenValuePairs);

    /**
     * Add a message with translation parameters as token value pairs
     *
     * @param translationTemplateId
     *            Translation Template ID
     * @param tokensAndValues
     *            Translation Template message as alternating tokens and values
     *
     * @throws IllegalArgumentException
     *             if number of tokensAndValues arguments is odd
     */
    void addMessage(final String translationTemplateId, final String... tokensAndValues);

    /**
     * Returns the currently recorded model type
     *
     * 1 for Test Task 2 for Test Module 3 for Test Case 4 for Test Step 5 for Test Assertion -1 for undefined
     *
     * @return model type as integer (1-5) or -1 for undefined
     */
    int currentModelType();

    /**
     * Register a listener, which is called when a Test Task is finished.
     *
     * @param listener
     *            TestTaskEndListener
     */
    void registerTestTaskEndListener(final TestTaskEndListener listener);
}
