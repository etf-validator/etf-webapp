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
package de.interactive_instruments.etf.dal.dto.report;

import java.util.List;

import de.interactive_instruments.etf.dal.dto.MetaDataItemDto;

public class StatisticalReportTableDto extends MetaDataItemDto {
    protected StatisticalReportTableTypeDto type;
    protected List<Entry> entries;

    public static class Entry {
        final Object[] entry;

        public Entry(final Object[] values) {
            this.entry = values;
        }
    }

    public StatisticalReportTableDto() {}

    private StatisticalReportTableDto(final StatisticalReportTableDto other) {
        super(other);
        this.type = other.type;
        this.entries = other.entries;
    }

    public StatisticalReportTableTypeDto getType() {
        return type;
    }

    public void setType(final StatisticalReportTableTypeDto type) {
        this.type = type;
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public void setEntries(final List<Entry> entries) {
        this.entries = entries;
    }

    @Override
    public StatisticalReportTableDto createCopy() {
        return new StatisticalReportTableDto(this);
    }
}
