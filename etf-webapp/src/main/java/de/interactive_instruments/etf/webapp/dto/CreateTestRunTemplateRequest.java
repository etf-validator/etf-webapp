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

import java.net.URI;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.interactive_instruments.etf.dal.dao.DtoResolver;
import de.interactive_instruments.etf.dal.dao.PreparedDtoResolver;
import de.interactive_instruments.etf.dal.dto.Dto;
import de.interactive_instruments.etf.dal.dto.IncompleteDtoException;
import de.interactive_instruments.etf.dal.dto.capabilities.TestObjectDto;
import de.interactive_instruments.etf.dal.dto.capabilities.TestObjectTypeDto;
import de.interactive_instruments.etf.dal.dto.capabilities.TestRunTemplateDto;
import de.interactive_instruments.etf.dal.dto.test.ExecutableTestSuiteDto;
import de.interactive_instruments.etf.model.EID;
import de.interactive_instruments.etf.model.EidFactory;
import de.interactive_instruments.etf.model.ParameterSet;
import de.interactive_instruments.etf.model.Parameterizable;
import de.interactive_instruments.etf.webapp.controller.LocalizableApiError;
import de.interactive_instruments.etf.webapp.conversion.CharEscaping;
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
        "description",
        "properties",
        "defaultParameterValues",
        "executableTestSuiteIds",
        "testObjectTypeIds",
        "testObjectIds"
})
@ApiModel(value = "CreateTestRunTemplateRequest", description = "Create a Test Run Template")
public class CreateTestRunTemplateRequest {

    private final static Logger logger = LoggerFactory.getLogger(CreateTestRunTemplateRequest.class);

    @ApiModelProperty(value = "A label for the Test Run Template. Mandatory.", example = "Conformity Class X", dataType = "String", required = true)
    @JsonProperty(required = true)
    @NotNull(message = "{l.enter.label}")
    private String label;

    @ApiModelProperty(value = "A description for the Test Run Template. Mandatory.", example = "The Conformity Class X bundles all Test Suites that...", dataType = "String", required = true)
    @JsonProperty(required = true)
    @NotNull(message = "{l.enter.description}")
    private String description;

    @ApiModelProperty(position = 1, value = "Additional meta information as key value pairs."
            + " See Implementation Notes for an complete example.")
    @JsonProperty
    private SimpleArguments properties;

    @ApiModelProperty(position = 2, value = "Default parameter values as key value pairs. "
            + " See Implementation Notes for an complete example.")
    @JsonProperty
    private SimpleArguments defaultParameterValues;

    @ApiModelProperty(position = 3, value = "The Executable Test Suites that are executed with this template.", required = true)
    @JsonProperty
    private String[] executableTestSuiteIds;

    @ApiModelProperty(position = 4, value = "Restricts the Test Object Types that can be used with this template."
            + " The passed Test Object Types must be compatible with the Executable Test Suites and the Test Object.")
    @JsonProperty
    private String[] testObjectTypeIds;

    @ApiModelProperty(position = 5, value = "Binds the template to one or multiple Test Objects.")
    @JsonProperty()
    private String[] testObjectIds;

    @JsonIgnore
    private PreparedDtoResolver<ExecutableTestSuiteDto> etsResolver;

    @JsonIgnore
    private DtoResolver<TestObjectDto> testObjectResolver;

    @JsonIgnore
    private DtoResolver<TestObjectTypeDto> testObjectTypeResolver;

    public CreateTestRunTemplateRequest() {}

    private <V extends Dto> List<V> getByIds(PreparedDtoResolver<V> resolver, final String[] eids)
            throws ObjectWithIdNotFoundException, StorageException {
        final List<V> items = new ArrayList<>();
        for (int i = 0; i < eids.length; i++) {
            items.add(resolver.getById(EidConverter.toEid(eids[i])).getDto());
        }
        return items;
    }

