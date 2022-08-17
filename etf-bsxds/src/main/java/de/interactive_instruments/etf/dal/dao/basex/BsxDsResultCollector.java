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
package de.interactive_instruments.etf.dal.dao.basex;

import java.io.*;
import java.util.*;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import de.interactive_instruments.IFile;
import de.interactive_instruments.MimeTypeUtils;
import de.interactive_instruments.SUtils;
import de.interactive_instruments.container.Pair;
import de.interactive_instruments.etf.dal.dao.DataStorage;
import de.interactive_instruments.etf.dal.dto.result.TestResultStatus;
import de.interactive_instruments.etf.dal.dto.result.TestTaskResultDto;
import de.interactive_instruments.etf.dal.dto.run.TestTaskDto;
import de.interactive_instruments.etf.model.EID;
import de.interactive_instruments.etf.model.EidFactory;
import de.interactive_instruments.etf.testdriver.AbstractTestCollector;
import de.interactive_instruments.etf.testdriver.AbstractTestResultCollector;
import de.interactive_instruments.etf.testdriver.TestRunLogger;
import de.interactive_instruments.etf.testdriver.TestTaskEndListener;
import de.interactive_instruments.exceptions.ExcUtils;
import de.interactive_instruments.exceptions.MimeTypeUtilsException;
import de.interactive_instruments.exceptions.ObjectWithIdNotFoundException;
import de.interactive_instruments.exceptions.StorageException;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
final class BsxDsResultCollector extends AbstractTestResultCollector {

    private final TestRunLogger testRunLogger;
    private final IFile tmpDir;
    private final IFile attachmentDir;
    private final IFile resultFile;
    private final TestTaskDto testTaskDto;
    private final DataStorage dataStorage;
    private final BufferedOutputStream fileOutputStream;
    private final List<String> testStepAttachmentIds = new ArrayList<>(8);
    private final XmlTestResultWriter writer;
    private final int errorLimit;
    private boolean internalError = false;
    private TestTaskEndListener listener;

    public BsxDsResultCollector(final DataStorage dataStorage, final TestRunLogger testRunLogger, final IFile resultFile,
            final IFile attachmentDir, final TestTaskDto testTaskDto) {
        this.testRunLogger = testRunLogger;
        this.tmpDir = attachmentDir.secureExpandPathDown("tmp");
        this.tmpDir.mkdirs();
        this.attachmentDir = attachmentDir;
        this.testTaskDto = testTaskDto;
        this.resultFile = resultFile;
        this.tmpDir.setIdentifier("Test Task " + testTaskDto.getId() + " temporary directory ");
        this.dataStorage = dataStorage;
        final String errorLimitStr = testTaskDto.getArguments().value("maximum_number_of_error_messages_per_test");
        // default fallback
        int errorLimitTmp = 150;
        if (!SUtils.isNullOrEmpty(errorLimitStr)) {
            try {
                errorLimitTmp = Integer.parseInt(errorLimitStr);
            } catch (final NumberFormatException e) {
                logger.error("Invalid error limit ", e);
            }
        }
        errorLimit = errorLimitTmp;
        try {
            fileOutputStream = new BufferedOutputStream(new FileOutputStream(resultFile), 16384);
            writer = new XmlTestResultWriter(XMLOutputFactory.newInstance().createXMLStreamWriter(
                    fileOutputStream, "UTF-8"),
                    errorLimit);
        } catch (XMLStreamException | IOException e) {
            throw new IllegalStateException(e);
        }
    }

    // TODO remove in 2.1.0
    @Deprecated
    @Override
    public IFile getAttachmentDir() {
        return attachmentDir;
    }

    // TODO remove in 2.1.0
    @Deprecated
    @Override
    public IFile getResultFile() {
        return resultFile;
    }

    @Override
    public boolean endWithSkippedIfTestCasesFailed(final String... strings)
            throws IllegalArgumentException, IllegalStateException {
        return false;
    }

    // Start writing results
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected String startTestTaskResult(final String resultedFrom, final long startTimestamp) throws XMLStreamException {
        return writer.writeStartTestTaskResult(
                resultedFrom, startTimestamp, testTaskDto.getTestObject().getId().getId());
    }

