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
package de.interactive_instruments.etf.test;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.interactive_instruments.IFile;
import de.interactive_instruments.SUtils;
import de.interactive_instruments.etf.dal.dao.DataStorage;
import de.interactive_instruments.etf.dal.dto.Dto;
import de.interactive_instruments.etf.dal.dto.result.*;
import de.interactive_instruments.etf.dal.dto.run.TestTaskDto;
import de.interactive_instruments.etf.dal.dto.test.*;
import de.interactive_instruments.etf.dal.dto.translation.TranslationArgumentCollectionDto;
import de.interactive_instruments.etf.model.DefaultEidMap;
import de.interactive_instruments.etf.model.EID;
import de.interactive_instruments.etf.model.EidFactory;
import de.interactive_instruments.etf.model.EidMap;
import de.interactive_instruments.etf.testdriver.TestResultCollector;
import de.interactive_instruments.etf.testdriver.TestRunLogger;
import de.interactive_instruments.etf.testdriver.TestTaskEndListener;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public class InMemoryTestResultCollector implements TestResultCollector {

    private final DataStorage inMemoryStorage;
    private final TestRunLogger logger;
    private final TestTaskDto testTaskDto;
    private final EidMap<AttachmentDto> attachments = new DefaultEidMap<>();

    private final TestTaskResultDto testTaskResult;
    private final List<TestModuleResultDto> testModuleResults = new ArrayList<>();
    private final List<TestCaseResultDto> testCaseResults = new ArrayList<>();
    private final List<TestStepResultDto> testStepResults = new ArrayList<>();
    private final List<TestAssertionResultDto> testAssertionResults = new ArrayList<>();

    private final static String resultID = "00000000-0000-0000-C000-000000000046";
    private TestTaskEndListener listener;
    private int currentModelType;

    public InMemoryTestResultCollector(final DataStorage inMemoryStorage, final TestRunLogger logger,
            final TestTaskDto testTaskDto) {
        this.inMemoryStorage = inMemoryStorage;
        this.logger = logger;
        this.testTaskDto = testTaskDto;
        this.testTaskResult = testTaskDto.getTestTaskResult();
        currentModelType = 0;
    }

    @Override
    public IFile getAttachmentDir() {
        return null;
    }

    @Override
    public IFile getResultFile() {
        return null;
    }

    @Override
    public String getTestTaskResultId() {
        return resultID;
    }

    @Override
    public File getTempDir() {
        try {
            return IFile.createTempDir("etf-unittests");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean endWithSkippedIfTestCasesFailed(final String... testCaseIds)
            throws IllegalArgumentException, IllegalStateException {
        return false;
    }

    @Override
    public TestResultStatus status(final String testModelItemId) throws IllegalArgumentException {
        return null;
    }

    @Override
    public boolean statusEqualsAny(final String testModelItemId, final String... testResultStatus)
            throws IllegalArgumentException {
        return false;
    }

    @Override
    public boolean isErrorLimitExceeded() {
        return false;
    }

    private EID createAttachment(String label, String encoding, String mimeType, String type) {
        final AttachmentDto attachmentDto = new AttachmentDto();
        attachmentDto.setLabel(label);
        attachmentDto.setEncoding(encoding);
        attachmentDto.setMimeType(mimeType);
        attachmentDto.setType(type);
        attachmentDto.setEmbeddedData("dummy");
        attachmentDto.setId(EidFactory.getDefault().createRandomId());
        this.attachments.put(attachmentDto.getId(), attachmentDto);
        return attachmentDto.getId();
    }

    @Override
    public String markAttachment(final String fileName, final String label, final String encoding, final String mimeType,
            final String type) throws IOException {
        return createAttachment(label, encoding, mimeType, type).getId();
    }

    @Override
    public String saveAttachment(final Reader reader, final String label, final String mimeType, final String type)
            throws IOException {
        return createAttachment(label, "UTF-8", mimeType, type).getId();
    }

    @Override
    public String saveAttachment(final InputStream inputStream, final String label, final String mimeType, final String type)
            throws IOException {
        return createAttachment(label, "UTF-8", mimeType, type).getId();
    }

    @Override
    public String saveAttachment(final String content, final String label, final String mimeType, final String type)
            throws IOException {
        return createAttachment(label, "UTF-8", mimeType, type).getId();
    }

    @Override
    public void internalError(final String translationTemplateId, final Map<String, String> tokenValuePairs,
            final Throwable e) {
        internalError(e);
    }

    @Override
    public void internalError(final Throwable e) {
        if (this.testTaskResult.getResultStatus() != TestResultStatus.INTERNAL_ERROR) {
            this.testTaskResult.setResultStatus(TestResultStatus.INTERNAL_ERROR);
            try {
                saveAttachment(new ByteArrayInputStream(e.getMessage().getBytes()), "error", "text/plain", "error");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            testTaskResult.setAttachments(this.attachments.asList());
        }
    }

    @Override
    public String internalError(final String errorMessage, final byte[] bytes, final String mimeType) {
        try {
            testTaskResult.setInternalError(new IllegalArgumentException(errorMessage));
            this.testTaskResult.setResultStatus(TestResultStatus.INTERNAL_ERROR);
            final String attachmentId = saveAttachment(
                    new ByteArrayInputStream(bytes != null ? bytes : errorMessage.getBytes()), "error", mimeType, "error");
            testTaskResult.setAttachments(this.attachments.asList());
            return attachmentId;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public TestRunLogger getLogger() {
        return this.logger;
    }

    private void stepDeeper(final int levelCheck) {
        currentModelType = levelCheck;
    }

    @Override
    public String startTestTask(final String testTaskId, final long startTimestamp)
            throws IllegalArgumentException, IllegalStateException {
        stepDeeper(1);
        if (this.testTaskResult.getId() == null) {
            this.testTaskResult.setId(EidFactory.getDefault().createRandomId());
        }
        if (this.testTaskResult.getResultedFrom() == null) {
            this.testTaskResult.setResultedFrom(this.testTaskDto.getExecutableTestSuite());
        }
        this.testTaskResult.setStartTimestamp(new Date(startTimestamp));
        return testTaskResult.getId().getId();
    }

    @Override
    public String startTestModule(final String testModuleId, final long startTimestamp)
            throws IllegalArgumentException, IllegalStateException {
        stepDeeper(2);
        final TestModuleResultDto testModuleResultDto = new TestModuleResultDto();
        testModuleResultDto.setParent(this.testTaskResult);
        testModuleResultDto.setId(EidFactory.getDefault().createRandomId());
        testModuleResultDto.setResultedFrom(findInTest(testModuleId));
        testTaskResult.addChild(testModuleResultDto);
        this.testModuleResults.add(testModuleResultDto);
        testModuleResultDto.setStartTimestamp(new Date(startTimestamp));
        return testModuleResultDto.getId().getId();
    }

    @Override
    public String startTestCase(final String testCaseId, final long startTimestamp)
            throws IllegalArgumentException, IllegalStateException {
        stepDeeper(3);
        final TestCaseResultDto testCaseResultDto = new TestCaseResultDto();
        testCaseResultDto.setId(EidFactory.getDefault().createRandomId());
        testCaseResultDto.setResultedFrom(findInTest(testCaseId));
        addParentChildRelation(testCaseResultDto, testModuleResults);
        testCaseResultDto.setStartTimestamp(new Date(startTimestamp));
        this.testCaseResults.add(testCaseResultDto);
        return testCaseResultDto.getId().getId();
    }

    @Override
    public String startTestStep(final String testStepId, final long startTimestamp)
            throws IllegalArgumentException, IllegalStateException {
        stepDeeper(4);
        final TestStepResultDto testStepResultDto = new TestStepResultDto();
        testStepResultDto.setId(EidFactory.getDefault().createRandomId());
        testStepResultDto.setResultedFrom(findInTest(testStepId));
        addParentChildRelation(testStepResultDto, testCaseResults);
        testStepResultDto.setStartTimestamp(new Date(startTimestamp));
        testStepResultDto.setResultStatus(TestResultStatus.UNDEFINED);
        this.testStepResults.add(testStepResultDto);
        return testStepResultDto.getId().getId();
    }

    @Override
    public String startTestAssertion(final String testAssertionId, final long startTimestamp)
            throws IllegalArgumentException, IllegalStateException {
        stepDeeper(5);
        final TestAssertionResultDto testAssertionResultDto = new TestAssertionResultDto();
        testAssertionResultDto.setId(EidFactory.getDefault().createRandomId());
        testAssertionResultDto.setResultedFrom(findInTest(testAssertionId));
        addParentChildRelation(testAssertionResultDto, testStepResults);
        testAssertionResultDto.setStartTimestamp(new Date(startTimestamp));
        this.testAssertionResults.add(testAssertionResultDto);
        return testAssertionResultDto.getId().getId();
    }

    private static TestModelItemDto findInTest(final String testModelItemId, final EidMap<? extends TestModelItemDto> items,
            final int maxSteps) {
        if (items != null) {
            final TestModelItemDto i = items.get(testModelItemId);
            if (i != null) {
                return i;
            }
            if (maxSteps > 0) {
                for (final Object item : items.asCollection()) {
                    if (((TestModelItemDto) item).getChildrenAsMap() != null) {
                        final TestModelItemDto child = findInTest(testModelItemId,
                                ((TestModelItemDto) item).getChildrenAsMap(), maxSteps - 1);
                        if (child != null) {
                            return child;
                        }
                    }
                }
            }
        }
        return null;
    }

    private static class PlaceHolder extends TestModelItemDto {
        public PlaceHolder(final String testModelItemId) {
            this.id = EidFactory.getDefault().createUUID(testModelItemId);
        }

        @Override
        public Dto createCopy() {
            final PlaceHolder copy = new PlaceHolder(this.id.getId());
            copy.children = this.children;
            return copy;
        }
    }

    private <T extends TestModelItemDto> T findInTest(final String testModelItemId) {
        final ExecutableTestSuiteDto ets = testTaskDto.getExecutableTestSuite();
        final T t = (T) findInTest(testModelItemId, ets.getChildrenAsMap(), 25);
        if (t == null) {
            return (T) new PlaceHolder(testModelItemId);
        }
        return t;
    }

    private static <T extends ResultModelItemDto> T findInResult(final String testModelItemId, final List<T> items) {
        if (items != null) {
            for (final T item : items) {
                if (item.getResultedFrom().getId().equals(testModelItemId)) {
                    return item;
                }
            }
        }
        return null;
    }

    private ResultModelItemDto findFromResultAndSetLevel(final String testModelItemId) {
        final TestAssertionResultDto testAssertionResult = findInResult(testModelItemId, testAssertionResults);
        if (testAssertionResult != null) {
            currentModelType = 4;
            return testAssertionResult;
        }

        final TestStepResultDto testStepResult = findInResult(testModelItemId, testStepResults);
        if (testStepResult != null) {
            currentModelType = 3;
            return testStepResult;
        }

        final TestCaseResultDto testCaseResult = findInResult(testModelItemId, testCaseResults);
        if (testCaseResult != null) {
            currentModelType = 2;
            return testCaseResult;
        }

        final TestModuleResultDto testModuleResult = findInResult(testModelItemId, testModuleResults);
        if (testModuleResult != null) {
            currentModelType = 1;
            return testModuleResult;
        }
        if (testTaskResult.getResultedFrom().getId().equals(testModelItemId) || testTaskDto.getId().equals(testModelItemId)) {
            currentModelType = 0;
            return testTaskResult;
        }
        return null;
    }

    private void setResult(final String testModelItemId, final ResultModelItemDto item, final long stopTimestamp,
            final int status) {
        final ResultModelItemDto candidate = findFromResultAndSetLevel(testModelItemId);
        if (candidate == null) {
            throw new IllegalArgumentException("Not found: " + testModelItemId);
        }
        candidate.setDuration(stopTimestamp - candidate.getStartTimestamp().getTime());
        final List<? extends ResultModelItemDto> children = candidate.getChildren();
        if (children != null) {
            final List<TestResultStatus> resultStatus = children.stream().map(
                    ResultModelItemDto::getResultStatus).collect(Collectors.toList());
            resultStatus.add(TestResultStatus.valueOf(status));
            candidate.setResultStatus(TestResultStatus.aggregateStatus(resultStatus));
        } else {
            candidate.setResultStatus(TestResultStatus.valueOf(status));
        }
    }

    private static void addParentChildRelation(final ResultModelItemDto child,
            final List<? extends ResultModelItemDto> parents) {
        final ResultModelItemDto parent = getLast(parents);
        parent.addChild(child);
        child.setParent(parent);
    }

    private static <T extends ResultModelItemDto> T getLast(final List<T> items) {
        if (items.isEmpty()) {
            throw new IllegalStateException();
        }
        return items.get(items.size() - 1);
    }

    @Override
    public String end(final String testModelItemId, final int status, final long stopTimestamp)
            throws IllegalArgumentException, IllegalStateException {
        switch (currentModelType--) {
        case 1:
            this.listener.testTaskFinished(this.testTaskResult);
            setResult(testModelItemId, this.testTaskResult, stopTimestamp, status);
            testTaskResult.setAttachments(this.attachments.asList());
            return testTaskResult.getId().getId();
        case 2:
            final TestModuleResultDto moduleResult = getLast(this.testModuleResults);
            setResult(testModelItemId, moduleResult, stopTimestamp, status);
            return moduleResult.getId().getId();
        case 3:
            final TestCaseResultDto testCaseResultDto = getLast(this.testCaseResults);
            setResult(testModelItemId, testCaseResultDto, stopTimestamp, status);
            return testCaseResultDto.getId().getId();
        case 4:
            final TestStepResultDto testStepDto = getLast(this.testStepResults);
            setResult(testModelItemId, testStepDto, stopTimestamp, status);
            return testStepDto.getId().getId();
        case 5:
            final TestAssertionResultDto testAssertionDto = getLast(this.testAssertionResults);
            setResult(testModelItemId, testAssertionDto, stopTimestamp, status);
            return testAssertionDto.getId().getId();
        default:
            currentModelType++;
            throw new IllegalStateException("Illegal state: " + currentModelType);
        }
    }

    @Override
    public String end(final String testModelItemId, final long stopTimestamp)
            throws IllegalArgumentException, IllegalStateException {
        return end(testModelItemId, 0, stopTimestamp);
    }

    @Override
    public void addMessage(final String translationTemplateId) {
        addMessage(translationTemplateId, (Map<String, String>) null);
    }

    @Override
    public void addMessage(final String translationTemplateId, final Map<String, String> tokenValuePairs) {
        final TranslationArgumentCollectionDto transArgument = new TranslationArgumentCollectionDto();
        transArgument.setRefTemplateName(translationTemplateId);
        if (tokenValuePairs != null) {
            for (final Map.Entry<String, String> e : tokenValuePairs.entrySet()) {
                transArgument.addTokenValue(e.getKey(), e.getValue());
            }
        }
        if (this.currentModelType == 5) {
            getLast(testAssertionResults).addMessage(transArgument);
        } else {
            getLast(testStepResults).addMessage(transArgument);
        }
    }

    @Override
    public void addMessage(final String translationTemplateId, final String... tokensAndValues) {
        addMessage(translationTemplateId, SUtils.toStrMap(tokensAndValues));
    }

    @Override
    public int currentModelType() {
        return this.currentModelType;
    }

    @Override
    public void registerTestTaskEndListener(final TestTaskEndListener listener) {
        this.listener = listener;
    }

    @Override
    public void release() {}
}
