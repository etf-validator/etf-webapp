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

import de.interactive_instruments.Configurable;
import de.interactive_instruments.Releasable;
import de.interactive_instruments.etf.component.loaders.LoadingContext;
import de.interactive_instruments.etf.dal.dto.test.ExecutableTestSuiteDto;
import de.interactive_instruments.etf.model.EidSet;

/**
 * Interface for loading Executable Test Suites
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public interface ExecutableTestSuiteLoader extends Configurable, Releasable {

    /**
     * Return all ExecutableTestSuites this loader could create after {@link #init()} has been called.
     *
     * @return set of created Dtos
     */
    EidSet<ExecutableTestSuiteDto> getExecutableTestSuites();

    /**
     * Injected to parse files and resolve dependencies
     *
     * @param loadingContext
     *            LoadingContext
     */
    void setLoadingContext(final LoadingContext loadingContext);
}
