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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpressionException;

import jlibs.xml.DefaultNamespaceContext;
import jlibs.xml.sax.dog.XMLDog;
import jlibs.xml.sax.dog.XPathResults;

import com.jayway.jsonpath.InvalidPathException;

import org.jaxen.saxpath.SAXPathException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import de.interactive_instruments.*;
import de.interactive_instruments.etf.dal.dto.capabilities.TestObjectTypeDto;
import de.interactive_instruments.etf.detector.DetectedTestObjectType;
import de.interactive_instruments.etf.detector.TestObjectTypeDetector;
import de.interactive_instruments.etf.model.*;
import de.interactive_instruments.etf.model.capabilities.*;
import de.interactive_instruments.exceptions.ExcUtils;
import de.interactive_instruments.exceptions.InitializationException;
import de.interactive_instruments.exceptions.InvalidStateTransitionException;
import de.interactive_instruments.exceptions.MimeTypeUtilsException;
import de.interactive_instruments.exceptions.config.ConfigurationException;
import de.interactive_instruments.io.DefaultFileIgnoreFilter;
import de.interactive_instruments.io.MultiFileFilter;

/**
 * Standard detector for Test Object Types.
 *
 * The standard detector takes xpath expressions for detecting the test object types, and checks for matches in XML
 * files. As the jdk xpath engine is very slow and memory hungry, the XMLDog engine which is based on Sax is used.
 *
 * Note: Only a subset of XPath 1.0 is supported https://github.com/santhosh-tekuri/jlibs/wiki/XMLDog
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 * @author Clemens Portele ( portele aT interactive-instruments doT de )
 */
public class StdTestObjectDetector implements TestObjectTypeDetector {

    private static Logger logger = LoggerFactory.getLogger(StdTestObjectDetector.class);
    private boolean initialized = false;

    private final List<CompiledDetectionExpression> detectionExpressions = new ArrayList<>();
    private final EidMap<CompiledDetectionExpression> detectionExpressionsEidMap = new DefaultEidMap<>();

    private final List<TestObjectType> mimeTypeDetections = new ArrayList<>();

    private static class TestObjecTypeComperator implements Comparator<TestObjectType> {
        private static int parentCount(final EidHolderWithParent o) {
            int ps = 0;
            TestObjectType parent = (TestObjectType) o.getParent();
            while (parent != null) {
                ++ps;
                parent = parent.getParent();
            }
            return ps;
        }

