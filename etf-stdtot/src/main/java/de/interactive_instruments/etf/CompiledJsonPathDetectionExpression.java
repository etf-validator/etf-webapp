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
package de.interactive_instruments.etf;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Objects;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

import de.interactive_instruments.UriUtils;
import de.interactive_instruments.etf.dal.dto.capabilities.TestObjectTypeDto;
import de.interactive_instruments.etf.detector.DetectedTestObjectType;
import de.interactive_instruments.etf.model.capabilities.RemoteResource;
import de.interactive_instruments.etf.model.capabilities.Resource;
import de.interactive_instruments.exceptions.ExcUtils;
import de.interactive_instruments.io.PConn;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public class CompiledJsonPathDetectionExpression extends CompiledDetectionExpression {

    private final JsonPath detectionExpression;
    private final JsonPath labelExpression;
    private final JsonPath descriptionExpression;

    protected CompiledJsonPathDetectionExpression(final TestObjectTypeDto testObjectType) {
        super(testObjectType);
        detectionExpression = JsonPath.compile(Objects.requireNonNull(testObjectType.getDetectionExpression()));
        if (testObjectType.getLabelExpression() != null) {
            labelExpression = JsonPath.compile(testObjectType.getLabelExpression());
        } else {
            labelExpression = null;
        }
        if (testObjectType.getDescriptionExpression() != null) {
            descriptionExpression = JsonPath.compile(testObjectType.getDescriptionExpression());
        } else {
            descriptionExpression = null;
        }
    }

    private String getValue(final RemoteResource normalizedResource, final JsonPath expression) {
        if (expression != null) {
            try {
                return expression.read(
                        normalizedResource.getPreparedConnection().replaceLastSegment("api").openStream());
            } catch (PathNotFoundException | IOException e) {
                ExcUtils.suppress(e);
            }
        }
        return null;
    }

    DetectedTestObjectType getDetectedTestObjectType(final Resource normalizedResource) throws IOException {
        final PConn.HttpInputStream httpInputStream = ((PConn.HttpInputStream) normalizedResource.openStream());
        if (!this.testObjectType.contentTypeFilter().isBaseMimeTypeAllowed(httpInputStream.getContentType())) {
            return null;
        }
        try {
            final Object result = detectionExpression.read(normalizedResource.openStream());
            if (!(result instanceof List) || ((List) result).size() <= 0) {
                return null;
            }
        } catch (PathNotFoundException | IOException e) {
            ExcUtils.suppress(e);
            return null;
        }

        // Todo
        final Resource withoutConformance;
        if (normalizedResource.getUri().toString().contains("/conformance")) {
            final URI newUri = UriUtils.replaceLastSegment(normalizedResource.getUri(), "/");
            withoutConformance = Resource.create(
                    normalizedResource.getName(), newUri, null);
        } else {
            withoutConformance = normalizedResource;
        }

        return new StdDetectedTestObjectType(
                this.testObjectType,
                withoutConformance,
                getValue((RemoteResource) normalizedResource, labelExpression),
                getValue((RemoteResource) normalizedResource, descriptionExpression),
                priority);
    }
}
