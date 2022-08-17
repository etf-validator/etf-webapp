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
package de.interactive_instruments.etf.webapp.dto;

import static de.interactive_instruments.etf.webapp.dto.DocumentationConstants.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.*;

import de.interactive_instruments.SUtils;
import de.interactive_instruments.etf.dal.dao.DtoResolver;
import de.interactive_instruments.etf.dal.dao.PreparedDtoResolver;
import de.interactive_instruments.etf.dal.dto.capabilities.TestObjectDto;
import de.interactive_instruments.etf.dal.dto.run.TestRunDto;
import de.interactive_instruments.etf.dal.dto.run.TestTaskDto;
import de.interactive_instruments.etf.dal.dto.test.ExecutableTestSuiteDto;
import de.interactive_instruments.etf.model.EidFactory;
import de.interactive_instruments.etf.model.Parameterizable;
import de.interactive_instruments.etf.webapp.controller.DataStorageService;
import de.interactive_instruments.etf.webapp.controller.LocalizableApiError;
import de.interactive_instruments.etf.webapp.conversion.CharEscaping;
import de.interactive_instruments.etf.webapp.conversion.EidConverter;
import de.interactive_instruments.exceptions.ObjectWithIdNotFoundException;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
@JsonPropertyOrder({
        "label",
        "executableTestSuiteIds",
        "arguments",
        "testObject"
})
@ApiModel(value = "StartTestRunRequest", description = "Start a test run")
public class StartTestRunRequest extends AbstractTestRunRequest {

    @ApiModelProperty(position = 0, value = TEST_RUN_LABEL_DESCRIPTION
            + " Mandatory.", example = TEST_RUN_LABEL_EXAMPLE, dataType = "String", required = true)
    @JsonProperty(required = true)
    @NotNull(message = "{l.enter.label}")
    private String label;

    @ApiModelProperty(position = 1, value = "List of Executable Test Suite IDs. Mandatory."
            + EID_DESCRIPTION + ". See Implementation Notes for an complete example.", required = true)
    @JsonProperty(required = true)
    @NotNull(message = "{l.json.empty.ets.list}")
    private List<String> executableTestSuiteIds;

    @ApiModelProperty(position = 2, value = "Test run arguments as key value pairs. Mandatory (use {} for empty arguments). See Implementation Notes for an complete example.", required = true)
    @JsonProperty
    private SimpleArguments arguments;

    @ApiModelProperty(position = 3, value = "Simplified Test Object. Either a reference to an existing Test Object or a new "
            + "Test Object definition which references a resource in the web. Mandatory. "
            + "See Test Object model for more information and the Implementation Notes for an complete example.", required = true)
    @JsonProperty(required = true)
    @NotNull(message = "{l.json.invalid.test.object}")
    private UseTestObjectCmd testObject;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonIgnore
    private PreparedDtoResolver<ExecutableTestSuiteDto> etsResolver;

    @JsonIgnore
    private DtoResolver<TestObjectDto> testObjectResolver;

    public StartTestRunRequest() {}

    public StartTestRunRequest(final String label, final List<String> executableTestSuiteIds,
            SimpleArguments arguments, final UseTestObjectCmd testObject,
            final Map<String, Object> additionalProperties) {
        this.label = label;
        this.executableTestSuiteIds = executableTestSuiteIds;
        this.arguments = arguments;
        this.testObject = testObject;
        this.additionalProperties = additionalProperties;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public TestRunDto toTestRun()
            throws ObjectWithIdNotFoundException, IOException, URISyntaxException {
        final TestRunDto testRun = new TestRunDto();
        testRun.setId(EidFactory.getDefault().createRandomId());

        testRun.setLabel(CharEscaping.unescapeSpecialChars(label));

        final TestObjectDto testObject = this.testObject.toTestObject(testObjectResolver);

        for (final String executableTestSuiteId : executableTestSuiteIds) {
            final TestTaskDto testTaskDto = new TestTaskDto();
            testTaskDto.setExecutableTestSuite(
                    etsResolver.getById(EidConverter.toEid(executableTestSuiteId)).getDto());
            testTaskDto.setTestObject(testObject);
            if (arguments != null || arguments.get().isEmpty()) {
                for (final Map.Entry<String, String> keyVal : arguments.get().entrySet()) {
                    final Parameterizable.Parameter param = testTaskDto.getExecutableTestSuite().getParameters()
                            .getParameter(keyVal.getKey());
                    if (param != null) {
                        if (!param.validate(keyVal.getValue())) {
                            throw new LocalizableApiError("l.json.invalid.parameter",
                                    false, 400, keyVal.getValue(), keyVal.getKey());
                        }
                        // fixme
                        /*
                         * if(param.isStatic()) { throw new LocalizableApiError("l.json.invalid.parameter.static", false, 400,
                         * keyVal.getValue(), keyVal.getKey()); }
                         */
                    }
                    testTaskDto.getArguments().setValue(keyVal.getKey(), CharEscaping.unescapeSpecialChars(keyVal.getValue()));
                }
            }
            // Set default values from ETS
            for (final Parameterizable.Parameter parameter : testTaskDto.getExecutableTestSuite().getParameters()
                    .getParameters()) {
                if ((parameter.getType() != null && parameter.getType().equals("file-resource")) ||
                        (parameter.isStatic() && !SUtils.isNullOrEmpty(parameter.getDefaultValue()) &&
                                SUtils.isNullOrEmpty(testTaskDto.getArguments().value(parameter.getName())))) {
                    testTaskDto.getArguments().setValue(parameter.getName(), parameter.getDefaultValue());
                }
            }
            if (testObject.getTestObjectTypes() == null) {
                testObject.setTestObjectTypes(
                        testTaskDto.getExecutableTestSuite().getSupportedTestObjectTypes());
            }
            testRun.addTestTask(testTaskDto);
        }
        return testRun;
    }

    public void inject(final DtoResolver<TestObjectDto> testObjectResolver,
            final DataStorageService dataStorageService) {
        this.etsResolver = dataStorageService.getDao(ExecutableTestSuiteDto.class);
        this.testObjectResolver = testObjectResolver;
    }
}