        @Override
        public int compare(final TestObjectType o1, final TestObjectType o2) {
            final int o1Parents = parentCount(o1);
            final int o2Parents = parentCount(o2);
            if (o1Parents == o2Parents) {
                return o1.compareTo(o2);
            } else if (o1Parents > o2Parents) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    private final XMLDog xmlDog = new XMLDog(new DefaultNamespaceContext(), null, null);

    @Override
    public EidMap<TestObjectTypeDto> supportedTypes() {
        return StdTestObjectTypes.types;
    }

    @Override
    public void init() throws ConfigurationException, InitializationException, InvalidStateTransitionException {
        for (final TestObjectTypeDto testObjectType : supportedTypes().values()) {
            if (!SUtils.isNullOrEmpty(testObjectType.getDetectionExpression())) {
                if (testObjectType.getDetectionExpressionType().equals(ExpressionType.XPATH)) {
                    try {
                        final CompiledXpathDetectionExpression compiledExpression = new CompiledXpathDetectionExpression(
                                testObjectType,
                                this.xmlDog);
                        detectionExpressions.add(compiledExpression);
                        detectionExpressionsEidMap.put(testObjectType.getId(), compiledExpression);
                    } catch (final SAXPathException e) {
                        logger.error("Could not compile XPath expression: ", e);
                    }
                } else if (testObjectType.getDetectionExpressionType().equals(ExpressionType.JSONPATH)) {
                    try {
                        final CompiledJsonPathDetectionExpression compiledExpression = new CompiledJsonPathDetectionExpression(
                                testObjectType);
                        detectionExpressions.add(compiledExpression);
                        detectionExpressionsEidMap.put(testObjectType.getId(), compiledExpression);
                    } catch (final InvalidPathException e) {
                        logger.error("Could not compile JsonPath expression: ", e);
                    }
                }
            } else if (testObjectType.contentTypeFilter().getAllowedMimeTypes() != null) {
                mimeTypeDetections.add(testObjectType);
            }
        }
        Collections.sort(mimeTypeDetections, new TestObjecTypeComperator());
        Collections.sort(detectionExpressions);
        initialized = true;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void release() {
        detectionExpressions.clear();
        detectionExpressionsEidMap.clear();
        initialized = false;
    }

    private DetectedTestObjectType detectLocalFile(final XPathResults results,
            final LocalResource resource, final List<CompiledXpathDetectionExpression> expressions) {
        for (final CompiledXpathDetectionExpression detectionExpression : expressions) {
            try {
                final DetectedTestObjectType type = detectionExpression.getDetectedTestObjectType(
                        results, resource);
                if (type != null) {
                    return type;
                }
            } catch (ClassCastException | XPathExpressionException e) {
                logger.error("Could not evaluate XPath expression: ", e);
            }
        }
        return null;
    }

    private MultiFileFilter mergeFilters(final Stream<Optional<MultiFileFilter>> optionalStream) {
        final Set<MultiFileFilter> filter = optionalStream.filter(
                Optional::isPresent).map(Optional::get).collect(Collectors.toSet());
        if (filter.isEmpty()) {
            return DefaultFileIgnoreFilter.getInstance();
        }
        return MultiFileFilter.mergeOr(filter);
    }

    /**
     * Detect Test Object Type from samples in a directory
     *
     * @param localResource
     *            directory as URI
     * @return Test Object Type or null if unknown
     * @throws IOException
     *             if an error occurs accessing the files
     */
    private DetectedTestObjectType detectInLocalDirFromSamples(final List<CompiledDetectionExpression> expressions,
            final LocalResource localResource) throws IOException {
        final IFile dir = localResource.getFile();
        final List<IFile> files = dir.getFilesInDirRecursive(mergeFilters(
                expressions.stream().map(CompiledDetectionExpression::getFilenameFilter)), 6, false);
        if (files == null || files.size() == 0) {
            return detectInLocalDirFromSamples(dir, localResource);
        } else {
            return detectInLocalDirFromSamples(files, expressions, localResource);
        }
    }

    private DetectedTestObjectType detectInLocalDirFromSamples(final List<IFile> files,
            final List<CompiledDetectionExpression> expressions, final LocalResource localResource) {
        final TreeSet<DetectedTestObjectType> detectedTypes = new TreeSet<>();
        for (final IFile sample : Sample.normalDistributed(files, 7)) {
            try {
                // Todo handle geojson here
                final List<CompiledXpathDetectionExpression> xpathExpressions = expressions.stream()
                        .filter(e -> e instanceof CompiledXpathDetectionExpression)
                        .map(e -> (CompiledXpathDetectionExpression) e).collect(Collectors.toList());
                final InputStream inputStream = new FileInputStream(sample);
                final DetectedTestObjectType detectedType = detectLocalFile(xmlDog.sniff(new InputSource(inputStream)),
                        localResource, xpathExpressions);
                if (detectedType != null) {
                    detectedTypes.add(detectedType);
                }
                if (detectedTypes.size() >= expressions.size()) {
                    // skip if we have detected types for all expressions
                    break;
                }
            } catch (XPathException | FileNotFoundException e) {
                ExcUtils.suppress(e);
            }
        }
        if (detectedTypes.isEmpty()) {
            return null;
        }
        return detectedTypes.first();
    }

    private DetectedTestObjectType detectInLocalDirFromSamples(final IFile dir, final LocalResource localResource)
            throws IOException {
        final List<IFile> files = dir.getFilesInDirRecursive(
                mergeFilters(mimeTypeDetections.stream().map(TestObjectType::filenameFilter)), 6,
                false);
        if (files != null) {
            final Map<TestObjectType, AtomicInteger> candidates = new LinkedHashMap<>();
            for (final IFile sample : Sample.normalDistributed(files, 7)) {
                for (final TestObjectType mimeTypeDetection : mimeTypeDetections) {
                    if (mimeTypeDetection.filenameFilter().isPresent() &&
                            mimeTypeDetection.filenameFilter().get().accept(sample)) {
                        final String mimeType;
                        try {
                            mimeType = MimeTypeUtils.detectMimeType(sample);
                        } catch (MimeTypeUtilsException ignore) {
                            continue;
                        }
                        if (mimeTypeDetection.contentTypeFilter().getAllowedMimeTypes().contains(mimeType)) {
                            final AtomicInteger c = candidates.putIfAbsent(mimeTypeDetection, new AtomicInteger(1));
                            if (c != null) {
                                c.incrementAndGet();
                            }
                        }
                    }
                }
            }
            // select candidate
            StdDetectedTestObjectType candidate = null;
            int currentMaxHits = 0;
            for (final Map.Entry<TestObjectType, AtomicInteger> testObjectTypeAndHits : candidates.entrySet()) {
                final int hits = testObjectTypeAndHits.getValue().get();
                if (hits > currentMaxHits) {
                    currentMaxHits = hits;
                    candidate = new StdDetectedTestObjectType((TestObjectTypeDto) testObjectTypeAndHits.getKey(),
                            localResource);
                } else if (hits == currentMaxHits && testObjectTypeAndHits.getKey().isInstanceOf(candidate)) {
                    candidate = new StdDetectedTestObjectType((TestObjectTypeDto) testObjectTypeAndHits.getKey(),
                            localResource);
                }
            }
            return candidate;
        }
        return null;
    }

    /**
     *
     * @param detectionExpression
     * @param resource
     * @return
     */
    private DetectedTestObjectType detectRemote(final CompiledDetectionExpression detectionExpression,
            final CachedRemoteResource resource) {
        try {
            //
            final Resource normalizedResource = detectionExpression.getNormalizedResource(resource);
            // Todo: refactoring: replace conditional with poly
            if (detectionExpression instanceof CompiledXpathDetectionExpression) {
                return ((CompiledXpathDetectionExpression) detectionExpression).getDetectedTestObjectType(
                        xmlDog.sniff(new InputSource(normalizedResource.openStream())), normalizedResource);
            } else if (detectionExpression instanceof CompiledJsonPathDetectionExpression) {
                return ((CompiledJsonPathDetectionExpression) detectionExpression)
                        .getDetectedTestObjectType(normalizedResource);
            }
        } catch (IOException | XPathException e) {
            logger.error("Error occurred during Test Object Type detection ", e);
        }
        return null;
    }

    private DetectedTestObjectType detectedType(final Resource resource,
            final List<CompiledDetectionExpression> expressions) {
        Collections.sort(expressions);

        // detect remote type
        if (resource instanceof RemoteResource) {
            final CachedRemoteResource cachedResource = Resource.toCached((RemoteResource) resource);
            for (final CompiledDetectionExpression expression : expressions) {
                final DetectedTestObjectType detectedType = detectRemote(expression, cachedResource);
                if (detectedType != null) {
                    return detectedType;
                }
            }
        } else {
            try {
                return detectInLocalDirFromSamples(expressions, (LocalResource) resource);
            } catch (IOException ign) {
                ExcUtils.suppress(ign);
                return null;
            }
        }
        return null;
    }

    @Override
    public DetectedTestObjectType detectType(final Resource resource, final Set<EID> expectedTypes) {

        // Types that can be detected by URI
        final List<CompiledDetectionExpression> uriDetectionCandidates = new ArrayList<>();
        // All others
        final List<CompiledDetectionExpression> expressionsForExpectedTypes = new ArrayList<>();
        for (final EID expectedType : expectedTypes) {
            final CompiledDetectionExpression detectionExpression = detectionExpressionsEidMap.get(expectedType);
            if (detectionExpression != null) {
                if (detectionExpression.isUriKnown(resource.getUri())) {
                    uriDetectionCandidates.add(detectionExpression);
                } else {
                    expressionsForExpectedTypes.add(detectionExpression);
                }
            }
        }
        if (!uriDetectionCandidates.isEmpty()) {
            // Test Object types could be detected by URI
            final DetectedTestObjectType detectedType = detectedType(resource, uriDetectionCandidates);
            if (detectedType != null) {
                return detectedType;
            }
        }
        // Test Object types could not be identified by URI
        final DetectedTestObjectType detectedType = detectedType(resource, expressionsForExpectedTypes);
        if (detectedType != null) {
            return detectedType;
        }

        // should never happen, fallback types are XML and WEBSERVICE
        return null;
    }

    @Override
    public DetectedTestObjectType detectType(final Resource resource) {
        return detectedType(resource, detectionExpressions);
    }
}
