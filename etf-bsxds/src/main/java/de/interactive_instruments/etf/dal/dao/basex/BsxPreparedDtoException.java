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

/**
 * Unknown internal error most likely occurs due to database inconsistencies
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public class BsxPreparedDtoException extends IllegalStateException {
    public BsxPreparedDtoException(final String s) {
        super(s);
    }

    public BsxPreparedDtoException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public BsxPreparedDtoException(final Throwable cause) {
        super(cause);
    }
}
