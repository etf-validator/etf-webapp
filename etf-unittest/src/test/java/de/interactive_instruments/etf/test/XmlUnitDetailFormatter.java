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
package de.interactive_instruments.etf.test;

import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.ComparisonType;
import org.xmlunit.diff.DefaultComparisonFormatter;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
class XmlUnitDetailFormatter extends DefaultComparisonFormatter {

    public String getControlDetailDescription(final Comparison comparison) {
        return getDetailDescription(comparison, comparison.getControlDetails());
    }

    public String getTestDetailDescription(final Comparison comparison) {
        return getDetailDescription(comparison, comparison.getTestDetails());
    }

    private String getDetailDescription(final Comparison difference, final Comparison.Detail detail) {
        final ComparisonType type = difference.getType();
        String description = type.getDescription();
        final String target = getShortString(detail.getTarget(), detail.getXPath(),
                type);

        if (type == ComparisonType.ATTR_NAME_LOOKUP) {
            return new StringBuilder().append(description).append(" ").append(detail.getXPath()).append(" ").append(target)
                    .toString();
        }
        return new StringBuilder().append(description).append(" ").append(getValue(detail.getValue(), type)).append(" ")
                .append(target).toString();
    }
}
