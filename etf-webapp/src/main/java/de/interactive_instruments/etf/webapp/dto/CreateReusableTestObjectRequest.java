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

import static de.interactive_instruments.etf.webapp.dto.DocumentationConstants.TEST_OBJECT_LABEL_DESCRIPTION;
import static de.interactive_instruments.etf.webapp.dto.DocumentationConstants.TEST_OBJECT_LABEL_EXAMPLE;

import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.web.util.HtmlUtils;

import de.interactive_instruments.etf.dal.dto.capabilities.ResourceDto;
import de.interactive_instruments.etf.dal.dto.capabilities.TestObjectDto;
import de.interactive_instruments.etf.model.EidFactory;
import de.interactive_instruments.etf.webapp.conversion.CharEscaping;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
@ApiModel(value = "TestObject", description = "A reusable test object")
public class CreateReusableTestObjectRequest {

    @ApiModelProperty(position = 1, value = TEST_OBJECT_LABEL_DESCRIPTION
            + " Mandatory.", example = TEST_OBJECT_LABEL_EXAMPLE, dataType = "String", required = true)
    @JsonProperty(required = true)
    @NotNull(message = "{l.enter.label}")
    private String label;

    @ApiModelProperty(value = "A description for the Test Object. Mandatory.", example = "Partial delivery of spatial data from department X", dataType = "String", required = true)
    @JsonProperty(required = true)
    @NotNull(message = "{l.enter.description}")
    private String description;

    @ApiModelProperty(value = "List of online resources as name / resource pairs. "
            + "Currently two resource types are supported: if a web service resource is defined, the resource name must be 'serviceEndpoint'. "
            + "If a data set resource is defined, the resource name must be 'data'. "
            + "PLEASE NOTE: only the one resource can be used in the current version. "
            + "Either an id or a resource property must be provided.")
    @JsonProperty
    private Map<String, String> resources;

    @ApiModelProperty(value = "Additional, optional properties for the Test Object. The properties 'password' and 'username' "
            + "can be set, if access to the resource is restricted.")
    @JsonProperty
    private Map<String, String> properties;

    private static Set<String> reservedProperties = new HashSet<String>() {
        {
            add("etf.uploaded");
            add("temporary");
        }
    };

    public TestObjectDto toTestObject() throws URISyntaxException {
        final TestObjectDto testObject = new TestObjectDto();
        testObject.setId(EidFactory.getDefault().createRandomId());
        testObject.setLabel(CharEscaping.unescapeSpecialChars(this.label));
        testObject.setDescription(CharEscaping.unescapeSpecialChars(this.description));
        testObject.setVersionFromStr("1.0.0");

        if (properties != null) {
            for (final Map.Entry<String, String> entry : properties.entrySet()) {
                if (!reservedProperties.contains(entry.getKey())) {
                    testObject.properties().setProperty(entry.getKey(), entry.getValue());
                }
            }
        }

        if (resources != null && !resources.isEmpty()) {
            for (final Map.Entry<String, String> nameUriEntry : resources.entrySet()) {
                final String uri = HtmlUtils.htmlUnescape(nameUriEntry.getValue());
                if (!uri.startsWith("testdatadir:") && !uri.startsWith("http")) {
                    testObject.properties().setProperty("uploaded", "true");
                }
                testObject.addResource(new ResourceDto(nameUriEntry.getKey(), uri));
            }
        }

        return testObject;
    }
}
