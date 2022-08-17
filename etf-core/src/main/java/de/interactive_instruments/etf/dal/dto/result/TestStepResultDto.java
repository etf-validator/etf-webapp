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
package de.interactive_instruments.etf.dal.dto.result;

import java.util.ArrayList;
import java.util.List;

import de.interactive_instruments.etf.dal.dto.translation.TranslationArgumentCollectionDto;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public final class TestStepResultDto extends ResultModelItemDto implements AttachmentDtoHolder {

    private List<AttachmentDto> attachments;
    private List<TranslationArgumentCollectionDto> messages;
    private List<TestStepResultDto> invokedTestSteps;
    private List<TestCaseResultDto> invokedTestCases;

    public TestStepResultDto() {}

    private TestStepResultDto(final TestStepResultDto other) {
        super(other);
        this.attachments = other.attachments;
        this.invokedTestSteps = other.invokedTestSteps;
        this.invokedTestCases = other.invokedTestCases;
        this.messages = other.messages;
    }

    public List<TestAssertionResultDto> getTestAssertionResults() {
        return (List<TestAssertionResultDto>) getChildren();
    }

    public void setTestAssertionResults(final List<TestAssertionResultDto> testAssertionResults) {
        setChildren(testAssertionResults);
    }

    public void addTestAssertionResult(final TestAssertionResultDto testAssertionResult) {
        addChild(testAssertionResult);
    }

    public List<AttachmentDto> getAttachments() {
        return attachments;
    }

    public void setAttachments(final List<AttachmentDto> attachments) {
        this.attachments = attachments;
    }

    public void addAttachment(final AttachmentDto attachment) {
        if (this.attachments == null) {
            attachments = new ArrayList<>();
        }
        attachments.add(attachment);
    }

    public List<TestStepResultDto> getInvokedTestSteps() {
        return invokedTestSteps;
    }

    public void setInvokedTestSteps(final List<TestStepResultDto> invokedTestSteps) {
        this.invokedTestSteps = invokedTestSteps;
    }

    public void addInvokedTestSteps(final TestStepResultDto testStep) {
        if (invokedTestSteps == null) {
            invokedTestSteps = new ArrayList<>(2);
        }
        invokedTestSteps.add(testStep);
    }

    public List<TestCaseResultDto> getInvokedTestCases() {
        return invokedTestCases;
    }

    public void setInvokedTestCases(final List<TestCaseResultDto> invokedTestCases) {
        this.invokedTestCases = invokedTestCases;
    }

    public void addInvokedTestCases(final TestCaseResultDto testCase) {
        if (invokedTestCases == null) {
            invokedTestCases = new ArrayList<>(2);
        }
        invokedTestCases.add(testCase);
    }

    public List<TranslationArgumentCollectionDto> getMessages() {
        return messages;
    }

    public void setMessages(final List<TranslationArgumentCollectionDto> messages) {
        this.messages = messages;
    }

    public void addMessage(final TranslationArgumentCollectionDto message) {
        if (this.messages == null) {
            this.messages = new ArrayList<>();
        }
        this.messages.add(message);
    }

    @Override
    public TestStepResultDto createCopy() {
        return new TestStepResultDto(this);
    }
}
