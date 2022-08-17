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

import java.util.List;

import de.interactive_instruments.etf.dal.dto.Dto;

/**
 * Command object for getting the specific Dtos from a DsResultSet
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
@FunctionalInterface
interface GetDtoResultCmd<T extends Dto> {

    List<T> getMainDtos(final DsResultSet dsResultSet);

    default T getMainDto(final DsResultSet dsResultSet) {
        final List<T> colResult = getMainDtos(dsResultSet);
        return colResult != null && !colResult.isEmpty() ? colResult.get(0) : null;
    }
}
