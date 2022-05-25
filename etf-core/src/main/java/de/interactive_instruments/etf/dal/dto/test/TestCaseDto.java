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
package de.interactive_instruments.etf.dal.dto.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.interactive_instruments.etf.model.NestedDependencyHolder;

public class TestCaseDto extends TestModelItemDto implements NestedDependencyHolder<TestCaseDto> {

    private List<TestCaseDto> dependencies;

    public TestCaseDto() {}

    private TestCaseDto(final TestCaseDto other) {
        super(other);
        this.dependencies = other.dependencies;
    }

    public List<TestStepDto> getTestSteps() {
        return (List<TestStepDto>) getChildren();
    }

    public void setTestSteps(final List<TestStepDto> testSteps) {
        setChildren(testSteps);
    }

    public void addTestStep(final TestStepDto testStep) {
        addChild(testStep);
    }

    public void setDependencies(final List<TestCaseDto> dependencies) {
        this.dependencies = dependencies;
    }

    public void addDependency(final TestCaseDto dependency) {
        if (this.dependencies == null) {
            this.dependencies = new ArrayList<>();
        }
        dependencies.add(dependency);
    }

    public Collection<TestCaseDto> getDependencies() {
        return dependencies;
    }

    @Override
    public TestCaseDto createCopy() {
        return new TestCaseDto(this);
    }
}
