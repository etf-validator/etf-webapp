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

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import de.interactive_instruments.etf.dal.dto.Dto;
import de.interactive_instruments.etf.model.EID;
import de.interactive_instruments.etf.model.EidFactory;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
@TestMethodOrder(value = MethodOrderer.Alphanumeric.class)
public class ItemFileLoaderTest {

    private static class TestItemFileLoaderResultListener implements ItemFileLoaderResultListener {

        @Override
        public void eventItemBuilt(final Dto dto) {

        }

        @Override
        public void eventItemDestroyed(final EID id) {

        }

        @Override
        public void eventItemUpdated(final Dto dto) {

        }
    }

    private static class TestItemFileLoader extends AbstractItemFileLoader {

        protected TestItemFileLoader(final ItemFileLoaderResultListener itemListener, final int priority) {
            super(itemListener, priority, new File("."));
        }

        @Override
        protected boolean doPrepare() {
            return false;
        }

        @Override
        protected Dto doBuild() {
            return null;
        }

        @Override
        protected void doRelease() {

        }
    }

    @Test
    public void test1SimpleRegistrationAndLookupType() {

        final TestItemFileLoader itemFileLoader = new TestItemFileLoader(new TestItemFileLoaderResultListener(), 9);
        itemFileLoader.dependsOn(EidFactory.getDefault().createUUID("TEST.ID"));

        final TestItemFileLoader itemFileLoader2 = new TestItemFileLoader(new TestItemFileLoaderResultListener(), 8);

        final List<TestItemFileLoader> fileLoader = new ArrayList<TestItemFileLoader>() {
            {
                add(itemFileLoader);
                add(new TestItemFileLoader(new TestItemFileLoaderResultListener(), 9));
                add(new TestItemFileLoader(new TestItemFileLoaderResultListener(), 9));
                add(itemFileLoader);
                add(itemFileLoader2);
                add(itemFileLoader);
                add(itemFileLoader2);
            }
        };

        fileLoader.sort(Comparator.naturalOrder());

    }
}
