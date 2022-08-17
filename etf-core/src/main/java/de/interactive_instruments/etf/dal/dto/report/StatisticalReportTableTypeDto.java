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
package de.interactive_instruments.etf.dal.dto.report;

import java.util.List;

import de.interactive_instruments.etf.dal.dto.RepositoryItemDto;

public class StatisticalReportTableTypeDto extends RepositoryItemDto {

    private List<String> columnHeaderLabels;
    private List<String> columnExpressions;

    public StatisticalReportTableTypeDto() {}

    private StatisticalReportTableTypeDto(final StatisticalReportTableTypeDto other) {
        super(other);
        this.columnHeaderLabels = other.columnHeaderLabels;
        this.columnExpressions = other.columnExpressions;
    }

    public List<String> getColumnHeaderLabels() {
        return columnHeaderLabels;
    }

    public void setColumnHeaderLabels(final List<String> columnHeaderLabels) {
        this.columnHeaderLabels = columnHeaderLabels;
    }

    public List<String> getColumnExpressions() {
        return columnExpressions;
    }

    public void setColumnExpressions(final List<String> columnExpressions) {
        this.columnExpressions = columnExpressions;
    }

    @Override
    public StatisticalReportTableTypeDto createCopy() {
        return new StatisticalReportTableTypeDto(this);
    }
}
