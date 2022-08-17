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

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;

import de.interactive_instruments.ImmutableVersion;
import de.interactive_instruments.SUtils;
import de.interactive_instruments.Version;
import de.interactive_instruments.etf.dal.dto.capabilities.TagDto;
import de.interactive_instruments.etf.model.Disableable;
import de.interactive_instruments.etf.model.EGAID;
import de.interactive_instruments.etf.model.EgaidHolder;

public abstract class RepositoryItemDto extends MetaDataItemDto implements EgaidHolder, Disableable {

    protected RepositoryItemDto replacedBy;
    protected String replacementReason;
    protected String itemHash;
    protected String version;
    protected String author;
    protected Date creationDate;
    protected String lastEditor;
    protected Date lastUpdateDate;
    protected List<TagDto> tagDtos;
    protected String remoteResource;
    protected String localPath;
    protected boolean disabled;
    protected String egaid;

    public RepositoryItemDto() {}

    protected RepositoryItemDto(final RepositoryItemDto other) {
        super(other);
        this.replacedBy = other.replacedBy;
        this.replacementReason = other.replacementReason;
        this.itemHash = other.itemHash;
        this.version = other.version;
        this.author = other.author;
        this.creationDate = other.creationDate;
        this.lastEditor = other.lastEditor;
        this.lastUpdateDate = other.lastUpdateDate;
        this.tagDtos = other.tagDtos;
        this.remoteResource = other.remoteResource;
        this.localPath = other.localPath;
        this.disabled = other.disabled;
        this.egaid = other.egaid;
    }

    /**
     * Gets the value of the itemHash property.
     *
     * @return possible object is byte[]
     */
    public String getItemHash() {
        return itemHash;
    }

    /**
     * Sets the value of the itemHash property.
     *
     * @param value
     *            allowed object is byte[]
     */
    public void setItemHash(final String value) {
        this.itemHash = value;
    }

    /**
     * Gets the value of the version property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getVersionAsStr() {
        return version;
    }

    public Version getVersion() {
        return version != null ? new Version(version) : null;
    }

    /**
     * Sets the value of the version property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setVersionFromStr(final String value) {
        this.version = Version.parse(value).getAsString();
    }

    public void setVersion(final Version version) {
        this.version = version.toString();
    }

    public void increaseVersion() {
        if (this.version == null) {
            this.version = Version.parse("1.0.0").getAsString();
        } else {
            this.version = Version.parse(this.version).incBugfix().getAsString();
        }
    }

    /**
     * Gets the value of the author property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Sets the value of the author property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setAuthor(String value) {
        this.author = value;
    }

    /**
     * Gets the value of the creationDate property.
     *
     * @return possible object is {@link String }
     *
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * Sets the value of the creationDate property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setCreationDate(Date value) {
        this.creationDate = value;
    }

    public void setCreationDateNowIfNotSet() {
        if (this.creationDate == null) {
            this.creationDate = new Date();
        }
    }

    /**
     * Gets the value of the lastEditor property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getLastEditor() {
        return lastEditor;
    }

    /**
     * Sets the value of the lastEditor property.
     *
     * Automatically sets the author if not set
     *
     * @param editor
     *            allowed object is {@link String }
     *
     */
    public void setLastEditor(final String editor) {
        this.lastEditor = editor;
        if (this.author == null) {
            this.author = editor;
        }
    }

    /**
     * Gets the value of the lastUpdateDate property.
     *
     * @return possible object is {@link String }
     *
     */
    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    /**
     * Sets the value of the lastUpdateDate property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setLastUpdateDate(Date value) {
        this.lastUpdateDate = value;
        if (this.creationDate == null) {
            this.creationDate = new Date();
        }
    }

    public void setLastUpdateDateNow() {
        setLastUpdateDate(new Date());
    }

    public ModelItemDto getReplacedBy() {
        return replacedBy;
    }

    public void setReplacedBy(final RepositoryItemDto replacedBy) {
        this.replacedBy = replacedBy;
    }

    public void setReplacementReason(final String replacementReason) {
        this.replacementReason = replacementReason;
    }

    public String getReplacementReason() {
        return replacementReason;
    }

    public List<TagDto> getTags() {
        return tagDtos;
    }

    public void setTags(final List<TagDto> tagDtos) {
        this.tagDtos = tagDtos;
    }

    public void addTag(final TagDto tagDto) {
        if (this.tagDtos == null) {
            this.tagDtos = new ArrayList<>();
        }
        this.tagDtos.add(tagDto);
    }

    public URI getRemoteResource() {
        return remoteResource != null ? URI.create(remoteResource) : null;
    }

    public void setRemoteResource(final URI remoteResource) {
        this.remoteResource = remoteResource != null ? remoteResource.toString() : null;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(final String localPath) {
        this.localPath = localPath;
    }

    @Override
    public void setDisabled(final boolean disabled) {
        this.disabled = disabled;
    }

    @Override
    public boolean isDisabled() {
        return this.disabled;
    }

    @Override
    public EGAID getEgaId() {
        return new EGAID() {
            private final Matcher m = EGAID_WP.matcher(egaid);

            @Override
            public String getArtifactId() {
                m.matches();
                return m.group(2);
            }

            @Override
            public String getGroupId() {
                m.matches();
                return m.group(1);
            }

            @Override
            public String getEgaId() {
                return egaid;
            }

            @Override
            public ImmutableVersion getVersion() {
                return version != null ? Version.parse(version) : null;
            }
        };
    }

    public void setEgaId(final String egaid) {
        if ("".equals(egaid)) {
            this.egaid = null;
        } else {
            final Matcher m = EGAID.EGAID_REF.matcher(egaid);
            if (!m.matches()) {
                throw new IllegalArgumentException("Not an EGAID: " + egaid);
            }
            this.egaid = m.group(1) + "." + m.group(2);
            final String v = m.group(4);
            if (v != null) {
                this.version = v;
            }
        }
    }

    public String getEgaIdRef() {
        return this.egaid + this.version;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("RepositoryItem{");
        sb.append("label=").append(label);
        sb.append(", id=").append(getId());
        sb.append(", parent=").append(parent != null ? parent.getId() : "null");
        sb.append(", description=").append(description);
        sb.append(", reference=").append(reference);
        sb.append(", replacedBy=").append(replacedBy != null ? replacedBy.getId() : "null");
        sb.append(", version=").append(version);
        sb.append(", creationDate=").append(creationDate);
        sb.append(", lastUpdateDate=").append(lastUpdateDate);
        sb.append(", itemHash=").append(itemHash);
        sb.append(", author=").append(author);
        sb.append(", lastEditor=").append(lastEditor);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public String getDescriptiveLabel() {
        if (!SUtils.isNullOrEmpty(version)) {
            final StringBuilder labelBuilder = new StringBuilder(128);
            labelBuilder.append("'").append(label).append(" (EID: ").append(id).append(", V: ").append(version).append(" )'");
            return labelBuilder.toString();
        } else {
            return super.getDescriptiveLabel();
        }
    }

    public void ensureBasicValidity() throws IncompleteDtoException {
        super.ensureBasicValidity();
        if (version == null) {
            throw new IncompleteDtoException("Required property 'version' must be set!");
        }
        if (creationDate == null) {
            throw new IncompleteDtoException("Required property 'creationDate' must be set!");
        }
        if (remoteResource == null && localPath == null) {
            throw new IncompleteDtoException("Neither property 'remoteResource' nor property 'localPath' are set !");
        }
    }
}
