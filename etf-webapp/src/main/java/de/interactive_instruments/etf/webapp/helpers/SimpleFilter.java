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
package de.interactive_instruments.etf.webapp.helpers;

import de.interactive_instruments.etf.dal.dao.Filter;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public class SimpleFilter implements Filter {

    private final int offset;
    private final int limit;
    private final String fields;

    public SimpleFilter(final int offset, final int limit) {
        this(offset, limit, "*");
    }

    private SimpleFilter(final int offset, final int limit, final String fields) {
        this.offset = offset > 0 ? offset : 0;
        this.limit = limit > 0 && limit < 5000 ? limit : 1500;
        this.fields = fields != null ? fields.trim() : "*";
    }

    public static Filter allItems() {
        return new SimpleFilter(0, 1500);
    }

    public static Filter filterItems(final String fields) {
        return new SimpleFilter(0, 1500, fields);
    }

    public static Filter filterItems(final int offset, final int limit, final String fields) {
        return new SimpleFilter(offset, limit, fields);
    }

    public static Filter singleItemFilter(final String fields) {
        return new SimpleFilter(0, 0, fields);
    }

    @Override
    public int offset() {
        return offset;
    }

    @Override
    public int limit() {
        return limit;
    }

    @Override
    public String fields() {
        return fields;
    }
}
