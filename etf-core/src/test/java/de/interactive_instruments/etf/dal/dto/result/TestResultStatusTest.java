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
package de.interactive_instruments.etf.dal.dto.result;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public class TestResultStatusTest {

    @Test
    public void testAggregateStatus() {

        assertEquals(TestResultStatus.PASSED,
                TestResultStatus.aggregateStatus(TestResultStatus.PASSED, TestResultStatus.PASSED));

        assertEquals(TestResultStatus.FAILED,
                TestResultStatus.aggregateStatus(TestResultStatus.PASSED, TestResultStatus.FAILED));

        assertEquals(TestResultStatus.FAILED,
                TestResultStatus.aggregateStatus(TestResultStatus.SKIPPED, TestResultStatus.INFO, TestResultStatus.FAILED));

        assertEquals(TestResultStatus.FAILED,
                TestResultStatus.aggregateStatus(TestResultStatus.PASSED_MANUAL, TestResultStatus.UNDEFINED,
                        TestResultStatus.FAILED));

        assertEquals(TestResultStatus.SKIPPED,
                TestResultStatus.aggregateStatus(TestResultStatus.SKIPPED, TestResultStatus.PASSED));

        assertEquals(TestResultStatus.WARNING,
                TestResultStatus.aggregateStatus(TestResultStatus.WARNING, TestResultStatus.INFO, TestResultStatus.PASSED));

        assertEquals(TestResultStatus.INFO, TestResultStatus.aggregateStatus(TestResultStatus.INFO, TestResultStatus.PASSED));

        assertEquals(TestResultStatus.PASSED_MANUAL,
                TestResultStatus.aggregateStatus(TestResultStatus.PASSED_MANUAL, TestResultStatus.PASSED));

        assertEquals(TestResultStatus.UNDEFINED,
                TestResultStatus.aggregateStatus(TestResultStatus.PASSED_MANUAL, TestResultStatus.UNDEFINED));

        assertEquals(TestResultStatus.UNDEFINED, TestResultStatus.aggregateStatus(TestResultStatus.UNDEFINED));

        assertEquals(TestResultStatus.UNDEFINED, TestResultStatus.aggregateStatus((TestResultStatus[]) null));

        assertEquals(TestResultStatus.INTERNAL_ERROR,
                TestResultStatus.aggregateStatus(TestResultStatus.PASSED, TestResultStatus.INTERNAL_ERROR,
                        TestResultStatus.PASSED));

        assertEquals(TestResultStatus.FAILED,
                TestResultStatus.aggregateStatus(TestResultStatus.PASSED, TestResultStatus.NOT_APPLICABLE,
                        TestResultStatus.FAILED));

        assertEquals(TestResultStatus.PASSED,
                TestResultStatus.aggregateStatus(TestResultStatus.PASSED, TestResultStatus.NOT_APPLICABLE));

        assertEquals(TestResultStatus.PASSED,
                TestResultStatus.aggregateStatus(TestResultStatus.NOT_APPLICABLE, TestResultStatus.PASSED));

        assertEquals(TestResultStatus.SKIPPED,
                TestResultStatus.aggregateStatus(TestResultStatus.NOT_APPLICABLE, TestResultStatus.PASSED,
                        TestResultStatus.SKIPPED));

        assertEquals(TestResultStatus.INTERNAL_ERROR,
                TestResultStatus.aggregateStatus(TestResultStatus.UNDEFINED, TestResultStatus.INTERNAL_ERROR));

        assertEquals(TestResultStatus.INTERNAL_ERROR,
                TestResultStatus.aggregateStatus(TestResultStatus.PASSED_MANUAL,
                        TestResultStatus.UNDEFINED, TestResultStatus.INTERNAL_ERROR));

        assertEquals(TestResultStatus.FAILED,
                TestResultStatus.aggregateStatus(TestResultStatus.PASSED_MANUAL,
                        TestResultStatus.FAILED, TestResultStatus.INTERNAL_ERROR));
    }
}
