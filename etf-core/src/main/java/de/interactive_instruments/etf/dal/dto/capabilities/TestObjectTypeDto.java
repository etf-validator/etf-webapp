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
package de.interactive_instruments.etf.dal.dto.capabilities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import de.interactive_instruments.UriUtils;
import de.interactive_instruments.etf.dal.dto.MetaDataItemDto;
import de.interactive_instruments.etf.model.ExpressionType;
import de.interactive_instruments.etf.model.capabilities.TestObjectType;
import de.interactive_instruments.io.ContentTypeFilter;
import de.interactive_instruments.io.MultiFileFilter;

/**
 * A Test Object Type describes a {@link TestObjectDto} and may possess information how the type can be detected.
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public class TestObjectTypeDto extends MetaDataItemDto<TestObjectTypeDto> implements TestObjectType {

    private List<TestObjectTypeDto> subTypes;

    private MultiFileFilter filenameFilter;

    // Optional list of supported mimetypes
    private ContentTypeFilter contentTypeFilter;

    // Optional detection expression
    private String detectionExpression;

    // Optional detection expression type
    private ExpressionType detectionExpressionType;

    // Optional expression for extracting the Test Object label
    private String labelExpression;

    // Optional expression for extracting the Test Object label type
    private ExpressionType labelExpressionType;

    // Optional expression for extracting the Test Object description
    private String descriptionExpression;

    // Optional expression for extracting the Test Object description type
    private ExpressionType descriptionExpressionType;

    // Optional default query (only used in remote resources yet)
    private String defaultPathAndQuery;

    // Optional regular expression to detect a type from an URI.
    private String uriDetectionExpression;

    // Optional naming convention, which is used to
    // check if the label of a Test Object matches this regular expression.
    // This might be useful for labeling test data deliveries according
    // to a prescribed scheme.
    private String namingConvention;

    public TestObjectTypeDto() {}

    private TestObjectTypeDto(final TestObjectTypeDto other) {
        super(other);
        this.subTypes = other.subTypes;
        this.filenameFilter = other.filenameFilter;
        this.contentTypeFilter = other.contentTypeFilter;
        this.detectionExpression = other.detectionExpression;
        this.detectionExpressionType = other.detectionExpressionType;
        this.labelExpression = other.labelExpression;
        this.labelExpressionType = other.labelExpressionType;
        this.descriptionExpression = other.descriptionExpression;
        this.descriptionExpressionType = other.descriptionExpressionType;
        this.defaultPathAndQuery = other.defaultPathAndQuery;
        this.uriDetectionExpression = other.uriDetectionExpression;
        this.namingConvention = other.namingConvention;
    }

    public List<TestObjectTypeDto> getSubTypes() {
        return subTypes;
    }

    public void setSubTypes(final List<TestObjectTypeDto> subTypes) {
        this.subTypes = subTypes;
    }

    public void addSubType(final TestObjectTypeDto subType) {
        if (this.subTypes == null) {
            subTypes = new ArrayList<>();
        }
        subTypes.add(subType);
    }

    public String getNamingConvention() {
        return namingConvention;
    }

    public void setNamingConvention(final String namingConvention) {
        this.namingConvention = namingConvention;
    }

    private static class PatternFilter implements MultiFileFilter {
        private final Pattern filenamePattern;

        PatternFilter(final Pattern filenamePattern) {
            this.filenamePattern = filenamePattern;
        }

        @Override
        public boolean accept(final File f) {
            return !f.isDirectory() && filenamePattern == null || filenamePattern.matcher(f.getName()).matches();
        }
    }

    @Override
    public Optional<MultiFileFilter> filenameFilter() {
        if (filenameFilter == null) {
            if (parent != null) {
                filenameFilter = parent.filenameFilter().orElse(null);
            }
        }
        return Optional.ofNullable(filenameFilter);
    }

    public void filenameFilter(final Pattern filenamePattern) {
        this.filenameFilter = new PatternFilter(filenamePattern);
    }

    public ContentTypeFilter contentTypeFilter() {
        if (contentTypeFilter == null) {
            if (parent != null) {
                contentTypeFilter = parent.contentTypeFilter();
            }
        }
        if (this.contentTypeFilter == null) {
            return ContentTypeFilter.allowAnyContentType();
        }
        return contentTypeFilter;
    }

    public void setMimeTypes(final List<String> mimeTypes) {
        this.contentTypeFilter = new ContentTypeFilter(mimeTypes);
    }

    public void setDetectionExpression(final String detectionExpression, final ExpressionType type) {
        this.detectionExpression = detectionExpression;
        this.detectionExpressionType = type;
    }

    public String getDetectionExpression() {
        return detectionExpression;
    }

    public ExpressionType getDetectionExpressionType() {
        return detectionExpressionType;
    }

    public void setLabelExpression(final String labelExpression, final ExpressionType type) {
        this.labelExpression = labelExpression;
        this.labelExpressionType = type;

    }

    public void setDefaultPathAndQuery(final String defaultPathAndQuery) {
        this.defaultPathAndQuery = defaultPathAndQuery;
    }

    public Map<String, List<String>> getDefaultQuery() {
        if (defaultPathAndQuery != null) {
            return UriUtils.getQueryParameters(defaultPathAndQuery);
        }
        return null;
    }

    public String getDefaultAccessPath() {
        return UriUtils.withoutQueryParameters(defaultPathAndQuery);
    }

    public void setUriDetectionExpression(final String uriDetectionExpression) {
        this.uriDetectionExpression = Pattern.compile(uriDetectionExpression).pattern();
    }

    public String getUriDetectionExpression() {
        return uriDetectionExpression;
    }

    public String getLabelExpression() {
        return labelExpression;
    }

    public ExpressionType getLabelExpressionType() {
        return labelExpressionType;
    }

    public void setDescriptionExpression(final String descriptionExpression, final ExpressionType type) {
        this.descriptionExpression = descriptionExpression;
        this.descriptionExpressionType = type;
    }

    public String getDescriptionExpression() {
        return descriptionExpression;
    }

    public ExpressionType getDescriptionExpressionType() {
        return descriptionExpressionType;
    }

    @Override
    public TestObjectTypeDto createCopy() {
        return new TestObjectTypeDto(this);
    }
}
