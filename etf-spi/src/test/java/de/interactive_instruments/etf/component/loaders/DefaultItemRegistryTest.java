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
package de.interactive_instruments.etf.component.loaders;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import de.interactive_instruments.etf.dal.dto.Dto;
import de.interactive_instruments.etf.model.EID;
import de.interactive_instruments.etf.model.EidFactory;
import de.interactive_instruments.etf.model.EidHolderMap;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
@TestMethodOrder(value = MethodOrderer.Alphanumeric.class)
public class DefaultItemRegistryTest {

    private ItemRegistry registry = new DefaultItemRegistry();

    private final static class TestDto extends Dto {
        private final String name;

        private TestDto(final String name) {
            this.name = name;
            this.id = EidFactory.getDefault().createUUID(name);
        }

        public String getName() {
            return name;
        }

        @Override
        public Dto createCopy() {
            final TestDto copy = new TestDto(name);
            copy.id = this.id;
            return copy;
        }
    }

    private static class ChangeListener implements ItemRegistry.DependencyChangeListener, Comparable<ChangeListener> {

        public Dto resolvedItem;
        public Class<? extends Dto> deregisteredItem;
        public EID deregisteredId;
        public Dto updatedItem;

        @Override
        public void fireEventDependencyResolved(final Dto resolvedItem) {
            this.resolvedItem = resolvedItem;
        }

        @Override
        public void fireEventDependencyDeregistered(final Class<? extends Dto> item, final EID eid) {
            this.deregisteredItem = item;
            this.deregisteredId = eid;
        }

        @Override
        public void fireEventDependencyUpdated(final Dto updatedItem) {
            this.updatedItem = updatedItem;
        }

        @Override
        public int compareTo(final ChangeListener o) {
            return Integer.compare(o.hashCode(), hashCode());
        }
    }

    private final TestDto testDto1 = new TestDto("1");
    private final TestDto testDto2 = new TestDto("2");

    @Test
    public void test1SimpleRegistrationAndLookupType() {

        registry.register(Collections.singleton(testDto1));

        final ChangeListener testListener = new ChangeListener();
        final EidHolderMap<? extends Dto> dtos = registry.lookupDependency(Collections.singleton(testDto1.getId()),
                testListener);
        assertNull(testListener.resolvedItem);
        assertNull(testListener.deregisteredItem);
        assertNull(testListener.deregisteredId);

        assertEquals(1, dtos.size());
        assertEquals(TestDto.class, dtos.asList().get(0).getClass());
        assertEquals(testDto1.getId(), ((TestDto) dtos.asCollection().iterator().next()).getId());
        assertEquals(testDto1.getName(), ((TestDto) dtos.asList().get(0)).getName());
    }

    @Test
    public void test2CallbackRegistration() {
        test1SimpleRegistrationAndLookupType();
        final ChangeListener testListener = new ChangeListener();
        {
            final EidHolderMap<? extends Dto> dtos = registry.lookupDependency(Collections.singleton(testDto1.getId()),
                    testListener);
            assertNull(testListener.resolvedItem);
            assertNull(testListener.deregisteredItem);
            assertNull(testListener.deregisteredId);
            assertNull(testListener.updatedItem);

            assertEquals(1, dtos.size());
            assertEquals(TestDto.class, dtos.asList().get(0).getClass());
            assertEquals(testDto1.getId(), ((TestDto) dtos.asCollection().iterator().next()).getId());
            assertEquals(testDto1.getName(), ((TestDto) dtos.asList().get(0)).getName());
        }
        {
            final List<EID> testDtos = new ArrayList<>();
            testDtos.add(testDto1.getId());
            testDtos.add(testDto2.getId());

            final EidHolderMap<? extends Dto> dtos2 = registry.lookupDependency(testDtos, testListener);
            assertNull(testListener.resolvedItem);
            assertNull(testListener.deregisteredItem);
            assertNull(testListener.deregisteredId);
            assertNull(testListener.updatedItem);

            assertEquals(1, dtos2.size());
            assertEquals(TestDto.class, dtos2.asList().get(0).getClass());
            assertEquals(testDto1.getId(), ((TestDto) dtos2.asCollection().iterator().next()).getId());
            assertEquals(testDto1.getName(), ((TestDto) dtos2.asList().get(0)).getName());
        }
    }

