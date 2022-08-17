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
package de.interactive_instruments.etf.dal.dto;

import java.util.Collection;
import java.util.Map;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public class DtoValidityCheckUtils {

    private DtoValidityCheckUtils() {

    }

    public static void ensureNotNullOrEmpty(final String name, final Collection c) throws IncompleteDtoException {
        if (c == null) {
            throw new IncompleteDtoException("Required property '" + name + "' must be set!");
        }
        if (c.isEmpty()) {
            throw new IncompleteDtoException("Required property '" + name + "' is empty!");
        }
    }

    public static void ensureNotNullOrEmpty(final String name, final Map m) throws IncompleteDtoException {
        if (m == null) {
            throw new IncompleteDtoException("Required property '" + name + "' must be set!");
        }
        if (m.isEmpty()) {
            throw new IncompleteDtoException("Required property '" + name + "' is empty!");
        }
    }

    public static void ensureNotNullOrEmpty(final String name, final String str) throws IncompleteDtoException {
        if (str == null) {
            throw new IncompleteDtoException("Required property '" + str + "' must be set!");
        }
        if (str.trim().isEmpty()) {
            throw new IncompleteDtoException("Required property '" + str + "' is empty!");
        }
    }

    public static void ensureNotNullAndHasId(final String name, Dto dto) throws IncompleteDtoException {
        if (dto == null) {
            throw new IncompleteDtoException("Required property '" + name + "' must be set!");
        }
        if (dto.getId() == null) {
            throw new IncompleteDtoException("Required property '" + name + "' is set but does not possess an ID!");
        }
    }

    public static void ensureNotNull(final String name, Object obj) throws IncompleteDtoException {
        if (obj == null) {
            throw new IncompleteDtoException("Required property '" + name + "' must be set!");
        }
    }
}
