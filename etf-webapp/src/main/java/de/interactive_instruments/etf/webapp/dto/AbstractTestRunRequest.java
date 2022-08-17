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
package de.interactive_instruments.etf.webapp.dto;

import java.io.IOException;
import java.net.URISyntaxException;

import de.interactive_instruments.etf.dal.dao.DtoResolver;
import de.interactive_instruments.etf.dal.dto.capabilities.TestObjectDto;
import de.interactive_instruments.etf.dal.dto.run.TestRunDto;
import de.interactive_instruments.etf.webapp.controller.DataStorageService;
import de.interactive_instruments.exceptions.ObjectWithIdNotFoundException;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public abstract class AbstractTestRunRequest {
    public abstract void inject(final DtoResolver<TestObjectDto> testObjectResolver,
            final DataStorageService dataStorageService);

    public abstract TestRunDto toTestRun() throws ObjectWithIdNotFoundException, IOException, URISyntaxException;
}
