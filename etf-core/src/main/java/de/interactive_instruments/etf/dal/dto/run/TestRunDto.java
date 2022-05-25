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
package de.interactive_instruments.etf.dal.dto.run;

import java.util.*;
import java.util.stream.Collectors;

import de.interactive_instruments.etf.dal.dto.DtoValidityCheckUtils;
import de.interactive_instruments.etf.dal.dto.IncompleteDtoException;
import de.interactive_instruments.etf.dal.dto.ModelItemDto;
import de.interactive_instruments.etf.dal.dto.capabilities.TestObjectDto;
import de.interactive_instruments.etf.dal.dto.result.TestResultStatus;
import de.interactive_instruments.etf.dal.dto.result.TestTaskResultDto;
import de.interactive_instruments.etf.dal.dto.test.ExecutableTestSuiteDto;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public class TestRunDto extends ModelItemDto {

    private String label;
    private String defaultLang;
    private Date startTimestamp;
    private String startedBy;
    private List<TestTaskDto> testTasks;
    private String logPath;
    // FIXME aggregate from test tasks
    private TestResultStatus testResultStatus;
    private final Map<String, String> exchange = new HashMap<>();

    public TestRunDto() {

    }

    private TestRunDto(final TestRunDto other) {
        super(other);
        this.id = other.id;
        this.label = other.label;
        this.defaultLang = other.defaultLang;
        this.startTimestamp = other.startTimestamp;
        this.startedBy = other.startedBy;
        if (other.testTasks != null) {
            this.testTasks = new ArrayList<>();
            this.testTasks.addAll(other.testTasks.stream().map(
                    TestTaskDto::createCopy).collect(Collectors.toList()));
        }
        this.logPath = other.logPath;
        this.testResultStatus = other.testResultStatus;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    public String getDefaultLang() {
        return defaultLang;
    }

    public void setDefaultLang(final String defaultLang) {
        this.defaultLang = defaultLang;
    }

    public Date getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(final Date startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public String getStartedBy() {
        return startedBy;
    }

    public void setStartedBy(final String startedBy) {
        this.startedBy = startedBy;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(final String logPath) {
        this.logPath = logPath;
    }

    public List<TestTaskDto> getTestTasks() {
        return testTasks;
    }

    public void setTestTasks(final List<TestTaskDto> testTasks) {
        this.testTasks = testTasks;
        this.testTasks.forEach(t -> t.setParent(this));
    }

    public void addTestTask(final TestTaskDto testTask) {
        if (this.testTasks == null) {
            this.testTasks = new ArrayList<>();
        }
        testTask.setParent(this);
        testTasks.add(testTask);
    }

    public List<TestObjectDto> getTestObjects() {
        if (this.testTasks != null) {
            return this.testTasks.stream().map(TestTaskDto::getTestObject).collect(Collectors.toList());
        }
        return null;
    }

    public void setExchangeProperty(final String key, final String value) {
        this.exchange.put(key, value);
    }

    public String getExchangeProperty(final String key) {
        return this.exchange.get(key);
    }

    public List<ExecutableTestSuiteDto> getExecutableTestSuites() {
        if (this.testTasks != null) {
            return this.testTasks.stream().map(TestTaskDto::getExecutableTestSuite).collect(Collectors.toList());
        }
        return null;
    }

    public List<TestTaskResultDto> getTestTaskResults() {
        if (this.testTasks != null) {
            return this.testTasks.stream()
                    .filter(r -> r.getTestTaskResult() != null)
                    .map(TestTaskDto::getTestTaskResult)
                    .collect(Collectors.toList());
        }
        return null;
    }

    public String getTestResultStatus() {
        // return the status if one is set - otherwise determine the value
        if (testResultStatus == null) {
            final List<TestTaskResultDto> testTaskResultDtos = getTestTaskResults();
            if (testTaskResultDtos != null && !testTaskResultDtos.isEmpty()) {
                final List<TestResultStatus> results = testTaskResultDtos.stream()
                        .filter(t -> t.getResultStatus() != null)
                        .map(TestTaskResultDto::getResultStatus)
                        .collect(Collectors.toList());
                return TestResultStatus.aggregateStatus(results).toString();
            }
            return TestResultStatus.UNDEFINED.toString();
        } else {
            return testResultStatus.toString();
        }
    }

    public void setTestResultStatus(final TestResultStatus testResultStatus) {
        this.testResultStatus = testResultStatus;
    }

    public void setTestResultStatus(final String testResultStatus) {
        this.testResultStatus = TestResultStatus.valueOf(testResultStatus);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("TestRunDto{");
        sb.append("label='").append(label).append('\'');
        sb.append(", id=").append(id.toString());
        sb.append(", defaultLang='").append(defaultLang).append('\'');
        sb.append(", startTimestamp=").append(startTimestamp);
        sb.append(", startedBy='").append(startedBy).append('\'');
        sb.append(", testTasks=").append(testTasks);
        sb.append(", testResultStatus=").append(testResultStatus);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public String getDescriptiveLabel() {
        final StringBuilder labelBuilder = new StringBuilder(120);
        labelBuilder.append("'").append(label).append(" (EID: ").append(id).append(" )'");
        return labelBuilder.toString();
    }

    public void ensureBasicValidity() throws IncompleteDtoException {
        super.ensureBasicValidity();
        DtoValidityCheckUtils.ensureNotNullOrEmpty("label", label);
        DtoValidityCheckUtils.ensureNotNull("startTimestamp", startTimestamp);
        DtoValidityCheckUtils.ensureNotNullOrEmpty("defaultLang", defaultLang);
        DtoValidityCheckUtils.ensureNotNullOrEmpty("testTasks", testTasks);
    }

    @Override
    public TestRunDto createCopy() {
        return new TestRunDto(this);
    }

}
