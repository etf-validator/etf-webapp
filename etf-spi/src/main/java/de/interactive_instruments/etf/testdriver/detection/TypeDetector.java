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
package de.interactive_instruments.etf.testdriver.detection;

import java.util.Collection;

import de.interactive_instruments.MediaType;
import de.interactive_instruments.etf.dal.dto.capabilities.TestObjectDto;
import de.interactive_instruments.etf.dal.dto.capabilities.TestObjectTypeDto;
import de.interactive_instruments.etf.model.EidHolder;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public interface TypeDetector extends EidHolder {

    Collection<MediaType> supportedTypes();

    interface TypeDetectionCmd {
        enum Status {
            TYPE_KNOWN, NEED_MORE_DATA, TYPE_UNKNOWN,
        }

        Status status();

        /**
         * Set the Test Object type for the Test Object
         *
         * @param dto
         *            Test Object Dto
         */
        void setType(final TestObjectDto dto);

        /**
         * All Types a Detector can detect
         *
         * @return list of TestObjectTypeDto
         */
        Collection<TestObjectTypeDto> getDetectibleTypes();
    }

    boolean supportsDetectionByMimeType();

    boolean supportsDetectionByFileExtension();

    boolean supportsDetectionByContent();
}
