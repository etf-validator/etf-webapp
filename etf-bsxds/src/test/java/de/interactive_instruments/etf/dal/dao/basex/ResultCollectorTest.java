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
package de.interactive_instruments.etf.dal.dao.basex;

import static de.interactive_instruments.etf.dal.dao.basex.BsxTestUtils.DATA_STORAGE;
import static de.interactive_instruments.etf.test.TestDtos.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;

import org.jsoup.Jsoup;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mockito;

import de.interactive_instruments.IFile;
import de.interactive_instruments.etf.dal.dao.Dao;
import de.interactive_instruments.etf.dal.dao.PreparedDto;
import de.interactive_instruments.etf.dal.dao.WriteDao;
import de.interactive_instruments.etf.dal.dto.result.AttachmentDto;
import de.interactive_instruments.etf.dal.dto.result.TestResultStatus;
import de.interactive_instruments.etf.dal.dto.result.TestTaskResultDto;
import de.interactive_instruments.etf.model.EidFactory;
import de.interactive_instruments.etf.model.OutputFormat;
import de.interactive_instruments.etf.testdriver.TestResultCollector;
import de.interactive_instruments.etf.testdriver.TestRunLogger;
import de.interactive_instruments.exceptions.InitializationException;
import de.interactive_instruments.exceptions.InvalidStateTransitionException;
import de.interactive_instruments.exceptions.ObjectWithIdNotFoundException;
import de.interactive_instruments.exceptions.StorageException;
import de.interactive_instruments.exceptions.config.ConfigurationException;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
@TestMethodOrder(value = MethodOrderer.Alphanumeric.class)
public class ResultCollectorTest {

    static IFile attachmentDir = null;
    private static Dao<TestTaskResultDto> dao;

    private static TestRunLogger loggerMock = Mockito.mock(TestRunLogger.class);

    @BeforeAll
    public static void setUp() throws IOException, InvalidStateTransitionException, StorageException, InitializationException,
            ConfigurationException, ObjectWithIdNotFoundException {

        BsxTestUtils.ensureInitialization();
        dao = BsxTestUtils.DATA_STORAGE.getDao(TestTaskResultDto.class);
        BsxTestUtils.forceDeleteAndAdd(TAG_DTO_1);
        BsxTestUtils.forceDeleteAndAdd(TAG_DTO_2);
        BsxTestUtils.forceDeleteAndAdd(TOT_DTO_1);
        BsxTestUtils.forceDeleteAndAdd(TOT_DTO_2);
        BsxTestUtils.forceDeleteAndAdd(TOT_DTO_3);
        BsxTestUtils.forceDeleteAndAdd(TO_DTO_1);
        BsxTestUtils.forceDeleteAndAdd(TTR_DTO_1);
        BsxTestUtils.forceDeleteAndAdd(TTR_DTO_2);
        BsxTestUtils.forceDeleteAndAdd(TR_DTO_1);
        BsxTestUtils.forceDeleteAndAdd(TASK_DTO_1.getTestObject());

        attachmentDir = IFile.createTempDir("etf-bsxds-test");

        Mockito.when(loggerMock.getLogFile()).thenReturn(new IFile(attachmentDir.expandPath("log.txt")));
    }

    @AfterAll
    public static void tearDown() throws IOException {
        if (attachmentDir != null) {
            // attachmentDir.deleteDirectory();
        }
    }

