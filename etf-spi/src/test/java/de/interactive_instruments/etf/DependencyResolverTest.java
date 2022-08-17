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
package de.interactive_instruments.etf;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.*;

import de.interactive_instruments.etf.dal.dto.test.ExecutableTestSuiteDto;
import de.interactive_instruments.etf.testdriver.CyclicDependencyException;
import de.interactive_instruments.etf.testdriver.DependencyGraph;
import de.interactive_instruments.exceptions.ObjectWithIdNotFoundException;
import de.interactive_instruments.exceptions.StorageException;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
@TestMethodOrder(value = MethodOrderer.Alphanumeric.class)
public class DependencyResolverTest {

    @Test
    public void testDependencyResolving() throws StorageException, ObjectWithIdNotFoundException, CyclicDependencyException {

        final DependencyGraph<ExecutableTestSuiteDto> dependencyResolver = new DependencyGraph();

        final ExecutableTestSuiteDto ets1 = TestUtils.createEts(1);
        final ExecutableTestSuiteDto ets2 = TestUtils.createEts(2);
        final ExecutableTestSuiteDto ets3 = TestUtils.createEts(3);
        final ExecutableTestSuiteDto ets4 = TestUtils.createEts(4);
        final ExecutableTestSuiteDto ets5 = TestUtils.createEts(5);
        final ExecutableTestSuiteDto ets6 = TestUtils.createEts(6);
        final ExecutableTestSuiteDto ets7 = TestUtils.createEts(7);

        ets1.addDependency(ets2);
        ets1.addDependency(ets3);
        ets1.addDependency(ets6);

        ets2.addDependency(ets4);
        ets2.addDependency(ets3);

        ets3.addDependency(ets4);
        ets3.addDependency(ets5);

        ets4.addDependency(ets6);

        ets5.addDependency(ets7);

        ets6.addDependency(ets7);

        final ArrayList<ExecutableTestSuiteDto> executableTestSuiteDtos = new ArrayList<ExecutableTestSuiteDto>() {
            {
                add(ets1);
                add(ets2);
                add(ets3);
                add(ets4);
                add(ets5);
                add(ets6);
                add(ets7);
            }
        };

        dependencyResolver.addAllDependencies(executableTestSuiteDtos);
        final List<ExecutableTestSuiteDto> sorted = dependencyResolver.sort();

        assertNotNull(sorted);
        assertEquals(7, sorted.size());

        assertEquals("ETS.7", sorted.get(6).getLabel());
        assertEquals("ETS.5", sorted.get(5).getLabel());
        assertEquals("ETS.6", sorted.get(4).getLabel());
        assertEquals("ETS.4", sorted.get(3).getLabel());
        assertEquals("ETS.3", sorted.get(2).getLabel());
        assertEquals("ETS.2", sorted.get(1).getLabel());
        assertEquals("ETS.1", sorted.get(0).getLabel());

    }

    @Test
    public void testCycleDetection() {

        Assertions.assertThrows(CyclicDependencyException.class, () -> {

            final DependencyGraph<ExecutableTestSuiteDto> dependencyResolver = new DependencyGraph();

            final ExecutableTestSuiteDto ets1 = TestUtils.createEts(1);
            final ExecutableTestSuiteDto ets2 = TestUtils.createEts(2);
            final ExecutableTestSuiteDto ets3 = TestUtils.createEts(3);
            final ExecutableTestSuiteDto ets4 = TestUtils.createEts(4);

            // Cycle
            ets1.addDependency(ets2);
            ets2.addDependency(ets3);
            ets3.addDependency(ets4);
            ets4.addDependency(ets1);

            final ArrayList<ExecutableTestSuiteDto> executableTestSuiteDtos = new ArrayList<ExecutableTestSuiteDto>() {
                {
                    add(ets1);
                    add(ets2);
                    add(ets3);
                    add(ets4);
                }
            };

            dependencyResolver.addAllDependencies(executableTestSuiteDtos);
            dependencyResolver.sort();

        });
    }

    @Test
    public void testIgnoreCycles() throws StorageException, ObjectWithIdNotFoundException, CyclicDependencyException {
        final DependencyGraph<ExecutableTestSuiteDto> dependencyResolver = new DependencyGraph();

        final ExecutableTestSuiteDto ets1 = TestUtils.createEts(1);
        final ExecutableTestSuiteDto ets2 = TestUtils.createEts(2);
        final ExecutableTestSuiteDto ets3 = TestUtils.createEts(3);
        final ExecutableTestSuiteDto ets4 = TestUtils.createEts(4);
        final ExecutableTestSuiteDto ets5 = TestUtils.createEts(5);
        final ExecutableTestSuiteDto ets6 = TestUtils.createEts(6);
        final ExecutableTestSuiteDto ets7 = TestUtils.createEts(7);

        // Cycle
        ets1.addDependency(ets2);
        ets2.addDependency(ets3);
        ets3.addDependency(ets4);
        ets4.addDependency(ets1);

        ets5.addDependency(ets6);

        ets7.addDependency(ets1);

        final ArrayList<ExecutableTestSuiteDto> executableTestSuiteDtos = new ArrayList<ExecutableTestSuiteDto>() {
            {
                add(ets1);
                add(ets2);
                add(ets3);
                add(ets4);

                add(ets5);
                add(ets6);

                add(ets7);
            }
        };

        dependencyResolver.addAllDependencies(executableTestSuiteDtos);
        final List<ExecutableTestSuiteDto> sortedList = dependencyResolver.sortIgnoreCylce();
        System.out.println(sortedList);

    }
}
