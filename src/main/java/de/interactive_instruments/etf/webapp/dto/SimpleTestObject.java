/**
 * Copyright 2017-2019 European Union, interactive instruments GmbH
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

import static de.interactive_instruments.etf.webapp.conversion.EidConverter.toEid;
import static de.interactive_instruments.etf.webapp.dto.DocumentationConstants.EID_DESCRIPTION;
import static de.interactive_instruments.etf.webapp.dto.DocumentationConstants.EID_EXAMPLE;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.web.util.HtmlUtils;

import de.interactive_instruments.Credentials;
import de.interactive_instruments.SUtils;
import de.interactive_instruments.UriUtils;
import de.interactive_instruments.etf.dal.dao.PreparedDtoResolver;
import de.interactive_instruments.etf.dal.dto.capabilities.ResourceDto;
import de.interactive_instruments.etf.dal.dto.capabilities.TestObjectDto;
import de.interactive_instruments.etf.model.EidFactory;
import de.interactive_instruments.etf.webapp.conversion.EidConverter;
import de.interactive_instruments.exceptions.ObjectWithIdNotFoundException;
import de.interactive_instruments.exceptions.StorageException;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
@ApiModel(value = "TestObject", description = "Simplified Test Object definition")
public class SimpleTestObject {

    @ApiModelProperty(value = EID_DESCRIPTION + ". " +
            "Either an id or a resource property must be provided.", example = EID_EXAMPLE, dataType = "String")
    @JsonProperty
    @Pattern(regexp = EidConverter.EID_PATTERN, flags = {Pattern.Flag.CASE_INSENSITIVE})
    private String id;

    @ApiModelProperty(value = "List of online resources as name / resource pairs. "
            + "Currently two resource types are supported: if a web service resource is defined, the resource name must be 'serviceEndpoint'. "
            + "If a data set resource is defined, the resource name must be 'data'. "
            + "PLEASE NOTE: only the one resource can be used in the current version. "
            + "Either an id or a resource property must be provided.")
    @JsonProperty
    private Map<String, String> resources;

    @ApiModelProperty(value = "Username for resources. "
            + "Optional and only used when the resource property is defined.", example = "FrankDrebin", dataType = "String")
    @JsonProperty
    private String username;

    @ApiModelProperty(value = "Password for resources"
            + "Optional and only used when the resource property is defined.", example = "S3CR3T", dataType = "String")
    @JsonProperty
    private String password;

    Map<String, String> getResources() {
        return resources;
    }

    public String getId() {
        return "EID" + toEid(id).toString();
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public SimpleTestObject() {}

    public SimpleTestObject(final TestObjectDto testObject) {
        this.id = testObject.getId().getId();
    }

    public TestObjectDto toTestObject(final PreparedDtoResolver<TestObjectDto> testObjectDao)
            throws URISyntaxException, IOException, ObjectWithIdNotFoundException, StorageException {
        final TestObjectDto testObject;
        if (resources != null && !resources.isEmpty()) {
            testObject = new TestObjectDto();
            for (final Map.Entry<String, String> nameUriEntry : resources.entrySet()) {
                testObject.addResource(new ResourceDto(nameUriEntry.getKey(), HtmlUtils.htmlUnescape(nameUriEntry.getValue())));
            }
            testObject.properties().setProperty("temporary", "true");
            testObject.setVersionFromStr("1.0.0");
            testObject.setCreationDateNowIfNotSet();
            // testObject.setRemoteResource(URI.create("http://private"));
            testObject.setLocalPath(".");
            final Credentials credentials;
            if (!SUtils.isNullOrEmpty(username)) {
                testObject.properties().setProperty("username", username);
                testObject.properties().setProperty("password", password);
                credentials = Credentials.fromProperties(testObject.properties());
                testObject.setDescription("Web Test Object (from protected resource location)");
            } else {
                credentials = null;
                testObject.setDescription("Web Test Object");
            }
            if (resources.size() == 1) {
                final String tmpLabel = UriUtils.proposeFilename(
                        testObject.getResourceCollection().iterator().next().getUri(), credentials, true);
                testObject.setLabel(tmpLabel);
            } else {
                testObject.setLabel("Temporary Test Object");
            }
        } else {
            testObject = testObjectDao.getById(
                    EidFactory.getDefault().createAndPreserveStr(toEid(id).toString())).getDto();
        }
        return testObject;
    }
}