    @Test
    public void test3RegistrationCallback() {
        test2CallbackRegistration();
        final ChangeListener testListener = new ChangeListener();
        {
            final List<EID> testDtos = new ArrayList<>();
            testDtos.add(testDto1.getId());
            testDtos.add(testDto2.getId());

            final EidHolderMap<? extends Dto> dtos2 = registry.lookupDependency(testDtos, testListener);
            assertNull(testListener.resolvedItem);
            assertNull(testListener.deregisteredItem);
            assertNull(testListener.deregisteredId);
            assertNull(testListener.updatedItem);

            assertEquals(1, dtos2.size());
            assertEquals(TestDto.class, dtos2.asList().get(0).getClass());
            assertEquals(testDto1.getId(), ((TestDto) dtos2.asCollection().iterator().next()).getId());
            assertEquals(testDto1.getName(), ((TestDto) dtos2.asList().get(0)).getName());
        }
        {
            assertNull(testListener.resolvedItem);
            assertNull(testListener.deregisteredItem);
            assertNull(testListener.deregisteredId);
            assertNull(testListener.updatedItem);
            registry.register(Collections.singleton(testDto2));

            assertEquals(testDto2.getClass(), testListener.resolvedItem.getClass());
            assertEquals(testDto2.getId(), testListener.resolvedItem.getId());
            assertEquals(testDto2.getName(), ((TestDto) testListener.resolvedItem).getName());
            testListener.resolvedItem = null;
            assertNull(testListener.deregisteredItem);
            assertNull(testListener.deregisteredId);
            assertNull(testListener.updatedItem);

            final List<EID> testDtos = new ArrayList<>();
            testDtos.add(testDto1.getId());
            testDtos.add(testDto2.getId());
            final EidHolderMap<? extends Dto> dtos2 = registry.lookupDependency(testDtos, testListener);
            assertEquals(2, dtos2.size());
            assertEquals(testDto1.getId(), dtos2.get(testDto1.getId()).getId());
            assertEquals(testDto2.getId(), dtos2.get(testDto2.getId()).getId());

            assertNull(testListener.resolvedItem);
            assertNull(testListener.deregisteredItem);
            assertNull(testListener.deregisteredId);
            assertNull(testListener.updatedItem);
        }
    }

    @Test
    public void test3deregistrationCallback() {
        test3RegistrationCallback();
        final ChangeListener testListener1 = new ChangeListener();
        final ChangeListener testListener2 = new ChangeListener();
        {
            final List<EID> testDtos = new ArrayList<>();
            testDtos.add(testDto1.getId());
            testDtos.add(testDto2.getId());

            final EidHolderMap<? extends Dto> dtos1 = registry.lookupDependency(testDtos, testListener1);
            assertEquals(2, dtos1.size());
            assertEquals(testDto1.getId(), dtos1.get(testDto1.getId()).getId());
            assertEquals(testDto2.getId(), dtos1.get(testDto2.getId()).getId());

            registry.deregister(Collections.singleton(testDto1));

            assertNull(testListener1.resolvedItem);
            assertEquals(TestDto.class, testListener1.deregisteredItem);
            testListener1.deregisteredItem = null;
            assertEquals(testDto1.getId(), testListener1.deregisteredId);
            testListener1.deregisteredId = null;
            assertNull(testListener1.updatedItem);

            final EidHolderMap<? extends Dto> dtos2 = registry.lookupDependency(testDtos, testListener2);
            assertEquals(1, dtos2.size());
            assertEquals(TestDto.class, dtos2.asList().get(0).getClass());
            assertEquals(testDto2.getId(), ((TestDto) dtos2.asCollection().iterator().next()).getId());
            assertEquals(testDto2.getName(), ((TestDto) dtos2.asList().get(0)).getName());

            assertNull(testListener1.resolvedItem);
            assertNull(testListener1.deregisteredItem);
            assertNull(testListener1.deregisteredId);
            assertNull(testListener1.updatedItem);

            assertNull(testListener2.resolvedItem);
            assertNull(testListener2.deregisteredItem);
            assertNull(testListener2.deregisteredId);
            assertNull(testListener2.updatedItem);

            registry.register(Collections.singleton(testDto1));

            assertEquals(testDto1.getClass(), testListener1.resolvedItem.getClass());
            assertEquals(testDto1.getId(), testListener1.resolvedItem.getId());
            assertEquals(testDto1.getName(), ((TestDto) testListener1.resolvedItem).getName());
            assertNull(testListener1.deregisteredItem);
            assertNull(testListener1.deregisteredId);
            assertNull(testListener1.updatedItem);
            testListener1.resolvedItem = null;

            assertEquals(testDto1.getClass(), testListener2.resolvedItem.getClass());
            assertEquals(testDto1.getId(), testListener2.resolvedItem.getId());
            assertEquals(testDto1.getName(), ((TestDto) testListener2.resolvedItem).getName());
            assertNull(testListener2.deregisteredItem);
            assertNull(testListener2.deregisteredId);
            assertNull(testListener2.updatedItem);
            testListener2.resolvedItem = null;

            registry.deregisterCallback(testListener1);
            registry.deregister(Collections.singleton(testDto2));

            assertNull(testListener1.resolvedItem);
            assertNull(testListener1.deregisteredItem);
            assertNull(testListener1.deregisteredId);
            assertNull(testListener1.updatedItem);

            assertNull(testListener2.resolvedItem);
            assertEquals(TestDto.class, testListener2.deregisteredItem);
            assertEquals(testDto2.getId(), testListener2.deregisteredId);
        }
    }
}