    @Test
    public TestTaskResultDto testCollector()
            throws IOException, ObjectWithIdNotFoundException, StorageException, ConfigurationException,
            InvalidStateTransitionException, InitializationException {

        final TestResultCollector c = new BsxDsResultCollector(BsxTestUtils.DATA_STORAGE,
                loggerMock, attachmentDir.expandPath("Result2.xml"), attachmentDir, TASK_DTO_1);

        // Start Test Task
        final String testTaskResultId = c.startTestTask(ETS_DTO_1.getId().getId());
        assertEquals(1, c.currentModelType());
        assertEquals(TestResultStatus.UNDEFINED, c.status(""));

        // Start Test Module
        c.startTestModule(ETS_DTO_1.getTestModules().get(0).getId().getId());
        assertEquals(2, c.currentModelType());
        assertEquals(TestResultStatus.UNDEFINED, c.status(""));

        // Start Test Case
        c.startTestCase(ETS_DTO_1.getTestModules().get(0).getTestCases().get(0).getId().getId());
        assertEquals(3, c.currentModelType());
        assertEquals(TestResultStatus.UNDEFINED, c.status(""));

        // Start Test Step (1)
        c.startTestStep(ETS_DTO_1.getTestModules().get(0).getTestCases().get(0).getTestSteps().get(0).getId().getId());
        assertEquals(4, c.currentModelType());
        assertEquals(TestResultStatus.UNDEFINED, c.status(""));

        // Start assertion
        c.startTestAssertion(ETS_DTO_1.getTestModules().get(0).getTestCases().get(0).getTestSteps().get(0).getTestAssertions()
                .get(0).getId().getId());
        assertEquals(5, c.currentModelType());
        assertEquals(TestResultStatus.UNDEFINED, c.status(""));
        c.addMessage("TR.Template.1", "TOKEN.1", "Value.1", "TOKEN.2", "Value.2", "TOKEN.3", "Value.3");
        c.saveAttachment(new StringReader("Message in Attachment"), "Message.1", "text/plain", "Message");
        c.end(ETS_DTO_1.getTestModules().get(0).getTestCases().get(0).getTestSteps().get(0).getTestAssertions().get(0).getId()
                .getId(), TestResultStatus.PASSED_MANUAL.value());
        assertEquals(TestResultStatus.PASSED_MANUAL, c.status(""));

        // Still in Test Step context
        assertEquals(4, c.currentModelType());

        // Start assertion
        c.startTestAssertion(ETS_DTO_1.getTestModules().get(0).getTestCases().get(0).getTestSteps().get(0).getTestAssertions()
                .get(1).getId().getId());
        assertEquals(5, c.currentModelType());
        assertEquals(TestResultStatus.UNDEFINED, c.status(""));
        c.addMessage("TR.Template.1", "TOKEN.1", "Value.1", "TOKEN.2", "Value.2", "TOKEN.3", "Value.3");
        c.saveAttachment(new StringReader("Message in Attachment"), "Message.1", "text/plain", "Message");
        c.end(ETS_DTO_1.getTestModules().get(0).getTestCases().get(0).getTestSteps().get(0).getTestAssertions().get(1).getId()
                .getId(), TestResultStatus.PASSED_MANUAL.value());
        assertEquals(TestResultStatus.PASSED_MANUAL, c.status(""));

        // End Test Step, back in Test Case context
        c.end(ETS_DTO_1.getTestModules().get(0).getTestCases().get(0).getTestSteps().get(0).getId().getId());
        assertEquals(3, c.currentModelType());
        assertEquals(TestResultStatus.PASSED_MANUAL, c.status(""));

        // Test calling another Test Step
        // Start Test Step (2)
        c.startTestStep(ETS_DTO_1.getTestModules().get(0).getTestCases().get(0).getTestSteps().get(1).getId().getId());
        assertEquals(4, c.currentModelType());
        assertEquals(TestResultStatus.UNDEFINED, c.status(""));

        // Call a Test Step (3)
        c.startTestStep(ETS_DTO_1.getTestModules().get(0).getTestCases().get(0).getTestSteps().get(2).getId().getId());
        assertEquals(4, c.currentModelType());
        assertEquals(TestResultStatus.UNDEFINED, c.status(""));

        // Start assertion
        c.startTestAssertion(ETS_DTO_1.getTestModules().get(0).getTestCases().get(0).getTestSteps().get(2).getTestAssertions()
                .get(0).getId().getId());
        assertEquals(5, c.currentModelType());
        assertEquals(TestResultStatus.UNDEFINED, c.status(""));
        c.addMessage("TR.Template.1", "TOKEN.1", "Value.1", "TOKEN.2", "Value.2", "TOKEN.3", "Value.3");
        c.saveAttachment(new StringReader("Message in Attachment"), "Message.1", "text/plain", "Message");
        c.end(ETS_DTO_1.getTestModules().get(0).getTestCases().get(0).getTestSteps().get(2).getTestAssertions().get(0).getId()
                .getId(), TestResultStatus.FAILED.value());
        assertEquals(TestResultStatus.FAILED, c.status(""));

        // Add relabeling to Test Step (3)
        c.saveAttachment("RELABELED_TEST_STEP", "relabel", "text/plain", "RELABEL");

        // In Test Step (3) context
        assertEquals(4, c.currentModelType());
        assertEquals(TestResultStatus.FAILED, c.status(""));

        // End Test Step call (3)
        c.end(ETS_DTO_1.getTestModules().get(0).getTestCases().get(0).getTestSteps().get(2).getId().getId());
        assertEquals(4, c.currentModelType());
        assertEquals(TestResultStatus.FAILED, c.status(""));

        // End Test Step (2), back in Test Case context
        c.end(ETS_DTO_1.getTestModules().get(0).getTestCases().get(0).getTestSteps().get(1).getId().getId());
        assertEquals(3, c.currentModelType());
        assertEquals(TestResultStatus.FAILED, c.status(""));

        // End Test Case
        c.end(ETS_DTO_1.getTestModules().get(0).getTestCases().get(0).getId().getId());
        assertEquals(2, c.currentModelType());
        assertEquals(TestResultStatus.FAILED, c.status(""));

        // Start another Test Case which depends on the first one
        c.startTestCase(ETS_DTO_1.getTestModules().get(0).getTestCases().get(1).getId().getId());
        assertEquals(3, c.currentModelType());
        assertEquals(TestResultStatus.UNDEFINED, c.status(""));

        // Start Test Step
        c.startTestStep(ETS_DTO_1.getTestModules().get(0).getTestCases().get(1).getTestSteps().get(0).getId().getId());
        assertEquals(4, c.currentModelType());
        assertEquals(TestResultStatus.UNDEFINED, c.status(""));

        // Call first Test Case
        c.startTestCase(ETS_DTO_1.getTestModules().get(0).getTestCases().get(0).getId().getId());
        assertEquals(3, c.currentModelType());
        assertEquals(TestResultStatus.UNDEFINED, c.status(""));

        // Start Test Step (1)
        c.startTestStep(ETS_DTO_1.getTestModules().get(0).getTestCases().get(0).getTestSteps().get(0).getId().getId());
        assertEquals(4, c.currentModelType());
        assertEquals(TestResultStatus.UNDEFINED, c.status(""));

        // Add an attachment
        c.saveAttachment("Test String", "Label", "text/plain", null);

        // Fail Test Step (1)
        c.end(ETS_DTO_1.getTestModules().get(0).getTestCases().get(0).getTestSteps().get(0).getId().getId(),
                TestResultStatus.FAILED.value());
        assertEquals(3, c.currentModelType());
        assertEquals(TestResultStatus.FAILED, c.status(""));

        // End called, first Test Case
        c.end(ETS_DTO_1.getTestModules().get(0).getTestCases().get(0).getId().getId());
        assertEquals(4, c.currentModelType());
        assertEquals(TestResultStatus.SKIPPED, c.status(""));

        // End Test Step, back in Test Case context
        c.end(ETS_DTO_1.getTestModules().get(0).getTestCases().get(1).getTestSteps().get(0).getId().getId());
        assertEquals(3, c.currentModelType());
        // failed test case = skipped
        assertEquals(TestResultStatus.SKIPPED, c.status(""));

        // End Test Case, back in Test Module context
        c.end(ETS_DTO_1.getTestModules().get(0).getTestCases().get(1).getId().getId());
        assertEquals(2, c.currentModelType());
        // aggregated status of all Test Cases in the context
        assertEquals(TestResultStatus.FAILED, c.status(""));

        // End Test Module
        c.end(ETS_DTO_1.getTestModules().get(0).getId().getId());
        assertEquals(1, c.currentModelType());
        // first Test Case failed
        assertEquals(TestResultStatus.FAILED, c.status(""));

        // End Test Task
        c.end(ETS_DTO_1.getId().getId());
        assertEquals(-1, c.currentModelType());
        assertEquals(TestResultStatus.FAILED, c.status(""));

        // Get TestTaskResult
        final TestTaskResultDto result = dao.getById(EidFactory.getDefault().createUUID(testTaskResultId)).getDto();
        assertEquals(testTaskResultId, result.getId().getId());

        return result;
    }

