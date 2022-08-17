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
package de.interactive_instruments.etf.dal.dao.basex;

import java.util.List;

import de.interactive_instruments.etf.dal.dto.capabilities.*;
import de.interactive_instruments.etf.dal.dto.result.TestTaskResultDto;
import de.interactive_instruments.etf.dal.dto.run.TestRunDto;
import de.interactive_instruments.etf.dal.dto.test.ExecutableTestSuiteDto;
import de.interactive_instruments.etf.dal.dto.test.TestItemTypeDto;
import de.interactive_instruments.etf.dal.dto.translation.TranslationTemplateBundleDto;

/**
 * Data storage result
 *
 * The object holds all unmarshaled data from a XQuery result. A XQuery result may include different object types that
 * are all unmarshaled in the specific lists. Although only one specific type is accessed through the generic
 * {@link BsxPreparedDto } or {@link BsxPreparedDtoCollection }, the objects may possess references to other object
 * types that were unmarshaled into the other lists.
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
final class DsResultSet {

    private List<ComponentDto> components;
    // private List<ParameterizedTestCaseDto> testCases;
    // private List<ParameterizedTestAssertionDto> testAssertions;
    private List<TestItemTypeDto> testItemTypes;
    private List<ExecutableTestSuiteDto> executableTestSuites;
    private List<TestObjectTypeDto> testObjectTypes;
    private List<TestObjectDto> testObjects;
    // private List<CredentialDto> credentials;
    // private List<UserDto> users;
    private List<TagDto> tags;
    private List<TranslationTemplateBundleDto> translationTemplateBundles;
    private List<ResultStyleDto> resultStyles;
    // private List<AbstractTestSuiteDto> abstractTestSuites;
    // private List<TestRunTemplateDto> testRunTemplates;
    private List<TestRunDto> testRuns;
    private List<TestTaskResultDto> testTaskResults;
    private List<TestRunTemplateDto> testRunTemplates;

    public List<ComponentDto> getComponents() {
        return components;
    }

    public List<TestItemTypeDto> getTestItemTypes() {
        return testItemTypes;
    }

    public List<ExecutableTestSuiteDto> getExecutableTestSuites() {
        return executableTestSuites;
    }

    public List<TestObjectTypeDto> getTestObjectTypes() {
        return testObjectTypes;
    }

    public List<TestObjectDto> getTestObjects() {
        return testObjects;
    }

    public List<TagDto> getTags() {
        return tags;
    }

    public List<TranslationTemplateBundleDto> getTranslationTemplateBundles() {
        return translationTemplateBundles;
    }

    public List<ResultStyleDto> getResultStyles() {
        return resultStyles;
    }

    public List<TestRunDto> getTestRuns() {
        return testRuns;
    }

    public List<TestTaskResultDto> getTestTaskResults() {
        return testTaskResults;
    }

    public List<TestRunTemplateDto> getTestRunTemplates() {
        return testRunTemplates;
    }
}
