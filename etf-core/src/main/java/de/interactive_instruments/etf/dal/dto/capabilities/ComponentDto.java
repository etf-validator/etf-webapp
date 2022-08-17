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
package de.interactive_instruments.etf.dal.dto.capabilities;

import de.interactive_instruments.etf.component.ComponentInfo;
import de.interactive_instruments.etf.dal.dto.MetaDataItemDto;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public class ComponentDto extends MetaDataItemDto {
    protected String vendor;
    protected String version;

    public ComponentDto() {}

    public ComponentDto(final ComponentInfo componentInfo) {
        this.label = componentInfo.getName();
        this.id = componentInfo.getId();
        this.description = componentInfo.getDescription();
        this.vendor = componentInfo.getVendor();
        this.version = componentInfo.getVersion();
    }

    private ComponentDto(final ComponentDto other) {
        super(other);
        this.vendor = other.vendor;
        this.version = other.version;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String value) {
        this.vendor = value;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String value) {
        this.version = value;
    }

    @Override
    public ComponentDto createCopy() {
        return new ComponentDto(this);
    }
}
