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
package de.interactive_instruments.etf;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import de.interactive_instruments.SUtils;
import de.interactive_instruments.etf.dal.dto.capabilities.TestObjectDto;
import de.interactive_instruments.etf.dal.dto.capabilities.TestObjectTypeDto;
import de.interactive_instruments.etf.detector.DetectedTestObjectType;
import de.interactive_instruments.etf.model.EID;
import de.interactive_instruments.etf.model.EidHolder;
import de.interactive_instruments.etf.model.ExpressionType;
import de.interactive_instruments.etf.model.capabilities.Resource;
import de.interactive_instruments.etf.model.capabilities.TestObjectType;
import de.interactive_instruments.io.ContentTypeFilter;
import de.interactive_instruments.io.MultiFileFilter;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
class StdDetectedTestObjectType implements DetectedTestObjectType {

    private final TestObjectTypeDto testObjectType;
    private final String extractedLabel;
    private final String extractedDescription;
    private final Resource normalizedResource;
    private final int priority;

    StdDetectedTestObjectType(final TestObjectTypeDto testObjectType, final Resource normalizedResource) {
        this(testObjectType, normalizedResource, null, null, 0);
    }

    StdDetectedTestObjectType(final TestObjectTypeDto testObjectType,
            final Resource normalizedResource, final String label, final String description,
            final int priority) {
        this.testObjectType = Objects.requireNonNull(testObjectType);
        this.normalizedResource = normalizedResource;
        this.extractedLabel = label;
        this.extractedDescription = description;
        this.priority = priority;
    }

    @Override
    public int hashCode() {
        return testObjectType.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return testObjectType.equals(obj);
    }

    @Override
    public List<TestObjectTypeDto> getSubTypes() {
        return testObjectType.getSubTypes();
    }

    @Override
    public Optional<MultiFileFilter> filenameFilter() {
        return testObjectType.filenameFilter();
    }

    @Override
    public ContentTypeFilter contentTypeFilter() {
        return testObjectType.contentTypeFilter();
    }

    @Override
    public String getDetectionExpression() {
        return testObjectType.getDetectionExpression();
    }

    @Override
    public ExpressionType getDetectionExpressionType() {
        return testObjectType.getDetectionExpressionType();
    }

    @Override
    public String getLabelExpression() {
        return testObjectType.getLabelExpression();
    }

    @Override
    public ExpressionType getLabelExpressionType() {
        return testObjectType.getLabelExpressionType();
    }

    @Override
    public String getDescriptionExpression() {
        return testObjectType.getDescriptionExpression();
    }

    @Override
    public ExpressionType getDescriptionExpressionType() {
        return testObjectType.getDescriptionExpressionType();
    }

    @Override
    public String getLabel() {
        return testObjectType.getLabel();
    }

    @Override
    public String getDescription() {
        return testObjectType.getDescription();
    }

    @Override
    public EID getId() {
        return testObjectType.getId();
    }

    @Override
    public TestObjectType getParent() {
        return testObjectType.getParent();
    }

    @Override
    public int compareTo(final Object o) {
        if (o instanceof StdDetectedTestObjectType) {
            return Integer.compare(priority, ((StdDetectedTestObjectType) (o)).priority);
        } else if (o instanceof EidHolder) {
            return getId().compareTo(((EidHolder) (o)).getId());
        }
        throw new IllegalArgumentException("Invalid object type comparison: " +
                o.getClass().getName() + " can not be compared.");
    }

    @Override
    public void enrichAndNormalize(final TestObjectDto testObject) {
        if (!SUtils.isNullOrEmpty(this.extractedLabel)) {
            testObject.setLabel(this.extractedLabel);
        }
        if (!SUtils.isNullOrEmpty(this.extractedDescription)) {
            testObject.setDescription(this.extractedDescription);
        }
        if (normalizedResource.getUri() != null && testObject.getResourceCollection() != null
                && !testObject.getResourceCollection().isEmpty()) {
            testObject.getResourceCollection().iterator().next().setUri(
                    normalizedResource.getUri());
        }
        testObject.setTestObjectType(this.testObjectType);
    }

    @Override
    public Resource getNormalizedResource() {
        return this.normalizedResource;
    }

    @Override
    public TestObjectTypeDto toTestObjectTypeDto() {
        return testObjectType.createCopy();
    }
}
