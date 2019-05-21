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

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.interactive_instruments.UriUtils;
import de.interactive_instruments.etf.dal.dto.result.AttachmentDto;
import de.interactive_instruments.exceptions.ExcUtils;
import io.swagger.annotations.ApiModel;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public class AttachmentCollection {

    @ApiModel(value = "Attachment", description = "Report attachment gathered during a test run")
    public final static class Attachment {
        @JsonProperty
        private String id;

        @JsonProperty
        private String label;

        @JsonProperty
        private String encoding;

        @JsonProperty
        private String mimeType;

        @JsonProperty
        private String type;

        @JsonProperty
        private String embeddedData;

        @JsonProperty
        private String size;

        public Attachment(final AttachmentDto attachmentDto) {
            this.id = attachmentDto.getId().getId();
            this.label = attachmentDto.getLabel();
            this.encoding = attachmentDto.getEncoding();
            this.mimeType = attachmentDto.getMimeType();
            this.type = attachmentDto.getType();
            this.embeddedData = attachmentDto.getEmbeddedData();
            try {
                this.size = String.valueOf(UriUtils.getContentLength(attachmentDto.getReferencedData()));
            } catch (IOException ign) {
                ExcUtils.suppress(ign);
            }
        }
    }

    private AttachmentCollection() {}

    public static final Collection<Attachment> create(final Collection<AttachmentDto> attachmentDtos) {
        return attachmentDtos.stream().map(Attachment::new).collect(Collectors.toList());
    }
}
