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
package de.interactive_instruments.etf.detector;

import java.util.Collection;
import java.util.Iterator;

import de.interactive_instruments.etf.model.capabilities.TestObjectType;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public class IncompatibleTestObjectTypeException extends Exception {

    private final DetectedTestObjectType detectedTestObjectType;

    private static String labelFor(final DetectedTestObjectType detectedTestObjectType) {
        final StringBuilder sb = new StringBuilder();
        sb.append("'");
        sb.append(detectedTestObjectType.getLabel());
        sb.append("'");
        return sb.toString();
    }

    private static String labelFor(final Collection<? extends TestObjectType> expected) {
        final StringBuilder builder = new StringBuilder();
        for (final Iterator<? extends TestObjectType> it = expected.iterator();;) {
            builder.append('\'');
            builder.append(it.next().getLabel());
            builder.append('\'');
            if (it.hasNext()) {
                builder.append(", ");
            } else {
                break;
            }
        }
        return builder.toString();
    }

    private static String errorText(final Collection<? extends TestObjectType> expected,
            final DetectedTestObjectType detectedTestObjectType) {
        if (expected.size() > 1) {
            return "Expected was a test object of one of the types " + labelFor(expected) +
                    " but detected another type '" + labelFor(detectedTestObjectType)
                    + "' which is also not a subtype of it";
        } else {
            return "Expected a Test Object of type '" +
                    expected.iterator().next().getLabel() + "' but detected another type '" +
                    labelFor(detectedTestObjectType)
                    + "' which is also not a subtype of it";
        }
    }

    public IncompatibleTestObjectTypeException(final TestObjectType expected,
            final DetectedTestObjectType detectedTestObjectType) {
        super("Expected a Test Object of type '" +
                expected.getLabel() + "' but detected another type '" + labelFor(detectedTestObjectType)
                + "' which is also not a subtype of it");
        this.detectedTestObjectType = detectedTestObjectType;
    }

    public IncompatibleTestObjectTypeException(final Collection<? extends TestObjectType> expected,
            final DetectedTestObjectType detectedTestObjectType) {
        super(errorText(expected, detectedTestObjectType));
        this.detectedTestObjectType = detectedTestObjectType;
    }

    public TestObjectType getDetectedTestObjectType() {
        return detectedTestObjectType.toTestObjectTypeDto();
    }
}
