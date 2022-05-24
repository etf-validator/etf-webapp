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

import java.io.IOException;
import java.io.OutputStream;

import de.interactive_instruments.etf.dal.dao.PreparedDto;
import de.interactive_instruments.etf.dal.dto.Dto;
import de.interactive_instruments.etf.model.EID;
import de.interactive_instruments.etf.model.OutputFormat;
import de.interactive_instruments.properties.PropertyHolder;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
final class ResolvedDto<T extends Dto> implements PreparedDto<T> {

    private final T dto;

    ResolvedDto(final T dto) {
        this.dto = dto;
    }

    @Override
    public EID getDtoId() {
        return dto.getId();
    }

    @Override
    public T getDto() {
        return dto;
    }

    @Override
    public void streamTo(final OutputFormat outputFormat, final PropertyHolder propertyHolder, final OutputStream outputStream)
            throws IOException {
        throw new IllegalAccessError("Not implemented");
    }

    @Override
    public int compareTo(final PreparedDto o) {
        return dto.getId().compareTo(o.getDtoId());
    }
}
