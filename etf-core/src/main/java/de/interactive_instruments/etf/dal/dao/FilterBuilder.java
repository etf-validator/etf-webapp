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
package de.interactive_instruments.etf.dal.dao;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public interface FilterBuilder {

    /**
     * The number of Dtos to skip
     *
     * @param offset
     *            non-negative offset as integer
     * @return the number of Dtos which where skipped
     */
    FilterBuilder offset(int offset);

    /**
     * The maximum number of Dtos that should be returned
     *
     * @param limit
     *            non-negative limit as integer
     * @return the maximum number of Dtos that should be returned
     */
    FilterBuilder limit(int limit);

    /**
     * Controls which references are included in a data storage result
     *
     * @param levelOfDetail
     *            level of detail filter
     * @return Filter builder
     */
    FilterBuilder levelOfDetail(Filter.LevelOfDetail levelOfDetail);

    interface PropertyExpression {
        Conj isLike(final String searchTerm);

        Conj isEqual(final String comparand);

        Conj isLessThan(final String comparand);

        Conj isGreaterThan(final String comparand);

        Conj exists();

        /**
         * Property name
         *
         * @return property name
         */
        String name();
    }

    interface Conj {
        PropertyExpression and();

        PropertyExpression or();
    }

    FilterBuilder fields(final String... property);

    FilterBuilder where(final PropertyExpression expression);

    enum Order {
        ASCENDING, DESCENDING
    }

    FilterBuilder orderBy(final Order order, final String... property);

    Filter build();
}
