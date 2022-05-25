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

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
final public class SwitchClassLoader {

    @FunctionalInterface
    interface FunctWithException<E extends Exception> {
        void doIt() throws E;

        default void doWithException(final ClassLoader useClassLoader, final FunctWithException<E> funct) throws E {
            final ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(useClassLoader);
                funct.doIt();
            } finally {
                Thread.currentThread().setContextClassLoader(currentLoader);
            }
        }
    }

    @FunctionalInterface
    interface Funct {
        void doIt();
    }

    public static void doIt(final ClassLoader useClassLoader, final Funct funct) {
        final ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(useClassLoader);
            funct.doIt();
        } finally {
            Thread.currentThread().setContextClassLoader(currentLoader);
        }
    }
}
