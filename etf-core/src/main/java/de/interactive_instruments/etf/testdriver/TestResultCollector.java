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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Map;

import de.interactive_instruments.IFile;
import de.interactive_instruments.etf.dal.dto.result.TestResultStatus;

/**
 * The TestResultCollector is used to report failures and messages during a test run as well as adding information about
 * the control flow, attachments and logging messages (info, debug, error) which are written to a log path and linked
 * from the test task result.
 *
 * The TestResultCollector is exposed by a test driver, injected into a test engine and consumed during a test run by a
 * test driver adapter.
 *
 * A class that implements the TestResultCollector should realize a state machine and could throw
 * {@link IllegalStateException}s if operations are called in the wrong order.
 *
 * Complex / external types are avoided in order to facilitate easy integration.
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public interface TestResultCollector extends BasicTestResultCollector {

    /**
     * temporary, use {@link TestResultCollector#markAttachment} instead
     *
     * TODO will be removed in version 2.0.0 release version
     *
     * @return attachment directory
     */
    @Deprecated
    IFile getAttachmentDir();

    /**
     * temporary
     *
     * TODO will be removed in version 2.0.0 release version
     *
     * @return result file
     */
    @Deprecated
    IFile getResultFile();

    /**
     * Returns the Test Task Result ID
     *
     * @return eid of the currently recorded Test Task Result
     */
    String getTestTaskResultId();

    /**
     * Called when a Test Task is run
     *
     * @param testTaskId
     *            Test Task EID
     *
     * @return eid of the recorded test result item
     *
     * @throws IllegalArgumentException
     *             if the eid is invalid or the test can't be started in the current context
     * @throws IllegalStateException
     *             if test already has been started or ended
     */
    default String startTestTask(final String testTaskId) throws IllegalArgumentException, IllegalStateException {
        return startTestTask(testTaskId, System.currentTimeMillis());
    }

    /**
     * Called when a Test Module is run
     *
     * @param testModuleId
     *            Test Module EID
     *
     * @return eid of the recorded test result item
     *
     * @throws IllegalArgumentException
     *             if the eid is invalid or the test can't be started in the current context
     * @throws IllegalStateException
     *             if test already has been started or ended
     */
    default String startTestModule(final String testModuleId) throws IllegalArgumentException, IllegalStateException {
        return startTestModule(testModuleId, System.currentTimeMillis());
    }

    /**
     * Called when a Test Case is run
     *
     * @param testCaseId
     *            Test Case EID
     *
     * @return eid of the recorded test result item
     *
     * @throws IllegalArgumentException
     *             if the eid is invalid or the test can't be started in the current context
     * @throws IllegalStateException
     *             if test already has been started or ended
     */
    default String startTestCase(final String testCaseId) throws IllegalArgumentException, IllegalStateException {
        return startTestCase(testCaseId, System.currentTimeMillis());
    }

    /**
     * Called when a Test Step is run
     *
     * @param testStepId
     *            Test Step EID
     *
     * @return eid of the recorded test result item
     *
     * @throws IllegalArgumentException
     *             if the eid is invalid or the test can't be started in the current context
     * @throws IllegalStateException
     *             if test already has been started or ended
     */
    default String startTestStep(final String testStepId) throws IllegalArgumentException, IllegalStateException {
        return startTestStep(testStepId, System.currentTimeMillis());
    }

    /**
     * Called when a Test Assertion is run
     *
     * @param testAssertionId
     *            Test Assertion EID
     *
     * @return eid of the recorded test result item
     *
     * @throws IllegalArgumentException
     *             if the eid is invalid or the test can't be started in the current context
     * @throws IllegalStateException
     *             if test already has been started or ended
     */
    default String startTestAssertion(final String testAssertionId) throws IllegalArgumentException, IllegalStateException {
        return startTestAssertion(testAssertionId, System.currentTimeMillis());
    }

    /**
     * If a Test Case depends on other TestCases this method must be called after the recording of a Test Case has been
     * started. If any of those TestCases have the status {@link TestResultStatus#FAILED} or
     * {@link TestResultStatus#SKIPPED}, the recording of the Test Case will be stopped by invoking
     * {@link TestResultCollector#end(String, int)} with the status {@link TestResultStatus#SKIPPED}.
     *
     * @param testCaseIds
     *            Test Case EIDs
     *
     * @return true if the Test Case has been skipped, false otherwise
     *
     * @throws IllegalArgumentException
     *             if test already has been ended
     * @throws IllegalStateException
     *             if the current Test Case already has been ended or the recording of a TestCase has not been started
     */
    boolean endWithSkippedIfTestCasesFailed(final String... testCaseIds) throws IllegalArgumentException, IllegalStateException;

    /**
     * Called just after a test item has been run
     *
     * @param testModelItemId
     *            Test Model Item EID
     * @param status
     *            {@link TestResultStatus} as integer
     *
     * @return eid of the recorded test result item
     *
     * @throws IllegalArgumentException
     *             if test already has been ended
     * @throws IllegalStateException
     *             if test already has been ended or hasn't been started yet
     */
    default String end(final String testModelItemId, final int status) throws IllegalArgumentException, IllegalStateException {
        return end(testModelItemId, status, System.currentTimeMillis());
    }

    /**
     * Called just after a test item has been run. The status is automatically determined from previous end() calls.
     *
     * Note: if the status cannot be determined or the collector is on the lowest test level (Test Assertion), the status
     * will be set to 'failed'!
     *
     * @param testModelItemId
     *            Test Model Item EID
     *
     * @return eid of the recorded test result item
     *
     * @throws IllegalArgumentException
     *             if test already has been ended
     * @throws IllegalStateException
     *             if test already has been ended or hasn't been started yet
     */
    default String end(final String testModelItemId) throws IllegalArgumentException, IllegalStateException {
        return end(testModelItemId, System.currentTimeMillis());
    }

    /**
     * Returns the {@link TestResultStatus} of a non-parametrized Test Model Item. "Undefined" is returned if the Test Model
     * Item has not yet been executed.
     *
     * @param testModelItemId
     *            Test Model Item EID
     *
     * @return {@link TestResultStatus}
     *
     * @throws IllegalArgumentException
     *             if the Test Model Item does not exist or the ID refers to a parametrized Test Model Item
     */
    TestResultStatus status(final String testModelItemId) throws IllegalArgumentException;

    /**
     * Returns true if the result of a non-parametrized Test Model Item is equal to the passed sequence of
     * {@link TestResultStatus}.
     *
     * @param testResultStatus
     *            {@link TestResultStatus} to compare
     * @param testModelItemId
     *            Test Model Item EID
     * @return {@code true} if one of the passed {@link TestResultStatus} are equal to each other and {@code false}
     *         otherwise
     *
     * @throws IllegalArgumentException
     *             if the Test Model Item does not exist or the ID refers to a parametrized Test Model Item
     */
    boolean statusEqualsAny(final String testModelItemId, final String... testResultStatus) throws IllegalArgumentException;

    /**
     * The error limit is set as a default test run parameter. Returns true if the error limit exceeded, false otherwise.
     * Should be used by the client to avoid constructing expensive error messages.
     *
     * @return true it the error limit exceeded, false otherwise
     */
    boolean isErrorLimitExceeded();

    /**
     * Mark a path in the temporary directory as an attachment which will be persisted and referenced in the result document
     *
     * @param fileName
     *            filename in temporary directory (relative path)
     * @param label
     *            Label for the attachment
     * @param encoding
     *            encoding of the data
     * @param mimeType
     *            mime type or null if the type should be auto detected
     * @param type
     *            attachment type
     *
     * @return eid of the recorded attachment
     *
     * @throws IOException
     *             if attachment could not be read
     */
    String markAttachment(
            final String fileName,
            final String label,
            final String encoding,
            final String mimeType,
            final String type) throws IOException;

    /**
     * Mark a path in the temporary directory as an attachment which will be persisted and referenced in the result document
     *
     * @param fileName
     *            filename in temporary directory (relative path)
     * @param label
     *            Label for the attachment
     * @param encoding
     *            encoding of the data
     * @param mimeType
     *            mime type or null if the type should be auto detected
     *
     * @return eid of the recorded attachment
     *
     * @throws IOException
     *             if attachment could not be read
     */
    default String markAttachment(
            final String fileName,
            final String label,
            final String encoding,
            final String mimeType) throws IOException {
        return markAttachment(fileName, label, encoding, mimeType, null);
    }

    /**
     * Save an attachment from a Reader
     *
     * @param reader
     *            reader
     * @param label
     *            Label for the attachment
     * @param mimeType
     *            mime type or null if the type should be auto detected
     * @param type
     *            attachment type
     *
     * @return eid of the recorded attachment
     *
     * @throws IOException
     *             if attachment could not be written
     */
    String saveAttachment(
            final Reader reader,
            final String label,
            final String mimeType,
            final String type) throws IOException;

    /**
     * Save an attachment from an input stream
     *
     * @param inputStream
     *            input stream
     * @param label
     *            Label for the attachment
     * @param mimeType
     *            mime type or null if the type should be auto detected
     * @param type
     *            attachment type
     *
     * @return eid of the recorded attachment
     *
     * @throws IOException
     *             if attachment could not be written
     */
    String saveAttachment(
            final InputStream inputStream,
            final String label,
            final String mimeType,
            final String type) throws IOException;

    /**
     * Save a String as attachment.
     *
     * The underlying collector may decide if the String is written to a path and referenced from the test result document
     * or if it is embedded into the test result document based on the size of the message.
     *
     * @param content
     *            input String
     * @param label
     *            Label for the attachment
     * @param mimeType
     *            mime type or null if the type should be auto detected
     * @param type
     *            attachment type
     *
     * @return eid of the recorded attachment
     *
     * @throws IOException
     *             if attachment could not be written
     */
    String saveAttachment(
            final String content,
            final String label,
            final String mimeType,
            final String type) throws IOException;

    /**
     * Returns a directory which can be used to store data temporary during the test run.
     *
     * The directory and its content will be deleted after the test run!
     *
     * Use the {@link TestResultCollector#markAttachment(String, String, String, String)} method to mark single files which
     * should be kept and attached to the test result document.
     *
     * @return temporary directory path
     *
     */
    File getTempDir();

    /**
     * Report an internal error and abort the test
     *
     * @param translationTemplateId
     *            Translation Template ID
     * @param tokenValuePairs
     *            Translation Template message as Token Value pair
     * @param e
     *            Exception
     */
    void internalError(
            final String translationTemplateId,
            final Map<String, String> tokenValuePairs,
            final Throwable e);

    /**
     * Report an error and abort the test
     *
     * @param e
     *            Exception
     */
    void internalError(final Throwable e);

    /**
     * Report an error and abort the test
     *
     * @param errorMessage
     *            untranslated error message
     * @param bytes
     *            error bytes to save to a file
     * @param mimeType
     *            MIME type
     * @return eid of the recorded test Task
     */
    String internalError(final String errorMessage, final byte[] bytes, final String mimeType);

    /**
     * Info message which is written to the log path
     *
     * @param message
     *            message as String
     */
    default void info(final String message) {
        getLogger().info(message);
    }

    /**
     * Error message which is written to the log path
     *
     * @param message
     *            message as String
     */
    default void error(final String message) {
        getLogger().error(message);
    }

    /**
     * Debug message which is written to the log path
     *
     * @param message
     *            message as String
     */
    default void debug(final String message) {
        getLogger().debug(message);
    }

    /**
     * Returns the logger object
     *
     * @return TestRunLogger object
     */
    TestRunLogger getLogger();

}
