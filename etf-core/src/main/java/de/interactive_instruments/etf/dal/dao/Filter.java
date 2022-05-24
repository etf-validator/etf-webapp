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
 * Filter criteria for a Dao request
 *
 * Todo: filter by tags for Executable Test Suite Controller + filter by Date for removeExpiredItemsHolder()
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public interface Filter {

    /**
     * The number of Dtos to skip
     *
     * @return the number of Dtos which where skipped
     */
    int offset();

    /**
     * The maximum number of Dtos that should be returned
     *
     * @return the maximum number of Dtos that should be returned
     */
    int limit();

    enum LevelOfDetail {
        /**
         * Don't include references in result
         */
        SIMPLE,
        /**
         * Include historical references to older items
         */
        HISTORY,

        /**
         * Include references -without historical references to older items
         */
        DETAILED_WITHOUT_HISTORY
    }

    /**
     * Controls which references are included in a data storage result
     *
     * @return Level of Detail
     */
    default LevelOfDetail levelOfDetail() {
        return LevelOfDetail.SIMPLE;
    }

    /**
     * List of fields that should be shown
     *
     * @return List of fields as String, separated with a comma, or *
     */
    default String fields() {
        return "*";
    }
}
