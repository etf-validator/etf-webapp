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
package de.interactive_instruments.etf.webapp.controller;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import de.interactive_instruments.Credentials;
import de.interactive_instruments.UriUtils;
import de.interactive_instruments.etf.dal.dto.capabilities.ResourceDto;
import de.interactive_instruments.etf.dal.dto.capabilities.TestObjectDto;
import de.interactive_instruments.etf.dal.dto.capabilities.TestObjectTypeDto;
import de.interactive_instruments.etf.detector.DetectedTestObjectType;
import de.interactive_instruments.etf.detector.IncompatibleTestObjectTypeException;
import de.interactive_instruments.etf.detector.TestObjectTypeDetectorManager;
import de.interactive_instruments.etf.detector.TestObjectTypeNotDetected;
import de.interactive_instruments.etf.model.EID;
import de.interactive_instruments.etf.model.EidMap;
import de.interactive_instruments.etf.model.capabilities.Resource;
import de.interactive_instruments.exceptions.ObjectWithIdNotFoundException;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
@Service
public class TestObjectTypeDetectionService {

    public void checkAndResolveTypes(final TestObjectDto dto, final Set<EID> expectedTypes)
            throws IOException, LocalizableApiError,
            ObjectWithIdNotFoundException {
        // First resource is the main resource
        final ResourceDto resourceDto = dto.getResourceCollection().iterator().next();
        final Resource resource = Resource.create(resourceDto.getName(),
                resourceDto.getUri(), Credentials.fromProperties(dto.properties()));
        final DetectedTestObjectType detectedTestObjectType;
        try {
            detectedTestObjectType = TestObjectTypeDetectorManager.detect(resource, expectedTypes);
        } catch (final TestObjectTypeNotDetected e) {
            throw new LocalizableApiError(e);
        } catch (IncompatibleTestObjectTypeException e) {
            throw new LocalizableApiError(e);
        }
        detectedTestObjectType.enrichAndNormalize(dto);
        if (!UriUtils.isFile(resourceDto.getUri())) {
            // service URI
            dto.setRemoteResource(resourceDto.getUri());
        } else {
            // fallback download URI
            final URI downloadUri = dto.getResourceByName("data");
            if (downloadUri != null && !UriUtils.isFile(downloadUri)) {
                dto.setRemoteResource(downloadUri);
            }
        }
    }

    public void resetTestObjectTypes(final TestObjectDto testObject, final Collection<TestObjectTypeDto> testObjectTypes) {
        final Set<EID> tots = testObjectTypes.stream().map(tot -> tot.getId()).collect(Collectors.toSet());
        final EidMap<TestObjectTypeDto> totImpls = TestObjectTypeDetectorManager.getTypes(tots);
        testObject.setTestObjectTypes(totImpls.asList());
    }
}
