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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import de.interactive_instruments.etf.dal.dao.PreparedDto;
import de.interactive_instruments.etf.dal.dto.Dto;
import de.interactive_instruments.etf.model.EID;

/**
 * A prepared XQuery statement for querying a single item and its references. The getDto() or the streamTo() method will
 * execute the request.
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
final class BsxPreparedDto<T extends Dto> extends AbstractBsxPreparedDto implements PreparedDto<T> {

    private final EID id;
    private final GetDtoResultCmd<T> getter;
    private T cachedDto;

    BsxPreparedDto(final EID id, final BsXQuery bsXquery, final GetDtoResultCmd<T> getter) {
        super(bsXquery);
        this.id = id;
        this.getter = getter;
    }

    @Override
    public EID getDtoId() {
        return id;
    }

    public T getDto() {
        if (cachedDto == null) {

            // cachedDto = (T) bsXquery.getCtx().getFromCache(id);

            if (cachedDto == null) {
                try {
                    final ByteArrayOutputStream output = new ByteArrayOutputStream();
                    bsXquery.execute(output);
                    final DsResultSet result = (DsResultSet) bsXquery.getCtx().createUnmarshaller().unmarshal(
                            new ByteArrayInputStream(output.toByteArray()));
                    cachedDto = getter.getMainDto(result);
                    if (cachedDto == null) {
                        bsXquery.getCtx().getLogger().error("Query ID: {}", "EID" + id);
                        throw new BsxPreparedDtoException("Data storage returned no data for \"" + id + "\"");
                    }
                } catch (IOException | JAXBException e) {
                    bsXquery.getCtx().getLogger().error("Query ID: {}", "EID" + id);
                    logError(e);
                    throw new BsxPreparedDtoException(e);
                }
            }
        }
        return cachedDto;
    }

    @Override
    public int compareTo(final PreparedDto o) {
        if (!(o instanceof BsxPreparedDto)) {
            return -1;
        }
        final BsxPreparedDto bsxO = (BsxPreparedDto) o;
        if (cachedDto != null && bsxO.cachedDto != null) {
            final int cmp = cachedDto.getId().compareTo(bsxO.cachedDto.getId());
            if (cmp != 0) {
                return cmp;
            }
        }
        return this.id.compareTo(bsxO.id);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("BsxPreparedDto{");
        sb.append("xquery=").append(bsXquery.toString());
        sb.append(", id=").append(id);
        sb.append(", cachedDto=").append(cachedDto != null ? cachedDto.getId() : "unresolved");
        sb.append('}');
        return sb.toString();
    }
}