    public TestRunTemplateDto toTestRunTemplate()
            throws LocalizableApiError, StorageException, IncompleteDtoException {

        final TestRunTemplateDto testRunTemplate = new TestRunTemplateDto();

        if (testObjectIds != null && testObjectIds.length > 0) {
            try {
                final Set<EID> ids = Arrays.stream(testObjectIds).map(id -> EidFactory.getDefault().createAndPreserveStr(id))
                        .collect(Collectors.toSet());
                testRunTemplate.setTestObjects(new ArrayList<>(testObjectResolver.getByIds(ids)));
            } catch (ObjectWithIdNotFoundException e) {
                throw new LocalizableApiError(e);
            }
        }
        final List<ExecutableTestSuiteDto> executableTestSuites;
        try {
            executableTestSuites = getByIds(etsResolver, executableTestSuiteIds);
        } catch (ObjectWithIdNotFoundException e) {
            throw new LocalizableApiError(e);
        }
        if (testObjectTypeIds != null && testObjectTypeIds.length > 0) {
            try {
                final Set<EID> ids = Arrays.stream(testObjectTypeIds)
                        .map(id -> EidFactory.getDefault().createAndPreserveStr(id)).collect(Collectors.toSet());
                testRunTemplate.setSupportedTestObjectTypes(testObjectTypeResolver.getByIds(ids));
            } catch (ObjectWithIdNotFoundException e) {
                throw new LocalizableApiError(e);
            }
        }

        final ParameterSet parameters = new ParameterSet();
        for (final ExecutableTestSuiteDto executableTestSuite : executableTestSuites) {
            testRunTemplate.addExecutableTestSuite(executableTestSuite);
            for (final Parameterizable.Parameter parameter : executableTestSuite.getParameters().getParameters()) {
                final Parameterizable.Parameter existingParam = parameters.getParameter(parameter.getName());
                if (existingParam != null) {
                    if (!Objects.equals(existingParam.getDefaultValue(), parameter.getDefaultValue())
                            && (defaultParameterValues == null
                                    || defaultParameterValues.get().get(parameter.getName()) == null)) {
                        throw new LocalizableApiError(
                                "l.json.testruntemplate.creation.parameter.override.required", false,
                                409, parameter.getName());
                    }
                    // else overridden later
                } else {
                    parameters.addParameter(parameter);
                }
            }
            if (testRunTemplate.getTestObjects() != null) {
                for (final TestObjectDto testObject : testRunTemplate.getTestObjects()) {
                    if (Collections.disjoint(testObject.getTestObjectTypes(),
                            executableTestSuite.getSupportedTestObjectTypes())) {
                        throw new LocalizableApiError("l.testObject.type.incomaptible", false,
                                400, testObject.getTestObjectTypes().iterator().next());
                    }
                }
            }
        }

        if (defaultParameterValues != null) {
            for (final Map.Entry<String, String> entry : defaultParameterValues.get().entrySet()) {
                final Parameterizable.Parameter param = parameters.getParameter(entry.getKey());
                if (param != null) {
                    final ParameterSet.MutableParameter newParam = new ParameterSet.MutableParameter(param);
                    final String allowedValues = param.getAllowedValues();
                    if (allowedValues != null) {
                        try {
                            final Pattern p = Pattern.compile(allowedValues);
                            if (!p.matcher(entry.getValue()).matches()) {
                                throw new LocalizableApiError("l.json.invalid.value",
                                        false, 400, entry.getValue(), allowedValues);
                            }
                        } catch (PatternSyntaxException e) {
                            logger.warn("Allowed value pattern cannot be compiled: ", e);
                        }
                    }
                    newParam.setDefaultValue(entry.getValue());
                    parameters.addParameter(newParam);
                } else {
                    parameters.addParameter(entry.getKey(), entry.getValue());
                }
            }
        }
        testRunTemplate.setParameters(parameters);
        testRunTemplate.setCreationDateNowIfNotSet();

        testRunTemplate.setLabel(CharEscaping.unescapeSpecialChars(label));
        testRunTemplate.setDescription(CharEscaping.unescapeSpecialChars(description));
        testRunTemplate.setItemHash(testRunTemplate.toString());
        testRunTemplate.setId(EidFactory.getDefault().createRandomId());
        testRunTemplate.setVersionFromStr("1.0.0");
        testRunTemplate.setRemoteResource(URI.create("http://local"));
        return testRunTemplate;
    }

    private static Set<EID> idStrsToEIDs(final String[] ids) {
        final Set<EID> eids = new HashSet<>();
        for (final String id : ids) {
            eids.add(EidConverter.toEid(id));
        }
        return eids;
    }

    public void init(
            final PreparedDtoResolver<ExecutableTestSuiteDto> etsResolver,
            final DtoResolver<TestObjectTypeDto> testObjectTypeResolver,
            final DtoResolver<TestObjectDto> testObjectResolver) {
        this.etsResolver = etsResolver;
        this.testObjectResolver = testObjectResolver;
        this.testObjectTypeResolver = testObjectTypeResolver;
    }
}
