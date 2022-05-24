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
package de.interactive_instruments.etf.dal.dto.result;

import java.net.URI;

import de.interactive_instruments.etf.dal.dto.Dto;

public class AttachmentDto extends Dto {
    private String encoding;
    private String label;
    private String mimeType;
    private String type;
    private URI referencedData;
    private String embeddedData;

    public AttachmentDto() {}

    private AttachmentDto(final AttachmentDto other) {
        this.encoding = other.encoding;
        this.label = other.label;
        this.mimeType = other.mimeType;
        this.type = other.type;
        this.referencedData = other.referencedData;
        this.embeddedData = other.embeddedData;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(final String encoding) {
        this.encoding = encoding;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(final String mimeType) {
        this.mimeType = mimeType;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public URI getReferencedData() {
        return referencedData;
    }

    public void setReferencedData(final URI referencedData) {
        this.referencedData = referencedData;
    }

    public String getEmbeddedData() {
        return embeddedData;
    }

    public void setEmbeddedData(final String embeddedData) {
        this.embeddedData = embeddedData;
    }

    @Override
    public AttachmentDto createCopy() {
        return new AttachmentDto(this);
    }
}