    @Test
    public void relabelTest() throws ConfigurationException, InvalidStateTransitionException, InitializationException,
            ObjectWithIdNotFoundException, IOException {
        final TestTaskResultDto testTaskResult = testCollector();
        ExecutableTestSuiteDaoTest.setUp();

        // Transform TestTaskResult
        final IFile outputHtml = new IFile(attachmentDir.getPath() + "/Result22html.html");
        final FileOutputStream fopHtml = new FileOutputStream(outputHtml);

        final WriteDao<TestTaskResultDto> writeDao = (WriteDao) DATA_STORAGE.getDao(TestTaskResultDto.class);
        OutputFormat htmlReportFormat = null;
        for (final OutputFormat outputFormat : writeDao.getOutputFormats().values()) {
            if ("text/html".equals(outputFormat.getMediaTypeType().getType())) {
                htmlReportFormat = outputFormat;
                break;
            }
        }

        final PreparedDto<TestTaskResultDto> preparedDto = BsxTestUtils.getByIdTest(testTaskResult);
        preparedDto.streamTo(htmlReportFormat, null, fopHtml);
        final String reportHtml = outputHtml.readContent().toString();

        // Check relabeling
        final String relabeledEid = "EID"
                + ETS_DTO_1.getTestModules().get(0).getTestCases().get(0).getTestSteps().get(2).getId().getId();
        final String relabeledDivText = Jsoup.parse(reportHtml)
                .select("h4 + div:has(div td:contains(" + relabeledEid + "))")
                .parents().first().text();
        assertTrue(relabeledDivText.contains("RELABELED_TEST_STEP"));
    }

    @Test
    public void testCollectorInternalError() throws IOException, ObjectWithIdNotFoundException, StorageException {
        final TestResultCollector c = new BsxDsResultCollector(BsxTestUtils.DATA_STORAGE,
                loggerMock, attachmentDir.expandPath("Result3.xml"), attachmentDir, TASK_DTO_1);
        final String id = c.internalError("Error message", "ERROR message in file".getBytes(), "text/plain");

        final TestTaskResultDto result = dao.getById(EidFactory.getDefault().createUUID(id)).getDto();
        assertEquals("Error message", result.getErrorMessage());
        assertNotNull(result.getAttachments());
        // Log file and error message in file
        assertEquals(2, result.getAttachments().size());
        AttachmentDto attachment = null;
        for (final AttachmentDto a : result.getAttachments()) {
            if ("internalError".equals(a.getType())) {
                attachment = a;
            }
        }
        assertNotNull(attachment);
        assertEquals("Internal error", attachment.getLabel());
        assertEquals("ERROR message in file", new IFile(attachment.getReferencedData()).readContent().toString());
    }

}
