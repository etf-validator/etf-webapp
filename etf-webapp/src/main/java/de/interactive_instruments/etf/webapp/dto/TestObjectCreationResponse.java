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

import static de.interactive_instruments.etf.webapp.controller.TestObjectController.TESTOBJECTS_URL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.web.multipart.MultipartFile;

import de.interactive_instruments.IFile;
import de.interactive_instruments.etf.dal.dto.capabilities.TestObjectDto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
@ApiModel(value = "TestObjectCreationResponse", description = "Test Object Creation response")
public class TestObjectCreationResponse {

    static class UploadMetadata {
        @ApiModelProperty(value = "File name", example = "file.xml")
        @JsonProperty
        private final String name;

        @ApiModelProperty(value = "File size in bytes", example = "2048")
        @JsonProperty
        private final String size;

        @ApiModelProperty(value = "File type", example = "text/xml")
        @JsonProperty
        private final String type;

        private UploadMetadata(final String fileName, final long fileSize, final String fileType) {
            this.name = fileName;
            this.size = String.valueOf(fileSize);
            this.type = fileType;
        }
    }

    static class SimplifiedTestObject {
        @JsonProperty(required = false)
        private final String id;

        @JsonProperty(required = false)
        private final String ref;

        private SimplifiedTestObject(final TestObjectDto testObjectDto) {
            if ("true".equals(testObjectDto.properties().getPropertyOrDefault("temporary", "false"))) {
                this.id = testObjectDto.getId().getId();
                this.ref = null;
            } else {
                this.id = null;
                this.ref = TESTOBJECTS_URL + "/" + testObjectDto.getId().getId();
            }
        }
    }

    @ApiModelProperty(value = "Created Test Object")
    @JsonProperty
    private final SimplifiedTestObject testObject;

    @ApiModelProperty(value = "File metadata")
    @JsonProperty
    private final List<UploadMetadata> files;

    @JsonIgnore
    private final String id;

    @JsonIgnore
    public String getNameForUpload() {
        if (files.size() == 1) {
            return files.get(0).name;
        } else if (files.size() == 2) {
            return files.get(0).name + " and " + files.get(1).name;
        } else if (files.size() > 2) {
            return files.get(0).name + " and " + (files.size() - 1) + " other files";
        } else {
            return "Empty Upload";
        }
    }

    @JsonIgnore
    public String getId() {
        return id;
    }

    public TestObjectCreationResponse(final TestObjectDto testObject, final Collection<List<MultipartFile>> multipartFiles) {
        this.testObject = new SimplifiedTestObject(testObject);
        this.id = testObject.getId().getId();
        this.files = new ArrayList<>();
        for (final List<MultipartFile> multipartFile : multipartFiles) {
            for (final MultipartFile mpf : multipartFile) {
                this.files.add(
                        new UploadMetadata(IFile.sanitize(mpf.getOriginalFilename()), mpf.getSize(), mpf.getContentType()));
            }
        }
    }
}
