/**
 * Copyright 2017-2018 European Union, interactive instruments GmbH
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This work was supported by the EU Interoperability Solutions for
 * European Public Administrations Programme (http://ec.europa.eu/isa)
 * through Action 1.17: A Reusable INSPIRE Reference Platform (ARE3NA).
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
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
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
