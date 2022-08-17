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

import de.interactive_instruments.etf.dal.dto.capabilities.TestObjectDto;
import de.interactive_instruments.etf.model.DefaultEidMap;
import de.interactive_instruments.etf.model.EidMap;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public class TypeDetectorManager {

    private EidMap<TypeDetector> detectors = new DefaultEidMap();

    void register(final TypeDetector detector) {
        detectors.put(detector.getId(), detector);
    }

    /**
     * Detects and sets the Test Object Type. If the switch 'enrichTestObject' is set to true, the detector might change
     * additional properties, like the name or the description
     *
     * @param testObject
     * @param enrichTestObject
     */
    void detectAndSetType(final TestObjectDto testObject, final boolean enrichTestObject) {

    }
}
