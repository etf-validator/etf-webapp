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
package de.interactive_instruments.etf.testdriver;

import java.util.NoSuchElementException;
import java.util.ServiceLoader;

import de.interactive_instruments.SUtils;
import de.interactive_instruments.exceptions.ExcUtils;

/**
 * Loads a EidFactory service provider
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
class TestResultCollectorFactoryLoader {

    public static final String ETF_TESTDRIVER_RESULT_COLLECTOR_FACTORY = "etf.testdriver.resultcollectorfactory";

    private static final class InstanceHolder {
        static final TestResultCollectorFactory findResultCollectorFactory() {
            final ServiceLoader<TestResultCollectorFactory> collectorFactories = ServiceLoader
                    .load(TestResultCollectorFactory.class);
            final String collectorFactoryClassname = System.getProperty(ETF_TESTDRIVER_RESULT_COLLECTOR_FACTORY);
            try {
                if (SUtils.isNullOrEmpty(collectorFactoryClassname)) {
                    return collectorFactories.iterator().next();
                } else {
                    for (final TestResultCollectorFactory collectorFactory : collectorFactories) {
                        if (collectorFactoryClassname.equals(collectorFactory.getClass().getName())) {
                            return collectorFactory;
                        }
                    }
                }
            } catch (NoSuchElementException e) {
                ExcUtils.suppress(e);
            }
            throw new RuntimeException("Can not load Result Collector Factory " +
                    collectorFactoryClassname != null ? collectorFactoryClassname : "");
        }

        static final TestResultCollectorFactory INSTANCE = findResultCollectorFactory();
    }

    public static TestResultCollectorFactory instance() {
        return TestResultCollectorFactoryLoader.InstanceHolder.INSTANCE;
    }
}
