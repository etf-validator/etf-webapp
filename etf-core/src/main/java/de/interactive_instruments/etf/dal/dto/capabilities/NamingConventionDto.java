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
package de.interactive_instruments.etf.dal.dto.capabilities;

import de.interactive_instruments.etf.dal.dto.MetaDataItemDto;

/**
 * Optional naming convention, which is used to check if the label of a Test Object matches this regular expression.
 * This might be useful for labeling test data deliveries according to a prescribed scheme.
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public class NamingConventionDto extends MetaDataItemDto {

    private String regex;

    public NamingConventionDto() {}

    public NamingConventionDto(final NamingConventionDto other) {
        super(other);
        this.regex = other.regex;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(final String regex) {
        this.regex = regex;
    }

    @Override
    public NamingConventionDto createCopy() {
        return new NamingConventionDto(this);
    }
}
