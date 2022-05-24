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
package de.interactive_instruments.etf.dal.dao.basex;

import static de.interactive_instruments.etf.dal.dao.basex.DsUtils.valueOfOrDefault;

import java.io.OutputStream;
import java.util.HashMap;

import org.basex.core.BaseXException;
import org.basex.core.cmd.XQuery;

import de.interactive_instruments.etf.dal.dao.Filter;

/**
 * Wrapped Xquery
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
final class BsXQuery {

    private XQuery xQuery;
    private final BsxDsCtx ctx;
    private final String queryStr;
    private final HashMap<String, String[]> parameter;

    BsXQuery(final BsxDsCtx ctx, final String queryStr) {
        this.ctx = ctx;
        this.queryStr = queryStr;
        this.parameter = new HashMap<>();
    }

    private BsXQuery(final BsxDsCtx ctx, final String queryStr, final HashMap parameter) {
        this.ctx = ctx;
        this.queryStr = queryStr;
        this.parameter = new HashMap<>(parameter);
    }

    BsXQuery parameter(final String name, final String value, final String type) {
        if (xQuery != null) {
            // reset query
            xQuery = null;
        }
        parameter.put(name, new String[]{value, type});
        return this;
    }

    BsXQuery parameter(final String name, final String value) {
        return parameter(name, value, null);
    }

    BsXQuery parameter(final Filter filter) {
        if (filter == null) {
            return this;
        }
        return parameter("offset", valueOfOrDefault(filter.offset(), "0"), "xs:integer")
                .parameter("limit", valueOfOrDefault(filter.limit(), "100"), "xs:integer")
                .parameter("levelOfDetail",
                        valueOfOrDefault(filter.levelOfDetail(), String.valueOf(Filter.LevelOfDetail.SIMPLE)))
                .parameter("fields", valueOfOrDefault(filter.fields(), "*"));
    }

    String getParameter(final String name) {
        final String[] res = parameter.get(name);
        if (res != null) {
            return res[0];
        }
        return null;
    }

    BsxDsCtx getCtx() {
        return ctx;
    }

    private void ensureInitializedQuery() {
        if (xQuery == null) {
            xQuery = new XQuery(queryStr);
            parameter.entrySet().forEach(e -> xQuery.bind("$" + e.getKey(), e.getValue()[0], e.getValue()[1]));
        }
    }

    void execute(final OutputStream os) throws BaseXException {
        ensureInitializedQuery();
        xQuery.execute(ctx.getBsxCtx(), os);
    }

    String execute() throws BaseXException {
        ensureInitializedQuery();
        return xQuery.execute(ctx.getBsxCtx());
    }

    BsXQuery createCopy() {
        return new BsXQuery(ctx, queryStr, this.parameter);
    }

    @Override
    public String toString() {
        return xQuery != null ? xQuery.toString() : "Note compiled: " + queryStr;
    }
}
