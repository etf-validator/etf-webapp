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

import java.net.URI;
import java.util.Optional;
import java.util.regex.Pattern;

import de.interactive_instruments.SUtils;
import de.interactive_instruments.UriUtils;
import de.interactive_instruments.etf.dal.dto.capabilities.TestObjectTypeDto;
import de.interactive_instruments.etf.model.capabilities.RemoteResource;
import de.interactive_instruments.etf.model.capabilities.Resource;
import de.interactive_instruments.io.MultiFileFilter;
import de.interactive_instruments.io.PConn;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
abstract class CompiledDetectionExpression implements Comparable<CompiledDetectionExpression> {

    protected final TestObjectTypeDto testObjectType;
    protected final int priority;
    protected final Pattern uriDetectionPattern;

    protected CompiledDetectionExpression(
            final TestObjectTypeDto testObjectType) {
        this.testObjectType = testObjectType;

        // order objects with parents before objects without parents
        TestObjectTypeDto parent = this.testObjectType.getParent();
        int cmp = 0;
        for (; parent != null; --cmp) {
            parent = parent.getParent();
        }
        cmp += this.testObjectType.getSubTypes() == null ? -1 : 0;
        priority = cmp;

        if (!SUtils.isNullOrEmpty(testObjectType.getUriDetectionExpression())) {
            this.uriDetectionPattern = Pattern.compile(testObjectType.getUriDetectionExpression(),
                    Pattern.CASE_INSENSITIVE);
        } else {
            this.uriDetectionPattern = null;
        }
    }

    boolean isUriKnown(final URI uri) {
        if (uriDetectionPattern != null) {
            return uriDetectionPattern.matcher(uri.toString()).matches();
        }
        return false;
    }

    Resource getNormalizedResource(final Resource resource) {
        if (this.testObjectType.getDefaultQuery() != null && resource instanceof RemoteResource) {
            final RemoteResource remoteResource = ((RemoteResource) resource);
            final PConn pconn = remoteResource
                    .getPreparedConnection()
                    .replaceLastSegment(this.testObjectType.getDefaultAccessPath())
                    .setQueryParameters(
                            UriUtils.toSingleQueryParameterValues(this.testObjectType.getDefaultQuery()))
                    .setAcceptHeader(this.testObjectType.contentTypeFilter().getAllowedMimeTypes());
            return remoteResource.createNewWith(pconn);
        }
        return resource;
    }

    public Optional<MultiFileFilter> getFilenameFilter() {
        return testObjectType.filenameFilter();
    }

    @Override
    public int compareTo(final CompiledDetectionExpression o) {
        final int cmp = Integer.compare(this.priority, o.priority);
        if (cmp == 0) {
            // Compare label so that "OGC Web Feature Service 2.0" is tested before
            // "OGC Web Feature Service 1.1"
            return -this.testObjectType.getLabel().compareTo(o.testObjectType.getLabel());
        }
        return cmp;
    }
}
