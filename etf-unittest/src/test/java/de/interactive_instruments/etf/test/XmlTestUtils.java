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
package de.interactive_instruments.etf.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public class XmlTestUtils {
    public static void compare(final String expected, final String actual) {
        final XmlUnitDetailFormatter formatter = new XmlUnitDetailFormatter();
        final Diff diff = DiffBuilder.compare(Input.fromString(actual))
                .withTest(Input.fromString(expected))
                .checkForSimilar().checkForIdentical()
                .ignoreComments()
                .ignoreWhitespace()
                .normalizeWhitespace()
                .ignoreElementContentWhitespace()
                .build();
        if (diff.hasDifferences()) {
            final Difference difference = diff.getDifferences().iterator().next();
            assertEquals(formatter.getControlDetailDescription(difference.getComparison()),
                    formatter.getTestDetailDescription(difference.getComparison()));
        }
    }
}
