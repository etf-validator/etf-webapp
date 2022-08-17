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
 * Realizes the Null Object pattern
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public final class NullLoadingContext implements LoadingContext {

    private static NullLoadingContext instance = new NullLoadingContext();

    private NullLoadingContext() {}

    public static NullLoadingContext instance() {
        return instance;
    }

    @Override
    public ItemRegistry getItemRegistry() {
        return NullItemRegistry.instance();
    }

    @Override
    public ItemFileObserverRegistry getItemFileObserverRegistry() {
        return NullItemFileObserverRegistry.instance();
    }
}
