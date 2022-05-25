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

import de.interactive_instruments.etf.dal.dto.Arguments;
import de.interactive_instruments.etf.dal.dto.DtoValidityCheckUtils;
import de.interactive_instruments.etf.dal.dto.IncompleteDtoException;
import de.interactive_instruments.etf.dal.dto.ModelItemDto;
import de.interactive_instruments.etf.dal.dto.capabilities.ResultStyleDto;
import de.interactive_instruments.etf.dal.dto.capabilities.TestObjectDto;
import de.interactive_instruments.etf.dal.dto.result.TestTaskResultDto;
import de.interactive_instruments.etf.dal.dto.test.ExecutableTestSuiteDto;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public class TestTaskDto extends ModelItemDto {

    private ExecutableTestSuiteDto executableTestSuite;
    private TestObjectDto testObject;
    private ResultStyleDto resultStyle;
    private TestTaskResultDto testTaskResult;
    private Arguments arguments;

    public TestTaskDto() {}

    private TestTaskDto(final TestTaskDto other) {
        super(other);
        this.executableTestSuite = other.executableTestSuite;
        this.testObject = other.testObject;
        this.resultStyle = other.resultStyle;
        this.testTaskResult = other.testTaskResult;
        this.arguments = other.arguments;
    }

    public ExecutableTestSuiteDto getExecutableTestSuite() {
        return executableTestSuite;
    }

    public void setExecutableTestSuite(final ExecutableTestSuiteDto executableTestSuite) {
        this.executableTestSuite = executableTestSuite;
    }

    public TestObjectDto getTestObject() {
        return testObject;
    }

    public void setTestObject(final TestObjectDto testObject) {
        this.testObject = testObject;
    }

    public ResultStyleDto getResultStyle() {
        return resultStyle;
    }

    public void setResultStyle(final ResultStyleDto resultStyle) {
        this.resultStyle = resultStyle;
    }

    public TestTaskResultDto getTestTaskResult() {
        return testTaskResult;
    }

    public void setTestTaskResult(final TestTaskResultDto testTaskResult) {
        this.testTaskResult = testTaskResult;
        if (testTaskResult != null) {
            testTaskResult.setParent(this);
        }
    }

    public Arguments getArguments() {
        if (arguments == null) {
            arguments = new Arguments();
        }
        return arguments;
    }

    public void setArguments(final Arguments arguments) {
        this.arguments = arguments;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("TestTaskDto{");
        sb.append("executableTestSuite=").append(executableTestSuite);
        sb.append(", id=").append(id.toString());
        sb.append(", testObject=").append(testObject);
        sb.append(", resultStyle=").append(resultStyle);
        sb.append(", testTaskResult=").append(testTaskResult);
        sb.append(", argumentCollection=").append(arguments);
        sb.append('}');
        return sb.toString();
    }

    public void ensureBasicValidity() throws IncompleteDtoException {
        super.ensureBasicValidity();
        DtoValidityCheckUtils.ensureNotNullAndHasId("executableTestSuite", executableTestSuite);
        DtoValidityCheckUtils.ensureNotNullAndHasId("executableTestSuite.testDriver", executableTestSuite.getTestDriver());
        DtoValidityCheckUtils.ensureNotNullAndHasId("testObject", testObject);
    }

    @Override
    public TestTaskDto createCopy() {
        return new TestTaskDto(this);
    }
}
