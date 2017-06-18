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

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
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
