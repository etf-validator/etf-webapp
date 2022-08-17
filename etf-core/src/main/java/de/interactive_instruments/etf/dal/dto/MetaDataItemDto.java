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
package de.interactive_instruments.etf.dal.dto;

import de.interactive_instruments.SUtils;
import de.interactive_instruments.etf.dal.dto.translation.LangTranslationTemplateCollectionDto;

/**
 * Data transfer object which possesses meta data
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public abstract class MetaDataItemDto<T extends ModelItemDto> extends ModelItemDto<T> {

    protected String label;
    protected LangTranslationTemplateCollectionDto labelTranslationTemplate;

    protected String description;
    protected LangTranslationTemplateCollectionDto descriptionTranslationTemplate;

    // protected Properties properties;

    protected String reference;

    public MetaDataItemDto() {}

    public MetaDataItemDto(final MetaDataItemDto other) {
        super(other);
        this.label = other.label;
        this.labelTranslationTemplate = other.labelTranslationTemplate;
        this.description = other.description;
        this.descriptionTranslationTemplate = other.descriptionTranslationTemplate;
        // this.properties = other.properties;
        this.reference = other.reference;
    }

    /**
     * Gets the value of the label property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the value of the label property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setLabel(String value) {
        this.label = value;
    }

    /**
     * Gets the value of the description property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the reference property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getReference() {
        return reference;
    }

    /**
     * Sets the value of the reference property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setReference(String value) {
        this.reference = value;
    }

    /* public Properties properties() { if (properties == null) { properties = new Properties(); } return properties; } */

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("MetaDataItem{");
        sb.append("id=").append(getId());
        sb.append(", parent=").append(parent != null ? parent.getId() : null);
        sb.append(", label=").append(label);
        sb.append(", description=").append(description);
        sb.append(", reference=").append(reference);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public String getDescriptiveLabel() {
        if (!SUtils.isNullOrEmpty(label)) {
            final StringBuilder labelBuilder = new StringBuilder(128);
            labelBuilder.append("'").append(label).append(" (EID: ").append(id).append(" )'");
            return labelBuilder.toString();
        } else {
            return super.getDescriptiveLabel();
        }
    }

    public void ensureBasicValidity() throws IncompleteDtoException {
        super.ensureBasicValidity();
        if (SUtils.isNullOrEmpty(label) && labelTranslationTemplate == null) {
            throw new IncompleteDtoException("Either property 'label' or 'labelTranslationTemplate' must be set!");
        }
    }
}
