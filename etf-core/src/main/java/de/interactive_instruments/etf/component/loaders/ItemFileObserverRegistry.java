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
import java.util.List;

/**
 * Registry to register / deregister ItemFileLoaderFactories that will be informed about changes in the specified file
 * path.
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public interface ItemFileObserverRegistry {

    /**
     * Register factories
     *
     * @param path
     *            path to observe
     * @param factories
     *            factories to register
     */
    void register(final Path path, final List<? extends ItemFileLoaderFactory> factories);

    /**
     * Deregister factories
     *
     * @param factories
     *            factories to deregister
     */
    void deregister(final List<? extends ItemFileLoaderFactory> factories);
}
