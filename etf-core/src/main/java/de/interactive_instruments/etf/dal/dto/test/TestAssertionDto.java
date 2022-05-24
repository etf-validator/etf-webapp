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
import java.util.List;
import java.util.Objects;

import de.interactive_instruments.etf.dal.dto.ModelItemDto;
import de.interactive_instruments.etf.dal.dto.translation.LangTranslationTemplateCollectionDto;

/**
 * An Assertion embodies the expectation of a test result.
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public class TestAssertionDto extends TestModelItemDto {

    private String expectedResult;
    private String expression;
    private TestItemTypeDto testAssertionType;
    private List<LangTranslationTemplateCollectionDto> translationTemplates;

    public TestAssertionDto() {}

    private TestAssertionDto(final TestAssertionDto other) {
        super(other);
        this.expectedResult = other.expectedResult;
        this.expression = other.expression;
        this.testAssertionType = other.testAssertionType;
        this.translationTemplates = other.translationTemplates;
    }

    public String getExpectedResult() {
        return expectedResult;
    }

    public void setExpectedResult(final String expectedResult) {
        this.expectedResult = expectedResult;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(final String expression) {
        this.expression = expression;
    }

    public TestItemTypeDto getType() {
        return testAssertionType;
    }

    public void setType(final TestItemTypeDto testAssertionType) {
        this.testAssertionType = testAssertionType;
    }

    public List<LangTranslationTemplateCollectionDto> getTranslationTemplates() {
        return translationTemplates;
    }

    public void setTranslationTemplates(final List<LangTranslationTemplateCollectionDto> translationTemplates) {
        this.translationTemplates = translationTemplates;
    }

    public void addTranslationTemplate(final LangTranslationTemplateCollectionDto translationTemplate) {
        Objects.requireNonNull(translationTemplate, "Translation Teamplate is null");
        if (translationTemplates == null) {
            translationTemplates = new ArrayList<>();
        }
        translationTemplates.add(translationTemplate);
    }

    public void addTranslationTemplateWithName(final String name) {
        // Get ETS
        ModelItemDto parent = this.parent;
        int i = 0;
        while (parent != null && i <= 2) {
            parent = parent.getParent();
            i++;
        }
        if (i < 3) {
            throw new IllegalStateException(
                    "Can't lookup Translation Template \"" + name + "\" "
                            + "due to invalid parent model item associations. "
                            + "Failed to get parent ETS on level " + i);
        }
        addTranslationTemplate(
                Objects.requireNonNull(((ExecutableTestSuiteDto) parent).getTranslationTemplateBundle(),
                        "Can't lookup Translation Template \"" + name + "\" "
                                + "because ETS \"" + parent.getId() +
                                "\" is not associated with a Translation Template Bundle")
                        .getTranslationTemplateCollection(name));
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("TestAssertionDto{");
        sb.append("super='").append(super.toString()).append('\'');
        sb.append(", expectedResult='").append(expectedResult).append('\'');
        sb.append(", expression='").append(expression).append('\'');
        sb.append(", type='").append(testAssertionType).append('\'');
        sb.append(", translationTemplates=").append(translationTemplates);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public TestAssertionDto createCopy() {
        return new TestAssertionDto(this);
    }
}
