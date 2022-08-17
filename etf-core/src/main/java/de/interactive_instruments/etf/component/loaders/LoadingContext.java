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
package de.interactive_instruments.etf.component.loaders;

/**
 * Access to objects that can be used during the Item (re-)loading process
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public interface LoadingContext {

    /**
     * Item Registry to register framework items
     *
     * @return ItemRegistry
     */
    ItemRegistry getItemRegistry();

    /**
     * A registry to register a callback interface for a file observer that is triggered, when a specific file is found in
     * the filesystem or
     *
     * @return ItemRegistry
     */
    ItemFileObserverRegistry getItemFileObserverRegistry();
}
