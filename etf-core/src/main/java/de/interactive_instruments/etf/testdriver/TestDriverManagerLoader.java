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
package de.interactive_instruments.etf.testdriver;

import java.util.ServiceLoader;

import de.interactive_instruments.SUtils;

/**
 * Loads a EidFactory service provider
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
class TestDriverManagerLoader {

    public static final String ETF_TESTDRIVER_MANAGER = "etf.testdriver.manager";

    private static final class InstanceHolder {
        static final TestDriverManager findTestDriverManager() {
            final ServiceLoader<TestDriverManager> managers = ServiceLoader.load(TestDriverManager.class);
            final String managerClassname = System.getProperty(ETF_TESTDRIVER_MANAGER);
            if (SUtils.isNullOrEmpty(managerClassname)) {
                return managers.iterator().next();
            } else {
                for (final TestDriverManager manager : managers) {
                    if (managerClassname.equals(manager.getClass().getName())) {
                        return manager;
                    }
                }
            }
            throw new RuntimeException("Can not load Test Driver Manager " + managerClassname);
        }

        static final TestDriverManager INSTANCE = findTestDriverManager();
    }

    public static TestDriverManager instance() {
        return TestDriverManagerLoader.InstanceHolder.INSTANCE;
    }
}
