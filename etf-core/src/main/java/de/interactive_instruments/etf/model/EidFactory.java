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
package de.interactive_instruments.etf.model;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Interface for a factory that creates etf identifier objects.
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public interface EidFactory {

    /**
     * Creates an EID with an internal random ID
     *
     * @return created EID
     */
    EID createRandomId();

    /**
     * The Factory ensures that:
     *
     * str equals createFromStrToUUID(str).getId();
     *
     * @param str
     *            string
     * @return created EID
     */
    EID createAndPreserveStr(String str);

    /**
     * The Factory ensures that:
     *
     * uuidStr equals createUUID(uuidStr).toUUID().toString() or that UUID.nameUUIDFromBytes(uuidStr).toString() equals
     * createUUID(uuidStr).toUUID().toString()
     *
     * @param uuidStr
     *            a string as UUID or a simple string from which a EID is generated
     * @return created EID
     */
    EID createUUID(String uuidStr);

    /**
     * The Factory ensures that:
     *
     * uuid equals createAndPreserveUUID(uuid).toUUID();
     *
     * @param uuid
     *            UUID object
     * @return created EID
     */
    EID createAndPreserveUUID(UUID uuid);

    /**
     * Returns the pattern to check the ID
     *
     * @return pattern to check an ID
     */
    Pattern getPattern();

    /**
     * Get the default factory
     *
     * @return default factory
     */
    static EidFactory getDefault() {
        return EidFactoryLoader.instance();
    }

}
