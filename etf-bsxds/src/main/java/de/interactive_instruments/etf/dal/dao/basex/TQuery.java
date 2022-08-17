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
package de.interactive_instruments.etf.dal.dao.basex;

import java.util.Objects;

/**
 * Type specific query
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
class TQuery {

    private final String dataBaseName;
    final String dataBaseQuery;
    final String typeQueryPath;
    final String typeName;

    public TQuery(final DataBaseType dataBaseType, final String typeName) {
        if (dataBaseType == DataBaseType.BASE || dataBaseType == DataBaseType.REUSABLE_TEST_OBJECTS) {
            this.dataBaseName = dataBaseType.dbName();
            this.dataBaseQuery = "db:open('" + dataBaseName + "')";
        } else {
            this.dataBaseName = null;
            dataBaseQuery = "(db:list()[starts-with(., 'r-')] ! db:open(.)";
        }
        this.typeName = typeName;
        this.typeQueryPath = "/etf:" + typeName;
    }

    public TQuery(final DataBaseType dataBaseType, final String dataBaseQuery, final String typeName) {
        this.dataBaseName = dataBaseType.dbName();
        this.dataBaseQuery = dataBaseQuery;
        this.typeName = typeName;
        this.typeQueryPath = "/etf:" + typeName;
    }

    public String defaultDatabaseName() {
        return Objects.requireNonNull(this.dataBaseName, "Could not determine name of database");
    }
}
