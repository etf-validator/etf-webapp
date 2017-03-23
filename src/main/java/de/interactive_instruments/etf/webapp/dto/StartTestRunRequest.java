/**
 * Copyright 2010-2017 interactive instruments GmbH
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

import com.fasterxml.jackson.annotation.*;

import de.interactive_instruments.etf.dal.dao.PreparedDtoResolver;
import de.interactive_instruments.etf.dal.dto.capabilities.TestObjectDto;
import de.interactive_instruments.etf.dal.dto.run.TestRunDto;
import de.interactive_instruments.etf.dal.dto.run.TestTaskDto;
import de.interactive_instruments.etf.dal.dto.test.ExecutableTestSuiteDto;
import de.interactive_instruments.etf.model.EidFactory;
import de.interactive_instruments.etf.webapp.conversion.EidConverter;
import de.interactive_instruments.exceptions.ObjectWithIdNotFoundException;
import de.interactive_instruments.exceptions.StorageException;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author J. Herrmann ( herrmann <aT) interactive-instruments (doT> de )
 */
@JsonPropertyOrder({
		"label",
		"executableTestSuiteIds",
		"arguments"
})
@ApiModel(value = "StartTestRunRequest", description = "Start a test run")
public class StartTestRunRequest {

	@ApiModelProperty(position = 0, value = TEST_RUN_LABEL_DESCRIPTION
			+ " Mandatory.", example = TEST_RUN_LABEL_EXAMPLE, dataType = "String", required = true)
	@JsonProperty
	private String label;

	// Double quote
	private final static String dQ = " \\u0022 ";

	@ApiModelProperty(position = 1, value = "List of Executable Test Suite IDs. Mandatory."
			+ EID_DESCRIPTION, example = "[" + dQ + EID_LIST_EXAMPLE + dQ + "]", required = true)
	@JsonProperty(required = true)
	private List<String> executableTestSuiteIds;

	@ApiModelProperty(position = 2, value = "Test run arguments as key value pairs. Mandatory (use {} for empty arguments). ", example = "{ "
			+ dQ + "parameter" + dQ + ": " + dQ + "value" + dQ + " }", required = true)
	@JsonProperty
	private SimpleArguments arguments;

	@ApiModelProperty(position = 3, value = "Simplified Test Object. Either a reference to an existing Test Object or a new "
			+ "Test Object definition which references a resource in the web. Mandatory. "
			+ "See Test Object model for more information", example = "{ " + dQ + "resources" + dQ + " : { " + dQ
					+ "serviceEndpoint" + dQ + ": " + dQ + "www.example.com/service" + dQ + " } }", required = true)
	@JsonProperty(required = true)
	private SimpleTestObject testObject;

	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	public StartTestRunRequest() {}

	public StartTestRunRequest(final String label, final List<String> executableTestSuiteIds,
			SimpleArguments arguments, final SimpleTestObject testObject,
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

	public TestRunDto toTestRun(final PreparedDtoResolver<TestObjectDto> testObjectDao,
			final PreparedDtoResolver<ExecutableTestSuiteDto> etsDao)
			throws ObjectWithIdNotFoundException, StorageException, IOException, URISyntaxException {
		final TestRunDto testRun = new TestRunDto();
		testRun.setId(EidFactory.getDefault().createRandomId());
		testRun.setLabel(label);
		final TestObjectDto testObject = this.testObject.toTestObject(testObjectDao);

		for (final String executableTestSuiteId : executableTestSuiteIds) {
			final TestTaskDto testTaskDto = new TestTaskDto();
			testTaskDto.setExecutableTestSuite(
					etsDao.getById(EidConverter.toEid(executableTestSuiteId)).getDto());
			testTaskDto.setTestObject(testObject);
			if (arguments == null || arguments.get().isEmpty()) {
				// FIXME
				testTaskDto.getArguments().setValue("etf.testcases", "*");
			} else {
				for (final Map.Entry<String, String> keyVal : arguments.get().entrySet()) {
					testTaskDto.getArguments().setValue(keyVal.getKey(), keyVal.getValue());
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
}