    protected String startTestModuleResult(final String resultedFrom, final long startTimestamp) throws XMLStreamException {
        return writer.writeStartTestModuleResult(resultedFrom, startTimestamp);
    }

    protected String doStartTestCaseResult(final String resultedFrom, final long startTimestamp) throws XMLStreamException {
        return writer.writeStartTestCaseResult(resultedFrom, startTimestamp);
    }

    protected String doStartTestStepResult(final String resultedFrom, final long startTimestamp) throws XMLStreamException {
        return writer.writeStartTestStepResult(resultedFrom, startTimestamp);
    }

    protected String doStartTestAssertionResult(final String resultedFrom, final long startTimestamp)
            throws XMLStreamException {
        return writer.writeStartTestAssertionResult(resultedFrom, startTimestamp);
    }

    // Finish results
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected String endTestTaskResult(final String testModelItemId, final int status, final long stopTimestamp)
            throws XMLStreamException, FileNotFoundException, StorageException {
        if (!this.internalError) {
            // Add log file as attachment
            writer.addAttachment(
                    EidFactory.getDefault().createRandomId().toString(),
                    new IFile(testRunLogger.getLogFile()), "Log file", "UTF-8", "text/plain", "LogFile");
            final String id = writer.writeEndTestTaskResult(testModelItemId, status, stopTimestamp);
            try {
                writer.flush();
                final EID resultId = ((AbstractBsxStreamWriteDao) dataStorage.getDao(
                        TestTaskResultDto.class)).addAndValidate(testTaskDto.getParent(), new FileInputStream(resultFile));
                if (listener != null) {
                    listener.testTaskFinished(dataStorage.getDao(TestTaskResultDto.class).getById(resultId).getDto());
                }
            } catch (ObjectWithIdNotFoundException e) {
                testRunLogger.error("Failed to reload result ", e);
                throw new StorageException(e);
            } catch (StorageException e) {
                testRunLogger.error("Failed to stream result file into store: {}", resultFile.getPath());
                throw e;
            } finally {
                writer.close();
            }
            return id;
        }
        return null;
    }

    protected String endTestModuleResult(final String testModelItemId, final int status, final long stopTimestamp)
            throws XMLStreamException {
        return writer.writeEndTestModuleResult(testModelItemId, status, stopTimestamp);
    }

    protected String endTestCaseResult(final String testModelItemId, final int status, final long stopTimestamp)
            throws XMLStreamException {
        return writer.writeEndTestCaseResult(testModelItemId, status, stopTimestamp);
    }

    protected String endTestStepResult(final String testModelItemId, final int status, final long stopTimestamp)
            throws XMLStreamException {
        writer.finalizeMessages();
        if (!testStepAttachmentIds.isEmpty()) {
            writer.addAttachmentRefs(testStepAttachmentIds);
            testStepAttachmentIds.clear();
        }
        return writer.writeEndTestStepResult(testModelItemId, status, stopTimestamp);
    }

    protected String endTestAssertionResult(final String testModelItemId, final int status, final long stopTimestamp)
            throws XMLStreamException {
        writer.finalizeMessages();
        return writer.writeEndTestAssertionResult(testModelItemId, status, stopTimestamp);
    }

