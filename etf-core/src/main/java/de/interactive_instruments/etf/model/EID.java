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
package de.interactive_instruments.etf.model;

import java.util.UUID;

/**
 * The ETF ID class is intended to provide identifiers in the ETF environment and maps a String to an internal
 * presentation and an UUID representation
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public interface EID extends Comparable {

    /**
     * Returns the ID as String representation
     *
     * The returned id will always be equal the string representation of the object from which the ID has been created, see
     * {@link EidFactory}.
     *
     * @return string representation
     */
    String getId();

    /**
     * Returns the ID in UUID representation (which may be a generated UUID hash from the internal ID)
     *
     * @return UUID representation
     */
    UUID toUuid();
}
