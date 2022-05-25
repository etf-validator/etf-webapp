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
package de.interactive_instruments.etf.dal.dto.result;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import de.interactive_instruments.etf.dal.dto.capabilities.TestObjectDto;
import de.interactive_instruments.etf.model.DefaultEidMap;
import de.interactive_instruments.etf.model.EID;
import de.interactive_instruments.etf.model.EidMap;

public class TestTaskResultDto extends ResultModelItemDto {

    private EidMap<AttachmentDto> attachments;

    private TestObjectDto testObject;

    private String errorMessage;

    public TestTaskResultDto() {

    }

    private TestTaskResultDto(final TestTaskResultDto other) {
        super(other);
        this.attachments = other.attachments;
        this.testObject = other.testObject;
        this.errorMessage = other.errorMessage;
    }

    public List<TestModuleResultDto> getTestModuleResults() {
        return (List<TestModuleResultDto>) getChildren();
    }

    public void setTestModuleResults(final List<TestModuleResultDto> testSuiteResults) {
        if (errorMessage != null) {
            setChildren(testSuiteResults);
        }
    }

    public void addTestModuleResult(final TestModuleResultDto testModuleResultDto) {
        if (errorMessage != null) {
            addChild(testModuleResultDto);
        }
    }

    public Collection<AttachmentDto> getAttachments() {
        return attachments != null ? attachments.values() : null;
    }

    public void setAttachments(final List<AttachmentDto> attachments) {
        for (final AttachmentDto attachment : attachments) {
            addAttachment(attachment);
        }
    }

    public void setInternalError(final Exception e) {
        if (errorMessage != null) {
            throw new IllegalStateException("Internal error already set");
        }
        errorMessage = e.getMessage();
        children = null;
        if (startTimestamp == null) {
            startTimestamp = new Date();
        }
        duration = 1;
        resultStatus = TestResultStatus.UNDEFINED.toString();
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void addAttachment(final AttachmentDto attachment) {
        if (this.attachments == null) {
            this.attachments = new DefaultEidMap<>();
        }
        this.attachments.put(attachment.getId(), attachment);
    }

    public AttachmentDto getAttachmentById(final EID id) {
        return this.attachments != null ? this.attachments.get(id) : null;
    }

    public TestObjectDto getTestObject() {
        return testObject;
    }

    public void setTestObject(final TestObjectDto testObject) {
        this.testObject = testObject;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("TestTaskResultDto{");
        sb.append("id=").append(id.toString());
        sb.append(", attachments=").append(attachments);
        sb.append(", testObject=").append(testObject);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public TestTaskResultDto createCopy() {
        return new TestTaskResultDto(this);
    }
}