    @Override
    protected void startInvokedTests() {
        try {
            writer.writeStartInvokedTests();
        } catch (XMLStreamException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    protected void endInvokedTests() {
        try {
            writer.writeEndInvokedTests();
        } catch (XMLStreamException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    protected void startTestAssertionResults() {
        try {
            writer.writeStartTestAssertionResults();
        } catch (XMLStreamException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    protected void endTestAssertionResults() {
        try {
            writer.writeEndTestAssertionResults();
        } catch (XMLStreamException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    protected void notifyError() {
        try {
            writer.flush();
        } catch (NullPointerException | XMLStreamException e) {
            ExcUtils.suppress(e);
        }
    }

    @Override
    protected AbstractTestCollector createCalledTestCaseResultCollector(final AbstractTestCollector parentCollector,
            final String testModelItemId, final long startTimestamp) {
        final IFile testCaseResultFile = this.tmpDir.secureExpandPathDown("TestCaseResult-EID" + testModelItemId);
        return new BsxDsTestCaseResultCollector(this, testStepAttachmentIds, testCaseResultFile, testModelItemId,
                startTimestamp);
    }

    @Override
    protected AbstractTestCollector createCalledTestStepResultCollector(final AbstractTestCollector parentCollector,
            final String testModelItemId, final long startTimestamp) {
        return new BsxDsTestStepResultCollector(this, testStepAttachmentIds, testModelItemId, startTimestamp);
    }

    @Override
    protected void mergeResultFromCollector(final AbstractTestCollector collector) {
        try {
            writer.flush();
            ((BsxDsResultCollectorWriter) collector).writeTo(fileOutputStream);
        } catch (Exception e) {
            logger.error("Failed to append collector results: ", e);
            throw new IllegalStateException(e);
        }
    }

    @Override
    protected String currentResultItemId() {
        return writer.currentResultItemId();
    }

    @Override
    public File getTempDir() {
        return tmpDir;
    }

    @Override
    public TestRunLogger getLogger() {
        return this.testRunLogger;
    }

    @Override
    public TestResultStatus status(final String testModelItemId) throws IllegalArgumentException {
        return TestResultStatus.valueOf(getContextStatus());
    }

    @Override
    public boolean statusEqualsAny(final String testModelItemId, final String... testResultStatus)
            throws IllegalArgumentException {
        throw new IllegalStateException("Unimplemented");
    }

    @Override
    public final boolean isErrorLimitExceeded() {
        return writer.isErrorLimitExceeded();
    }

    @Override
    public void doAddMessage(final String translationTemplateId) {
        writer.addMessage(translationTemplateId);
    }

    @Override
    public void doAddMessage(final String translationTemplateId, final Map<String, String> tokenValuePairs) {
        writer.addMessage(translationTemplateId, tokenValuePairs);
    }

    @Override
    public void doAddMessage(final String translationTemplateId, final String... tokensAndValues) {
        writer.addMessage(translationTemplateId, tokensAndValues);
    }

    @Override
    public String markAttachment(final String fileName, final String label, final String encoding, String mimeType,
            final String type) throws IOException {
        final IFile attachmentFile = tmpDir.secureExpandPathDown(fileName);
        attachmentFile.expectFileIsReadable();
        final String eid = UUID.randomUUID().toString();
        if (mimeType == null) {
            try {
                mimeType = MimeTypeUtils.detectMimeType(attachmentFile);
            } catch (final MimeTypeUtilsException ign) {
                ExcUtils.suppress(ign);
            }
            if (SUtils.isNullOrEmpty(mimeType)) {
                mimeType = "text/plain";
            }
        }
        writer.addAttachment(eid, attachmentFile, label, encoding, mimeType, type);
        if (currentModelType() == 4) {
            testStepAttachmentIds.add(eid);
        }
        return "EID" + eid;
    }

    private IFile createAttachmentFile(final String eid, final String mimeType) {
        String extension = "";
        if (mimeType != null) {
            try {
                extension = MimeTypeUtils.getFileExtensionForMimeType(mimeType);
            } catch (MimeTypeUtilsException e) {
                ExcUtils.suppress(e);
            }
        }
        return attachmentDir.secureExpandPathDown(eid + extension);
    }

    @Override
    public String saveAttachment(final InputStream inputStream, final String label, final String mimeType, final String type)
            throws IOException {
        final String eid = UUID.randomUUID().toString();
        final IFile attachmentFile = createAttachmentFile(eid, mimeType);
        attachmentFile.writeContent(inputStream, "UTF-8");
        try {
            final Pair<String, IFile> mimeTypeAndFilePair = MimeTypeUtils.setFileExtension(attachmentFile, mimeType);
            writer.addAttachment(eid, mimeTypeAndFilePair.getRight(), label, "UTF-8", mimeTypeAndFilePair.getLeft(), type);
        } catch (IOException | MimeTypeUtilsException e) {
            ExcUtils.suppress(e);
        }
        if (currentModelType() == 4) {
            testStepAttachmentIds.add(eid);
        }
        return "EID" + eid;
    }

    @Override
    public String saveAttachment(final Reader reader, final String label, final String mimeType, final String type)
            throws IOException {
        final String eid = UUID.randomUUID().toString();
        final IFile attachmentFile = createAttachmentFile(eid, mimeType);
        IOUtils.copy(reader, new FileOutputStream(attachmentFile), "UTF-8");
        try {
            final Pair<String, IFile> mimeTypeAndFilePair = MimeTypeUtils.setFileExtension(attachmentFile, mimeType);
            writer.addAttachment(eid, mimeTypeAndFilePair.getRight(), label, "UTF-8", mimeTypeAndFilePair.getLeft(), type);
        } catch (IOException | MimeTypeUtilsException e) {
            ExcUtils.suppress(e);
        }
        if (currentModelType() == 4) {
            testStepAttachmentIds.add(eid);
        }
        return "EID" + eid;
    }

    @Override
    public String saveAttachment(final String content, final String label, String mimeType, final String type)
            throws IOException {
        final String eid = UUID.randomUUID().toString();
        if (mimeType == null) {
            try {
                mimeType = MimeTypeUtils.detectMimeType(content);
            } catch (final MimeTypeUtilsException ign) {
                ExcUtils.suppress(ign);
            }
            if (SUtils.isNullOrEmpty(mimeType)) {
                mimeType = "text/plain";
            }
        }
        writer.addAttachment(eid, Base64.getEncoder().encode(content.getBytes("UTF-8")), label, "UTF-8", mimeType, type);
        if (currentModelType() == 4) {
            testStepAttachmentIds.add(eid);
        }
        return "EID" + eid;
    }

    @Override
    public void internalError(final String translationTemplateId, final Map<String, String> tokenValuePairs,
            final Throwable e) {
        if (!this.internalError) {
            this.internalError = true;
        }
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public void internalError(final Throwable e) {
        final String errorMessage;
        if (e != null) {
            if (e.getMessage() != null) {
                errorMessage = e.getMessage();
            } else if (!SUtils.isNullOrEmpty(ExceptionUtils.getRootCauseMessage(e))) {
                errorMessage = ExceptionUtils.getRootCauseMessage(e);
            } else {
                errorMessage = e.getClass().getName();
            }
        } else {
            errorMessage = "Unknown error";
        }
        internalError(errorMessage, (byte[]) null, null);
    }

    @Override
    public String internalError(final String errorMessage, final byte[] bytes, final String mimeType) {
        if (!this.internalError) {
            this.internalError = true;

            try {
                resultFile.delete();
                final BufferedOutputStream errorOutputStream = new BufferedOutputStream(new FileOutputStream(resultFile),
                        16384);
                final XMLStreamWriter errorWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(
                        errorOutputStream, "UTF-8");

                if (bytes != null) {
                    final String attachmentEid = UUID.randomUUID().toString();
                    final IFile attachmentFile = createAttachmentFile(attachmentEid, mimeType);
                    IOUtils.write(bytes, new FileOutputStream(attachmentFile));
                    XmlTestResultWriter.internalError(errorWriter,
                            testTaskDto.getExecutableTestSuite().getId().getId(),
                            testTaskDto.getTestObject().getId().getId(),
                            errorMessage, testRunLogger.getLogFile(),
                            attachmentEid, attachmentFile, mimeType);
                } else {
                    XmlTestResultWriter.internalError(errorWriter,
                            testTaskDto.getExecutableTestSuite().getId().getId(),
                            testTaskDto.getTestObject().getId().getId(),
                            errorMessage, testRunLogger.getLogFile(),
                            null, null, null);
                }

                final EID resultId = ((AbstractBsxStreamWriteDao) dataStorage.getDao(
                        TestTaskResultDto.class)).addAndValidate(testTaskDto.getParent(), new FileInputStream(resultFile));
                if (listener != null) {
                    listener.testTaskFinished(dataStorage.getDao(TestTaskResultDto.class).getById(resultId).getDto());
                }
                return resultId.toString();
            } catch (XMLStreamException | ObjectWithIdNotFoundException | IOException e) {
                throw new IllegalStateException("Could not save internal error", e);
            }
        }
        return null;
    }

    @Override
    public void release() {

    }

    @Override
    public void registerTestTaskEndListener(final TestTaskEndListener listener) {
        this.listener = listener;
    }
}
