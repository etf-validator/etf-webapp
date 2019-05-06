/**
 * Copyright 2017-2019 European Union, interactive instruments GmbH
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This work was supported by the EU Interoperability Solutions for
 * European Public Administrations Programme (http://ec.europa.eu/isa)
 * through Action 1.17: A Reusable INSPIRE Reference Platform (ARE3NA).
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

    public SimpleFilter(final int offset, final int limit, final String fields) {
        this.offset = offset > 0 ? offset : 0;
        this.limit = limit > 0 && limit < 5000 ? limit : 1500;
        this.fields = fields != null ? fields.trim() : "*";
    }

    public SimpleFilter(final String fields) {
        this.offset = 0;
        this.limit = 1500;
        this.fields = fields != null ? fields.trim() : "*";
    }

    public SimpleFilter() {
        this.offset = 0;
        this.limit = 1500;
        this.fields = "*";
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
