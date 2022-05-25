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

import java.nio.file.Path;

import de.interactive_instruments.Releasable;

/**
 * A factory to create an item loader that implements a FileChangeListener interface.
 *
 * @see FileChangeListener
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public interface ItemFileLoaderFactory {

    /**
     * The callback interface that the ItemFileLoader uses to report changes
     *
     * The listener should implement the comparable interface which can be used to prioritize the loading process.
     */
    interface FileChangeListener extends Releasable, Comparable<FileChangeListener> {
        /**
         * File created
         */
        void eventFileCreated();

        /**
         * File deleted
         */
        void eventFileDeleted();

        /**
         * File updated
         */
        void eventFileUpdated();
    }

    /**
     * Priority of this factory
     *
     * Smaller values mean a higher priority.
     *
     * @return priority
     */
    default int getPriority() {
        return 700;
    }

    /**
     * Returns true, if the minimum criteria are fulfilled (could be based on the filename), false otherwise.
     *
     * @param path
     *            path to check
     * @return true if this factory could handle this path, false otherwise
     */
    boolean couldHandle(final Path path);

    /**
     * This will create a factory that must implement the {@link FileChangeListener} interface.
     *
     * If the file cannot be build, no exception should be thrown, but {@code null} should be returned.
     *
     * @param file
     *            file to use for loading
     * @return a factory that implements the listener or null if the build failed
     */
    FileChangeListener load(final Path file);
}
