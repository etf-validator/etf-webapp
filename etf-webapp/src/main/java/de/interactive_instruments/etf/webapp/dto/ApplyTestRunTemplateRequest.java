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
package de.interactive_instruments.etf.webapp.dto;

import static de.interactive_instruments.etf.webapp.dto.DocumentationConstants.*;
import static de.interactive_instruments.etf.webapp.dto.DocumentationConstants.EID_EXAMPLE;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import de.interactive_instruments.SUtils;
import de.interactive_instruments.etf.dal.dao.DtoResolver;
import de.interactive_instruments.etf.dal.dao.PreparedDtoResolver;
import de.interactive_instruments.etf.dal.dto.capabilities.TestObjectDto;
import de.interactive_instruments.etf.dal.dto.capabilities.TestRunTemplateDto;
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
        "testRunTemplateId",
        "label",
        "arguments",
        "testObject"
})
@ApiModel(value = "ApplyTestRunTemplateRequest", description = "Apply a Test Run Template")
public class ApplyTestRunTemplateRequest extends AbstractTestRunRequest {

    @ApiModelProperty(value = EID_DESCRIPTION + ". ", example = EID_EXAMPLE, dataType = "String")
    @JsonProperty(required = true)
    @Pattern(regexp = EidConverter.EID_PATTERN, flags = {Pattern.Flag.CASE_INSENSITIVE})
    private String testRunTemplateId;

    @ApiModelProperty(position = 1, value = TEST_RUN_LABEL_DESCRIPTION
            + " Mandatory.", example = TEST_RUN_LABEL_EXAMPLE, dataType = "String", required = true)
    @JsonProperty(required = true)
    @NotNull(message = "{l.enter.label}")
    private String label;

    @ApiModelProperty(position = 2, value = "Test run arguments as key value pairs. "
            + "Mandatory (use {} for empty arguments). See Implementation Notes for an complete example.", required = true)
    @JsonProperty
    private SimpleArguments arguments;

    @ApiModelProperty(position = 3, value = "Simplified Test Object. This property is mandatory if the Test Run Template does"
            + " not reference a Test Object. If the Test Run Template has a reference, this property is silently ignored!"
            + " The simplified Test Object can either reference an existing Test Object or contain a new"
            + " Test Object definition which references a resource in the web."
            + " See Test Object model for more information and the Implementation Notes for an complete example.")
    @JsonProperty
    private UseTestObjectCmd testObject;

    @JsonIgnore
    private PreparedDtoResolver<TestRunTemplateDto> testRunTemplateResolver;

    @JsonIgnore
    private DtoResolver<TestObjectDto> testObjectResolver;

    @Override
    public TestRunDto toTestRun()
            throws ObjectWithIdNotFoundException, IOException, URISyntaxException {

        final TestRunTemplateDto testRunTemplateDto = testRunTemplateResolver.getById(
                EidConverter.toEid(testRunTemplateId)).getDto();

        final TestObjectDto testObject;
        if (testRunTemplateDto.getTestObjects() == null || testRunTemplateDto.getTestObjects().isEmpty()) {
            if (this.testObject == null) {
                throw new LocalizableApiError("l.json.invalid.test.object", false, 400);
            }
            testObject = this.testObject.toTestObject(testObjectResolver);
        } else {
            testObject = testRunTemplateDto.getTestObjects().get(0);
        }

        final TestRunDto testRun = new TestRunDto();
        testRun.setId(EidFactory.getDefault().createRandomId());
        testRun.setLabel(CharEscaping.unescapeSpecialChars(label));

        for (final ExecutableTestSuiteDto executableTestSuite : testRunTemplateDto.getExecutableTestSuites()) {
            final TestTaskDto testTaskDto = new TestTaskDto();
            testTaskDto.setExecutableTestSuite(executableTestSuite);
            testTaskDto.setTestObject(testObject);
            // Set parameters from user
            if (arguments != null) {
                for (final Map.Entry<String, String> keyVal : arguments.get().entrySet()) {
                    Parameterizable.Parameter param = testRunTemplateDto.getParameters().getParameter(keyVal.getKey());
                    if (param == null || SUtils.isNullOrEmpty(param.getAllowedValues())) {
                        param = executableTestSuite.getParameters().getParameter(keyVal.getKey());
                    }
                    if (param != null) {
                        if (!param.validate(keyVal.getValue())) {
                            throw new LocalizableApiError("l.json.invalid.parameter", false, 400, keyVal.getValue(),
                                    keyVal.getKey());
                        }
                        if (param.isStatic()) {
                            continue;
                            // FIXME
                            // throw new LocalizableApiError("l.json.invalid.parameter.static", false, 400, keyVal.getValue(),
                            // keyVal.getKey());
                        }
                    }
                    testTaskDto.getArguments().setValue(keyVal.getKey(), CharEscaping.unescapeSpecialChars(keyVal.getValue()));
                }
            }
            // Set default values from ETS
            for (final Parameterizable.Parameter parameter : executableTestSuite.getParameters().getParameters()) {
                if (!SUtils.isNullOrEmpty(parameter.getDefaultValue()) && parameter.isStatic()) {
                    testTaskDto.getArguments().setValue(parameter.getName(), parameter.getDefaultValue());
                }
            }
            // overwrite parameters with the template parameters
            for (final Parameterizable.Parameter parameter : testRunTemplateDto.getParameters().getParameters()) {
                if (!SUtils.isNullOrEmpty(parameter.getDefaultValue()) && parameter.isStatic()) {
                    testTaskDto.getArguments().setValue(parameter.getName(), parameter.getDefaultValue());
                } else if (parameter.isRequired()) {
                    // Check if the parameter has been set by the user or a default value from the ETS can be used
                    if (!testTaskDto.getArguments().containsName(parameter.getName())) {
                        // check if the Test Run Template provides a default value
                        if (!SUtils.isNullOrEmpty(parameter.getDefaultValue())) {
                            testTaskDto.getArguments().setValue(parameter.getName(), parameter.getDefaultValue());
                        } else {
                            throw new LocalizableApiError("l.json.required.parameter.not.set", false, 400, parameter.getName());
                        }
                    }
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
        this.testRunTemplateResolver = dataStorageService.getDao(TestRunTemplateDto.class);
        this.testObjectResolver = testObjectResolver;
    }
}
